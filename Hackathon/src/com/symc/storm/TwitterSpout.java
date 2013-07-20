package com.symc.storm;

import backtype.storm.Config;
import twitter4j.auth.AccessToken;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TwitterSpout extends BaseRichSpout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SpoutOutputCollector _collector;
	LinkedBlockingQueue<Status> queue = null;
	TwitterStream _twitterStream;

	public TwitterSpout() {
	}

	@Override
	public void open(@SuppressWarnings("rawtypes") Map conf,
			TopologyContext context, SpoutOutputCollector collector) {
		queue = new LinkedBlockingQueue<Status>(1000);
		_collector = collector;
		StatusListener listener = new StatusListener() {

			@Override
			public void onStatus(Status status) {
				queue.offer(status);
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice sdn) {
			}

			@Override
			public void onTrackLimitationNotice(int i) {
			}

			@Override
			public void onScrubGeo(long l, long l1) {
			}

			@Override
			public void onException(Exception e) {
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub

			}

		};
		TwitterStreamFactory fact = new TwitterStreamFactory();
		String token = "228505511-bSJET8xOy0PHN3jklOYAQBB3f9TFmkOYb62m2hnA";
		String tokenSecret = "0mQo1Ay3tQefRSYl5iHPfyQmoJuAJ38Wlsi1O8Cv4Ck";
		String consumer = "njChgpwWKsJuzUyErMngZg";
		String consumerSecret = "A9enqKITbufAnmzR47Uqj11xSDUvLokH2iKH5kIHg";
		AccessToken accessToken = new AccessToken(token, tokenSecret);
		_twitterStream = fact.getInstance();
		_twitterStream.setOAuthConsumer(consumer, consumerSecret);
		_twitterStream.setOAuthAccessToken(accessToken);
		_twitterStream.addListener(listener);
		String[] filters = {"SDCC", "comiccon", "cosplay"};
		_twitterStream.filter(new FilterQuery(0, null, filters, null));
	}

	@Override
	public void nextTuple() {
		Status ret = queue.poll();
		if (ret == null) {
			Utils.sleep(50);
		} else {
			_collector.emit(new Values(ret));
		}
	}

	@Override
	public void close() {
		_twitterStream.shutdown();
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		Config ret = new Config();
		ret.setMaxTaskParallelism(1);
		return ret;
	}

	@Override
	public void ack(Object id) {
	}

	@Override
	public void fail(Object id) {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("tweet"));
	}

}
