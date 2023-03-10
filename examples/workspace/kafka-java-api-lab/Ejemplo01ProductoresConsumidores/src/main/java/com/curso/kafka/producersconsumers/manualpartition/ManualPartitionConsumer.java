package com.curso.kafka.producersconsumers.manualpartition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.curso.kafka.producersconsumers.partitioner.PartitionProducer;

public class ManualPartitionConsumer {
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
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, PartitionProducer.BROKER_LIST);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");// __consumer_offsets
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "SeekConsumer");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		
		List<TopicPartition> particiones = new ArrayList<>();
		particiones.add(new TopicPartition(PartitionProducer.TOPIC, 0));
		particiones.add(new TopicPartition(PartitionProducer.TOPIC, 2));
		
		consumer.assign(particiones);
		
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
