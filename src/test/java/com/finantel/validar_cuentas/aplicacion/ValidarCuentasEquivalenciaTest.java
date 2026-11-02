package com.finantel.validar_cuentas.aplicacion;

import com.finantel.validar_cuentas.aplicacion.ValidarCuentasService;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ValidarCuentasEquivalenciaTest {

    @Mock ValidarCuentasRepository repo;
    @InjectMocks ValidarCuentasService service;

    private CuentaContable cuenta(String codigo, String clasificacion, String tipo, String estado) {
        CuentaContable c = new CuentaContable();
        c.setCodigo(codigo);
        c.setDescripcion("Cuentas por cobrar");
        c.setClasificacion(clasificacion);
        c.setTipo(tipo);
        c.setEstado(estado);
        return c;
    }

    // ENTRADA: código = "1011"; clasificación = "Activo Corriente"; tipo = "Débito"; estado = "Activa"
    // RESULTADO: La cuenta se crea sin errores
    @Test
    void EQ_CU10_cuenta_valida_se_crea() {
        assertDoesNotThrow(() -> service.crear(cuenta("1011", "Activo Corriente", "Débito", "Activa")));
    }

    // ENTRADA: código = "A"; clasificación = "Activo Corriente"; tipo = "Débito"; estado = "Activa"
    // RESULTADO: Se rechaza con el mensaje "solo números enteros"
    @Test
    void EQ_CU10_codigo_con_letra_se_rechaza() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(cuenta("A", "Activo Corriente", "Débito", "Activa")));
        assertTrue(ex.getMessage().contains("solo números enteros"));
    }

    // ENTRADA: código = "1011"; clasificación = null (opción vacía); tipo = "Débito"; estado = "Activa"
    // RESULTADO: Se rechaza con el mensaje "Clasificación inválida"
    @Test
    void EQ_CU10_clasificacion_vacia_se_rechaza() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(cuenta("1011", null, "Débito", "Activa")));
        assertTrue(ex.getMessage().contains("Clasificación inválida"));
    }

    // ENTRADA: código = "1011"; clasificación = "Activo Corriente"; tipo = null (opción vacía); estado = "Activa"
    // RESULTADO: Se rechaza con el mensaje "Tipo inválido"
    @Test
    void EQ_CU10_tipo_vacio_se_rechaza() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.crear(cuenta("1011", "Activo Corriente", null, "Activa")));
        assertTrue(ex.getMessage().contains("Tipo inválido"));
    }
}
