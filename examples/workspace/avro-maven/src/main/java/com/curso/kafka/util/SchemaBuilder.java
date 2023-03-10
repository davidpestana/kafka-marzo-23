package com.curso.kafka.util;

import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;

public class SchemaBuilder {
	public static String getSchemaFromClassName(String className) throws ClassNotFoundException {
		Schema schema = ReflectData.get().getSchema(Class.forName(className));
		System.out.println(schema.toString());
		return schema.toString();
	}
}
