package com.curso.kafka.avro;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.curso.kafka.avro.model.Clima;

public class AvroConsumer {
	private static final AtomicBoolean closed = new AtomicBoolean(false);
	
	public static void main(String[] args) throws IOException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Apagando");
				closed.set(true);
			}
		});
		// Configs
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AvroProducer.BROKER_LIST);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");// __consumer_offsets
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "AvroConsumer");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
		
		KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList(AvroProducer.TOPIC));
		while(!closed.get()) {
			ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(1));
			for(ConsumerRecord<String, byte[]> record: records) {
				System.out.printf("particion = %2d offset = %5d key = %7s ts = %8s value %12s\n",
						record.partition(),
						record.offset(),
						record.key(),
						String.valueOf(record.timestamp()),
						Clima.fromByteBuffer(ByteBuffer.wrap(record.value()))
						);
			}
		}
		consumer.close();
	}
	

}
