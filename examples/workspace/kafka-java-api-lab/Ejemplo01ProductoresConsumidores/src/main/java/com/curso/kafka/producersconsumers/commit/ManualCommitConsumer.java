package com.curso.kafka.producersconsumers.commit;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.curso.kafka.producersconsumers.partitioner.PartitionProducer;

public class ManualCommitConsumer {
	private static final AtomicBoolean closed = new AtomicBoolean(false);
	
	public static void main(String[] args) throws InterruptedException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Apagando");
				closed.set(true);
			}
		});
		// Configs
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, PartitionProducer.BROKER_LIST);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "AutoCommitConsumer");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList(PartitionProducer.TOPIC));
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
			consumer.commitSync();
			Thread.sleep(5000);
			
		}
		consumer.close();
	}
}
