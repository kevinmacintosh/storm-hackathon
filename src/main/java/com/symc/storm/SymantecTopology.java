package com.symc.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import twitter4j.Status;

public class SymantecTopology {

	public static class GetGeoBolt extends BaseBasicBolt {
		int totCounter = 0;
		int badCounter = 0;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void execute(Tuple tuple, BasicOutputCollector collector) {
			totCounter++;
			Status status = (Status) tuple.getValue(0);
			if(status.getGeoLocation() == null){
				badCounter++;
			}
			collector.emit(new Values("No Geolocation: " + badCounter + " -- Total: " + totCounter));
			
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("coords"));
		}
	}
	
/*	public static class getStateBolt extends BaseBasicBolt {

		@Override
		public void execute(Tuple input, BasicOutputCollector collector) {
			if(input == null){
				//collecter.emit(new Values());
			}
			float lat = input.getFloat(0);
			float lon = input.getFloat(1);
			
			
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			// TODO Auto-generated method stub
			
		}
		
	}*/

	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("tweet", new TwitterSpout(), 10);
		builder.setBolt("coords", new GetGeoBolt(), 3).shuffleGrouping("tweet");
		
		Config conf = new Config();
		conf.setDebug(true);

		if (args != null && args.length > 0) {
			conf.setNumWorkers(3);

			StormSubmitter.submitTopology(args[0], conf,
					builder.createTopology());
		} else {

			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("test", conf, builder.createTopology());
			Utils.sleep(10000);
			cluster.killTopology("test");
			cluster.shutdown();
		}
	}

}
