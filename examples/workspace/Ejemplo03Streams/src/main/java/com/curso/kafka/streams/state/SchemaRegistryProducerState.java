package com.curso.kafka.streams.state;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.curso.kafka.avro.model.Clima;
import com.curso.kafka.util.OpenWeatherMap;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;

public class SchemaRegistryProducerState {
	

	public static String CITY = "madrid";
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		// Config
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Stream05State.BROKER_LIST);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
		props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		
		KafkaProducer<String, Clima> producer = new KafkaProducer<>(props);
		Thread shutdownHook = new Thread(producer::close);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        
		while(true) {
			Clima clima = OpenWeatherMap.getWeatherFromOpenWeatherMap(CITY);
			ProducerRecord<String, Clima> record = new ProducerRecord<>(Stream05State.TOPIC_ORIGEN, CITY, clima);
			System.out.println("Enviando mensaje : " + record.toString());
			producer.send(record);
			Thread.sleep(1500);
		}

	}

}
