package com.curso.kafka.producersconsumers.partitioner;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.curso.kafka.util.TopicCreator;

public class PartitionProducer {
	
	public static String BROKER_LIST = "localhost:9092";
	public static String TOPIC = "partition-producer-topic";
	
	public static void main(String[] args) throws InterruptedException {
		// Topics creados
		
		TopicCreator.createTopics(BROKER_LIST, TOPIC);
		
		// Config
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_LIST);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, SimplePartitioner.class.getName());
		
		KafkaProducer<String, String> producer = new KafkaProducer<>(props);
		
		for(int id=0; id < 5000; id++) {
			String key = String.format("key[%d]", id);
			String value = String.format("value[%d]", id);
			ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key, value);
			System.out.println("Enviando mensaje : " + record.toString());
			producer.send(record);
			Thread.sleep(1000);
		}
		producer.flush();
		producer.close();
	}

}
