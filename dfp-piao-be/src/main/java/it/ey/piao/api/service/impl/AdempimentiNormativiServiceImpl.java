package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.AdempimentiNormativiMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.repository.mongo.IAzioneRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.IAdempimentiNormativiService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import it.ey.utils.StoricoStatoSezioneUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdempimentiNormativiServiceImpl implements IAdempimentiNormativiService {

    private final AdempimentiNormativiMapper adempimentiNormativiMapper;
    private final IAdempimentiNormativiRepository adempimentiNormativiRepository;
    private final ISezione23Repository sezione23Repository;
    private static final Logger log = LoggerFactory.getLogger(AdempimentiNormativiServiceImpl.class);
    private final StoricoModificaHelper storicoModificaHelper;
    private final  IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    public AdempimentiNormativiServiceImpl(AdempimentiNormativiMapper adempimentiNormativiMapper, IAdempimentiNormativiRepository adempimentiNormativiRepository, ISezione23Repository sezione23Repository, StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.adempimentiNormativiMapper = adempimentiNormativiMapper;
        this.adempimentiNormativiRepository = adempimentiNormativiRepository;
        this.sezione23Repository = sezione23Repository;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    public void saveOrUpdate(AdempimentiNormativiDTO adempimentoNormativoDTO) {
        try {
            AdempimentiNormativi entity = adempimentiNormativiMapper.toEntity(adempimentoNormativoDTO,new CycleAvoidingMappingContext());
            entity.setSezione23(sezione23Repository.getReferenceById(adempimentoNormativoDTO.getIdSezione23()));


            AdempimentiNormativi saved =   adempimentiNormativiRepository.save(entity);
            if (adempimentoNormativoDTO.getCampiModificati() != null && !adempimentoNormativoDTO.getCampiModificati().isBlank() && adempimentoNormativoDTO.getIdPiao() != null ) {
                storicoModificaHelper.salvaStoricoSePresente(adempimentoNormativoDTO, saved.getSezione23().getId(), adempimentoNormativoDTO.getIdPiao(), Sezione.SEZIONE_23);
            }

            if (adempimentoNormativoDTO.getStatoSezione() != null && !adempimentoNormativoDTO.getStatoSezione().isBlank() && !StatoEnum.fromDescrizione(adempimentoNormativoDTO.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(saved.getSezione23().getId(),Sezione.SEZIONE_23.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(adempimentoNormativoDTO.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(adempimentoNormativoDTO.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(saved.getSezione23().getId())
                        .codTipologiaFK(Sezione.SEZIONE_23.name())
                        .testo(StatoEnum.fromDescrizione(adempimentoNormativoDTO.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(adempimentoNormativoDTO.getUpdatedByNameSurname())
                        .createdByRole(adempimentoNormativoDTO.getUpdatedByRole())
                        .build());
            }
        } catch (Exception e) {
            log.error("Errore durante Save o update  per fase id={}: {}", adempimentoNormativoDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il save o update dell'Adempimento Normativo", e);
        }
    }

    @Override
    public void saveOrUpdateAll(java.util.List<AdempimentiNormativiDTO> adempimentiNormativi) {
        if (adempimentiNormativi == null || adempimentiNormativi.isEmpty()) {
            log.debug("Lista adempimenti normativi vuota o null, skip salvataggio batch");
            return;
        }

        log.info("Salvataggio batch di {} adempimenti normativi", adempimentiNormativi.size());

        try {
            List<AdempimentiNormativi> entitiesToSave = new ArrayList<>();

            for (AdempimentiNormativiDTO dto : adempimentiNormativi) {
                if (dto == null) {
                    log.warn("MisuraPrevenzioneDTO nullo nella lista, skip");
                    continue;
                }
                AdempimentiNormativi entity = adempimentiNormativiMapper.toEntity(dto,new CycleAvoidingMappingContext());

                // Imposto la relazione con Sezione23
                entity.setSezione23(sezione23Repository.getReferenceById(dto.getIdSezione23()));

                entitiesToSave.add(entity);

            }
            adempimentiNormativiRepository.saveAll(entitiesToSave);
            log.info("Batch salvataggio completato: {} adempimenti normativi salvati", adempimentiNormativi.size());

        } catch (Exception e) {

            log.error("Errore durante il salvataggio batch degli adempimenti normativi: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio batch degli adempimenti normativi", e);
        }
    }
    @Override
    public void deleteAdempimentoNormativo(Long id, String campiModificati, Long idPiao, String testoSezione,String updatedByNameSurname, String updatedByRole, String statoSezione)
    {
        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<AdempimentiNormativi> existing = adempimentiNormativiRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un adempimento normativo non esistente con id={}", id);
                throw new RuntimeException("Adempimento Normativo non trovato con id: " + id);
            }

            // Cancellazione da PostgreSQL
            adempimentiNormativiRepository.softDeleteById(id, LocalDateTime.now());

            // Salva storico modifica dopo la cancellazione
            if (campiModificati != null && !campiModificati.isBlank() && idPiao != null) {
                BaseDTO dto = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();

                storicoModificaHelper.salvaStoricoSePresente(dto, existing.get().getSezione23().getId(), idPiao, Sezione.SEZIONE_23);
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank()) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.get().getSezione23().getId(), Sezione.SEZIONE_23.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(existing.get().getSezione23().getId())
                            .codTipologiaFK(Sezione.SEZIONE_23.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

            log.info("Adempimento Normativo con id={} cancellato con successo", id);
        } catch (Exception e) {
            log.error("Errore durante la cancellazione dell'adempimento normativo id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione dell'Adempimento Normativo", e);
            }
    }
}
