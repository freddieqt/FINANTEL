package com.finantel.validar_cuentas.presentacion;

import com.finantel.compartido.NegocioException;
import com.finantel.validar_cuentas.aplicacion.ValidarCuentasService;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/validar-cuentas")
public class ValidarCuentasController {

    private static final Logger log = LoggerFactory.getLogger(ValidarCuentasController.class);
    private final ValidarCuentasService service;

    public ValidarCuentasController(ValidarCuentasService service) {
        this.service = service;
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<?> validar(@PathVariable("codigo") String codigo) {
        return ResponseEntity.ok(service.validarCuenta(codigo));
    }

    @GetMapping
    public ResponseEntity<?> listarActivas() {
        return ResponseEntity.ok(service.listarCuentasActivas());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CuentaContable c) {
        try {
            service.crear(c);
            return ResponseEntity.ok(Map.of("mensaje", "Cuenta " + c.getCodigo() + " — " + c.getDescripcion() + " creada correctamente"));
        } catch (NegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "El código de cuenta contable ya se encuentra registrado."));
        } catch (Exception e) {
            log.error("Error al registrar cuenta contable", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al crear la cuenta contable."));
        }
    }
}
