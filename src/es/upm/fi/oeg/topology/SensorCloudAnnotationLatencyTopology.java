package es.upm.fi.oeg.topology;


import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import es.upm.fi.oeg.bolt.LatencyObserverBolt;
import es.upm.fi.oeg.bolt.SensorCloudParserBolt;
import es.upm.fi.oeg.bolt.SweetAnnotationsBolt;
import es.upm.fi.oeg.spout.FileSpout;

/*
 * Measures the amount of time to parse and annotate Sensor Cloud messages.
 * The result is a document, latencyResults.txt, that is stored at the Storm node where the LatencyObserverBolt is executed.
 * Each line of latencyResults.txt includes the latency in milliseconds of parsing a message, the timestamps of the processing and arrival, and the content of the message. 
 */
public class SensorCloudAnnotationLatencyTopology {
	
	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("sensorCloudSpout", new FileSpout("/log1h.txt"));
		builder.setBolt("sensorCloudParser", new SensorCloudParserBolt()).shuffleGrouping("sensorCloudSpout");
		builder.setBolt("sweetAnnotator", new SweetAnnotationsBolt()).shuffleGrouping("sensorCloudParser");
		builder.setBolt("latencyObserver", new LatencyObserverBolt("/tmp/latencyResults.txt")).shuffleGrouping("sweetAnnotator");
		
		// Topology general configuration
		Config config = new Config();
		config.setDebug(true);
		config.setMaxSpoutPending(1024);
		
		// To run the topology on the Storm cluster the call must include at least one argument, e.g. the topology name
		if (args != null && args.length > 0) {
			config.setNumWorkers(1);
			StormSubmitter.submitTopologyWithProgressBar(args[0], config, builder.createTopology());
	    }
		else {
		  config.setMaxTaskParallelism(3);
	      LocalCluster cluster = new LocalCluster();
	      cluster.submitTopology("annotation-latency-topology", config, builder.createTopology());

	      Thread.sleep(20000);

	      cluster.shutdown();
	    }
	}

}
