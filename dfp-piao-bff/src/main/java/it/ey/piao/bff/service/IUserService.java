package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.LabelValueDTO;
import it.ey.dto.UserDTO;
import it.ey.dto.UtenteRuoloPaDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IUserService {
    public Mono<GenericResponseDTO<UserDTO>> getUserbyToken();
    public Mono<GenericResponseDTO<List<UtenteRuoloPaDTO>>> findUtentiByPa(String codicePa, List<String> roleNames);
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> saveUtenteByPa(UtenteRuoloPaDTO utenteRuoloPaDTO);
    public Mono<GenericResponseDTO<Void>> deleteUtentePa(String id, String codicePa);
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> retrieveProfileById(String externalId, String codePa);
    public Mono<GenericResponseDTO<List<LabelValueDTO>>> getRuoliByCodePA(String codicePa);
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> updateUtenteByPa(String id, String codicePa, UtenteRuoloPaDTO utenteRuoloPaDTO);

    /**
     * Crea (upsert) un referente: crea/aggiorna utente su BIP con ruolo Referente,
     * poi abilita tutte le sezioni effettive del PIAO sul nostro DB.
     * Non richiede autenticazione: l'id amministrazione è nel campo codicePA del DTO.
     */
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> createReferente(UtenteRuoloPaDTO utenteRuoloPaDTO);

    /**
     * Crea (upsert) un validatore: crea/aggiorna utente su BIP con ruolo Validatore,
     * poi abilita tutte le sezioni effettive del PIAO sul nostro DB.
     * Non richiede autenticazione: l'id amministrazione è nel campo codicePA del DTO.
     */
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> createValidatore(UtenteRuoloPaDTO utenteRuoloPaDTO);

    /**
     * Recupera gli utenti da BIP per ruolo e li filtra in base alle sezioni
     * assegnate sul sistema interno.
     */
    public Mono<GenericResponseDTO<List<UtenteRuoloPaDTO>>> findUtentiByRuoloAndSezioni(
        String codicePa, List<String> roleNames, List<String> sezioni);

    /**
     * Crea (upsert) un redattore: crea/aggiorna utente su BIP con ruolo Redattore,
     * poi abilita tutte le sezioni effettive del PIAO sul nostro DB.
     * Non richiede autenticazione: l'id amministrazione è nel campo codicePA del DTO.
     */
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> createRedattore(UtenteRuoloPaDTO utenteRuoloPaDTO);

}
