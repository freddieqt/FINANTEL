package com.finantel.registrar_asiento_contable.presentacion;

import com.finantel.compartido.NegocioException;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.infraestructura.mapping.BuilderAsiento;
import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jspecify.annotations.NonNull;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asientos")
public class AsientoContableController {

    private static final Logger log = LoggerFactory.getLogger(AsientoContableController.class);
    private final AsientoContableService service;
    private final AuditoriaService auditoriaService;

    public AsientoContableController(AsientoContableService service,
            AuditoriaService auditoriaService) {
        this.service = service;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<AsientoContable> listar() {
        return service.listar();
    }

    @PostMapping("/builder")
    public ResponseEntity<String> construirAsiento(@RequestBody AsientoContable datos) {

        // 1. Construye con el patrón Builder usando los datos del usuario
        BuilderAsiento builderAsiento = new BuilderAsiento.Builder()
                .numeroCorrelativo(datos.getNumeroCorrelativo())
                .tipo(datos.getTipo())
                .descripcion(datos.getDescripcion())
                .debe(datos.getDebe())
                .haber(datos.getHaber())
                .cuentaContable(datos.getCuentaContable())
                .centroCosto(datos.getCentroCosto())
                .moneda(datos.getMoneda() != null ? datos.getMoneda() : "PEN")
                .tipoCambio(datos.getTipoCambio() != null ? datos.getTipoCambio() : 1.0)
                .usuario(datos.getUsuario())
                .build();

        // 2. Convierte a entidad real
        AsientoContable asiento = new AsientoContable();
        asiento.setNumeroCorrelativo(builderAsiento.getNumeroCorrelativo());
        asiento.setTipo(builderAsiento.getTipo());
        asiento.setDescripcion(builderAsiento.getDescripcion());
        asiento.setDebe(builderAsiento.getDebe());
        asiento.setHaber(builderAsiento.getHaber());
        asiento.setCuentaContable(builderAsiento.getCuentaContable());
        asiento.setCentroCosto(builderAsiento.getCentroCosto());
        asiento.setMoneda(builderAsiento.getMoneda());
        asiento.setTipoCambio(builderAsiento.getTipoCambio());
        asiento.setUsuario(builderAsiento.getUsuario());

        // 3. Guarda en BD
        service.crear(asiento);

        return ResponseEntity.ok("Operación completada");
    }

    @GetMapping("/{id}")
    public AsientoContable buscar(@PathVariable("id") @NonNull Integer id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable("id") @NonNull Integer id,
            @RequestBody AsientoContable datos,
            Principal principal) {
        AsientoContable anterior = service.buscarPorId(id);
        String valAnterior = anterior.getTipo() + ": " + anterior.getDescripcion();
        service.actualizar(id, datos);
        auditoriaService.registrar(principal.getName(), "Edición",
                "Asiento #" + id, valAnterior,
                datos.getTipo() + ": " + datos.getDescripcion());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/anular/{id}")
    public ResponseEntity<?> anular(@PathVariable("id") @NonNull Integer id, Principal principal) {
        service.buscarPorId(id);
        service.anular(id);
        auditoriaService.registrar(principal.getName(), "Anulación",
                "Asiento #" + id, "Activo", "Anulado");
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody AsientoContable a, Principal principal) {
        try {
            a.setUsuario(principal.getName());
            service.crear(a);
            auditoriaService.registrar(principal.getName(), "Creación",
                    "Asiento", "—", a.getDescripcion());
            return ResponseEntity.ok().build();
        } catch (NegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "Error de integridad de datos: el registro contiene datos duplicados o viola una restricción de la base de datos."));
        } catch (Exception e) {
            log.error("Error al crear asiento contable", e);
            return ResponseEntity.status(500).body(Map.of("error", "Ocurrió un error interno al crear el asiento."));
        }
    }
}
