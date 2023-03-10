package com.curso.kafka.streams.join;

import org.apache.kafka.streams.kstream.ValueJoiner;

import com.curso.kafka.avro.model.Pelicula;
import com.curso.kafka.avro.model.PeliculaValorada;
import com.curso.kafka.avro.model.Valoracion;

public class PeliculasValoracionJoiner implements ValueJoiner<Valoracion, Pelicula, PeliculaValorada>{

	@Override
	public PeliculaValorada apply(Valoracion valoracion, Pelicula pelicula) {
		
		return PeliculaValorada.newBuilder()
				.setId(pelicula.getId())
				.setTitulo(pelicula.getTitulo())
				.setValoracion(valoracion.getValoracion())
				.setYearPub(pelicula.getYearPub()).build();
	}

}
