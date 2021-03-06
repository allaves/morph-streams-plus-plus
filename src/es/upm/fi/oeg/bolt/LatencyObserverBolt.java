package es.upm.fi.oeg.bolt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.swing.text.DateFormatter;

import org.apache.commons.lang3.time.DateFormatUtils;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class LatencyObserverBolt extends BaseRichBolt {
	private OutputCollector collector;
	private String filePath;

	public LatencyObserverBolt(String filePath) {
		this.filePath = filePath;
	}
	
	@Override
	public void prepare(Map stormConf, TopologyContext context,	OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		// Convert system time to xsd:dateTime
		long arrivalTime = System.currentTimeMillis();
		//String arrivalTimeStr = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(arrivalTime);
		String messageId = input.getMessageId().toString();
		String message = input.getValues().toString();
		long processingTime = Long.parseLong(input.getStringByField("observationResultTime"));
		// Latency in milliseconds
		long latency = arrivalTime - processingTime;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
			writer.newLine();
			writer.append(latency + ", " + arrivalTime + ", " + message + ", " + messageId);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		collector.ack(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// Actually, there is no message emitted
		declarer.declare(new Fields("arrivalTimestamp", "message"));
	}
	
//	@Override
//	public void cleanup() {
//		
//	}
	
	

}
