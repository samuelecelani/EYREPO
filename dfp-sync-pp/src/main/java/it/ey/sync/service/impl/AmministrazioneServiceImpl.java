package it.ey.sync.service.impl;

import it.ey.sync.dto.AmministrazioneDTO;
import it.ey.sync.dto.GenericResponseDTO;
import it.ey.sync.enums.WebServiceType;
import it.ey.sync.httpClient.WebClientService;
import it.ey.sync.mapper.AmministrazioneMapper;
import it.ey.sync.repository.AmministrazioneRepository;
import it.ey.sync.service.AmministrazioneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmministrazioneServiceImpl implements AmministrazioneService {

    private final AmministrazioneRepository repository;
    private final AmministrazioneMapper mapper;
    private final WebClientService webClientService;

    @Override
    public List<AmministrazioneDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public Optional<AmministrazioneDTO> findByCodiceIpa(String codiceIpa) {
        return repository.findById(codiceIpa).map(mapper::toDto);
    }

    @Override
    @Transactional
    public AmministrazioneDTO save(AmministrazioneDTO dto) {
        log.debug("Salvataggio amministrazione ricevuta: {}", dto);
        if (dto.getCodiceIpa() == null || dto.getCodiceIpa().isBlank()) {
            log.warn("Amministrazione scartata: codiceIpa è null o vuoto. DTO: {}", dto);
            return dto;
        }
        return repository.findById(dto.getCodiceIpa())
                .map(existing -> {
                    existing.setDenominazione(dto.getDenominazione());
                    existing.setTipologia(dto.getTipologia());
                    log.debug("Aggiornata amministrazione: {}", dto.getCodiceIpa());
                    return mapper.toDto(repository.saveAndFlush(existing));
                })
                .orElseGet(() -> {
                    log.debug("Creata nuova amministrazione: {}", dto.getCodiceIpa());
                    return mapper.toDto(repository.saveAndFlush(mapper.toEntity(dto)));
                });
    }

    @Override
    @Transactional
    public void deleteByCodiceIpa(String codiceIpa) {
        repository.deleteById(codiceIpa);
    }

    @Override
    public Mono<List<AmministrazioneDTO>> getAmministrazioni() {
        return webClientService.get(
                "/api/piao/bff/external/pp/amministrazioni",
                WebServiceType.BFF,
                new HttpHeaders(),
                new ParameterizedTypeReference<GenericResponseDTO<List<AmministrazioneDTO>>>() {}
        ).map(GenericResponseDTO::getData);
    }

    @Override
    @Transactional
    public void syncAll(List<AmministrazioneDTO> list) {
        // Salva/aggiorna tutte le amministrazioni ricevute
        list.forEach(this::save);

        // Elimina quelle non più presenti nella lista
        List<String> codiciRicevuti = list.stream()
                .map(AmministrazioneDTO::getCodiceIpa)
                .filter(c -> c != null && !c.isBlank())
                .toList();

    }
}
