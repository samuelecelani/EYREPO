package it.ey.piao.bff.service;

import it.ey.dto.ConfigurazioniDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface IConfigurazioniService
{
    Mono<GenericResponseDTO<List<ConfigurazioniDTO>>> getAllConfigurazioni();
    Mono<GenericResponseDTO<List<String>>> getAllDataDaAndDataA();
    Mono<GenericResponseDTO<Void>> setValoreFromCodice(String codice, String valore);
    Mono<GenericResponseDTO<String>> getValoreFromCodice(String codice);
    Mono<GenericResponseDTO<ConfigurazioniDTO>> getConfigurazioneByCodice(String codice);

    /**
     * Recupera in un'unica chiamata (endpoint FREE, no auth) le date PIAO necessarie
     * al bootstrap del FE: {@code DATA_COMPILAZIONE_PIAO} e {@code DATA_SCADENZA_PIAO}.
     */
    Mono<Map<String, String>> getPiaoDatesFree();
}
