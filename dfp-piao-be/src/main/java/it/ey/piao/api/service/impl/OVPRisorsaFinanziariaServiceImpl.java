package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.OVPDTO;
import it.ey.dto.OVPRisorsaFinanziariaDTO;
import it.ey.entity.OVP;
import it.ey.entity.OVPRisorsaFinanziaria;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.enums.TypeErrorEnum;
import it.ey.piao.api.exception.BusinessException;
import it.ey.piao.api.mapper.OVPRisorsaFinanziariaMapper;
import it.ey.piao.api.repository.IOVPRisorsaFinanziariaRepository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.OVPRepository;
import it.ey.piao.api.service.IOVPRisorsaFinanziariaService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class OVPRisorsaFinanziariaServiceImpl implements IOVPRisorsaFinanziariaService {

    private final IOVPRisorsaFinanziariaRepository iovpRisorsaFinanziariaRepository;
    private final OVPRepository ovpRepository;
    private final OVPRisorsaFinanziariaMapper ovpRisorsaFinanziariaMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;




    private static final Logger log = LoggerFactory.getLogger(OVPRisorsaFinanziariaServiceImpl.class);

    public OVPRisorsaFinanziariaServiceImpl(IOVPRisorsaFinanziariaRepository iovpRisorsaFinanziariaRepository, OVPRepository ovpRepository, OVPRisorsaFinanziariaMapper ovpRisorsaFinanziariaMapper, ApplicationEventPublisher eventPublisher, StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.iovpRisorsaFinanziariaRepository = iovpRisorsaFinanziariaRepository;
        this.ovpRepository = ovpRepository;
        this.ovpRisorsaFinanziariaMapper = ovpRisorsaFinanziariaMapper;
        this.eventPublisher = eventPublisher;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    public void saveOrUpdate(List<OVPRisorsaFinanziariaDTO> request, Long idOVP) {
        try {
            //recupero l'ovp
            OVP ovp = ovpRepository.getReferenceById(idOVP);
            // Mappo i DTO in entità
            List<OVPRisorsaFinanziaria> entities = request.stream()
                .map(dto -> {
                    OVPRisorsaFinanziaria entity = ovpRisorsaFinanziariaMapper.toEntity(dto);

                    // Carico l'OVP
                    entity.setOvp(ovp);

                    return entity;
                })
                .toList();

            // Salvo le entità
            iovpRisorsaFinanziariaRepository.saveAll(entities);


        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate OVPRisorsaFinanziariaServiceImpl: {}", e.getMessage(), e);

            throw new RuntimeException("Errore durante il salvataggio o aggiornamento dell'OVPRisorsaFinanziariaServiceImpl", e);
        }
    }

    @Transactional
    @Override
    public void deleteById(Long id,
                           String campiModificati,
                           Long idPiao,
                           String testoSezione,
                           String updatedByNameSurname,
                           String updatedByRole,
                           String statoSezione) {

        if (id == null) {
            throw new IllegalArgumentException("id è obbligatorio");
        }

        try {

            OVPRisorsaFinanziaria risorsa = iovpRisorsaFinanziariaRepository.findById(id)
                .orElseThrow(() ->
                    new BusinessException(
                        BusinessException.INTERNAL_ERROR,
                        "OVPRisorsaFinanziaria non trovata con id: " + id,
                        TypeErrorEnum.ERROR
                    )
                );

            // Ricavo idSezione21 tramite OVP -> Sezione21
            Long idSezione21 = (risorsa.getOvp() != null && risorsa.getOvp().getSezione21() != null)
                ? risorsa.getOvp().getSezione21().getId()
                : null;



            // Evento BeforeUpdate
            eventPublisher.publishEvent(
                new BeforeUpdateEvent<>(OVPRisorsaFinanziaria.class, risorsa)
            );

            // Soft delete puntuale
            LocalDateTime deactivationTime = LocalDateTime.now();
            iovpRisorsaFinanziariaRepository.softDeleteById(id, deactivationTime);

            // Storico modifica (se presenti parametri)
            if (campiModificati != null && !campiModificati.isBlank()
                && idPiao != null && idSezione21 != null) {

                BaseDTO storico = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();

                storicoModificaHelper.salvaStoricoSePresente(
                    storico,
                    idSezione21,
                    idPiao,
                    Sezione.SEZIONE_21
                );
            }

            // Storico stato sezione (se cambia)
            if (statoSezione != null && !statoSezione.isBlank() && idSezione21 != null) {

                String statoDb = StoricoStatoSezioneUtils.getStato(
                    storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(
                        idSezione21,
                        Sezione.SEZIONE_21.name()
                    )
                );

                String statoReq = StatoEnum.fromDescrizione(statoSezione).name();

                if (!statoReq.equals(statoDb)) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder()
                            .statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build()
                            )
                            .idEntitaFK(idSezione21)
                            .codTipologiaFK(Sezione.SEZIONE_21.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build()
                    );
                }
            }

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(risorsa));
            log.info("OVPRisorsaFinanziaria con id={} cancellata (soft delete) con successo", id);

        } catch (BusinessException ex) {
            throw ex;

        } catch (Exception ex) {
            log.error("Errore durante la cancellazione OVPRisorsaFinanziaria ID={}: {}", id, ex.getMessage(), ex);

            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante la cancellazione della Risorsa Finanziaria",
                TypeErrorEnum.ERROR
            );
        }
    }



}
