# PDFGenerator Module

Modulo Spring Boot per la generazione di PDF tramite JasperReports, con integrazione ArtemisQ/ActiveMQ.

## Caratteristiche

- ✅ Lettura messaggi da coda ArtemisQ/ActiveMQ
- ✅ Generazione PDF con JasperReports
- ✅ Template HTML/JRXML come risorse statiche
- ✅ API REST per health check
- ✅ Configurazione esternalizzata

## Dipendenze Principali

- Spring Boot 3.5.6
- Spring Boot Starter Artemis (JMS)
- JasperReports 6.21.0
- Lombok
- Jackson (JSON processing)

## Configurazione

### Application Properties

```properties
# Porta del servizio
server.port=9084

# ActiveMQ Artemis
spring.artemis.broker-url=tcp://localhost:61616
spring.artemis.user=admin
spring.artemis.password=admin

# Nome della coda
pdf.queue.name=pdf-generation-queue

# Directory output PDF
pdf.output.directory=./generated-pdfs
```

### Environment Variables

Puoi sovrascrivere le configurazioni tramite variabili d'ambiente:

- `ACTIVEMQ_URL`: URL del broker ActiveMQ
- `ACTIVEMQ_USERNAME`: Username ActiveMQ
- `ACTIVEMQ_PASSWORD`: Password ActiveMQ
- `PDF_QUEUE_NAME`: Nome della coda
- `PDF_OUTPUT_DIR`: Directory di output

## Utilizzo

### Inviare una richiesta di generazione PDF alla coda

Formato del messaggio JSON:

```json
{
  "templateName": "report-template",
  "outputFileName": "report_2024",
  "data": {
    "title": "Report Mensile",
    "subtitle": "Gennaio 2024",
    "generationDate": "2024-01-15",
    "description": "Report dettagliato delle attività"
  }
}
```

### Template JasperReports

I template JRXML devono essere posizionati in:
```
src/main/resources/jasper/
```

Esempio: `src/main/resources/jasper/report-template.jrxml`

### Template HTML

I template HTML sono disponibili in:
```
src/main/resources/templates/
```

Esempio: `src/main/resources/templates/report-template.html`

## Build e Deploy

### Build locale

```bash
mvn clean package
```

### Build Docker

```bash
docker build -t dfp-piao-pdfgenerator:1.0.0 -f PDFGenerator/Dockerfile .
```

### Run con Docker Compose

```bash
docker-compose up dfp-piao-pdfgenerator
```

## Endpoints

### Health Check
```
GET http://localhost:9084/health/ready
```

### OpenAPI Documentation
```
GET http://localhost:9084/openapi-ui
```

## Struttura del Progetto

```
PDFGenerator/
├── src/main/java/it/ey/pdfgenerator/
│   ├── PdfGeneratorApplication.java      # Main application
│   ├── config/
│   │   └── ArtemisConfig.java            # Configurazione ActiveMQ
│   ├── consumer/
│   │   └── PdfQueueConsumer.java         # Consumer coda JMS
│   ├── dto/
│   │   └── PdfRequest.java               # DTO richiesta PDF
│   └── service/
│       └── PdfGeneratorService.java      # Servizio generazione PDF
├── src/main/resources/
│   ├── jasper/
│   │   └── report-template.jrxml         # Template JasperReports
│   ├── templates/
│   │   └── report-template.html          # Template HTML
│   └── application.properties            # Configurazione
└── Dockerfile                            # Docker image definition
```

## Note

- I PDF generati vengono salvati nella directory configurata (default: `./generated-pdfs`)
- Il servizio ascolta automaticamente sulla coda configurata
- I template possono essere personalizzati aggiungendo nuovi file JRXML
- I parametri del template sono passati tramite la mappa `data` nel messaggio JSON
