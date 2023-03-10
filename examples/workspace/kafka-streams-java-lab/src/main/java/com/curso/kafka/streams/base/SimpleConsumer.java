package com.curso.kafka.streams.base;


import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class SimpleConsumer {
	private static final AtomicBoolean closed = new AtomicBoolean(false);
	
	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Apagando");
				closed.set(true);
			}
		});
		// Configs
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");// __consumer_offsets
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "SimpleConsumer");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		//props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList(Ejemplo01StreamsBase.TOPIC_DESTINO));
		while(!closed.get()) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
			for(ConsumerRecord<String, String> record: records) {
				System.out.printf("particion = %2d offset = %5d key = %7s ts = %8s value %12s\n",
						record.partition(),
						record.offset(),
						record.key(),
						String.valueOf(record.timestamp()),
						record.value()
						);
			}
		}
		consumer.close();
	}
	

}
