package it.ey.piao.bff.file;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;


import it.ey.dto.TestDTO;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class GeneratePiao {

    private final Template exampleTemplate;

    public GeneratePiao() throws IOException {
        // Carica il template da resources/example.qute
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("example.qute");
        if (inputStream == null) {
            throw new IOException("Template 'example.qute' non trovato.");
        }

        String templateContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Engine engine = Engine.builder().addDefaults().build();
        this.exampleTemplate = engine.parse(templateContent);
    }

    public byte[] execute(TestDTO request) throws IOException {

        Map<String, Object> data = new HashMap<>();
        data.put("titolo", request.getCreatedBy());
        data.put("testo", request.getTesto());

        TemplateInstance templateInstance = exampleTemplate.data(data);
        String htmlContent = templateInstance.render();

        if (!htmlContent.contains("<html") || !htmlContent.contains("</html>")) {

        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlContent, null);

        InputStream fontStream = getClass().getResourceAsStream("/fonts/tahoma.ttf");
        if (fontStream != null) {
            builder.useFont(() -> fontStream, "Tahoma");
        }

        builder.toStream(outputStream);
        builder.run();
        return outputStream.toByteArray();
    }
}
