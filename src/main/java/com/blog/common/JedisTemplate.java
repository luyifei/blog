package com.blog.common;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.BitOP;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
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
	 * 命令用于迭代当前数据库中的数据库键。
	 * 
	 * @param cursor
	 *            游标
	 * @return ScanResult.cursor下次游标 ScanResult.results结果集
	 */
	public ScanResult<String> scan(final String cursor) {
		return scan(cursor, new ScanParams());
	}

	/**
	 * 命令用于迭代当前数据库中的数据库键。
	 * 
	 * @param cursor
	 *            游标
	 * @param params
	 *            ScanParams.match匹配规则 ScanParams.count每次迭代所返回的元素数量
	 * @return ScanResult.cursor下次游标 ScanResult.results结果集
	 */
	public ScanResult<String> scan(final String cursor, final ScanParams params) {
		return execute(new JedisAction<ScanResult<String>>() {
			@Override
			public ScanResult<String> action(Jedis jedis) {
				return jedis.scan(cursor, params);
			}
		});
	}

	/**
	 * 命令用于迭代哈希键中的键值对。
	 * 
	 * @param cursor
	 *            游标
	 * 
	 * @return ScanResult.cursor下次游标 ScanResult.results结果集
	 */
	public ScanResult<Map.Entry<String, String>> hscan(final String key, final String cursor) {
		return hscan(key, cursor, new ScanParams());
	}

	/**
	 * 命令用于迭代哈希键中的键值对。
	 * 
	 * @param cursor
	 *            游标
	 * @param params
	 *            参考scan()
	 * 
	 * @return ScanResult.cursor下次游标 ScanResult.results结果集
	 */
	public ScanResult<Map.Entry<String, String>> hscan(final String key, final String cursor, final ScanParams params) {
		return execute(new JedisAction<ScanResult<Map.Entry<String, String>>>() {
			@Override
			public ScanResult<Map.Entry<String, String>> action(Jedis jedis) {
				return jedis.hscan(key, cursor, params);
			}
		});
	}

	/**
	 * 命令用于迭代集合键中的元素。
	 * 
	 * @param cursor
	 *            游标
	 * 
	 * @return ScanResult.cursor下次游标 ScanResult.results结果集
	 */
	public ScanResult<String> sscan(final String key, final String cursor) {
		return sscan(key, cursor, new ScanParams());
	}

	/**
	 * 命令用于迭代集合键中的元素。
	 * 
	 * @param cursor
	 *            游标
	 * @param params
	 *            参考scan()
	 * 
	 * @return ScanResult.cursor下次游标 ScanResult.results结果集
	 */
	public ScanResult<String> sscan(final String key, final String cursor, final ScanParams params) {
		return execute(new JedisAction<ScanResult<String>>() {
			@Override
			public ScanResult<String> action(Jedis jedis) {
				return jedis.sscan(key, cursor, params);
			}
		});
	}

	/**
	 * 命令用于迭代有序集合中的元素（包括元素成员和元素分值）。
	 * 
	 * @param cursor
	 *            游标
	 * 
	 * @return ScanResult.cursor下次游标 ScanResult.results结果集
	 */
	public ScanResult<Tuple> zscan(final String key, final String cursor) {
		return zscan(key, cursor, new ScanParams());
	}

	/**
	 * 命令用于迭代有序集合中的元素（包括元素成员和元素分值）。
	 * 
	 * @param cursor
	 *            游标
	 * @param params
	 *            参考scan()
	 * 
	 * @return ScanResult.cursor下次游标 ScanResult.results结果集
	 */
	public ScanResult<Tuple> zscan(final String key, final String cursor, final ScanParams params) {
		return execute(new JedisAction<ScanResult<Tuple>>() {
			@Override
			public ScanResult<Tuple> action(Jedis jedis) {
				return jedis.zscan(key, cursor, params);
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

	/**
	 * 移除给定 key 的生存时间，将这个 key 从『易失的』(带生存时间 key )转换成『持久的』(一个不带生存时间、永不过期的 key )。
	 * 
	 * @version >= 2.2.0
	 * @param key
	 * @return
	 */
	public Boolean persist(final String key) {
		return execute(new JedisAction<Boolean>() {
			public Boolean action(Jedis jedis) {
				return jedis.persist(key) == 1 ? true : false;
			}
		});
	}

	/**
	 * 这个命令和 EXPIRE 命令的作用类似，但是它以毫秒为单位设置 key 的生存时间，而不像 EXPIRE 命令那样，以秒为单位。
	 * 
	 * @param key
	 * @param second
	 * @return
	 */
	public Boolean pexpire(final String key, final long milliseconds) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.pexpire(key, milliseconds) == 1 ? true : false;
			}
		});
	}

	/**
	 * 这个命令和 EXPIREAT 命令类似，但它以毫秒为单位设置 key 的过期 unix 时间戳，而不是像 EXPIREAT 那样，以秒为单位。
	 * 
	 * @version >= 2.6.0
	 * @param key
	 * @param millisecondsTimestamp
	 * @return
	 */
	public Boolean pexpireat(final String key, final long millisecondsTimestamp) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.pexpireAt(key, millisecondsTimestamp) == 1 ? true : false;
			}
		});
	}

	/**
	 * 这个命令类似于 TTL 命令，但它以毫秒为单位返回 key 的剩余生存时间，而不是像 TTL 命令那样，以秒为单位。
	 * 
	 * @version >= 2.6.0
	 * @param key
	 * @return
	 */
	public Long pttl(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.pttl(key);
			}
		});
	}

	/**
	 * 从当前数据库中随机返回(不删除)一个 key 。
	 * 
	 * @version >= 1.0.0
	 * @return
	 */
	public String randomkey() {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.randomKey();
			}
		});
	}

	/**
	 * 将 key 改名为 newkey 。 当 key 和 newkey 相同，或者 key 不存在时，返回一个错误。 当 newkey 已经存在时，
	 * RENAME 命令将覆盖旧值。
	 * 
	 * @version >= 1.0.0
	 * @param oldkey
	 * @param newkey
	 * @return
	 */
	public String rename(final String oldkey, final String newkey) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.rename(oldkey, newkey);
			}
		});
	}

	/**
	 * 当且仅当 newkey 不存在时，将 key 改名为 newkey 。
	 * 
	 * 当 key 不存在时，返回一个错误。
	 * 
	 * @version >= 1.0.0
	 * @param oldkey
	 * @param newkey
	 * @return
	 */
	public Boolean renamenx(final String oldkey, final String newkey) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.renamenx(oldkey, newkey) == 1 ? true : false;
			}
		});
	}

	/**
	 * SORT key 返回键值从小到大排序的结果。
	 * 
	 * @param key
	 * @return
	 */
	public List<String> sort(final String key) {
		return execute(new JedisAction<List<String>>() {
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.sort(key);
			}
		});
	}

	/**
	 * SORT key 返回按照指定方式排序的结果。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param sortingParameters
	 * @return
	 */
	public List<String> sort(final String key, final SortingParams sortingParameters) {
		return execute(new JedisAction<List<String>>() {
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.sort(key, sortingParameters);
			}
		});
	}

	/**
	 * 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return
	 */
	public Long ttl(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.ttl(key);
			}
		});
	}

	/**
	 * none (key不存在) string (字符串) list (列表) set (集合) zset (有序集) hash (哈希表)
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return
	 */
	public String type(final String key) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.type(key);
			}
		});
	}

	// String(字符串)

	/**
	 * 将字符串值 value 关联到 key 。
	 * 
	 * 如果 key 已经持有其他值， SET 就覆写旧值，无视类型。
	 * 
	 * 对于某个原本带有生存时间（TTL）的键来说， 当 SET 命令成功在这个键上执行时， 这个键原有的 TTL 将被清除。
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Boolean set(final String key, final String value) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.set(key, value).equals("OK");
			}
		});
	}

	/**
	 * 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位)。
	 * 
	 * 如果 key 已经存在， SETEX 命令将覆写旧值。
	 * 
	 * @version >= 2.0.0
	 * @param key
	 * @param seconds
	 * @param value
	 * @return
	 */
	public Boolean setex(final String key, final int seconds, final String value) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.setex(key, seconds, value).equals("OK");
			}
		});
	}

	/**
	 * 这个命令和 SETEX 命令相似，但它以毫秒为单位设置 key 的生存时间，而不是像 SETEX 命令那样，以秒为单位。
	 * 
	 * @version >= 2.6.0
	 * @param key
	 * @param milliseconds
	 * @param value
	 * @return
	 */
	public Boolean psetex(final String key, final long milliseconds, final String value) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.psetex(key, milliseconds, value).equals("OK");
			}
		});
	}

	/**
	 * 将 key 的值设为 value ，当且仅当 key 不存在。
	 * 
	 * 若给定的 key 已经存在，则 SETNX 不做任何动作。
	 * 
	 * SETNX 是『SET if Not eXists』
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param value
	 * @return
	 */
	public Boolean setnx(final String key, final String value) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.setnx(key, value) == 1;
			}
		});
	}

	/**
	 * 用 value 参数覆写(overwrite)给定 key 所储存的字符串值，从偏移量 offset 开始。
	 * 
	 * 不存在的 key 当作空白字符串处理。
	 * 
	 * @version >= 2.2.0
	 * @param key
	 * @param offset
	 * @param value
	 * @return 被 SETRANGE 修改之后，字符串的长度。
	 */
	public Long setrange(String key, long offset, String value) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.setrange(key, offset, value);
			}
		});
	}

	/**
	 * 同时设置一个或多个 key-value 对。
	 * 
	 * 如果某个给定 key 已经存在，那么 MSET 会用新值覆盖原来的旧值，如果这不是你所希望的效果，请考虑使用 MSETNX 命令：它只会在所有给定
	 * key 都不存在的情况下进行设置操作。
	 * 
	 * MSET 是一个原子性(atomic)操作，所有给定 key 都会在同一时间内被设置，某些给定 key 被更新而另一些给定 key
	 * 没有改变的情况，不可能发生。
	 * 
	 * @version >= 1.0.1
	 * @param keysvalues
	 */
	public void mset(final String... keysvalues) {
		execute(new JedisActionNoResult() {
			@Override
			public void action(Jedis jedis) {
				jedis.mset(keysvalues);
			}
		});
	}

	/**
	 * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在。
	 * 
	 * 即使只有一个给定 key 已存在， MSETNX 也会拒绝执行所有给定 key 的设置操作。
	 * 
	 * @param keysvalues
	 * @return 当所有 key 都成功设置，返回 1 。 如果所有给定 key 都设置失败(至少有一个 key 已经存在)，那么返回 0 。
	 */
	public Long msetnx(final String... keysvalues) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.msetnx(keysvalues);
			}
		});
	}

	/**
	 * 返回 key 所关联的字符串值。
	 * 
	 * 如果 key 不存在那么返回特殊值 nil 。
	 * 
	 * 假如 key 储存的值不是字符串类型，返回一个错误，因为 GET 只能用于处理字符串值。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return
	 */
	public String get(final String key) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.get(key);
			}
		});
	}

	/**
	 * 返回所有(一个或多个)给定 key 的值。
	 * 
	 * 如果给定的 key 里面，有某个 key 不存在，那么这个 key 返回特殊值 nil 。因此，该命令永不失败。
	 * 
	 * @version >= 1.0.0
	 * @param keys
	 * @return 一个包含所有给定 key 的值的列表。
	 */
	public List<String> mget(final String... keys) {
		return execute(new JedisAction<List<String>>() {
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.mget(keys);
			}
		});
	}

	/**
	 * 返回 key 中字符串值的子字符串，字符串的截取范围由 start 和 end 两个偏移量决定(包括 start 和 end 在内)。
	 * 负数偏移量表示从字符串最后开始计数， -1 表示最后一个字符， -2 表示倒数第二个，以此类推。
	 * 
	 * @version >= 2.4.0
	 * @param key
	 * @param startOffset
	 *            开始位置
	 * @param endOffset
	 *            结束位置
	 * @return 截取得出的子字符串。
	 */
	public String getrange(String key, long startOffset, long endOffset) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.getrange(key, startOffset, endOffset);
			}
		});
	}

	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)。
	 * 
	 * 当 key 存在但不是字符串类型时，返回一个错误。
	 * 
	 * @param key
	 * @param value
	 * @return 返回给定 key 的旧值。 当 key 没有旧值时，也即是， key 不存在时，返回 nil 。
	 */
	public String getSet(final String key, final String value) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.getSet(key, value);
			}
		});
	}

	/**
	 * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。
	 * 
	 * 如果 key 不存在， APPEND就简单地将给定 key 设为 value。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param value
	 * @return 追加 value 之后， key 中字符串的长度。
	 */
	public Long append(final String key, final String value) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.append(key, value);
			}
		});
	}

	/**
	 * 将 key 中储存的数字值减一。
	 * 
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作。
	 * 
	 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return 执行 DECR 命令之后 key 的值。
	 */
	public Long decr(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {

				return jedis.decr(key);
			}
		});
	}

	/**
	 * 将 key 所储存的值减去减量 decrement 。
	 * 
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECRBY 操作。
	 * 
	 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param integer
	 * @return 减去 decrement 之后， key 的值。
	 */
	public Long decrby(final String key, final long integer) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.decrBy(key, integer);
			}
		});
	}

	/**
	 * 将 key 中储存的数字值增一。
	 * 
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
	 * 
	 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return 执行 INCR 命令之后 key 的值。
	 */
	public Long incr(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.incr(key);
			}
		});
	}

	/**
	 * 将 key 所储存的值加上增量 increment 。
	 * 
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCRBY 命令。
	 * 
	 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
	 * 
	 * 本操作的值限制在 64 位(bit)有符号数字表示之内。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param integer
	 * @return 加上 increment 之后， key 的值。
	 */
	public Long incrby(final String key, final long integer) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.incrBy(key, integer);
			}
		});
	}

	/**
	 * 
	 * 为 key 中所储存的值加上浮点数增量 increment 。
	 * 
	 * @version >= 2.6.0
	 * @param key
	 * @param value
	 * @return 执行命令之后 key 的值。
	 */
	public Double incrByFloat(final String key, final double value) {
		return execute(new JedisAction<Double>() {
			@Override
			public Double action(Jedis jedis) {
				return jedis.incrByFloat(key, value);
			}
		});
	}

	/**
	 * 对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit)。
	 * 
	 * 位的设置或清除取决于 value 参数，可以是 0 也可以是 1 。
	 * 
	 * 当 key 不存在时，自动生成一个新的字符串值。
	 * 
	 * 'a' 的ASCII码是 97。转换为二进制是：01100001。offset的学名叫做“偏移”。
	 * 
	 * @version >= 2.2.0
	 * @param key
	 * @param offset
	 *            偏移量
	 * @param value
	 *            true:1 false:0
	 * @return 指定偏移量原来储存的位 true:1 false:0
	 */
	public Boolean setbit(String key, long offset, boolean value) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.setbit(key, offset, value);
			}
		});
	}

	/**
	 * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)。
	 * 
	 * 当 offset 比字符串值的长度大，或者 key 不存在时，返回 0 。
	 * 
	 * @version >= 2.2.0
	 * @param key
	 * @param offset
	 *            偏移量
	 * @return 指定偏移量原来储存的位 true:1 false:0
	 */
	public Boolean getbit(String key, long offset) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.getbit(key, offset);
			}
		});
	}

	/**
	 * 计算给定字符串中，被设置为 1 的比特位的数量。
	 * 
	 * 一般情况下，给定的整个字符串都会被进行计数，通过指定额外的 start 或 end 参数，可以让计数只在特定的位上进行。
	 * 
	 * @version >= 2.6.0
	 * @param key
	 * @return
	 */
	public Long bitcount(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {

				return jedis.bitcount(key);
			}
		});
	}

	/**
	 * 计算给定字符串中，被设置为 1 的比特位的数量。
	 * 
	 * 一般情况下，给定的整个字符串都会被进行计数，通过指定额外的 start 或 end 参数，可以让计数只在特定的位上进行。
	 * 
	 * @version >= 2.6.0
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public Long bitcount(final String key, long start, long end) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {

				return jedis.bitcount(key, start, end);
			}
		});
	}

	/**
	 * 对一个或多个保存二进制位的字符串 key 进行位元操作，并将结果保存到 destkey 上。
	 * 
	 * operation 可以是 AND 、 OR 、 NOT 、 XOR 这四种操作中的任意一种：
	 * 
	 * BITOP AND destkey key [key ...] ，对一个或多个 key 求逻辑并，并将结果保存到 destkey 。
	 * 
	 * BITOP OR destkey key [key ...] ，对一个或多个 key 求逻辑或，并将结果保存到 destkey 。
	 * 
	 * BITOP XOR destkey key [key ...] ，对一个或多个 key 求逻辑异或，并将结果保存到 destkey 。
	 * 
	 * BITOP NOT destkey key ，对给定 key 求逻辑非，并将结果保存到 destkey。 除了 NOT
	 * 操作之外，其他操作都可以接受一个或多个key 作为输入。
	 * 
	 * @param op
	 * @param destKey
	 * @param srcKeys
	 * @return 保存到 destkey 的字符串的长度，和输入 key 中最长的字符串长度相等。
	 */
	public Long bitop(BitOP op, final String destKey, String... srcKeys) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.bitop(op, destKey, srcKeys);
			}
		});
	}

	/**
	 * 返回 key 所储存的字符串值的长度。
	 * 
	 * @param key
	 * @return 字符串值的长度。 当 key 不存在时，返回 0 。
	 */
	public Long strlen(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.strlen(key);
			}
		});
	}

	// Hash（哈希表）

	/**
	 * 将哈希表 key 中的域 field 的值设为 value 。
	 * 
	 * 如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
	 * 
	 * 如果域 field 已经存在于哈希表中，旧值将被覆盖。
	 * 
	 * @version >= 2.0.0
	 * @param key
	 * @param field
	 * @param value
	 * @return 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 。 如果哈希表中域 field
	 *         已经存在且旧值已被新值覆盖，返回 0 。
	 */
	public Long hset(final String key, final String field, final String value) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.hset(key, field, value);
			}
		});
	}

	/**
	 * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。
	 * 
	 * 若域 field 已经存在，该操作无效。
	 * 
	 * @version >= 2.0.0
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public Boolean hsetnx(final String key, final String field, final String value) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.hsetnx(key, field, value) == 1;
			}
		});
	}

	/**
	 * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
	 * 
	 * 此命令会覆盖哈希表中已存在的域。
	 * 
	 * 如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。
	 * 
	 * @version >= 2.0.0
	 * @param key
	 * @param hash
	 * @return
	 */
	public Boolean hmset(final String key, final Map<String, String> hash) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.hmset(key, hash).equals("OK");
			}
		});
	}

	/**
	 * 查看哈希表 key 中，给定域 field 是否存在。
	 * 
	 * @param key
	 * @param field
	 * @return 如果哈希表不含有给定域，或 key 不存在，返回 false 。
	 */
	public Boolean hexists(final String key, final String field) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.hexists(key, field);
			}
		});
	}

	/**
	 * 返回哈希表 key 中给定域 field 的值。
	 * 
	 * @version >= 2.0.0
	 * @param key
	 * @param field
	 * @return 给定域的值。 当给定域不存在或是给定 key 不存在时，返回 nil 。
	 */
	public String hget(final String key, final String field) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.hget(key, field);
			}
		});
	}

	/**
	 * 返回哈希表 key 中，一个或多个给定域的值。
	 * 
	 * @version >= 2.0.0
	 * @param key
	 * @param fields
	 * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
	 */
	public List<String> hmget(final String key, final String... fields) {
		return execute(new JedisAction<List<String>>() {
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.hmget(key, fields);
			}
		});
	}

	/**
	 * 返回哈希表 key 中，所有的域和值。
	 * 
	 * @param key
	 * @return
	 */
	public Map<String, String> hgetAll(final String key) {
		return execute(new JedisAction<Map<String, String>>() {
			@Override
			public Map<String, String> action(Jedis jedis) {
				return jedis.hgetAll(key);
			}
		});
	}

	/**
	 * 返回哈希表 key 中的所有域。
	 * 
	 * @version >= 2.0.0
	 * @param key
	 * @return 一个包含哈希表中所有域的表。
	 */
	public Set<String> hkeys(final String key) {
		return execute(new JedisAction<Set<String>>() {
			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.hkeys(key);
			}
		});
	}

	/**
	 * 返回哈希表 key 中所有域的值。
	 * 
	 * @version >= 2.0.0
	 * @param key
	 * @return 一个包含哈希表中所有值的表。
	 */
	public List<String> hvals(final String key) {
		return execute(new JedisAction<List<String>>() {
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.hvals(key);
			}
		});
	}

	/**
	 * 返回哈希表 key 中域的数量。
	 * 
	 * @param key
	 * @return 哈希表中域的数量。
	 */
	public Long hlen(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {

				return jedis.hlen(key);
			}
		});
	}

	/**
	 * 为哈希表 key 中的域 field 的值加上增量 increment 。
	 * 
	 * @version >= 2.0.0
	 * @param key
	 * @param field
	 * @param value
	 * @return 执行 HINCRBY 命令之后，哈希表 key 中域 field 的值。
	 */
	public Long hincrBy(final String key, final String field, final long value) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.hincrBy(key, field, value);
			}
		});
	}

	/**
	 * 为哈希表 key 中的域 field 加上浮点数增量 increment 。
	 * 
	 * @version >= 2.6.0
	 * @param key
	 * @param field
	 * @param value
	 * @return 执行加法操作之后 field 域的值。
	 */
	public Double hincrByFloat(final String key, final String field, final long value) {
		return execute(new JedisAction<Double>() {
			@Override
			public Double action(Jedis jedis) {
				return jedis.hincrByFloat(key, field, value);
			}
		});
	}

	/**
	 * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
	 * 
	 * @version >= 2.0.0
	 * @param key
	 * @param fields
	 * @return 被成功移除的域的数量，不包括被忽略的域。
	 */
	public Long hdel(final String key, final String... fields) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.hdel(key, fields);
			}
		});
	}

	// List（列表）

	/**
	 * 将一个或多个值 value 插入到列表 key 的表头
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param strings
	 * @return 执行 LPUSH 命令后，列表的长度。
	 */
	public Long lpush(final String key, final String... strings) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.lpush(key, strings);
			}
		});
	}

	/**
	 * 将值 value 插入到列表 key 的表头，当且仅当 key 存在并且是一个列表。
	 * 
	 * 和 LPUSH 命令相反，当 key 不存在时， LPUSHX 命令什么也不做。
	 * 
	 * @version >= 2.2.0
	 * @param key
	 * @param strings
	 * @return LPUSHX 命令执行之后，表的长度。
	 */
	public Long lpushx(final String key, final String... strings) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.lpushx(key, strings);
			}
		});
	}

	/**
	 * 将一个或多个值 value 插入到列表 key 的表尾(最右边)。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param strings
	 * @return 执行 RPUSH 操作后，表的长度。
	 */
	public Long rpush(final String key, final String... strings) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.rpush(key, strings);
			}
		});
	}

	/**
	 * 将值 value 插入到列表 key 的表尾，当且仅当 key 存在并且是一个列表。
	 * 
	 * 和 RPUSH 命令相反，当 key 不存在时， RPUSHX 命令什么也不做。
	 * 
	 * @version >= 2.2.0
	 * @param key
	 * @param strings
	 * @return RPUSHX 命令执行之后，表的长度。
	 */
	public Long rpushx(final String key, final String... strings) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.rpushx(key, strings);
			}
		});
	}

	/**
	 * 将值 value 插入到列表 key 当中，位于值 pivot 之前或之后。
	 * 
	 * 当 pivot 不存在于列表 key 时，不执行任何操作。
	 * 
	 * 当 key 不存在时， key 被视为空列表，不执行任何操作。
	 * 
	 * 如果 key 不是列表类型，返回一个错误。
	 * 
	 * @version >= 2.2.0
	 * @param key
	 * @param where
	 * @param pivot
	 * @param value
	 * @return 如果命令执行成功，返回插入操作完成之后，列表的长度。 如果没有找到 pivot ，返回 -1 。 如果 key
	 *         不存在或为空列表，返回 0 。
	 */
	public Long linsert(final String key, final LIST_POSITION where, final String pivot, final String value) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.linsert(key, where, pivot, value);
			}
		});
	}

	/**
	 * 将列表 key 下标为 index 的元素的值设置为 value 。
	 * 
	 * 当 index 参数超出范围，或对一个空列表( key 不存在)进行 LSET 时，返回一个错误。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param index
	 * @param value
	 * @return
	 */
	public Boolean lset(final String key, final long index, final String value) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.lset(key, index, value).equals("OK");
			}
		});
	}

	/**
	 * 返回列表 key 中，下标为 index 的元素。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param index
	 * @return 列表中下标为 index 的元素。 如果 index 参数的值不在列表的区间范围内(out of range)，返回 nil 。
	 */
	public String lindex(final String key, final long index) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.lindex(key, index);
			}
		});
	}

	/**
	 * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 stop 指定。
	 * 
	 * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param start
	 * @param end
	 * @return 一个列表，包含指定区间内的元素。
	 */
	public List<String> lrange(final String key, final long start, final long end) {
		return execute(new JedisAction<List<String>>() {
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.lrange(key, start, end);
			}
		});
	}

	/**
	 * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
	 * 
	 * 举个例子，执行命令 LTRIM list 0 2 ，表示只保留列表 list 的前三个元素，其余元素全部删除。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public Boolean ltrim(final String key, final long start, final long end) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.ltrim(key, start, end).equals("OK");
			}
		});
	}

	/**
	 * 移除并返回列表 key 的头元素。
	 * 
	 * @param key
	 * @return 列表的头元素。 当 key 不存在时，返回 nil 。
	 */
	public String lpop(String key) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.lpop(key);
			}
		});
	}

	/**
	 * 它是 LPOP 命令的阻塞版本，当给定列表内没有任何元素可供弹出的时候，连接将被 BLPOP 命令阻塞，直到等待超时或发现可弹出元素为止。
	 * 
	 * 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的头元素。
	 * 
	 * @version >= 2.0.0
	 * @param timeout
	 * @param keys
	 * @return 如果列表为空，返回一个 nil 。 否则，返回一个含有两个元素的列表，第一个元素是被弹出元素所属的 key
	 *         ，第二个元素是被弹出元素的值。
	 */
	public List<String> blpop(final int timeout, final String... keys) {
		return execute(new JedisAction<List<String>>() {
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.blpop(timeout, keys);
			}
		});
	}

	/**
	 * 移除并返回列表 key 的尾元素。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return 列表的尾元素。 当 key 不存在时，返回 nil 。
	 */
	public String rpop(final String key) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.rpop(key);
			}
		});
	}

	/**
	 * 它是 RPOP 命令的阻塞版本，当给定列表内没有任何元素可供弹出的时候，连接将被 BRPOP 命令阻塞，直到等待超时或发现可弹出元素为止。
	 * 
	 * 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的尾部元素。
	 * 
	 * @version >= 2.0.0
	 * @param timeout
	 * @param keys
	 * @return 如果列表为空，返回一个 nil 。 否则，返回一个含有两个元素的列表，第一个元素是被弹出元素所属的 key
	 *         ，第二个元素是被弹出元素的值。
	 */
	public List<String> brpop(final int timeout, final String... keys) {
		return execute(new JedisAction<List<String>>() {
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.brpop(timeout, keys);
			}
		});
	}

	/**
	 * 将列表 source 中的最后一个元素(尾元素)弹出，并返回给客户端。
	 * 
	 * 将 source 弹出的元素插入到列表 destination ，作为 destination 列表的的头元素。
	 * 
	 * @version >= 1.2.0
	 * @param srckey
	 * @param dstkey
	 * @return 被弹出的元素。
	 */
	public String rpoplpush(final String srckey, final String dstkey) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.rpoplpush(srckey, dstkey);
			}
		});
	}

	/**
	 * brpoplpush 是 rpoplpush 的阻塞版本，当给定列表 source 不为空时， brpoplpush 的表现和 rpoplpush
	 * 一样。
	 * 
	 * @version >= 2.2.0
	 * @param source
	 * @param destination
	 * @param timeout
	 *            seconds
	 * @return
	 */
	public String brpoplpush(String source, String destination, int timeout) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.brpoplpush(source, destination, timeout);
			}
		});
	}

	/**
	 * 根据参数 count 的值，移除列表中与参数 value 相等的元素。
	 * 
	 * count 的值可以是以下几种：
	 * 
	 * count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。
	 * 
	 * count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
	 * 
	 * count = 0 : 移除表中所有与 value 相等的值。
	 * 
	 * @param key
	 * @param count
	 * @param value
	 * @return 被移除元素的数量。
	 */
	public Long lrem(final String key, final long count, final String value) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.lrem(key, count, value);
			}
		});
	}

	/**
	 * 返回列表 key 的长度。
	 * <p>
	 * 如果 key 不存在，则 key 被解释为一个空列表，返回 0 .
	 * <p>
	 * 如果 key 不是列表类型，返回一个错误。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return 列表 key 的长度。
	 */
	public Long llen(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.llen(key);
			}
		});
	}

	// Set（集合）
	/**
	 * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。
	 * 
	 * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param members
	 * @return 被添加到集合中的新元素的数量，不包括被忽略的元素。
	 */
	public Long sadd(final String key, final String... members) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.sadd(key, members);
			}
		});
	}

	/**
	 * 
	 * 返回集合 key 的基数(集合中元素的数量)。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return 集合的基数。 当 key 不存在时，返回 0 。
	 */
	public Long scard(final String key) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.scard(key);
			}
		});
	}

	/**
	 * 返回集合 key 中的所有成员。
	 * 
	 * 不存在的 key 被视为空集合。
	 * 
	 * @param key
	 * @return 集合中的所有成员。
	 */
	public Set<String> smembers(final String key) {
		return execute(new JedisAction<Set<String>>() {
			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.smembers(key);
			}
		});
	}

	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合之间的差集。
	 * 
	 * 不存在的 key 被视为空集。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return 交集成员的列表。
	 */
	public Set<String> sdiff(final String... keys) {
		return execute(new JedisAction<Set<String>>() {
			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.sdiff(keys);
			}
		});
	}

	/**
	 * 这个命令的作用和 SDIFF 类似，但它将结果保存到 destination 集合，而不是简单地返回结果集。
	 * 
	 * 如果 destination 集合已经存在，则将其覆盖。
	 * 
	 * destination 可以是 key 本身。
	 * 
	 * @version >= 1.0.0
	 * @param dstkey
	 * @param keys
	 * @return 结果集中的元素数量。
	 */
	public Long sdiffstore(final String dstkey, final String... keys) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.sdiffstore(dstkey, keys);
			}
		});
	}

	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合的交集。
	 * 
	 * 不存在的 key 被视为空集。
	 * 
	 * 当给定集合当中有一个空集时，结果也为空集(根据集合运算定律)。
	 * 
	 * @version >= 1.0.0
	 * @param keys
	 * @return 交集成员的列表。
	 */
	public Set<String> sinter(final String... keys) {
		return execute(new JedisAction<Set<String>>() {
			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.sinter(keys);
			}
		});
	}

	/**
	 * 这个命令类似于 SINTER 命令，但它将结果保存到 destination 集合，而不是简单地返回结果集。
	 * 
	 * 如果 destination 集合已经存在，则将其覆盖。
	 * 
	 * destination 可以是 key 本身。
	 * 
	 * @version >= 1.0.0
	 * @param dstkey
	 * @param keys
	 * @return 结果集中的成员数量。
	 */
	public Long sinterstore(final String dstkey, final String... keys) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.sinterstore(dstkey, keys);
			}
		});
	}

	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合的并集。
	 * 
	 * 不存在的 key 被视为空集。
	 * 
	 * @version >= 1.0.0
	 * @param keys
	 * @return 并集成员的列表。
	 */
	public Set<String> sunion(final String... keys) {
		return execute(new JedisAction<Set<String>>() {
			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.sunion(keys);
			}
		});
	}

	/**
	 * 这个命令类似于 SUNION 命令，但它将结果保存到 destination 集合，而不是简单地返回结果集。
	 * 
	 * 如果 destination 已经存在，则将其覆盖。
	 * 
	 * destination 可以是 key 本身。
	 * 
	 * @param dstkey
	 * @param keys
	 * @return 结果集中的元素数量。
	 */
	public Long sunionstore(final String dstkey, final String... keys) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.sunionstore(dstkey, keys);
			}
		});
	}

	/**
	 * 判断 member 元素是否集合 key 的成员。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param member
	 * @return
	 */
	public Boolean sismember(final String key, final String member) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.sismember(key, member);
			}
		});
	}

	/**
	 * 将 member 元素从 source 集合移动到 destination 集合。
	 * 
	 * SMOVE 是原子性操作。
	 * 
	 * 如果 source 集合不存在或不包含指定的 member 元素，则 SMOVE 命令不执行任何操作，仅返回 0 。否则， member 元素从
	 * source 集合中被移除，并添加到 destination 集合中去。
	 * 
	 * 当 destination 集合已经包含 member 元素时， SMOVE 命令只是简单地将 source 集合中的 member 元素删除。
	 * 
	 * @version >= 1.0.0
	 * @param srckey
	 * @param dstkey
	 * @param member
	 * @return
	 */
	public Boolean smove(final String srckey, final String dstkey, final String member) {
		return execute(new JedisAction<Boolean>() {
			@Override
			public Boolean action(Jedis jedis) {
				return jedis.smove(srckey, dstkey, member) == 1;
			}
		});
	}

	/**
	 * 移除并返回集合中的一个随机元素。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return 被移除的随机元素。
	 */
	public String spop(final String key) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.spop(key);
			}
		});
	}

	/**
	 * 移除并返回集合中的count个随机元素。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param count
	 * @return 被移除的随机元素的集合。
	 */
	public Set<String> spop(final String key, final long count) {
		return execute(new JedisAction<Set<String>>() {
			@Override
			public Set<String> action(Jedis jedis) {
				return jedis.spop(key, count);
			}
		});
	}

	/**
	 * 如果命令执行时，只提供了 key 参数，那么返回集合中的一个随机元素。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @return
	 */
	public String srandmember(final String key) {
		return execute(new JedisAction<String>() {
			@Override
			public String action(Jedis jedis) {
				return jedis.srandmember(key);
			}
		});
	}

	/**
	 * 如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count
	 * 大于等于集合基数，那么返回整个集合。
	 * 
	 * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count的绝对值。
	 * 
	 * @version >= 2.6.0
	 * @param key
	 * @param count
	 * @return
	 */
	public List<String> srandmember(final String key, final int count) {
		return execute(new JedisAction<List<String>>() {
			@Override
			public List<String> action(Jedis jedis) {
				return jedis.srandmember(key, count);
			}
		});
	}

	/**
	 * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
	 * 
	 * @version >= 1.0.0
	 * @param key
	 * @param members
	 * @return 被成功移除的元素的数量，不包括被忽略的元素。
	 */
	public Long srem(final String key, final String... members) {
		return execute(new JedisAction<Long>() {
			@Override
			public Long action(Jedis jedis) {
				return jedis.srem(key, members);
			}
		});
	}

}
