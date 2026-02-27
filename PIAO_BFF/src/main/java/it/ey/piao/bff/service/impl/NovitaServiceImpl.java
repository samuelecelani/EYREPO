package it.ey.piao.bff.service.impl;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.NovitaDTO;
import it.ey.dto.Status;
import it.ey.piao.bff.service.INovitaService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class NovitaServiceImpl implements INovitaService {

        @Override
        public Mono<GenericResponseDTO<List<NovitaDTO>>> getNovita(String modulo) {
            List<NovitaDTO> novitaList = List.of(
                new NovitaDTO(1L, "INFO", "Novità dal Dipartimento di Funzione Pubblica", "Operativo il nuovo Portale PIAO: programmazione più semplice", "È attivo il Portale PIAO in cui gli enti potranno inserire i loro Piani integrati di attività e organizzazione e trasmetterli agli organismi preposti per il controllo e la validazione."),
                new NovitaDTO(2L, "AGGIORNAMENTO", "Circolare dal Dipartimento di Funzione Pubblica", "Pubblicata la circolare n. 2/2022 con indicazioni operative sul PIAO", "L'11 ottobre è stata emanata la nota circolare n. 2/2022 del Dipartimento della Funzione pubblica che contiene indicazioni operative per la compilazione del Piano Integrato di Attività e Organizzazione."),
                new NovitaDTO(3L, "NEWS", "Altro", "PIAO: ecco i decreti sul Piano integrato di attività e organizzazione", "Decolla il PIAO, il Piano integrato di attività e organizzazione che assorbe molti dei documenti di programmazione delle pubbliche amministrazioni in un unico documento strategico."),
                new NovitaDTO(4L, "NEWS", "Altro2", "PIAO: ecco i decreti sul Piano integrato di attività e organizzazione", "Decolla il PIAO, il Piano integrato di attività e organizzazione che assorbe molti dei documenti di programmazione delle pubbliche amministrazioni in un unico documento strategico."),
                new NovitaDTO(5L, "INFO", "Trasparenza e Anticorruzione", "Integrazione PTPCT nel PIAO", "Il PTPCT confluisce nel PIAO, con sezioni dedicate a misure di prevenzione della corruzione e programmi di trasparenza."),
                new NovitaDTO(6L, "AGGIORNAMENTO", "Performance e Valutazione", "Nuovi indicatori di performance nel PIAO", "Aggiornati gli indicatori di performance per allineamento agli obiettivi strategici e al ciclo della performance."),
                new NovitaDTO(7L, "NEWS", "Organizzazione e Personale", "Reclutamento e fabbisogni: sezione dedicata nel PIAO", "La sezione ‘Organizzazione e capitale umano’ disciplina fabbisogni, reclutamento e percorsi di sviluppo."),
                new NovitaDTO(8L, "INFO", "Formazione e Competenze", "Catalogo formativo collegato al PIAO", "Previsto l’aggiornamento annuale del catalogo formativo con focus su competenze digitali e project management."),
                new NovitaDTO(9L, "AGGIORNAMENTO","Transizione Digitale", "Allineamento al Piano Triennale ICT", "Le misure PIAO devono coordinarsi con il Piano Triennale per l’informatica nella PA e con gli obiettivi di interoperabilità."),
                new NovitaDTO(10L, "NEWS", "Smart Working e Lavoro Agile", "Linee guida per il lavoro agile nel PIAO", "Definite policy e indicatori per il lavoro agile, con obiettivi di produttività e benessere organizzativo."),
                new NovitaDTO(11L, "INFO", "Accessibilità e Inclusione", "Accessibilità digitale: obblighi nel PIAO", "Inserite misure per l’accessibilità dei servizi digitali e la rimozione di barriere nei processi interni."),
                new NovitaDTO(12L, "AGGIORNAMENTO", "Gestione Rischi e Sicurezza", "Mappatura dei rischi operativi nel PIAO", "Aggiornata la mappatura dei rischi e il piano di mitigazione con indicatori di controllo periodico."),
                new NovitaDTO(13L, "NEWS", "Open Data e Trasparenza", "Piano di valorizzazione dei dati pubblici", "Introdotte azioni per pubblicazione, qualità e riuso dei dati aperti, in coerenza con le politiche nazionali."),
                new NovitaDTO(14L, "INFO", "Sostenibilità e Ambiente", "Green Public Administration nel PIAO", "Previste misure per riduzione consumi, acquisti verdi e mobilità sostenibile in ambito PA."),
                new NovitaDTO(15L, "AGGIORNAMENTO", "Acquisti e Procurement", "Procedure e controlli nel ciclo degli acquisti", "Allineate le procedure di procurement con obiettivi di trasparenza, efficienza e prevenzione del rischio."),
                new NovitaDTO(16L, "NEWS", "Comunicazione e Stakeholder", "Partecipazione e accountability nel PIAO", "Pianificate iniziative di consultazione e reporting verso stakeholder interni ed esterni per aumentare l’accountability.")
            );

            GenericResponseDTO<List<NovitaDTO>> response = new GenericResponseDTO<>();
            response.setData(novitaList);
            response.setStatus(new Status());
            response.getStatus().setSuccess(Boolean.TRUE);

            return Mono.just(response);
        }
    }

