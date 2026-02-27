package it.ey.piao.api.service.impl;


import it.ey.dto.PiaoDTO;
import it.ey.entity.Piao;
import it.ey.enums.StatoEnum;
import it.ey.enums.Tipologia;
import it.ey.enums.TipologiaOnline;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.PiaoRepository;
import it.ey.piao.api.service.*;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;

@Service
@Transactional
public class PiaoServiceImpl implements IPiaoService {

    private static final Logger log = LoggerFactory.getLogger(PiaoServiceImpl.class);

    private final PiaoMapper piaoMapper;
    private final PiaoRepository piaoRepository;
    private final ISezione1Service sezione1Service;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    private final ISezione21Service sezione21Service;
    private final ISezione22Service sezione22Service;
    private final ISezione23Service sezione23Service;
    private final ISezione31Service sezione31Service;
    private final ISezione4Service sezione4Service;
    private final ApplicationEventPublisher eventPublisher;
    private final ISezione331Service sezione331Service;

    public PiaoServiceImpl(PiaoMapper piaoMapper, PiaoRepository piaoRepository, Sezione1ServiceImpl sezione1Service, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, ISezione21Service sezione21Service, ISezione22Service sezione22Service, ISezione23Service sezione23Service, ISezione4Service sezione4Service, ApplicationEventPublisher eventPublisher, ISezione331Service sezione331Service, ISezione31Service sezione31Service) {
        this.piaoMapper = piaoMapper;
        this.piaoRepository = piaoRepository;
        this.sezione1Service = sezione1Service;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.sezione21Service = sezione21Service;
        this.sezione22Service = sezione22Service;
        this.sezione23Service = sezione23Service;
        this.sezione31Service = sezione31Service;
        this.sezione4Service = sezione4Service;
        this.eventPublisher = eventPublisher;
        this.sezione331Service = sezione331Service;
    }

    @Override
    public PiaoDTO getOrCreatePiao(PiaoDTO piao) {
        try {
            // Validazione input
            if (piao == null || piao.getCodPAFK() == null) {
                log.error("CodPAFK mancante nel PiaoDTO");
                throw new IllegalArgumentException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
            }

            LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
            LocalDate endOfYear = LocalDate.now().withMonth(12).withDayOfMonth(31);

            // Data attuale
            LocalDate today = LocalDate.now();

            // Data prestabilita (01 dicembre dello stesso anno)
            LocalDate targetDate = LocalDate.of(today.getYear(), Month.DECEMBER, 1);


            log.info("Ricerca PIAO per PA={} tra {} e {}", piao.getCodPAFK(), startOfYear, endOfYear);

            Piao existing = piaoRepository.findTopByCodPAFKAndCreatedTsBetweenOrderByVersioneDesc(
                piao.getCodPAFK(), startOfYear, endOfYear
            );


            if (existing != null){
                if (!existing.getIdStato().equals(StatoEnum.PUBBLICATO.getId())) {
                log.info("PIAO trovato: ID={} versione={}", existing.getId(), existing.getVersione());
                    return piaoMapper.toDto(existing, new CycleAvoidingMappingContext());
                }

                else{
                    boolean nextYear = !today.isBefore(targetDate);

                    LocalDate endNewYear = LocalDate.of(today.getYear() + 1, Month.DECEMBER, 1);
                    //Verificare che non esista un piao dell'anno successivo. Se esiste lo ritorno
                    if(nextYear ){
                       Piao piaoNextYear =  piaoRepository.findTopByCodPAFKAndCreatedTsBetweenOrderByVersioneDesc(
                            piao.getCodPAFK(), targetDate, endNewYear);
                        if (piaoNextYear == null) {
                            return createPiao(piao, true);
                        }
                        else   if (!piaoNextYear.getIdStato().equals(StatoEnum.PUBBLICATO.getId())) {
                            return piaoMapper.toDto(piaoNextYear,new CycleAvoidingMappingContext());
                        }
                        else{
                            return null; //Funzione redigi PIAO DISABILITATA, NON RITORNO ALCUN PIAO NEXTYEAR
                        }
                }
                    return null; //Funzione redigi PIAO DISABILITATA, NON RITORNO ALCUN PIAO
                }
            }
            else {
                log.info("Nessun PIAO trovato, inizializzo un  nuovo...");
                return createPiao(piao,false);
            }

        } catch (Exception ex) {
            log.error("Errore in getOrCreatePiao: {}", ex.getMessage(), ex);
            throw new RuntimeException("Errore durante la creazione o il recupero del PIAO", ex);
        }
    }

