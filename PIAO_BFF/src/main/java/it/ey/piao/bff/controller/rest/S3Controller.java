//package it.ey.piao.bff.controller.rest;
//
//import it.ey.piao.bff.service.S3Service;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
////Controller per salvare file su S3 a titolo di esempio è stato usato solo il pdf
////Possibili migliorie: Gestire dinamicamente estensione e tipo di file
//@RestController
//@RequestMapping("/fonte-dati")
//public class S3Controller {
//
//    @Autowired
//    private S3Service s3Service;
//
//    //@Operation(summary = "Carica un file su S3")
//    @PostMapping(value = "/upload", consumes = "multipart/form-data")
//    public ResponseEntity<String> uploadFile(
//        @RequestParam("file") MultipartFile file
//        // , @RequestParam("dataFonteDati")  @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataFonteDati,
//       // @RequestParam("tipoFonteDati") Integer tipoFonteDati
//    ) {
//
//        try {
//            // Controllo se il file ha estensione valida
//            String filename = file.getOriginalFilename();
////            if (filename == null || (!filename.endsWith(".xls") && !filename.endsWith(".xlsx"))) {
////                return ResponseEntity.badRequest().body("Formato file non supportato. Carica un file XLS o XLSX.");
////            }
//
//            // Upload del file
//            String fileUrl = s3Service.uploadFile(file);
//            return ResponseEntity.ok("File caricato con successo: " + fileUrl);
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().body("Errore durante l'upload: " + e.getMessage());
//        }
//    }
//
//}
//
