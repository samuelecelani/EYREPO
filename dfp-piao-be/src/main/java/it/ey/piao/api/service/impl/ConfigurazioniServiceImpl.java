package it.ey.piao.api.service.impl;

import it.ey.dto.ConfigurazioniDTO;
import it.ey.entity.Configurazioni;
import it.ey.piao.api.mapper.ConfigurazioniMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IConfigurazioniRepository;
import it.ey.piao.api.service.IConfigurazioniService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigurazioniServiceImpl implements IConfigurazioniService
{
    private static final String DATA_SCADENZA_PIAO = "DATA_SCADENZA_PIAO";
    private static final String DATA_COMPILAZIONE_PIAO = "DATA_COMPILAZIONE_PIAO";

    private final IConfigurazioniRepository configurazioniRepository;
    private final ConfigurazioniMapper configurazioniMapper;

    private static final Logger log = LoggerFactory.getLogger(ConfigurazioniServiceImpl.class);

    public ConfigurazioniServiceImpl(
        IConfigurazioniRepository configurazioniRepository,
        ConfigurazioniMapper configurazioniMapper)
    {
        this.configurazioniRepository = configurazioniRepository;
        this.configurazioniMapper = configurazioniMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigurazioniDTO getConfigurazioneByCodice(String codice)
    {
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            return configurazioniRepository.findByCodice(codice)
                .map(entity -> configurazioniMapper.toDto(entity, context))
                .orElse(null);
        } catch (Exception e) {
            log.error("Errore durante il recupero della Configurazione con codice={}: {}",
                codice, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della Configurazione con codice=" + codice, e);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<ConfigurazioniDTO> getAllConfigurazioni()
    {
        List<ConfigurazioniDTO> response;
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try{
            response = configurazioniMapper.toDtoList(configurazioniRepository.findAll(), context);
        } catch (Exception e) {
            log.error("Errore durante il recupero delle Configurazioni: {}",
                e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle Configurazioni ", e);
        }
        return response;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void setValoreFromCodice(String codice, String valore)
    {
        if (valore == null || valore.isBlank()) {
            throw new IllegalArgumentException("Il valore non può essere nullo");
        }

        if (codice == null || codice.isBlank()) {
            throw new IllegalArgumentException("Il codice non può essere nullo");
        }

        try{
            configurazioniRepository.setValoreFromCodice(codice, valore);
        } catch (Exception e) {
            log.error("Errore durante il set di valore: {} con codice: {}, errore: {}",
                valore, codice, e.getMessage(), e);
            throw new RuntimeException("Errore durante il set del valore con il codice ", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getValoreFromCodice(String codice)
    {
        if (codice == null || codice.isBlank()) {
            throw new IllegalArgumentException("Il codice non può essere nullo");
        }

        String response = "";

        try{
            response = configurazioniRepository.getValoreFromCodice(codice);
        } catch (Exception e) {
            log.error("Errore durante il get di valore con codice: {}, errore: {}",
                codice, e.getMessage(), e);
            throw new RuntimeException("Errore durante il get del valore con il codice ", e);
        }
        return response;
    }

    // Metodo custom attualmente bozzato , da reworkare e fixare .
    private Object getTypeOfPiaoPDF(Configurazioni configurazioni)
    {
        return switch (configurazioni.getTypeDato().toLowerCase())
        {
            case "string" -> String.class;
            case "number" -> Integer.class;
            case "date" -> LocalDate.class;
            default -> Object.class;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getPiaoDates()
    {
        Map<String, String> result = new HashMap<>();
        result.put(DATA_COMPILAZIONE_PIAO, readValore(DATA_COMPILAZIONE_PIAO));
        result.put(DATA_SCADENZA_PIAO, readValore(DATA_SCADENZA_PIAO));
        return result;
    }

    /**
     * Legge il valore della configurazione associata al codice indicato. Ritorna {@code null}
     * se la configurazione non esiste.
     */
    private String readValore(String codice)
    {
        ConfigurazioniDTO dto = getConfigurazioneByCodice(codice);
        return (dto != null) ? dto.getValore() : null;
    }

}
