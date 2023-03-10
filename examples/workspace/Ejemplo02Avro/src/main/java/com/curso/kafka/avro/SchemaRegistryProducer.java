package com.curso.kafka.avro;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.curso.kafka.avro.model.Clima;
import com.curso.kafka.util.OpenWeatherMap;
import com.curso.kafka.util.TopicCreator;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;

public class SchemaRegistryProducer {
	
	public static String BROKER_LIST = "localhost:9092";
	public static String TOPIC_BASE = "topic-avro-schema-registry";
	public static String CITY = "madrid";
	
	public static void main(String[] args) throws InterruptedException, IOException {
		// Topics creados
		
		TopicCreator.createTopics(BROKER_LIST, TOPIC_BASE);
		
		// Config
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_LIST);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
		props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		
		KafkaProducer<String, Clima> producer = new KafkaProducer<>(props);
		Thread shutdownHook = new Thread(producer::close);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        
		while(true) {
			Clima clima = OpenWeatherMap.getWeatherFromOpenWeatherMap(CITY);
			ProducerRecord<String, Clima> record = new ProducerRecord<>(TOPIC_BASE, CITY, clima);
			System.out.println("Enviando mensaje : " + record.toString());
			producer.send(record);
			Thread.sleep(1500);
		}
	}
}
