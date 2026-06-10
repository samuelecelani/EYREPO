package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione331DTO;
import it.ey.enums.TipoTabellaSezione331;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ISezione331Service {
    Mono<GenericResponseDTO<Sezione331DTO>> getOrCreate(PiaoDTO request);

    Mono<GenericResponseDTO<Sezione331DTO>> saveOrUpdate(Sezione331DTO request);

    Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa);

    Mono<GenericResponseDTO<Sezione331DTO>> findByPiao(Long idPiao);

    Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa);

    Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> revocaValidazione(Long id, String osservazioni, String codicePa);

    Mono<GenericResponseDTO<Void>> annullaValidazione(Long id, String codicePa);

    Mono<GenericResponseDTO<Map<String, Object>>> getTabellaMock(TipoTabellaSezione331 tipoTabella);

    /**
     * @param identitafk      se {@code storageMinerva=true} viene usato come {@code identitafk} per
     *                        l'upsert su storageminerva.
     * @param storageMinerva  se {@code true}, dopo aver ottenuto le tabelle, le persiste su
     *                        storageminerva (una riga per tabella, con codtipologiafk = tipoTabella).
     */
    Mono<GenericResponseDTO<List<Map<String, Object>>>> getAllTabelleMock(String codiceAmministrazione,
                                                                          String annoRiferimento,
                                                                          Long identitafk,
                                                                          Boolean storageMinerva);

    Mono<GenericResponseDTO<String>> getTokenMinerva();
}