     @Override
        public boolean redigiPiaoIsAllowed(String codPAFK){
        try {
            // Validazione input
            if (!StringUtils.isNotBlank(codPAFK)) {
                log.error("CodPAFK mancante nel PiaoDTO");
                throw new IllegalArgumentException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
            }

            LocalDate today = LocalDate.now();
            LocalDate targetDate = LocalDate.of(today.getYear(), Month.DECEMBER, 1);
            LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
            boolean isNewYear = !today.isBefore(targetDate);
            LocalDate endOfYear =  isNewYear ?  LocalDate.of(today.getYear() + 1, Month.DECEMBER, 1) : LocalDate.now().withMonth(12).withDayOfMonth(31) ;



            log.info("Ricerca PIAO per PA={} tra {} e {}",codPAFK, startOfYear, endOfYear);

            Piao existing = piaoRepository.findTopByCodPAFKAndCreatedTsBetweenOrderByVersioneDesc(
                codPAFK, startOfYear, endOfYear
            );

            return existing == null ||
            !existing.getIdStato().equals(StatoEnum.PUBBLICATO.getId())||
                isNewYear  ;


        }
        catch (Exception ex) {
            log.error("Errore in getOrCreatePiao: {}", ex.getMessage(), ex);
            throw new RuntimeException("Errore ", ex);
        }
    }

    @Override
    public List<PiaoDTO> getAllPiaoByCodPAFK(String codPAF) {
        return  piaoRepository.findByCodPAFKOrderByCreatedTsDesc(codPAF)
            .stream()
            .map(p -> piaoMapper.toDto(p, new CycleAvoidingMappingContext()))
            .toList();
    }

    private PiaoDTO createPiao(PiaoDTO piao, boolean nextYear) {
        PiaoDTO response = null;
        try {
            Piao newPiao = piaoMapper.toEntity(piao);
            //TODO: Capire come recuperare i dati da un pdf caricato
            newPiao.setTipologia(Tipologia.valueOf(piao.getTipologia().toUpperCase()));
            if (!newPiao.getTipologia().equals(Tipologia.PDF)) {
                newPiao.setTipologiaOnline(TipologiaOnline.valueOf(piao.getTipologiaOnline().toUpperCase()));
            }
            newPiao.setVersione("1.0");
            log.debug("Versione impostata a 1.0");
            log.info("Salvataggio nuovo PIAO per PA={} anno={}", newPiao.getCodPAFK(), Year.now().getValue());

            if (nextYear){
                newPiao.setDenominazione("PIAO"+ " " + String.valueOf(Year.now().getValue()+1).substring(2) + "-" + String.valueOf(Year.now().getValue() + 3).substring(2));
            }
           else {
               newPiao.setDenominazione("PIAO" + " " +  String.valueOf(Year.now().getValue()).substring(2) + "-" + String.valueOf(Year.now().getValue() + 2).substring(2));
            }
            newPiao.setIdStato(StatoEnum.DA_COMPILARE.getId());
             response = piaoMapper.toDto(piaoRepository.save(newPiao),new CycleAvoidingMappingContext());
            //Recupero o creo tutte le sezioni
            populateSezioni(response);

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception ex) {
            eventPublisher.publishEvent(new TransactionFailureEvent<>(piaoMapper.toEntity(response),ex));
            log.error("Errore in createPiao: {}", ex.getMessage(), ex);
            throw new RuntimeException("Errore durante la creazione del PIAO", ex);
        }
    }

    /**
     * Metodo helper per popolare tutte le sezioni del PIAO
     * Evita duplicazione di codice e garantisce consistenza
     */
    private void populateSezioni(PiaoDTO piaoDTO) {
        sezione1Service.getOrCreateSezione1(piaoDTO);
        sezione21Service.getOrCreateSezione21(piaoDTO);
        sezione22Service.getOrCreateSezione22(piaoDTO);
        sezione23Service.getOrCreateSezione23(piaoDTO);
        sezione31Service.getOrCreateSezione31(piaoDTO);
        sezione4Service.getOrCreateSezione4(piaoDTO);
        sezione331Service.getOrCreateSezione331(piaoDTO);
    }
}
