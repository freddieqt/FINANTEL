package com.finantel.ocr_documentos.presentacion;

import com.finantel.ocr_documentos.aplicacion.OcrDocumentosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/ocr")
public class OcrDocumentosController {

    private static final Logger log = LoggerFactory.getLogger(OcrDocumentosController.class);
    private final OcrDocumentosService service;

    public OcrDocumentosController(OcrDocumentosService service) {
        this.service = service;
    }

    @PostMapping("/leer")
    public ResponseEntity<?> leerDocumento(@RequestParam("archivo") MultipartFile archivo) {
        try {
            String texto = service.extraerTexto(archivo);
            return ResponseEntity.ok(texto);
        } catch (com.finantel.compartido.NegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al procesar el documento OCR", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al procesar el documento."));
        }
    }
}
