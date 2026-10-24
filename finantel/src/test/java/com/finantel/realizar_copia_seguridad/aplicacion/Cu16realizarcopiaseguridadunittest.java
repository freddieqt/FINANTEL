package com.finantel.realizar_copia_seguridad.aplicacion;

import com.finantel.realizar_copia_seguridad.dominio.entity.CopiaSeguridadEntity;
import com.finantel.realizar_copia_seguridad.dominio.repository.CopiaSeguridadRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CU16RealizarCopiaSeguridadUnitTest {

    @Mock
    private CopiaSeguridadRepository repo;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private RealizarCopiaSeguridadService service;

    @Test
    void CP_U1_realizarCopia_guarda_una_copia_manual_completada() {
        String mensaje = service.realizarCopia("prueba@finantel.pe");

        ArgumentCaptor<CopiaSeguridadEntity> captor = ArgumentCaptor.forClass(CopiaSeguridadEntity.class);
        verify(repo).save(captor.capture());

        CopiaSeguridadEntity guardada = captor.getValue();
        assertEquals("prueba@finantel.pe", guardada.getUsuario());
        assertEquals("Manual", guardada.getTipo());
        assertEquals("Completado", guardada.getEstado());
        assertEquals("Copia de seguridad realizada correctamente", mensaje);
    }

    @Test
    void CP_U2_realizarCopia_registra_la_accion_en_auditoria() {
        service.realizarCopia("prueba@finantel.pe");

        verify(auditoriaService).registrar(
            eq("prueba@finantel.pe"), eq("Copia de seguridad"), eq("RespaldosBD"),
            isNull(), eq("Copia manual realizada"));
    }

    @Test
    void CP_U3_listar_devuelve_las_copias_del_repositorio() {
        when(repo.findAll()).thenReturn(List.of(new CopiaSeguridadEntity()));

        assertEquals(1, service.listar().size());
    }
}