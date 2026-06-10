package it.ey.piao.api.service.impl;

import it.ey.dto.AvvisoDTO;
import it.ey.entity.Avviso;
import it.ey.piao.api.mapper.AvvisoMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IAvvisoRepository;
import it.ey.piao.api.service.IAvvisoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AvvisoServiceImpl implements IAvvisoService {

    private final IAvvisoRepository avvisoRepository;
    private final AvvisoMapper avvisoMapper;
    private static final Logger log = LoggerFactory.getLogger(AvvisoServiceImpl.class);

    public AvvisoServiceImpl(IAvvisoRepository avvisoRepository, AvvisoMapper avvisoMapper) {
        this.avvisoRepository = avvisoRepository;
        this.avvisoMapper = avvisoMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<AvvisoDTO> getAll() {
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            // findAll su tutti gli avvisi del DB
            List<Avviso> avvisi = avvisoRepository.findAll();

            // pulizia degli avvisi: quelli che hanno più di un giorno di distacco tra la sua creazione e la data odierna vengono eliminati fisicamente dal DB
            deleteNotTodayAvvisi(avvisi.stream().filter(a -> a.getTipologiaContenuto() == null).toList());

            // ritorno i dati filtrati e puliti
            return avvisoMapper.toDtoList(avvisi.stream().filter(a -> a.getTipologiaContenuto() != null).toList(), context);
        } catch (Exception e) {
            log.error("Errore durante il recupero degli avvisi: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero degli avvisi", e);
        }
    }

    @Override
    public AvvisoDTO getById(Long id) {
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Avviso avviso = avvisoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avviso non trovato con id: " + id));
            return avvisoMapper.toDto(avviso, context);
        } catch (IllegalArgumentException e) {
            log.warn("Avviso non trovato: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Errore durante il recupero dell'avviso con id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dell'avviso", e);
        }
    }

    @Override
    public AvvisoDTO create(AvvisoDTO avvisoDTO) {
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Avviso avviso = avvisoMapper.toEntity(avvisoDTO, context);
            Avviso saved = avvisoRepository.save(avviso);
            log.info("Avviso creato con successo: id={}", saved.getId());
            return avvisoMapper.toDto(saved, context);
        } catch (Exception e) {
            log.error("Errore durante la creazione dell'avviso: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante la creazione dell'avviso", e);
        }
    }

    @Override
    public AvvisoDTO update(Long id, AvvisoDTO avvisoDTO) {
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Avviso existing = avvisoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avviso non trovato con id: " + id));

            Avviso updated = avvisoMapper.toEntity(avvisoDTO, context);
            updated.setId(existing.getId());
            Avviso saved = avvisoRepository.save(updated);
            log.info("Avviso aggiornato con successo: id={}", saved.getId());
            return avvisoMapper.toDto(saved, context);
        } catch (IllegalArgumentException e) {
            log.warn("Avviso non trovato per aggiornamento: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Errore durante l'aggiornamento dell'avviso con id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante l'aggiornamento dell'avviso", e);
        }
    }

    @Override
    public void delete(Long id) {
        try {
            int updated = avvisoRepository.softDeleteById(id, LocalDateTime.now());
            if (updated == 0) {
                throw new IllegalArgumentException("Avviso non trovato con id: " + id);
            }
            log.info("Avviso eliminato logicamente con successo: id={}", id);
        } catch (IllegalArgumentException e) {
            log.warn("Avviso non trovato per eliminazione: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Errore durante l'eliminazione dell'avviso con id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante l'eliminazione dell'avviso", e);
        }
    }

    private void deleteNotTodayAvvisi(List<Avviso> avvisi)
    {
        try {
            LocalDate now = LocalDate.now();
            avvisi.stream()
                .filter(a -> a.getCreatedTs() != null
                    && a.getCreatedTs().plusDays(1).isBefore(now))
                .forEach(a -> {
                    // delete FISICA per pulizia del DB
                    avvisoRepository.deleteById(a.getId());
                    log.info("Avviso eliminato fisicamente con successo: id={}", a.getId());
                });
        } catch (Exception e) {
            log.error("Errore durante l'eliminazione degli avvisi scaduti: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante l'eliminazione degli avvisi scaduti", e);
        }
    }


}
