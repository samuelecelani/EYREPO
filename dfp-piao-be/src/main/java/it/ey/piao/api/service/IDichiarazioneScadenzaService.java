package it.ey.piao.api.service;

import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.dto.SollecitiDichiarazioniDFPDTO;
import it.ey.dto.StoricoDichiarazioneDFPDTO;
import it.ey.enums.StatoDichiarazioneEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IDichiarazioneScadenzaService
{
    DichiarazioneScadenzaDTO saveOrUpdate(DichiarazioneScadenzaDTO dto);
    void delete(Long id);
    DichiarazioneScadenzaDTO getExistingDichiarazioneScadenza(String codPAFK);
    /** Recupera la DichiarazioneScadenza collegata al PIAO indicato (null se assente). */
    DichiarazioneScadenzaDTO findByIdPiao(Long idPiao);
    List<StoricoDichiarazioneDFPDTO> findAllStorico();
    void updateStato(Long id, Boolean stato);

    /**
     * Ricerca dichiarazioni di mancata/ritardata compilazione per il PIAO indicato (denominazione obbligatoria),
     * con filtri opzionali su tipologia ISTAT, codPAFK dell'amministrazione e stato (INVIATA / NON_INVIATA).
     */
    List<SollecitiDichiarazioniDFPDTO> searchDichiarazioni(String denominazionePiao,
                                                           String tipologiaIstat,
                                                           String codPAFK,
                                                           StatoDichiarazioneEnum statoDichiarazione);

    /**
     * Variante PAGINATA del search: ritorna una Page&lt;SollecitiDichiarazioniDFPDTO&gt;
     * (compatibile con Spring Data) consumabile direttamente dal FE come Page&lt;T&gt;.
     */
    Page<SollecitiDichiarazioniDFPDTO> searchDichiarazioniPaged(String denominazionePiao,
                                                                String tipologiaIstat,
                                                                String codPAFK,
                                                                StatoDichiarazioneEnum statoDichiarazione,
                                                                Pageable pageable);
}
