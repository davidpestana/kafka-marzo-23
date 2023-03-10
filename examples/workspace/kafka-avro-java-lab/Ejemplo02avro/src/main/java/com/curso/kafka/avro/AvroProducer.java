package com.curso.kafka.avro;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.curso.kafka.avro.model.Clima;
import com.curso.kafka.util.OpenWeatherMap;
import com.curso.kafka.util.TopicCreator;

public class AvroProducer {
	
	public static String BROKER_LIST = "localhost:9092";
	public static String TOPIC = "topic-avro2";
	public static String CITY = "madrid";
	
	public static void main(String[] args) throws InterruptedException, IOException {
		// Topics creados
		
		TopicCreator.createTopics(BROKER_LIST, TOPIC);
		
		// Config
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_LIST);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
		
		KafkaProducer<String, byte[]> producer = new KafkaProducer<>(props);
		Thread shutdownHook = new Thread(producer::close);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        
		while(true) {
			Clima clima = OpenWeatherMap.getWeatherFromOpenWeatherMap(CITY);
			byte[] climaSerializado = clima.toByteBuffer().array();
			ProducerRecord<String, byte[]> record = new ProducerRecord<>(TOPIC, CITY, climaSerializado);
			System.out.println("Enviando mensaje : " + record.toString());
			producer.send(record);
			Thread.sleep(1500);
		}

	}

}
