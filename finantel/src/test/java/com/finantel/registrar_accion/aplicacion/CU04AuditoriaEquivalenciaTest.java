package com.finantel.registrar_accion.aplicacion;

import com.finantel.registrar_accion.dominio.entity.AuditoriaAccion;
import com.finantel.registrar_accion.dominio.repository.AuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU04AuditoriaEquivalenciaTest {

    @Mock private AuditoriaRepository repo;
    private AuditoriaService servicio;

    @BeforeEach
    void setUp() {
        servicio = new AuditoriaService(repo);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Inicio de sesión",
        "Inicio de sesión fallido",
        "Crear asiento",
        "Inactivar asiento",
        "Modificar usuario"
    })
    void cada_clase_de_accion_se_guarda_con_su_tipo(String accion) {
        servicio.registrar("admin@finantel.pe", accion, "Objeto", null, null);
        ArgumentCaptor<AuditoriaAccion> captor = ArgumentCaptor.forClass(AuditoriaAccion.class);
        verify(repo).save(captor.capture());
        assertEquals(accion, captor.getValue().getAccion());
    }
}