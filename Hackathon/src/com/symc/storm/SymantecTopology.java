package com.symc.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

public class SymantecTopology {

	public static class HeatBolt extends BaseBasicBolt {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void execute(Tuple tuple, BasicOutputCollector collector) {
			collector.emit(new Values(tuple));
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("heat"));
		}
	}

	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("tweet", new TwitterSpout(args[0], args[1]), 10);
		builder.setBolt("heat", new HeatBolt(), 3)
				.shuffleGrouping("tweet");
		Config conf = new Config();
		conf.setDebug(true);

		/*
		 * if(args!=null && args.length > 0) { conf.setNumWorkers(3);
		 * 
		 * StormSubmitter.submitTopology(args[0], conf,
		 * builder.createTopology()); } else {
		 */
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("test", conf, builder.createTopology());
		Utils.sleep(10000);
		cluster.killTopology("test");
		cluster.shutdown();
		// }
	}

}
