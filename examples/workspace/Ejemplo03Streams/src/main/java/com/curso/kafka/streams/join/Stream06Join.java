package com.curso.kafka.streams.join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import com.curso.kafka.avro.model.Pelicula;
import com.curso.kafka.avro.model.PeliculaValorada;
import com.curso.kafka.avro.model.Valoracion;
import com.curso.kafka.util.TopicCreator;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class Stream06Join {

	public static final String TOPIC_PELICULAS = "stream-topic-peliculas";
	public static final String TOPIC_PELICULAS_REKEYED = "stream-topic-rekeyed";
	public static final String TOPIC_VALORACION = "stream-topic-valoracion";
	public static final String TOPIC_PELICULAS_VALORADAS = "stream-topic-peliculas-valoradas";
	private static final CountDownLatch latch = new CountDownLatch(1); 
	
	public static void createTopics(Properties envProps) {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));
        AdminClient client = AdminClient.create(config);

        List<NewTopic> topics = new ArrayList<>();

        topics.add(new NewTopic(TOPIC_PELICULAS,3,Short.parseShort("1")));
        topics.add(new NewTopic(TOPIC_PELICULAS_REKEYED,3,Short.parseShort("1")));
        topics.add(new NewTopic(TOPIC_PELICULAS_VALORADAS,3,Short.parseShort("1")));
        topics.add(new NewTopic(TOPIC_VALORACION,3,Short.parseShort("1")));
        client.createTopics(topics);
        client.close();
    }
	
	public static void main(String[] args) throws InterruptedException {
		// Generación de propiedades de conexión
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "Stream05join"); 
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9091,localhost:9092,localhost:9093");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
		props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        
		TopicCreator.createTopics("localhost:9092",
				TOPIC_PELICULAS,TOPIC_PELICULAS_REKEYED,TOPIC_PELICULAS_VALORADAS,TOPIC_VALORACION);
		
        // Generación de serde para serializar/deserializar

        Serde<PeliculaValorada> pVSerde = new SpecificAvroSerde<>();
        pVSerde.configure(Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);

        // Construcción del StreamBuilder que genera la topología del procesador.
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Pelicula> peliculasStream = builder.<String,Pelicula>stream(TOPIC_PELICULAS);
        peliculasStream.map((key,pelicula) -> new KeyValue<>(String.valueOf(pelicula.getId()), pelicula));
        peliculasStream.to(TOPIC_PELICULAS_REKEYED);
        peliculasStream.print(Printed.<String, Pelicula>toSysOut().withLabel(TOPIC_PELICULAS_REKEYED));
        KTable<String, Pelicula> peliculas = builder.table(TOPIC_PELICULAS_REKEYED);
        
        KStream<String, Valoracion> valoraciones = builder.<String,Valoracion>stream(TOPIC_VALORACION);
        KStream<String, PeliculaValorada> peliculasValoradas = valoraciones.join(peliculas, new PeliculasValoracionJoiner());
        peliculasValoradas.to(TOPIC_PELICULAS_VALORADAS,Produced.<String, PeliculaValorada>with(Serdes.String(),pVSerde));
        peliculasValoradas.print(Printed.<String, PeliculaValorada>toSysOut().withLabel(TOPIC_PELICULAS_VALORADAS));
        
        final KafkaStreams streams = new KafkaStreams(builder.build(),props);
        Runtime.getRuntime().addShutdownHook(new Thread("stream-shutdown-hook") {

			@Override
			public void run() {
				streams.close();
				latch.countDown();
			}
        	
        });
        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
	}

}
