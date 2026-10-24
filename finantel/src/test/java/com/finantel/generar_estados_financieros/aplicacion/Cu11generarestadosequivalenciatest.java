package com.finantel.generar_estados_financieros.aplicacion;

import com.finantel.generar_estados_financieros.dominio.repository.EstadosFinancierosRepository;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CU11GenerarEstadosEquivalenciaTest {

    @Mock
    private EstadosFinancierosRepository repo;

    @InjectMocks
    private GenerarEstadosFinancierosService service;

    @ParameterizedTest
    @CsvSource({
        "5001, ingresos",
        "600, costos",
        "6010, gastosOperativos",
        "4501, gastosOperativos",
        "6501, gastosFinancieros"
    })
    void CP_E1_cada_clase_de_cuenta_cae_en_su_seccion(String codigo, String seccion) {
    	when(repo.findTodosSaldos()).thenReturn(java.util.Collections.singletonList(
                new Object[]{codigo, "Cuenta", "Gastos", 100.0}
            ));

        Map<String, Object> r = service.getBalanceGeneral();

        assertEquals(1, ((java.util.List<?>) r.get(seccion)).size());
    }
}