package it.ey.piao.bff.service;

import it.ey.dto.ApprovazioneDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.external.PiaoExternalDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IPiaoService {
    public Mono<GenericResponseDTO<PiaoDTO>> initializePiao(PiaoDTO piao, String triennioRiferimento);

    Mono<GenericResponseDTO<PiaoDTO>> findById(Long id);

    public Mono<GenericResponseDTO<Boolean>> redigiPiaoIsAllowed(String codPAFK);

    /**
     * Recupera la tipologia del PIAO corrente nel triennio per la PA indicata.
     */
    Mono<GenericResponseDTO<PiaoDTO>> getTipologiaCorrente(String codPAFK);

    public Mono<GenericResponseDTO<List<PiaoDTO>>> findPiaoByCodPAFK(String codPAFK);

    Mono<GenericResponseDTO<PiaoDTO>> findPiaoPrecedente(String codPAFK);

    Mono<GenericResponseDTO<Void>> pubblicaPiao(ApprovazioneDTO approvazione);

    Mono<GenericResponseDTO<ApprovazioneDTO>> getApprovazione(Long idPiao);

    Mono<GenericResponseDTO<List<PiaoDTO>>> consultazionePiao(String codPAFK, String denominazione, String versione);

    Mono<GenericResponseDTO<PiaoDTO>> findPiaoLastVersion(String codPAFK, String denominazione);


    Mono<GenericResponseDTO<Void>> richiediValidazione(Long idPiao);

    Mono<GenericResponseDTO<Void>> validaSezione(Long idPiao);

    Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long idPiao, String osservazioni);

    Mono<GenericResponseDTO<Void>> revocaValidazione(Long idPiao, String osservazioni);

    Mono<GenericResponseDTO<Void>> annullaValidazione(Long idPiao);

    Mono<GenericResponseDTO<Void>> salvaInBozzaPiaoPDF(PiaoDTO piao);
    Mono<GenericResponseDTO<Void>> pubblicaPiaoPDF(PiaoDTO piao);

    /**
     * Recupera i dati del PIAO per l'esposizione esterna.
     * Include Anagrafica, OVP con strategie e indicatori, e popolazione suddivisa per età.
     */
    Mono<GenericResponseDTO<PiaoExternalDTO>> findPiaoExternal(String codPAFK);

    /**
     * Recupera i dati external dei PIAO a partire da una lista di idPiao.
     */
    Mono<GenericResponseDTO<List<PiaoExternalDTO>>> findPiaoExternalByIds(List<Long> idPiaoList);

    Mono<GenericResponseDTO<List<String>>> getTrienniRiferimento();

    /**
     * Recupera tutti i PIAO pubblicati, con filtro opzionale per codPAFK.
     */
    Mono<GenericResponseDTO<List<PiaoDTO>>> findAllPiaoPubblicatiByCodePA(String codPAFK);

    /**
     * Ricerca PIAO pubblicati con filtri opzionali su codiceIpa e tipologia.
     */
    Mono<GenericResponseDTO<List<PiaoDTO>>> searchPubblicati(String codiceIpa, String tipologia);

    /**
     * Ricerca PIAO pubblicati per denominazione (obbligatoria) e tipologia (facoltativa).
     * Ritorna sempre l'informazione della tipologiaIstat nel PiaoDTO.
     */
    Mono<GenericResponseDTO<List<PiaoDTO>>> searchPubblicatiByDenominazione(String denominazione, String tipologia);

}
