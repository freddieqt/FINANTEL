package com.finantel.validar_cuentas.aplicacion;

import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU10ValidarCuentasUnitTest {

    @Mock
    private ValidarCuentasRepository repo;

    @InjectMocks
    private ValidarCuentasService service;

    private CuentaContable cuenta(String codigo, String estado) {
        CuentaContable c = new CuentaContable();
        c.setCodigo(codigo);
        c.setEstado(estado);
        return c;
    }

    @Test
    void CP_U1_cuenta_activa_se_reporta_como_valida() {
        when(repo.findByCodigo("1011")).thenReturn(Optional.of(cuenta("1011", "Activa")));

        assertEquals("Cuenta válida y activa", service.validarCuenta("1011"));
    }

    @Test
    void CP_U2_cuenta_inactiva_se_reporta_como_no_permitida() {
        when(repo.findByCodigo("1012")).thenReturn(Optional.of(cuenta("1012", "Inactiva")));

        assertEquals("Cuenta inactiva — no permitida", service.validarCuenta("1012"));
    }

    @Test
    void CP_U3_codigo_inexistente_se_reporta_como_no_encontrado() {
        when(repo.findByCodigo("9999")).thenReturn(Optional.empty());

        assertEquals("Cuenta no encontrada", service.validarCuenta("9999"));
    }

    @Test
    void CP_U4_listar_devuelve_todas_las_cuentas_del_repositorio() {
        when(repo.findAll()).thenReturn(List.of(cuenta("1011", "Activa")));

        assertEquals(1, service.listar().size());
    }
}