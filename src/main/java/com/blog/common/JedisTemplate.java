package com.blog.common;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

/**
 * 
 * @author sunny redis命令参考 http://doc.redisfans.com/index.html
 */
public class JedisTemplate {
	private static Logger log = LoggerFactory.getLogger(JedisTemplate.class);

	private JedisPool jedisPool;

	public JedisTemplate(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	/**
	 * 有返回值的操作
	 *
	 * @param <T>
	 */
	public interface JedisAction<T> {
		T action(Jedis jedis);
	}

	/**
	 * 无返回值的操作
	 * 
	 *
	 */
	public interface JedisActionNoResult {
		void action(Jedis jedis);
	}

	/**
	 * 有返回值的批量操作
	 *
	 */
	public interface PipelineAction {
		void action(Pipeline pipeline);
	}

	/**
	 * 无返回值的批量操作
	 *
	 */
	public interface PipelineActionNoResult {
		void action(Pipeline pipeline);
	}

	/**
	 * 执行操作
	 * 
	 * @param jedisAction
	 * @return
	 */
	public <T> T execute(JedisAction<T> jedisAction) {
		Jedis jedis = null;
		boolean broken = false;
		try {
			// 获取一个redis连接资源
			jedis = jedisPool.getResource();
			// 使用jedis资源执行请求
			return jedisAction.action(jedis);
		} catch (JedisException e) {
			broken = handleJedisException(e);
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	public void execute(JedisActionNoResult jedisAction) {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = jedisPool.getResource();
			jedisAction.action(jedis);
		} catch (JedisException e) {
			broken = handleJedisException(e);
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	public List<Object> execute(PipelineAction pipelineAction) throws JedisException {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = jedisPool.getResource();
			// 获取一个pipeline资源
			Pipeline pipeline = jedis.pipelined();
			// 向pipeline装载数据
			pipelineAction.action(pipeline);
			// 批量执行,返回执行结果
			return pipeline.syncAndReturnAll();
		} catch (JedisException e) {
			broken = handleJedisException(e);
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	public void execute(PipelineActionNoResult pipelineAction) throws JedisException {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = jedisPool.getResource();
			Pipeline pipeline = jedis.pipelined();
			pipelineAction.action(pipeline);
			pipeline.sync();
		} catch (JedisException e) {
			broken = handleJedisException(e);
			throw e;
		} finally {
			closeResource(jedis, broken);
		}
	}

	protected boolean handleJedisException(JedisException jedisException) {
		if (jedisException instanceof JedisConnectionException) {
			// redis连接断开
			log.error("Redis connection " + jedisPool + " lost.", jedisException);
		} else if (jedisException instanceof JedisDataException) {
			if ((jedisException.getMessage() != null) && (jedisException.getMessage().indexOf("READONLY") != -1)) {
				// 该redis服务只读
				log.error("Redis connection " + jedisPool + " are read-only slave.", jedisException);
			} else {
				return false;
			}
		} else {
			// 其他异常
			log.error("Jedis exception happen.", jedisException);
		}
		return true;
	}

	protected void closeResource(Jedis jedis, boolean conectionBroken) {
		try {
			if (conectionBroken) {
				jedisPool.returnBrokenResource(jedis);
			} else {
				jedisPool.returnResource(jedis);
			}
		} catch (Exception e) {
			log.error("return back jedis failed, will fore close the jedis.", e);
			// JedisUtils.destroyJedis(jedis);
		}
	}

	// 公共的内容

	/**
	 * 删除给定的一个或多个 key ,不存在的 key 会被忽略。
	 * 
	 * @version >= 1.0.0
	 * @param keys
	 * @return 返回执行成功key的总数
	 */
	public Long del(final String... keys) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.del(keys);
			}
		});
	}

	/**
	 * 检查给定 key 是否存在。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return
	 */
	public Boolean exists(final String key) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.exists(key);
			}
		});
	}

	/**
	 * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param seconds
	 * @return
	 */
	public Boolean expire(final String key, final int seconds) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.expire(key, seconds) == 1 ? true : false;
			}
		});
	}

	/**
	 * 为 key 设置生存时间;时间参数是 UNIX 时间戳(unix timestamp)。
	 * 
	 * @version >= 1.2.0
	 * @param key
	 * @param unixTime
	 * @return
	 */
	public Boolean expireat(final String key, final int unixTime) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.expireAt(key, unixTime) == 1 ? true : false;
			}
		});
	}

	/**
	 * 查找所有符合给定模式 pattern 的 key 。
	 * 
	 * @version >= 1.0.0
	 * @param pattern
	 * @return
	 */
	public Set<String> keys(final String pattern) {
		return execute(new JedisAction<Set<String>>() {
			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.keys(pattern);
			}
		});
	}

	/**
	 * 将 key 原子性地从当前实例传送到目标实例的指定数据库上，一旦传送成功， key 保证会出现在目标实例上，而当前实例上的 key 会被删除。
	 * MIGRATE 127.0.0.1 7777 greeting 0 1000
	 * 
	 * @param host
	 * @param port
	 * @param key
	 * @param destinationDb
	 * @param timeout
	 * @return 迁移成功时返回 OK，否则返回相应的错误。
	 */
	public String migrate(final String host, final int port, final String key, final int destinationDb,
			final int timeout) {
		// TODO 未实测
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {

				return jedis.migrate(host, port, key, destinationDb, timeout);
			}
		});
	}

	/**
	 * 将当前数据库的 key 移动到给定的数据库 db 当中。
	 * 
	 * @param key
	 * @param dbIndex
	 * @return
	 */
	public Boolean move(final String key, final int dbIndex) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.move(key, dbIndex) == 1 ? true : false;
			}
		});
	}

	public Boolean persist(final String key) {
		// TODO 移除过期时间
		return null;
	}
	
	public Boolean pexpire(final String key,int second){
		return null;
	}
	
}
