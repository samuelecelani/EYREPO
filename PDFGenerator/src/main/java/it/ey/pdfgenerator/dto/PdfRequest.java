package it.ey.pdfgenerator.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfRequest {
    private String templateName;
    private Map<String, Object> data;
    private String outputFileName;
}
