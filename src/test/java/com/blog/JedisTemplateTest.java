package com.blog;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.blog.common.JedisTemplate;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

public class JedisTemplateTest {
	@Test
	public void JedisTemplateTest() {
		JedisTemplate template = new JedisTemplate(new JedisPool("127.0.0.1", 6379));
		Map<String, Double> ky = new HashMap<String, Double>();
		ky.put("1", 1d);
		ky.put("2", 2d);
		ky.put("3", 3d);
		ky.put("4", 4d);
		ky.put("5", 5d);
		ky.put("6", 6d);
		ky.put("7", 7d);
		// template.zadd("testzset", ky);
		Double reply = template.zincrby("testzset", 1d, "7", ZIncrByParams.zIncrByParams().xx());
		System.out.println(reply);
	}
	@Test
	public void subscribeTest() {
		JedisTemplate template = new JedisTemplate(new JedisPool("127.0.0.1", 6379));
		RedisMsgPubSubListener li = new RedisMsgPubSubListener();
		template.subscribe(li, "test");
	}
	@Test
	public void testPublish() throws InterruptedException {
		JedisTemplate template = new JedisTemplate(new JedisPool("127.0.0.1", 6379));
		template.publish("test", "我是天才");
		Thread.sleep(2000);
		template.publish("test", "我牛逼");
		Thread.sleep(2000);
		template.publish("test", "哈哈");
	}
}
