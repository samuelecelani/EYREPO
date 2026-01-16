package it.ey.piao.api.service.impl;


import it.ey.dto.PiaoDTO;
import it.ey.entity.Piao;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.enums.Tipologia;
import it.ey.enums.TipologiaOnline;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.PiaoRepository;
import it.ey.piao.api.service.IPiaoService;
import it.ey.piao.api.service.ISezione1Service;
import it.ey.piao.api.service.ISezione21Service;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.utils.StoricoStatoSezioneUtils;
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

    private final GenericMapper genericMapper;
    private final PiaoRepository piaoRepository;
    private final ISezione1Service sezione1Service;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    private final ISezione21Service sezione21Service;
    private final ApplicationEventPublisher eventPublisher;

    public PiaoServiceImpl(GenericMapper genericMapper, PiaoRepository piaoRepository, Sezione1ServiceImpl sezione1Service, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, ISezione21Service sezione21Service, ApplicationEventPublisher eventPublisher) {
        this.genericMapper = genericMapper;
        this.piaoRepository = piaoRepository;
        this.sezione1Service = sezione1Service;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.sezione21Service = sezione21Service;
        this.eventPublisher = eventPublisher;
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
                PiaoDTO response = genericMapper.map(existing, PiaoDTO.class);
                response.setSezione1(sezione1Service.getOrCreateSezione1(response));
                response.setSezione21(sezione21Service.getOrCreateSezione21(response));
                return response;
                }

                else{
                    boolean nextYear = !today.isBefore(targetDate);

                    LocalDate endNewYear = LocalDate.of(today.getYear() + 1, Month.DECEMBER, 1);;
                    //Verificare che non esista un piao dell'anno successivo. Se esiste lo ritorno
                    if(nextYear ){
                       Piao piaoNextYear =  piaoRepository.findTopByCodPAFKAndCreatedTsBetweenOrderByVersioneDesc(
                            piao.getCodPAFK(), targetDate, endNewYear);
                        if (piaoNextYear == null) {
                            return createPiao(piao, true);
                        }
                        else   if (!piaoNextYear.getIdStato().equals(StatoEnum.PUBBLICATO.getId())) {
                            PiaoDTO response = genericMapper.map(piaoNextYear, PiaoDTO.class);
                            response.setSezione1(sezione1Service.getOrCreateSezione1(response));
                            return response;
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
        return  piaoRepository.findByCodPAFK(codPAF)
            .stream()
            .map(res ->{
                PiaoDTO response = genericMapper.map(res, PiaoDTO.class);
                response.getSezione1().setStatoSezione(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(res.getSezione1().getId(), Sezione.SEZIONE_1.name())));
            return  response;
            })
            .toList();
    }

    private PiaoDTO createPiao(PiaoDTO piao, boolean nextYear) {
        PiaoDTO response = null;
        try {
            Piao newPiao = genericMapper.map(piao, Piao.class);
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
             response = genericMapper.map(piaoRepository.save(newPiao), PiaoDTO.class);
            //Recupero o creo Sezione1
            response.setSezione1(sezione1Service.getOrCreateSezione1(response));
            response.setSezione21(sezione21Service.getOrCreateSezione21(response));

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception ex) {
            eventPublisher.publishEvent(new TransactionFailureEvent<>(genericMapper.map(response,Piao.class),ex));
            log.error("Errore in createPiao: {}", ex.getMessage(), ex);
            throw new RuntimeException("Errore durante la creazione del PIAO", ex);
        }
    }
}
