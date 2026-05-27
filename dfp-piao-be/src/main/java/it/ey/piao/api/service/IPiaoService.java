package it.ey.piao.api.service;

import it.ey.dto.ApprovazioneDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.external.DocumentoPiaoExternalPPDTO;
import it.ey.dto.external.PiaoExternalDTO;

import java.util.List;

public interface IPiaoService {
    public PiaoDTO getOrCreatePiao(PiaoDTO piao, String triennioRiferimento);
    public boolean redigiPiaoIsAllowed(String codPAFK);

    /**
     * Recupera la tipologia del PIAO corrente nel triennio per la PA indicata.
     * @param codPAFK codice della pubblica amministrazione
     * @return PiaoDTO con tipologia e tipologiaOnline, oppure null se non esiste
     */
    PiaoDTO getTipologiaCorrente(String codPAFK);

    public List<PiaoDTO> getAllPiaoByCodPAFK(String codPAF);

    PiaoDTO findById(Long id);

    /**
     * Recupera il PIAO dell'anno precedente per la PA indicata.
     * @param codPAFK codice della pubblica amministrazione
     * @return PiaoDTO dell'anno precedente, oppure null se non esiste
     */
    PiaoDTO findPiaoPrecedente(String codPAFK);
    void pubblicaPiao(ApprovazioneDTO approvazione);
    ApprovazioneDTO getApprovazione(Long idPiao);

    List <PiaoDTO> findByCodPafkAndDenominazioneAndVersione(String codPAFK, String denominazione, String versione);
    PiaoDTO findPiaoLastVersion(String codPAFK, String denominazione);

    PiaoDTO richiediValidazione(Long idPiao, String userNameSurname, String userRole, String fiscalCode);
    PiaoDTO validaSezione(Long idPiao, String userNameSurname, String userRole, String fiscalCode);
    PiaoDTO rifiutaValidazione(Long idPiao, String osservazioni, String userNameSurname, String userRole, String fiscalCode);
    PiaoDTO revocaValidazione(Long idPiao, String osservazioni, String userNameSurname, String userRole, String fiscalCode);
    PiaoDTO annullaValidazione(Long idPiao, String userNameSurname, String userRole, String fiscalCode);
    void salvaInBozzaPiaoPDF(PiaoDTO piao);
    void pubblicaPiaoPDF(PiaoDTO piao);

    /**
     * Recupera i dati del PIAO per l'esposizione esterna.
     * Include Anagrafica, OVP con strategie e indicatori, e popolazione suddivisa per età.
     * @param codPAFK codice della pubblica amministrazione
     * @return PiaoExternalDTO con tutti i dati relazionati
     */
    PiaoExternalDTO findPiaoExternal(String codPAFK);

    /**
     * Recupera i dati del PIAO per l'esposizione esterna, filtrando per una lista di idPiao.
     * Include Anagrafica, OVP con strategie e indicatori, e popolazione suddivisa per età.
     * @param idPiaoList lista di ID PIAO
     * @return lista di PiaoExternalDTO con tutti i dati relazionati
     */
    List<PiaoExternalDTO> findPiaoExternalByIds(List<Long> idPiaoList);

    /**
     * Recupera tutti i PIAO pubblicati (idStato=8), con i relativi allegati.
     */
    List<DocumentoPiaoExternalPPDTO> findAllPiaoPubblicati(Long idPiao, String denominazione, String codePa);



    public List<String> getTrienniRiferimento();

    /**
     * Recupera tutti i PIAO con stato PUBBLICATO.
     * Se codPAFK è valorizzato filtra per PA, altrimenti restituisce tutti.
     */
    List<PiaoDTO> findAllPiaoPubblicatiByCodePA(String codPAFK);

    /**
     * Ricerca PIAO pubblicati con filtri opzionali su codiceIpa (codPAFK) e tipologia (tipologiaIstat da Anagrafica).
     */
    List<PiaoDTO> searchPubblicati(String codiceIpa, String tipologia);

    /**
     * Ricerca PIAO pubblicati per denominazione (obbligatoria) e tipologia (facoltativa).
     * Ritorna sempre l'informazione della tipologiaIstat dalla tabella Anagrafica nel PiaoDTO.
     */
    List<PiaoDTO> searchPubblicatiByDenominazione(String denominazione, String tipologia);

}
