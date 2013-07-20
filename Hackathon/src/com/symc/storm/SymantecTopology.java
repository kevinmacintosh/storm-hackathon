package com.symc.storm;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import java.util.Map;

public class SymantecTopology {


        public static class HeatBolt extends BaseRichBolt {


        }


        public static void main(String[] args) throws Exception {
           TopologyBuilder builder = new TopologyBuilder();

           builder.setSpout("tweet", new TwitterSpout(), 10);
           builder.setBolt("heat", new HeatBolt(), 3)
                .fieldsGrouping("tweet", new Fields("location"));
          Config conf = new Config();
           conf.setDebug(true);

           if(args!=null && args.length > 0) {
             conf.setNumWorkers(3);

              StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
           } else {

             LocalCluster cluster = new LocalCluster();
             cluster.submitTopology("test", conf, builder.createTopology());
             Utils.sleep(10000);
             cluster.killTopology("test");
             cluster.shutdown();
        }
    }







}

