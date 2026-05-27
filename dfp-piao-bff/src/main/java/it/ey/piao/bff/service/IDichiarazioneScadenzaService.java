package it.ey.piao.bff.service;

import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PageDTO;
import it.ey.dto.SollecitiDichiarazioniDFPDTO;
import it.ey.dto.StoricoDichiarazioneDFPDTO;
import it.ey.enums.StatoDichiarazioneEnum;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IDichiarazioneScadenzaService
{
    Mono<GenericResponseDTO<DichiarazioneScadenzaDTO>> saveOrUpdate(DichiarazioneScadenzaDTO dto);
    Mono<Void> deleteById(Long id);
    Mono<GenericResponseDTO<DichiarazioneScadenzaDTO>> getExistingDichiarazioneScadenza(String codPAFK);
    /** Recupera la DichiarazioneScadenza collegata al PIAO indicato (può essere null). */
    Mono<GenericResponseDTO<DichiarazioneScadenzaDTO>> findByIdPiao(Long idPiao);
    Mono<GenericResponseDTO<List<StoricoDichiarazioneDFPDTO>>> findAllStorico();
    Mono<GenericResponseDTO<Void>> updateStato(Long id, Boolean stato);

    Mono<GenericResponseDTO<List<SollecitiDichiarazioniDFPDTO>>> searchDichiarazioni(String denominazionePiao,
                                                                                     String tipologiaIstat,
                                                                                     String codPAFK,
                                                                                     StatoDichiarazioneEnum statoDichiarazione);

    /**
     * Variante PAGINATA del search: forwarda al BE i parametri di pagina/ordinamento e ritorna un PageDTO.
     * @param sort opzionale, formato Spring "property,direction" (può essere ripetuto, es. ["amministrazione,asc","createdTs,desc"]).
     */
    Mono<GenericResponseDTO<PageDTO<SollecitiDichiarazioniDFPDTO>>> searchDichiarazioniPaged(String denominazionePiao,
                                                                                             String tipologiaIstat,
                                                                                             String codPAFK,
                                                                                             StatoDichiarazioneEnum statoDichiarazione,
                                                                                             int page,
                                                                                             int size,
                                                                                             List<String> sort);
}
