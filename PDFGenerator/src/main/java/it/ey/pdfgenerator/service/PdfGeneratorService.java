package it.ey.pdfgenerator.service;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PdfGeneratorService {

    private static final String TEMPLATE_PATH = "jasper/";
    private static final String OUTPUT_PATH = "generated-pdfs/";

    public byte[] generatePdf(String templateName, Map<String, Object> data) throws Exception {
        log.info("Generazione PDF con template: {}", templateName);

        // Carica il template JRXML
        InputStream templateStream = loadTemplate(templateName);
        
        // Compila il template
        JasperDesign jasperDesign = JRXmlLoader.load(templateStream);
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        // Prepara i parametri
        Map<String, Object> parameters = new HashMap<>(data);

        // Riempi il report con i dati
        JasperPrint jasperPrint = JasperFillManager.fillReport(
            jasperReport,
            parameters,
            new JREmptyDataSource()
        );

        // Esporta in PDF
        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
        
        log.info("PDF generato con successo: {} byte", pdfBytes.length);
        return pdfBytes;
    }

    public void savePdf(byte[] pdfBytes, String fileName) throws IOException {
        // TODO: salvare il file in S3

        log.info("PDF salvato in: {}", "nome file");
    }

    private InputStream loadTemplate(String templateName) throws IOException {
        String templatePath = TEMPLATE_PATH + templateName + ".jrxml";
        ClassPathResource resource = new ClassPathResource(templatePath);
        
        if (!resource.exists()) {
            throw new FileNotFoundException("Template non trovato: " + templatePath);
        }
        
        return resource.getInputStream();
    }
}
