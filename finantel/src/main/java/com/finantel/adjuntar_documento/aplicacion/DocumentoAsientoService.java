package com.finantel.adjuntar_documento.aplicacion;

import com.finantel.adjuntar_documento.dominio.entity.DocumentoAsiento;
import com.finantel.adjuntar_documento.dominio.repository.DocumentoAsientoRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class DocumentoAsientoService {

    static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final List<String> EXTENSIONES = List.of("pdf", "png", "jpg", "jpeg");

    private final DocumentoAsientoRepository repo;
    private final AsientoContableRepository asientoRepo;
    private final StorageDocumentos storage;
    private final AuditoriaService auditoria;

    public DocumentoAsientoService(DocumentoAsientoRepository repo,
                                   AsientoContableRepository asientoRepo,
                                   StorageDocumentos storage,
                                   AuditoriaService auditoria) {
        this.repo = repo;
        this.asientoRepo = asientoRepo;
        this.storage = storage;
        this.auditoria = auditoria;
    }

    public DocumentoAsiento adjuntar(Integer asientoId, String nombreOriginal, byte[] contenido, String usuario) {
        AsientoContable asiento = asientoRepo.findById(asientoId)
                .orElseThrow(() -> new RuntimeException("Asiento no encontrado"));

        if (!usuario.equals(asiento.getUsuario())) {
            throw new AccessDeniedException("No puede adjuntar documentos a un asiento ajeno");
        }
        if (contenido == null || contenido.length == 0) {
            throw new RuntimeException("El archivo está vacío");
        }
        if (contenido.length > MAX_BYTES) {
            throw new RuntimeException("El archivo supera el límite de 5 MB");
        }

        String nombre = limpiarNombre(nombreOriginal);
        String ext = extension(nombre);
        if (!EXTENSIONES.contains(ext) || !firmaValida(ext, contenido)) {
            throw new RuntimeException("Tipo de archivo no permitido");
        }

        String ruta = storage.guardar(contenido, ext);

        DocumentoAsiento doc = new DocumentoAsiento();
        doc.setAsientoId(asientoId);
        doc.setNombreOriginal(nombre);
        doc.setRuta(ruta);
        doc.setTipo(ext);
        doc.setTamano((long) contenido.length);
        doc.setUsuario(usuario);
        DocumentoAsiento guardado = repo.save(doc);

        auditoria.registrar(usuario, "ADJUNTAR_DOCUMENTO", "Documentos", null,
                "Adjuntó " + nombre + " al asiento " + asientoId);
        return guardado;
    }

    public byte[] descargar(Integer docId, String usuario, String rol) {
        DocumentoAsiento doc = repo.findById(docId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        boolean esDueno = usuario != null && usuario.equals(doc.getUsuario());
        boolean esContador = "Contador_General".equals(rol);
        if (!esDueno && !esContador) {
            throw new AccessDeniedException("Sin permiso para descargar este documento");
        }
        return storage.leer(doc.getRuta());
    }

    private String limpiarNombre(String nombre) {
        String n = nombre == null ? "" : nombre;
        int p = Math.max(n.lastIndexOf('/'), n.lastIndexOf('\\'));
        return n.substring(p + 1);
    }

    private String extension(String nombre) {
        int p = nombre.lastIndexOf('.');
        return p < 0 ? "" : nombre.substring(p + 1).toLowerCase(Locale.ROOT);
    }

    private boolean firmaValida(String ext, byte[] c) {
        switch (ext) {
            case "pdf": return empiezaCon(c, 0x25, 0x50, 0x44, 0x46);
            case "png": return empiezaCon(c, 0x89, 0x50, 0x4E, 0x47);
            default:    return empiezaCon(c, 0xFF, 0xD8, 0xFF);
        }
    }

    private boolean empiezaCon(byte[] c, int... firma) {
        if (c.length < firma.length) return false;
        for (int i = 0; i < firma.length; i++) {
            if ((c[i] & 0xFF) != firma[i]) return false;
        }
        return true;
    }
}