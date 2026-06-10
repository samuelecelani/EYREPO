package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName("PDF")
public class PdfNotificationDTO extends NotificationDTO {

    private static final long serialVersionUID = 1L;

    /**
     * Nome del template Jasper da utilizzare per la generazione del PDF.
     */
    private String templateName;

    /**
     * Dati da iniettare nel template Jasper.
     */
    private Map<String, Object> data;

    /**
     * Nome del file di output generato.
     */
    private String outputFileName;

    /**
     * Chiavi S3 dei file caricati (allegati).
     */
    private List<String> uploadedFileKeys;

    /**
     * Chiave S3 del PDF generato.
     */
    private String generatedPdfS3Key;

    /**
     * Allegato PDF generato con i riferimenti S3 e metadati.
     */
    private AllegatoDTO allegato;
    private boolean isUploadFile;
}
