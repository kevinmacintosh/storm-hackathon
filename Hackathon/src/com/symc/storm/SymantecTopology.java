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

import storm.trident.TridentTopology;
import twitter4j.Status;

public class SymantecTopology {

	public static class getPictureBolt extends BaseBasicBolt {
		private static final long serialVersionUID = 1L;

		@Override
		public void execute(Tuple tuple, BasicOutputCollector collector) {

			Status status = (Status) tuple.getValue(0);

			collector.emit(new Values(status.getMediaEntities()));

		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("pictures"));
		}
	}

	public static void main(String[] args) throws Exception {

		Config conf = new Config();
		conf.setDebug(true);

		TridentTopology builder = new TridentTopology();

		builder.newStream(teamPrefix("tweets"), new TwitterSpout())
				.parallelismHint(10)
				.each(new Fields("tweet"), new getPictureBolt(),
						new Fields("picture"))
				.persistentAggregate(
				// A nontransactional state built on Riak (Use their HTTP API to
				// see progress)
						new RiakBackingMap.Factory(
								teamPrefix("Symantec"), // The riak 'bucket'
														// name to store results
														// in
								Common.getRiakHosts(), Common.getRiakPort(),
								String.class // The type of the data to store
												// (serialized as json)
						));

		if (args != null && args.length > 0) {
			conf.setNumWorkers(3);

			HackReduceStormSubmitter.submitTopology("Symantec", config,
					builder.build());
		} else {

			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("test", conf, builder.createTopology());
			Utils.sleep(10000);
			cluster.killTopology("test");
			cluster.shutdown();
		}
	}
}
