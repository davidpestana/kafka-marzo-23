package com.curso.kafka.avro;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.curso.kafka.avro.model.Ciudad;
import com.curso.kafka.avro.model.Clima;
import com.curso.kafka.avro.model.Datos;
import com.curso.kafka.util.OpenWeatherMap;
import com.curso.kafka.util.TopicCreator;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;

public class SchemaRegistryProducerRecordNameStrategyDatos {
	
	public static String CITY = "madrid";
	
	public static void main(String[] args) throws InterruptedException, IOException {
		// Topics creados
		
		TopicCreator.createTopics(SchemaRegistryProducerRecordNameStrategyClima.BROKER_LIST,SchemaRegistryProducerRecordNameStrategyClima.TOPIC_BASE);
		
		// Config
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, SchemaRegistryProducerRecordNameStrategyClima.BROKER_LIST);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
		props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class.getName());
		
		KafkaProducer<Ciudad, Datos> producer = new KafkaProducer<>(props);
		Thread shutdownHook = new Thread(producer::close);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        
		while(true) {
			Clima clima = OpenWeatherMap.getWeatherFromOpenWeatherMap(CITY);
			Ciudad ciudad = Ciudad.newBuilder().setCiudad(CITY).build();
			ProducerRecord<Ciudad, Datos> record = new ProducerRecord<>(SchemaRegistryProducerRecordNameStrategyClima.TOPIC_BASE, ciudad, clima.getDatos());
			System.out.println("Enviando mensaje : " + record.toString());
			producer.send(record);
			Thread.sleep(1500);
		}
	}
}
