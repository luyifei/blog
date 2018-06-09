package com.blog;

import redis.clients.jedis.Client;
import redis.clients.jedis.JedisPubSub;

public class RedisMsgPubSubListener extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		System.out.println("channel:" + channel + "receives message :" + message);
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		// TODO Auto-generated method stub
		super.onPMessage(pattern, channel, message);
	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		System.out.println("channel:" + channel + "is been subscribed:" + subscribedChannels);
	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) {
		// TODO Auto-generated method stub
		super.onUnsubscribe(channel, subscribedChannels);
	}

	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels) {
		// TODO Auto-generated method stub
		super.onPUnsubscribe(pattern, subscribedChannels);
	}

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {
		// TODO Auto-generated method stub
		super.onPSubscribe(pattern, subscribedChannels);
	}

	@Override
	public void onPong(String pattern) {
		// TODO Auto-generated method stub
		super.onPong(pattern);
	}

	@Override
	public void unsubscribe() {
		// TODO Auto-generated method stub
		super.unsubscribe();
	}

	@Override
	public void unsubscribe(String... channels) {
		// TODO Auto-generated method stub
		super.unsubscribe(channels);
	}

	@Override
	public void subscribe(String... channels) {
		// TODO Auto-generated method stub
		super.subscribe(channels);
	}

	@Override
	public void psubscribe(String... patterns) {
		// TODO Auto-generated method stub
		super.psubscribe(patterns);
	}

	@Override
	public void punsubscribe() {
		// TODO Auto-generated method stub
		super.punsubscribe();
	}

	@Override
	public void punsubscribe(String... patterns) {
		// TODO Auto-generated method stub
		super.punsubscribe(patterns);
	}

	@Override
	public void ping() {
		// TODO Auto-generated method stub
		super.ping();
	}

	@Override
	public boolean isSubscribed() {
		// TODO Auto-generated method stub
		return super.isSubscribed();
	}

	@Override
	public void proceedWithPatterns(Client client, String... patterns) {
		// TODO Auto-generated method stub
		super.proceedWithPatterns(client, patterns);
	}

	@Override
	public void proceed(Client client, String... channels) {
		// TODO Auto-generated method stub
		super.proceed(client, channels);
	}

	@Override
	public int getSubscribedChannels() {
		// TODO Auto-generated method stub
		return super.getSubscribedChannels();
	}

}
