package it.ey.piao.api.configuration.mapper;

import it.ey.dto.*;
import it.ey.entity.*;

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
        configureObbiettivoPerformanceMapping();
        configureOVPStrategia();
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

    /**
     * Configura il mapping per ObbiettivoPerformance escludendo le liste figlie.
     * Le liste stakeHolders e indicatori devono essere popolate manualmente.
     */
    private void configureObbiettivoPerformanceMapping() {
        // Entity → DTO: esclude le liste figlie
        modelMapper.typeMap(ObbiettivoPerformance.class, ObbiettivoPerformanceDTO.class)
                .addMappings(mapper -> {
                    mapper.skip(ObbiettivoPerformanceDTO::setStakeholders);
                    mapper.skip(ObbiettivoPerformanceDTO::setIndicatori);
                });
        modelMapper.typeMap(ObiettivoPrevenzione.class, ObiettivoPrevenzioneDTO.class)
            .addMappings(mapper -> {
                //  mapper.skip(ObiettivoPrevenzione::setStakeHolders);
                mapper.skip(ObiettivoPrevenzioneDTO::setIndicatori);
            });
        modelMapper.typeMap(MisuraPrevenzione.class, MisuraPrevenzioneDTO.class)
            .addMappings(mapper -> {
              //  mapper.skip(MisuraPrevenzione::setStakeHolders);
                mapper.skip(MisuraPrevenzioneDTO::setIndicatori);
            });

        modelMapper.typeMap(ObbiettivoPerformanceDTO.class, ObbiettivoPerformance.class)
                .addMappings(mapper -> {
                    mapper.skip(ObbiettivoPerformance::setStakeholders);
                    mapper.skip(ObbiettivoPerformance::setIndicatori);
                });
        // DTO → Entity: esclude le liste figlie
        modelMapper.typeMap(MisuraPrevenzioneDTO.class, MisuraPrevenzione.class)
            .addMappings(mapper -> {
               // mapper.skip(MisuraPrevenzione::setStakeHolders);
                mapper.skip(MisuraPrevenzione::setIndicatori);
            });

        modelMapper.typeMap(ObiettivoPrevenzioneDTO.class, ObiettivoPrevenzione.class)
            .addMappings(mapper -> {
                //  mapper.skip(ObiettivoPrevenzioneDTO::setStakeHolders);
                mapper.skip(ObiettivoPrevenzione::setIndicatori);
            });

        modelMapper.typeMap(ObbligoLegge.class, ObbligoLeggeDTO.class)
            .addMappings(mapper -> {
                mapper.skip(ObbligoLeggeDTO::setDatiPubblicati);
            });

        modelMapper.typeMap(ObbligoLeggeDTO.class, ObbligoLegge.class)
            .addMappings(mapper -> {
                mapper.skip(ObbligoLegge::setDatiPubblicati);
            });


    }

    /**
     * Configura il mapping per OVPStrategia escludendo le liste figlie.
     * La lista indicatori devono essere popolate manualmente.
     */
    private void configureOVPStrategia() {
        // Entity → DTO: esclude le liste figlie
        modelMapper.typeMap(OVPStrategia.class, OVPStrategiaDTO.class)
                .addMappings(mapper -> {
                    mapper.skip(OVPStrategiaDTO::setIndicatori);
                });

        // DTO → Entity: esclude le liste figlie
        modelMapper.typeMap(OVPStrategiaDTO.class, OVPStrategia.class)
                .addMappings(mapper -> {
                    mapper.skip(OVPStrategia::setIndicatori);
                });
    }


    public <S, T> T map(S source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }

}

