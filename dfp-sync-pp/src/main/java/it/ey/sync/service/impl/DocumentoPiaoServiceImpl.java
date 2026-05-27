package it.ey.sync.service.impl;

import it.ey.sync.dto.DocumentoPiaoDTO;
import it.ey.sync.dto.GenericResponseDTO;
import it.ey.sync.enums.WebServiceType;
import it.ey.sync.httpClient.WebClientService;
import it.ey.sync.mapper.DocumentoPiaoMapper;
import it.ey.sync.repository.AllegatoPiaoRepository;
import it.ey.sync.repository.DocumentoPiaoRepository;
import it.ey.sync.service.DocumentoPiaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentoPiaoServiceImpl implements DocumentoPiaoService {

    private final DocumentoPiaoRepository repository;
    private final DocumentoPiaoMapper mapper;
    private final WebClientService webClientService;
    private final AllegatoPiaoRepository allegatoPiaoRepository;

    @Override
    public List<DocumentoPiaoDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public Optional<DocumentoPiaoDTO> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public DocumentoPiaoDTO save(DocumentoPiaoDTO dto) {
        log.debug("Salvataggio documento PIAO ricevuto: {}", dto);
        if (dto.getId() == null || dto.getId().isBlank()) {
            log.warn("Documento PIAO scartato: id è null o vuoto. DTO: {}", dto);
            return dto;
        }
        return repository.findById(dto.getId())
                .map(existing -> {
                    // Aggiorna i campi del documento
                    existing.setCodicePiao(dto.getCodicePiao());
                    existing.setFullName(dto.getFullName());
                    existing.setVersione(dto.getVersione());
                    existing.setDataApprovazione(dto.getDataApprovazione());
                    existing.setDataPubblicazione(dto.getDataPubblicazione());
                    existing.setLinkEsterno(dto.getLinkEsterno());

                    // Aggiungi solo gli allegati nuovi con una singola query
                    if (dto.getAllegati() != null && !dto.getAllegati().isEmpty()) {
                        Set<String> nomiEsistenti = new HashSet<>(
                                allegatoPiaoRepository.findNomeFilesByDocumentoPiaoId(dto.getId()));

                        if (existing.getAllegati() == null) {
                            existing.setAllegati(new ArrayList<>());
                        }

                        dto.getAllegati().stream()
                                .filter(allegato -> !nomiEsistenti.contains(allegato.getNomeFile()))
                                .forEach(allegato -> {
                                    var entity = new it.ey.sync.entity.AllegatoPiao();
                                    entity.setNomeFile(allegato.getNomeFile());
                                    entity.setDocumentoPiao(existing);
                                    entity.setS3_key(allegato.getS3_key());
                                    existing.getAllegati().add(entity);
                                    log.debug("Aggiunto nuovo allegato '{}' al documento {}", allegato.getNomeFile(), dto.getId());
                                });
                    }

                    return mapper.toDto(repository.save(existing));
                })
                .orElseGet(() -> {
                    log.debug("Creato nuovo documento PIAO: {}", dto.getId());
                    return mapper.toDto(repository.save(mapper.toEntity(dto)));
                });
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public Mono<List<DocumentoPiaoDTO>> getPiaoPubblicati(Long idPiao,String denominazione, String codePa) {

        StringBuilder url = new StringBuilder("/api/piao/bff/external/pp/piao");
        List<String> params = new ArrayList<>();
        if (idPiao != null) {
            params.add("idPiao=" + idPiao);
        }
        if (StringUtils.isNotBlank(denominazione)) {
            params.add("denominazione=" + denominazione);
        }
        if (StringUtils.isNotBlank(codePa)) {
            params.add("codePa=" + codePa);
        }
        if (!params.isEmpty()) {
            url.append("?").append(String.join("&", params));
        }

        return webClientService.get(
                url.toString(),
                WebServiceType.BFF,
                new HttpHeaders(),
                new ParameterizedTypeReference<GenericResponseDTO<List<DocumentoPiaoDTO>>>() {}
        ).map(GenericResponseDTO::getData);
    }

    @Override
    @Transactional
    public void syncAll(List<DocumentoPiaoDTO> list) {
        list.forEach(dto -> {
            save(dto);

            // Elimina gli allegati non più presenti per questo documento
            if (dto.getId() != null && !dto.getId().isBlank()) {
                if (dto.getAllegati() == null || dto.getAllegati().isEmpty()) {
                    allegatoPiaoRepository.deleteByDocumentoPiaoId(dto.getId());
                    log.debug("Eliminati tutti gli allegati del documento {}", dto.getId());
                } else {
                    List<String> nomiFileRicevuti = dto.getAllegati().stream()
                            .map(a -> a.getNomeFile())
                            .filter(n -> n != null && !n.isBlank())
                            .toList();
                    allegatoPiaoRepository.deleteByDocumentoPiaoIdAndNomeFileNotIn(dto.getId(), nomiFileRicevuti);
                    log.debug("Eliminati allegati non più presenti per documento {}", dto.getId());
                }
            }
        });
    }
}

