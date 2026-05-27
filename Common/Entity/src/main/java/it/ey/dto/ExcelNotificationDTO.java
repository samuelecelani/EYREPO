package it.ey.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ExcelNotificationDTO extends NotificationDTO {

    private static final long serialVersionUID = 1L;

    /**
     * Nome del template da utilizzare per la generazione dell'Excel.
     */
    private String templateName;

    private String nomeModulo;

    /**
     * Lista di dati per la generazione dell'Excel (un elemento per ogni sheet/PIAO).
     * Se contiene un solo elemento, genera un Excel con un singolo sheet.
     * Se contiene più elementi, genera un Excel multi-sheet.
     */
    private List<Map<String, Object>> data;

    /**
     * Nome del file di output generato.
     */
    private String outputFileName;

    /**
     * Chiavi S3 dei file caricati (allegati).
     */
    private List<String> uploadedFileKeys;

    /**
     * Chiave S3 dell'Excel generato.
     */
    private String generatedExcelS3Key;

    /**
     * Allegato Excel generato con i riferimenti S3 e metadati.
     */
    private AllegatoDTO allegato;

    /**
     * Se true, indica che il consumer deve effettuare l'upload dell'Excel generato su S3.
     */
    private boolean uploadFile;
}

