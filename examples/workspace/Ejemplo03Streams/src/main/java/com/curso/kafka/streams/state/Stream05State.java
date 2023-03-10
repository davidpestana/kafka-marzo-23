package com.curso.kafka.streams.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import com.curso.kafka.avro.model.Clima;
import com.curso.kafka.avro.model.Datos;
import com.curso.kafka.util.TopicCreator;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class Stream05State {

	public static final String TOPIC_ORIGEN = "stream-state-topic-origen";
	public static final String TOPIC_DESTINO = "stream-state-topic-destino";
	public static final String TOPIC_DATOS = "stream-state-topic-datos";
	public static final String TOPIC_KSTREAM = "stream-state-topic-datos";
	public static final String KV_STORE = "sumaTemperaturas";
	public static String BROKER_LIST = "localhost:9092";
	
	public static void main(String[] args) throws InterruptedException {
		
		TopicCreator.createTopics(BROKER_LIST, TOPIC_ORIGEN,TOPIC_DESTINO,TOPIC_KSTREAM,TOPIC_DATOS);
		
		
		// Generación de propiedades de conexión
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, Stream05State.class.getName()+"1.0.0"); 
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.FloatSerde.class.getName());
		props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        
        // Generación	 de serde para serializar/deserializar
        Serde<String> stringSerde = Serdes.String();
        Serde<Clima> climaSerde = new SpecificAvroSerde<>();
        Serde<Datos> datosSerde = new SpecificAvroSerde<>();
        climaSerde.configure(Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);
        datosSerde.configure(Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);
        // Construcción del StreamBuilder que genera la topología del procesador.
        StreamsBuilder builder = new StreamsBuilder();
          // Construcción de caché en un topic
        Map<String, String> changeLogConfigs = new HashMap<>(); 
        changeLogConfigs.put("retention.ms","172800000" ); 
        changeLogConfigs.put("retention.bytes", "10000000000");
        changeLogConfigs.put("cleanup.policy", "compact,delete");
       
        //;
       
        // Conexión del stream como consumidor desde topic-origen
        KStream<String, Clima> simpleFirstStream = 
            builder.stream(TOPIC_ORIGEN, Consumed.with(stringSerde, climaSerde));
        // Procesador (Primer nodo hijo) que pasa el texto a mayúsculas
        KStream<String, Clima> upperCasedStream =
            simpleFirstStream.mapValues( (value) -> {
            	value.setNombre((value.getNombre()+"").toUpperCase());
            	return value;
            });
        // Segundo nodo
        
        KStream<String, Datos> datosKStream = upperCasedStream.<String,Datos>map((key,value) -> new KeyValue<String,Datos>(value.getNombre().toString(), value.getDatos()));
        datosKStream.to(TOPIC_DATOS, Produced.<String, Datos>with(stringSerde, datosSerde));
        datosKStream.print(Printed.<String,Datos>toSysOut().withLabel(TOPIC_DATOS));
        // Conexión del stream al topic-destino como productor de mensajes
        KGroupedStream<String,Float>temperaturasKStream = simpleFirstStream.mapValues((value)->value.getDatos().getTemp()).groupByKey();
        temperaturasKStream.reduce((tempAlmacenada,nuevaTemp)-> tempAlmacenada + nuevaTemp, 
        Materialized.<String,Float,KeyValueStore<Bytes, byte[]>>as(KV_STORE).withLoggingEnabled(changeLogConfigs));

        upperCasedStream.to( TOPIC_DESTINO,
            Produced.with(stringSerde, climaSerde));
        // Log de desarrollo
        upperCasedStream.print(Printed.<String,Clima>toSysOut().withLabel(TOPIC_DESTINO));
        
        
        // Nodo que obtiene solo los datos
        
        
        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(),props);
        
        Thread shutdownHook = new Thread(kafkaStreams::close);
		Runtime.getRuntime().addShutdownHook(shutdownHook);
        // Ejecución del hilo de kafka streams.
        kafkaStreams.start();
        Thread.sleep(5000);
        while(!kafkaStreams.state().isRunningOrRebalancing()) {
        	System.out.println("Waiting 2s. State is : " + kafkaStreams.state());
        	Thread.sleep(2000);
        }
        while(kafkaStreams.state().isRunningOrRebalancing()) {
        	ReadOnlyKeyValueStore<String, Long> keyValueStore =
        		kafkaStreams.store(StoreQueryParameters.fromNameAndType(KV_STORE, QueryableStoreTypes.keyValueStore()));
        	System.out.println("Media Temperatura Madrid:" + keyValueStore.get(SchemaRegistryProducerState.CITY));
        	Thread.sleep(2000);
        }
	}

}
