package com.finantel.duplicar_asiento.integracion;

import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DuplicarAsientoIT {

    private static final String DESCRIPCION_ORIGEN = "DUP IT";
    private static final String DESCRIPCION_COPIA = "COPIA - " + DESCRIPCION_ORIGEN;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AsientoContableRepository repo;

    private Integer origenId;
    private String correlativoOrigen;

    @BeforeEach
    void crearOrigen() {
        AsientoContable a = new AsientoContable();
        a.setTipo("Operación ordinaria");
        a.setDescripcion(DESCRIPCION_ORIGEN);
        a.setDebe(100.00);
        a.setHaber(100.00);
        a.setEstado("Activo");
        a.setUsuario("admin.prueba@finantel.pe");
        a.setPeriodoId(/* id de un periodo abierto, p. ej. el de 2026-10 */ 1);
        a.setCuentaContable("1011");
        a.setCentroCosto("General");
        a.setMoneda("PEN");
        a.setTipoCambio(1.0);
        a.setNumeroCorrelativo("AS-DUP-IT-ORIGEN");
        repo.save(a);
        origenId = a.getId();
        correlativoOrigen = a.getNumeroCorrelativo();
    }

    @AfterEach
    void limpiar() {
        repo.findAll().stream()
            .filter(a -> DESCRIPCION_COPIA.equals(a.getDescripcion())
                      || DESCRIPCION_ORIGEN.equals(a.getDescripcion()))
            .forEach(repo::delete);
    }

    @Test
    void IT_POST_duplica_asiento_con_correlativo_nuevo_y_mismos_montos() throws Exception {
        mvc.perform(post("/duplicar-asiento/{id}", origenId)
                .with(user("admin.prueba@finantel.pe")))
            .andExpect(status().isOk());

        List<AsientoContable> copias = repo.findAll().stream()
            .filter(a -> DESCRIPCION_COPIA.equals(a.getDescripcion()))
            .toList();

        assertEquals(1, copias.size());
        AsientoContable copia = copias.get(0);
        assertNotEquals(correlativoOrigen, copia.getNumeroCorrelativo());
        assertEquals(100.00, copia.getDebe(), 0.001);
        assertEquals(100.00, copia.getHaber(), 0.001);
    }
}