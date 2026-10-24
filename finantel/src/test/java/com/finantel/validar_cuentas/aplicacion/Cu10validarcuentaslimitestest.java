package com.finantel.validar_cuentas.aplicacion;

import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import com.finantel.validar_cuentas.infraestructura.persistence.ValidarCuentasConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU10ValidarCuentasLimitesTest {

    private final ValidarCuentasConfig config = new ValidarCuentasConfig();

    @Mock
    private ValidarCuentasRepository repo;

    @InjectMocks
    private ValidarCuentasService service;

    private CuentaContable cuenta(String codigo, String clasificacion, String tipo, String estado) {
        CuentaContable c = new CuentaContable();
        c.setCodigo(codigo);
        c.setClasificacion(clasificacion);
        c.setTipo(tipo);
        c.setEstado(estado);
        return c;
    }

    @Test
    void CP_L1_codigo_vacio_es_rechazado() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            service.crear(cuenta("", "Activo Corriente", "Débito", "Activa")));

        assertTrue(ex.getMessage().contains("El código debe contener solo números enteros"));
    }

    @Test
    void CP_L2_codigo_con_letras_es_rechazado() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            service.crear(cuenta("10A1", "Activo Corriente", "Débito", "Activa")));

        assertTrue(ex.getMessage().contains("El código debe contener solo números enteros"));
    }

    @Test
    void CP_L3_clasificacion_nula_es_rechazada() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            service.crear(cuenta("1011", null, "Débito", "Activa")));

        assertTrue(ex.getMessage().contains("Clasificación inválida"));
    }

    @Test
    void CP_L4_config_reconoce_Activa_como_cuenta_activa() {
        // Comportamiento esperado: el servicio considera "Activa" como activa,
        // así que la configuración también debe hacerlo. Si falla, es un defecto.
        assertTrue(config.esCuentaActiva("Activa"));
    }

    @Test
    void CP_L5_listarCuentasActivas_sin_cuentas_devuelve_lista_vacia() {
        when(repo.findByEstado(anyString())).thenReturn(List.of());

        assertTrue(service.listarCuentasActivas().isEmpty());
    }
}