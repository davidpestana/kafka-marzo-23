package com.curso.kafka.streams.avro;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import com.curso.kafka.avro.model.Clima;
import com.curso.kafka.avro.model.Datos;
import com.curso.kafka.util.TopicCreator;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class Ejemplo02StreamsAvro {

	public static String TOPIC_ORIGEN = "streams-ejemplo02avro-origen";
	public static String TOPIC_DESTINO = "streams-ejemplo02avro-destino";
	public static String KAFKA_SERVERS = "localhost:9092";
	
	public static void main(String[] args) throws InterruptedException {
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, Ejemplo02StreamsAvro.class.getName()+"1.0.0");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,KAFKA_SERVERS);
		props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "3");
		
		TopicCreator.createTopics(KAFKA_SERVERS, TOPIC_ORIGEN,TOPIC_DESTINO);
		
		// Fase de topologias
		StreamsBuilder builder = new StreamsBuilder();
		Serde<Clima> climaSerde = new SpecificAvroSerde<>();
		Serde<Datos> datosSerde = new SpecificAvroSerde<>();
		climaSerde.configure(Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);
		datosSerde.configure(Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);
		KStream<String, Clima> streamInicial = builder.stream(TOPIC_ORIGEN,Consumed.with(Serdes.String(), climaSerde));
		KStream<String, Datos> streamDatos = streamInicial.mapValues((value)-> value.getDatos());
		
		streamDatos.to(TOPIC_DESTINO,Produced.with(Serdes.String(), datosSerde));
		streamDatos.print(Printed.<String,Datos>toSysOut().withLabel(TOPIC_DESTINO));
		// Fase de construcción
		KafkaStreams streams = new KafkaStreams(builder.build(), props);
		
		Thread thread = new Thread(streams::close);
		Runtime.getRuntime().addShutdownHook(thread);
		
		streams.start();
		
	}

}
