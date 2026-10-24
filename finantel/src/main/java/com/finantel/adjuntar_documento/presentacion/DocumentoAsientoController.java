package com.finantel.adjuntar_documento.presentacion;

import com.finantel.adjuntar_documento.aplicacion.DocumentoAsientoService;
import com.finantel.adjuntar_documento.dominio.entity.DocumentoAsiento;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/asientos")
public class DocumentoAsientoController {

    private final DocumentoAsientoService service;

    public DocumentoAsientoController(DocumentoAsientoService service) {
        this.service = service;
    }

    @PostMapping(value = "/{id}/documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> adjuntar(@PathVariable Integer id,
                                      @RequestParam("archivo") MultipartFile archivo,
                                      Authentication auth) throws IOException {
        try {
            DocumentoAsiento doc = service.adjuntar(id, archivo.getOriginalFilename(), archivo.getBytes(), auth.getName());
            return ResponseEntity.ok(Map.of("id", doc.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/documento/{docId}")
    public ResponseEntity<byte[]> descargar(@PathVariable Integer docId, Authentication auth) {
        boolean esContador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_CONTADOR_GENERAL"));
        String rol = esContador ? "Contador_General" : "";
        byte[] bytes = service.descargar(docId, auth.getName(), rol);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .body(bytes);
    }
}