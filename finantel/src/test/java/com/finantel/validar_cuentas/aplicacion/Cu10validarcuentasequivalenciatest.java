package com.finantel.validar_cuentas.aplicacion;

import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CU10ValidarCuentasEquivalenciaTest {

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

    @ParameterizedTest
    @ValueSource(strings = {"Activo Corriente", "Activo No Corriente", "Pasivo Corriente",
                            "Pasivo No Corriente", "Patrimonio", "Ingresos", "Gastos"})
    void CP_E1_clasificaciones_validas_son_aceptadas(String clasificacion) {
        assertDoesNotThrow(() -> service.crear(cuenta("1011", clasificacion, "Débito", "Activa")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Débito", "Crédito"})
    void CP_E2_tipos_validos_son_aceptados(String tipo) {
        assertDoesNotThrow(() -> service.crear(cuenta("1011", "Activo Corriente", tipo, "Activa")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Activa", "Inactiva"})
    void CP_E3_estados_validos_son_aceptados(String estado) {
        assertDoesNotThrow(() -> service.crear(cuenta("1011", "Activo Corriente", "Débito", estado)));
    }
}