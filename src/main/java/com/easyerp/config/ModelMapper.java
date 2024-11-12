package com.easyerp.config;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ModelMapper {
	
//	 public <T, R> R convert(T source, Function<T, R> converter) {
//	        if (source == null) {
//	            throw new IllegalArgumentException("Fonte não pode ser null");
//	        }
//	        return converter.apply(source);
//	    }
//
//	    public <T, R> Page<R> convertPage(Page<T> source, Function<T, R> converter) {
//	        if (source == null) {
//	            throw new IllegalArgumentException("Fonte não pode ser null");
//	        }
//	        return source.map(converter);
//	    }

    public <T, R> Page<R> convertPage(Page<T> source, Converter<T, R> converter) {
        return source.map(converter::convert);
    }

    public <T, R> R converter(T source, Converter<T, R> converter) {
        return converter.convert(source);
    }
    
    public <T, R> List<R> convertList(List<T> source, Converter<T, R> converter) {
        return source.stream().map(converter::convert).collect(Collectors.toList());
    }

    // Converter um set de entidades em um set de DTOs
    public <T, R> Set<R> convertSet(Set<T> source, Converter<T, R> converter) {
        return source.stream().map(converter::convert).collect(Collectors.toSet());
    }
}
