package it.ey.piao.api.utilsPrivate;

import it.ey.dto.SezioneBaseDTO;
import it.ey.enums.CodTipologiaIndicatoreEnum;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.*;
    import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
    import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SezioneUtils
{
    private static  ISezione1Repository sezione1Repository;
    private static Sezione1Mapper sezione1Mapper;

    private static ISezione21Repository sezione21Repository;
    private static Sezione21Mapper sezione21Mapper;

    private static ISezione22Repository sezione22Repository;
    private static Sezione22Mapper sezione22Mapper;

    private static ISezione23Repository sezione23Repository;
    private static Sezione23Mapper sezione23Mapper;

    private static ISezione31Repository sezione31Repository;
    private static Sezione31Mapper sezione31Mapper;

    private static ISezione32Repository sezione32Repository;
    private static Sezione32Mapper sezione32Mapper;

    private static ISezione331Repository sezione331Repository;

    private static Sezione331Mapper sezione331Mapper;

    private static ISezione332Repository sezione332Repository;
    private static Sezione332Mapper sezione332Mapper;

    private static ISezione4Repository sezione4Repository;
    private static Sezione4Mapper sezione4Mapper;

    public SezioneUtils(ISezione1Repository sezione1Repository,
                        Sezione1Mapper sezione1Mapper,
                        ISezione21Repository sezione21Repository,
                        Sezione21Mapper sezione21Mapper,
                        ISezione22Repository sezione22Repository,
                        Sezione22Mapper sezione22Mapper,
                        ISezione23Repository sezione23Repository,
                        Sezione23Mapper sezione23Mapper,
                        ISezione31Repository sezione31Repository,
                        Sezione31Mapper sezione31Mapper,
                        ISezione32Repository sezione32Repository,
                        Sezione32Mapper sezione32Mapper,
                        ISezione331Repository sezione331Repository,
                        Sezione331Mapper sezione331Mapper,
                        ISezione332Repository sezione332Repository,
                        Sezione332Mapper sezione332Mapper,
                        ISezione4Repository sezione4Repository,
                        Sezione4Mapper sezione4Mapper)
    {
        SezioneUtils.sezione1Repository = sezione1Repository;
        SezioneUtils.sezione1Mapper = sezione1Mapper;
        SezioneUtils.sezione21Repository = sezione21Repository;
        SezioneUtils.sezione21Mapper = sezione21Mapper;
        SezioneUtils.sezione22Repository = sezione22Repository;
        SezioneUtils.sezione22Mapper = sezione22Mapper;
        SezioneUtils.sezione23Repository = sezione23Repository;
        SezioneUtils.sezione23Mapper = sezione23Mapper;
        SezioneUtils.sezione31Repository = sezione31Repository;
        SezioneUtils.sezione31Mapper = sezione31Mapper;
        SezioneUtils.sezione32Repository = sezione32Repository;
        SezioneUtils.sezione32Mapper = sezione32Mapper;
        SezioneUtils.sezione331Repository = sezione331Repository;
        SezioneUtils.sezione331Mapper = sezione331Mapper;
        SezioneUtils.sezione332Repository = sezione332Repository;
        SezioneUtils.sezione332Mapper = sezione332Mapper;
        SezioneUtils.sezione4Repository = sezione4Repository;
        SezioneUtils.sezione4Mapper = sezione4Mapper;
    }

    public static SezioneBaseDTO getSezione(Sezione sezione, Long idPiao)
    {
        switch (sezione) {
            case SEZIONE_1 ->
            {
                return sezione1Mapper.toDto(sezione1Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext());
            }
            case SEZIONE_21 ->
            {
                return sezione21Mapper.toDto(sezione21Repository.findByPiaoId(idPiao).orElseThrow(), new CycleAvoidingMappingContext());
            }
            case SEZIONE_22 ->
            {
                return sezione22Mapper.toDto(sezione22Repository.findByPiaoId(idPiao).orElseThrow(), new CycleAvoidingMappingContext());
            }
            case SEZIONE_23 ->
            {
                return sezione23Mapper.toDto(sezione23Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext());
            }
            case SEZIONE_31 ->
            {
                return sezione31Mapper.toDto(sezione31Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext());
            }
            case SEZIONE_32 ->
            {
                return sezione32Mapper.toDto(sezione32Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext());
            }
            case SEZIONE_331 ->
            {
                return sezione331Mapper.toDto(sezione331Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext());
            }
            case SEZIONE_332 ->
            {
                return sezione332Mapper.toDto(sezione332Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext());
            }
            case SEZIONE_4 ->
            {
                return sezione4Mapper.toDto(sezione4Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext());
            }
            default -> { return null; }
        }
    }

    public static Long getIdSezione(Sezione sezione, Long idPiao)
    {
        switch (sezione) {
            case SEZIONE_1 ->
            {
                return sezione1Mapper.toDto(sezione1Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext()).getId();
            }
            case SEZIONE_21 ->
            {
                return sezione21Mapper.toDto(sezione21Repository.findByPiaoId(idPiao).orElseThrow(), new CycleAvoidingMappingContext()).getId();
            }
            case SEZIONE_22 ->
            {
                return sezione22Mapper.toDto(sezione22Repository.findByPiaoId(idPiao).orElseThrow(), new CycleAvoidingMappingContext()).getId();
            }
            case SEZIONE_23 ->
            {
                return sezione23Mapper.toDto(sezione23Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext()).getId();
            }
            case SEZIONE_31 ->
            {
                return sezione31Mapper.toDto(sezione31Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext()).getId();
            }
            case SEZIONE_32 ->
            {
                return sezione32Mapper.toDto(sezione32Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext()).getId();
            }
            case SEZIONE_331 ->
            {
                return sezione331Mapper.toDto(sezione331Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext()).getId();
            }
            case SEZIONE_332 ->
            {
                return sezione332Mapper.toDto(sezione332Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext()).getId();
            }
            case SEZIONE_4 ->
            {
                return sezione4Mapper.toDto(sezione4Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext()).getId();
            }
            default -> { return null; }
        }
    }

    public static Map<Long, Sezione> getIdSezione(CodTipologiaIndicatoreEnum indicatore, Long idPiao)
    {
        switch (indicatore) {
            case OVP ->
            {
                return Map.of(sezione21Mapper.toDto(sezione21Repository.findByPiaoId(idPiao).orElseThrow(), new CycleAvoidingMappingContext()).getId(), Sezione.SEZIONE_21);
            }
            case PERFORMANCE,
                 ACCESSI_DIGITALE,
                 ACCESSI_FISICI,
                 SEMPLIFICAZIONE,
                 PARI_OPPORTUNITA,
                 PERFORMANCE_ORGANIZZATIVA,
                 PERFORMANCE_INDIVIDUALE ->
            {
                return Map.of(sezione22Mapper.toDto(sezione22Repository.findByPiaoId(idPiao).orElseThrow(), new CycleAvoidingMappingContext()).getId(), Sezione.SEZIONE_22);
            }
            case OBIETTIVO_GENERALE,
                 MISURA_GENERALE,
                 OBIETTIVO_PREVENZIONE,
                 MISURA_PREVENZIONE ->
            {
                return Map.of(sezione23Mapper.toDto(sezione23Repository.findByIdPiao(idPiao), new CycleAvoidingMappingContext()).getId(), Sezione.SEZIONE_23);
            }
            default -> { return null; }
        }
    }

    /**
     * Aggiorna l'idStato sulla sezione corrispondente in base al codTipologiaFK.
     * idEntitaFK rappresenta l'id della sezione.
     */
    public static void aggiornaIdStatoSezione(String codTipologiaFK, Long idSezione, Long idStato) {
        Sezione sezione = Sezione.valueOf(codTipologiaFK);

        switch (sezione) {
            case SEZIONE_1 -> {
                sezione1Repository.findById(idSezione).ifPresent(s -> {
                    s.setIdStato(idStato);
                    sezione1Repository.save(s);
                });
            }
            case SEZIONE_21 -> {
                sezione21Repository.findById(idSezione).ifPresent(s -> {
                    s.setIdStato(idStato);
                    sezione21Repository.save(s);
                });
            }
            case SEZIONE_22 -> {
                sezione22Repository.findById(idSezione).ifPresent(s -> {
                    s.setIdStato(idStato);
                    sezione22Repository.save(s);
                });
            }
            case SEZIONE_23 -> {
                sezione23Repository.findById(idSezione).ifPresent(s -> {
                    s.setIdStato(idStato);
                    sezione23Repository.save(s);
                });
            }
            case SEZIONE_31 -> {
                sezione31Repository.findById(idSezione).ifPresent(s -> {
                    s.setIdStato(idStato);
                    sezione31Repository.save(s);
                });
            }
            case SEZIONE_32 -> {
                sezione32Repository.findById(idSezione).ifPresent(s -> {
                    s.setIdStato(idStato);
                    sezione32Repository.save(s);
                });
            }
            case SEZIONE_331 -> {
                sezione331Repository.findById(idSezione).ifPresent(s -> {
                    s.setIdStato(idStato);
                    sezione331Repository.save(s);
                });
            }
            case SEZIONE_332 -> {
                sezione332Repository.findById(idSezione).ifPresent(s -> {
                    s.setIdStato(idStato);
                    sezione332Repository.save(s);
                });
            }
            case SEZIONE_4 -> {
                sezione4Repository.findById(idSezione).ifPresent(s -> {
                    s.setIdStato(idStato);
                    sezione4Repository.save(s);
                });
            }
            default -> { }
        }
    }
}
