package it.ey.piao.bff.service;

import it.ey.dto.ExcelNotificationDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IExcelGenerationService {

    /**
     * Genera Excel in batch a partire da una lista di idPiao.
     * Per ogni idPiao recupera i dati PiaoExternal dal BE, costruisce un ExcelNotificationDTO
     * e invia la lista al Notifica_BE su /api/v1/excel/generation/batch per la scrittura sulla coda.
     *
     * @param idPiaoList lista di ID PIAO
     * @param codicePa   codice della Pubblica Amministrazione
     * @return Mono con la lista di ExcelNotificationDTO inviati
     */
    Mono<GenericResponseDTO<List<ExcelNotificationDTO>>> generateExcelBatch(List<Long> idPiaoList, String codicePa);
}

