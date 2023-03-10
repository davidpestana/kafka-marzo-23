package com.curso.kafka.util;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

public class TopicCreator {
	public static void createTopics(String servers,String... topics) throws InterruptedException {
		Properties props = new Properties();
		props.put("bootstrap.servers", servers);
		AdminClient adminClient = AdminClient.create(props);
		ArrayList<NewTopic> newTopics = new ArrayList<>();
		for(String topicName: topics) {
			newTopics.add(new NewTopic(topicName, 3,Short.parseShort("1")));
		}
		CreateTopicsResult result = adminClient.createTopics(newTopics);
		
		try {
			// El .get() consigue que el topic sea síncrono
			result.all().get();
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		} catch (ExecutionException e) {
			System.out.println(e.getMessage());
		}	
	}
}
