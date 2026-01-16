
package it.ey.piao.bff.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${aws.s3.bucket}")
    private String S3bucketName;

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("dd-MM-yyyy'_'HH:mm:ss");

    public S3Service(S3Presigner s3Presigner, S3Client s3Client) {
        this.s3Presigner = s3Presigner;
        this.s3Client = s3Client;
    }

    /**
     * Upload reattivo da FilePart (WebFlux) -> ritorna la key salvata su S3/MinIO.
     */
    public Mono<String> uploadFile(FilePart filePart) {
        if (filePart == null) {
            return Mono.error(new IllegalArgumentException("File mancante"));
        }

        filePart.filename();
        final String originalName = !filePart.filename().isBlank()
            ? filePart.filename()
            : "file";

        // Deduce estensione dal nome o dal content-type
        String ext = FilenameUtils.getExtension(originalName);
        if (ext.isBlank()) {
            filePart.headers();
            String ct = filePart.headers().getContentType() != null
                ? filePart.headers().getContentType().toString()
                : null;
            ext = mimeToExt(ct);
        }
        if (ext == null || ext.isBlank()) {
            ext = "bin";
        }

        String base = FilenameUtils.removeExtension(originalName);
        String fileKey = base + "_" + LocalDateTime.now().format(FILE_TS) + "." + ext;
        fileKey = sanitizeFileKey(fileKey);

        // Content-Type coerente
        filePart.headers();
        final String contentType = filePart.headers().getContentType() != null
            ? Objects.requireNonNull(filePart.headers().getContentType()).toString()
            : extToMime(ext);

        String finalFileKey = fileKey;
        return Mono.fromCallable(() -> Files.createTempFile("upload_", "_" + UUID.randomUUID()))
            .flatMap(tempPath ->
                filePart.transferTo(tempPath)
                    .thenReturn(tempPath)
            )
            .flatMap(tempPath ->
                Mono.fromCallable(() -> {
                        try {
                            // Upload sincrono S3Client dentro boundedElastic
                            s3Client.putObject(
                                PutObjectRequest.builder()
                                    .bucket(S3bucketName)
                                    .key(finalFileKey)
                                    .contentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                    // opzionale:
                                    // .contentDisposition("inline; filename=\"" + originalName + "\"")
                                    .build(),
                                RequestBody.fromFile(tempPath)
                            );
                            return finalFileKey;
                        } finally {
                            try { Files.deleteIfExists(tempPath); } catch (Exception ignored) {}
                        }
                    }
                ).subscribeOn(Schedulers.boundedElastic())
            );
    }

    /**
     * Upload reattivo di un PDF generato lato server (byte[]) -> ritorna la key.
     */
    public Mono<String> uploadFileGenerated(byte[] file) {
        if (file == null || file.length == 0) {
            return Mono.error(new IllegalArgumentException("File generato vuoto o nullo"));
        }

        return Mono.fromCallable(() -> {
                String fileKey = "Generazione_pdf_di_prova_" + LocalDateTime.now().format(FILE_TS) + ".pdf";
                fileKey = sanitizeFileKey(fileKey);

                s3Client.putObject(
                    PutObjectRequest.builder()
                        .bucket(S3bucketName)
                        .key(fileKey)
                        .contentType(MediaType.APPLICATION_PDF_VALUE)
                        .build(),
                    RequestBody.fromBytes(file)
                );

                return fileKey;
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Genera Presigned URL reattiva.
     */
    public Mono<String> generatePresignedUrl(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return Mono.error(new IllegalArgumentException("fileKey mancante o vuota"));
        }

        String safeKey = sanitizeFileKey(fileKey);
        String filenameForHeader = sanitizeFilenameForHeader(fileKey);

        // Deduce MIME dal fileKey (estensione)
        String responseCt = extToMime(FilenameUtils.getExtension(fileKey));
        if (responseCt == null) {
            responseCt = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String finalResponseCt = responseCt;
        return Mono.fromCallable(() -> {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(S3bucketName)
                    .key(safeKey)
                    .responseContentType(finalResponseCt)
                    .responseContentDisposition("inline; filename=\"" + filenameForHeader + "\"")
                    .build();

                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(4)) // TTL configurabile
                    .getObjectRequest(getObjectRequest)
                    .build();

                return s3Presigner.presignGetObject(presignRequest).url().toString();
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Delete reattivo.
     */
    public Mono<Void> deleteFile(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return Mono.error(new IllegalArgumentException("fileKey mancante o vuota"));
        }

        String safeKey = sanitizeFileKey(fileKey);

        return Mono.fromRunnable(() ->
                s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(S3bucketName)
                    .key(safeKey)
                    .build())
            )
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    // ------- Utility -------

    private String sanitizeFilenameForHeader(String name) {
        return name.replace("\r", "").replace("\n", "").replace("\"", "");
    }

    private String sanitizeFileKey(String originalName) {
        return Normalizer.normalize(originalName, Normalizer.Form.NFD)
            .replaceAll("[^\\p{ASCII}]", "") // rimuove accenti
            .replace(":", "_")               // sostituisce i due punti (presenti nel timestamp)
            .replace(" ", "_");              // sostituisce spazi
    }

    private String mimeToExt(String mime) {
        if (mime == null) return null;
        return switch (mime) {
            case MediaType.APPLICATION_PDF_VALUE -> "pdf";
            case MediaType.IMAGE_JPEG_VALUE -> "jpg";
            case MediaType.IMAGE_PNG_VALUE -> "png";
            case "image/webp" -> "webp";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "pptx";
            case MediaType.TEXT_PLAIN_VALUE -> "txt";
            default -> null;
        };
    }

    private String extToMime(String ext) {
        if (ext == null) return null;
        return switch (ext.toLowerCase()) {
            case "pdf" -> MediaType.APPLICATION_PDF_VALUE;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "webp" -> "image/webp";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt" -> MediaType.TEXT_PLAIN_VALUE;
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}
