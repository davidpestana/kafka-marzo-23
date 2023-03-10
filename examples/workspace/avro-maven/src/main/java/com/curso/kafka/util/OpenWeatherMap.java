package com.curso.kafka.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.curso.kafka.avro.model.Clima;
import com.curso.kafka.avro.model.Datos;
import com.curso.kafka.avro.model.Detalles;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;;

public class OpenWeatherMap {
	private static String API_KEY = "d02c68d0317975837c41b4339c1dff01";

	private static Map<String, Object> jsonToMap(String responseString) {
		return new Gson().fromJson(responseString, new TypeToken<HashMap<String, Object>>() {
		}.getType());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Clima getWeatherFromOpenWeatherMap(String city) throws IOException {
		String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY;
		StringBuilder result = new StringBuilder();
		URL urlRequest = new URL(url);
		URLConnection connection = urlRequest.openConnection();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			result.append(line);
		}
		reader.close();
		System.out.println(result);
		Map resultMap = jsonToMap(result.toString());
		Map mainMap = jsonToMap(resultMap.get("main").toString());
		List<Object> list = (List<Object>) resultMap.get("weather");
		List<Detalles> detalles = new ArrayList();
		for (Object detail : list) {
			Map<String, Object> detailMap = (Map) detail;
			detalles.add(Detalles.newBuilder().setId((long) Double.parseDouble(detailMap.get("id").toString()))
					.setPrincipal(detailMap.get("main").toString()).setIcono(detailMap.get("icon").toString())
					.setDescripcion(detailMap.get("description").toString()).build());
		}
		Datos datos = Datos.newBuilder().setPresion((int) Double.parseDouble(mainMap.get("pressure").toString()))
				.setHumedad((int) Double.parseDouble(mainMap.get("humidity").toString()))
				.setTemp((int) Double.parseDouble(mainMap.get("temp").toString()))
				.setTempMax((int) Double.parseDouble(mainMap.get("temp_max").toString()))
				.setTempMin((int) Double.parseDouble(mainMap.get("temp_min").toString())).build();
		Clima clima = Clima.newBuilder().setId((long) Double.parseDouble(resultMap.get("id").toString()))
				.setNombre(resultMap.get("name").toString()).setDatos(datos).setDetalles(detalles).build();
		return clima;
	}
}