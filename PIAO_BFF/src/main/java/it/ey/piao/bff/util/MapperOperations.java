package it.ey.piao.bff.util;

import java.util.List;

import org.modelmapper.ModelMapper;

public class MapperOperations {

    private MapperOperations() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <S,T> T convert(S source, Class<T> clazz) {
        return new ModelMapper().map(source, clazz);
    }

    public static <S, T> List<T> convertList(List<S> source, Class<T> targetClass) {
        return source
          .stream()
          .map(element -> new ModelMapper().map(element, targetClass))
          .toList();
    }
}
