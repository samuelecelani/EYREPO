package it.ey.piao.api.configuration.mapper;

import it.ey.entity.StatoPIAO;
import it.ey.entity.StatoSezione;

import lombok.Getter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;


@Getter
@Component
@Primary
public class GenericMapper {

    private final ModelMapper modelMapper;

    public GenericMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        configureGenericConverters();
    }


    private void configureGenericConverters() {
        // Enum → String (già presente)
        Converter<Enum<?>, String> enumToStringConverter = ctx ->
            ctx.getSource() == null ? null : ctx.getSource().name();

        // StatoPIAO → String (usa il campo testo)
        Converter<StatoPIAO, String> statoPiaoToStringConverter = ctx ->
            ctx.getSource() == null ? null : ctx.getSource().getTesto();

        // StatoSezione → String (usa il campo testo)
        Converter<StatoSezione, String> statoSezioneToStringConverter = ctx ->
            ctx.getSource() == null ? null : ctx.getSource().getTesto();

        // Registrazione converter
        modelMapper.addConverter(enumToStringConverter);
        modelMapper.addConverter(statoPiaoToStringConverter);
        modelMapper.addConverter(statoSezioneToStringConverter);
    }


    public <S, T> T map(S source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }

}

