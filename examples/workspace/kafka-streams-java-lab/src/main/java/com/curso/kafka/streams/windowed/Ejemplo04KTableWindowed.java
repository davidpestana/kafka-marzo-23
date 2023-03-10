package com.curso.kafka.streams.windowed;

import java.time.Duration;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindowedKStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import com.curso.kafka.util.TopicCreator;

public class Ejemplo04KTableWindowed {

	public static String TOPIC_ORIGEN = "streams-ejemplo03ktw-origen";
	public static String TOPIC_DESTINO = "streams-ejemplo03ktw-destino";
	public static String KAFKA_SERVERS = "localhost:9092";
	
	public static void main(String[] args) throws InterruptedException {
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, Ejemplo04KTableWindowed.class.getName()+"1.0.0");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,KAFKA_SERVERS);
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass().getName());
		//props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "3");
		
		TopicCreator.createTopics(KAFKA_SERVERS, TOPIC_ORIGEN,TOPIC_DESTINO);
		
		// Fase de topologias
		StreamsBuilder builder = new StreamsBuilder();
		
		KStream<String, String> streamInicial = builder.stream(TOPIC_ORIGEN,Consumed.with(Serdes.String(), Serdes.String()));
		KGroupedStream<String, String> groupByKey = streamInicial.map((operacion,usuario)-> new KeyValue<>(usuario, usuario)).groupByKey();
		
		TimeWindowedKStream<String, String> misTrocitosDe20Seg = groupByKey.windowedBy(TimeWindows.of(Duration.ofSeconds(5)));
		
		KTable<Windowed<String>, Long> kTable = misTrocitosDe20Seg.count();
		KTable<Windowed<String>, Long> miFiltroDeAbusones = kTable.filter((usuario,numOperaciones)-> numOperaciones > 10);
		KStream<String, Long> filter = miFiltroDeAbusones.toStream().filter((usuario,numOperaciones) -> numOperaciones != null)
				.map((clave,valor)->new KeyValue<>(clave.key().toString(), valor));
		filter.print(Printed.<String,Long>toSysOut().withLabel("Abusones"));
		filter.to(TOPIC_DESTINO,Produced.with(Serdes.String(), Serdes.Long()));
		// Fase de construcción
		KafkaStreams streams = new KafkaStreams(builder.build(), props);
		
		Thread thread = new Thread(streams::close);
		Runtime.getRuntime().addShutdownHook(thread);
		
		streams.start();
		
	}

}
