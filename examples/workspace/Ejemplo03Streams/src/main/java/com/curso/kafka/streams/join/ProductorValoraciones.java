package com.curso.kafka.streams.join;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.curso.kafka.avro.model.Valoracion;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;

public class ProductorValoraciones {
	
	public static final String CITY = "madrid";

	public static void main(String[] args) throws InterruptedException, IOException {
		Properties properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
		properties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		
		KafkaProducer<String,Valoracion> producer = new KafkaProducer<>(properties);
		
		Thread shutdownHook = new Thread(producer::close);
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		int i = 0;
		while(true) {
			i++;
			Valoracion valoracion = Valoracion.newBuilder()
					.setId(i)
					.setValoracion(Math.random()).build();
			
			 
					ProducerRecord<String, Valoracion> record = new ProducerRecord<>(
							Stream06Join.TOPIC_VALORACION,
							"Pelicula "+i, 
							valoracion);
			producer.send(record);
			Thread.sleep(500);
		}
	}
	

}
