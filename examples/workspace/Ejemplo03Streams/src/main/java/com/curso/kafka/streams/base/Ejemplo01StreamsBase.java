package com.curso.kafka.streams.base;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import com.curso.kafka.util.TopicCreator;

public class Ejemplo01StreamsBase {

	public static String TOPIC_ORIGEN = "streams-ejemplo01base-origen";
	public static String TOPIC_DESTINO = "streams-ejemplo01base-destino";
	public static String KAFKA_SERVERS = "localhost:9092";
	
	public static void main(String[] args) throws InterruptedException {
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, Ejemplo01StreamsBase.class.getName()+"1.0.0");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,KAFKA_SERVERS);
		props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "3");
		
		TopicCreator.createTopics(KAFKA_SERVERS, TOPIC_ORIGEN,TOPIC_DESTINO);
		
		// Fase de topologias
		StreamsBuilder builder = new StreamsBuilder();
		
		KStream<String, String> streamInicial = builder.stream(TOPIC_ORIGEN,Consumed.with(Serdes.String(), Serdes.String()));
	
		KStream<String, String> streamValorEnMays = streamInicial.mapValues((valor)-> valor.toUpperCase());
		streamValorEnMays.to(TOPIC_DESTINO,Produced.with(Serdes.String(), Serdes.String()));
		streamValorEnMays.print(Printed.<String,String>toSysOut().withLabel(TOPIC_DESTINO));
		
		// Fase de construcción
		KafkaStreams streams = new KafkaStreams(builder.build(), props);
		
		Thread thread = new Thread(streams::close);
		Runtime.getRuntime().addShutdownHook(thread);
		
		streams.start();
		
	}

}
