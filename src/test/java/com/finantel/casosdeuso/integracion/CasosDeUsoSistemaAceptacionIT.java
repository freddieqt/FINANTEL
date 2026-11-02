package com.finantel.casosdeuso.integracion;

import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_accion.dominio.repository.AuditoriaRepository;
import com.finantel.realizar_copia_seguridad.dominio.entity.CopiaSeguridadEntity;
import com.finantel.realizar_copia_seguridad.dominio.repository.CopiaSeguridadRepository;
import com.finantel.restaurar_base_datos.dominio.repository.RestaurarBaseDatosRepository;
import com.finantel.tipo_cambio.aplicacion.TipoCambioSbsService;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class CasosDeUsoSistemaAceptacionIT {

    private static final String USUARIO = "pruebas-aceptacion@finantel.pe";
    private static final String ACCOUNT = "9898";

    @Autowired private MockMvc mvc;
    @Autowired private AsientoContableService asientoService;
    @Autowired private AsientoContableRepository asientoRepository;
    @Autowired private AuditoriaService auditoriaService;
    @Autowired private AuditoriaRepository auditoriaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ValidarCuentasRepository cuentaRepository;
    @MockitoBean private CopiaSeguridadRepository copiaSeguridadRepository;
    @MockitoBean private RestaurarBaseDatosRepository restauracionRepository;
    @MockitoBean private TipoCambioSbsService tipoCambioSbsService;

    private Integer asientoId;
    private final UsernamePasswordAuthenticationToken autenticacion =
        new UsernamePasswordAuthenticationToken(
            USUARIO,
            "N/A",
            List.of(
                new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"),
                new SimpleGrantedAuthority("ROLE_AUDITOR"),
                new SimpleGrantedAuthority("ROLE_CONTADOR_GENERAL")
            )
        );

    @BeforeEach
    void prepararDatosDelCaso() {
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
        when(copiaSeguridadRepository.findAll()).thenReturn(List.of());
        when(restauracionRepository.findAll()).thenReturn(List.of());
        when(tipoCambioSbsService.actualizar()).thenReturn(java.util.Map.of("compra", 3.75, "venta", 3.8));

        if (usuarioRepository.findByEmail(USUARIO).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNombre("Usuario de pruebas");
            usuario.setEmail(USUARIO);
            usuario.setPassword("password-de-prueba");
            usuario.setRol("Administrador");
            usuario.setEstado("Activo");
            usuario.setIntentos(0);
            usuarioRepository.saveAndFlush(usuario);
        }

        CuentaContable cuenta = cuentaRepository.findById(ACCOUNT).orElseGet(CuentaContable::new);
        cuenta.setCodigo(ACCOUNT);
        cuenta.setDescripcion("Cuenta de integración");
        cuenta.setClasificacion("Activo Corriente");
        cuenta.setTipo("Débito");
        cuenta.setEstado("Activa");
        cuentaRepository.saveAndFlush(cuenta);

        AsientoContable asiento = new AsientoContable();
        asiento.setTipo("Operación ordinaria");
        asiento.setDescripcion("Asiento de sistema y aceptación");
        asiento.setDebe(25.0);
        asiento.setHaber(25.0);
        asiento.setCuentaContable(ACCOUNT);
        asiento.setMoneda("PEN");
        asiento.setTipoCambio(1.0);
        asiento.setUsuario(USUARIO);
        asiento.setPeriodoId(1);
        asientoService.crear(asiento);
        asientoId = asiento.getId();
    }

    @AfterEach
    void limpiarAutenticacion() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("Prueba de sistema · {0}")
    @ParameterizedTest(name = "Sistema {0}")
    @MethodSource("casosDeUso")
    void cadaCasoDeUsoRespondeEnLaCapaHttp(String cu) throws Exception {
        ResultActions respuesta = ejecutar(cu);

        int codigoEsperado = cu.equals("CU02") ? 401 : cu.equals("CU19") ? 400 : 200;
        var response = respuesta.andReturn().getResponse();
        assertEquals(codigoEsperado, response.getStatus(),
            cu + " devolvió: " + response.getContentAsString());
    }

    @DisplayName("Prueba de integración · {0}")
    @ParameterizedTest(name = "Integración {0}")
    @MethodSource("casosDeUso")
    void cadaCasoDeUsoIntegraRutaServiciosYPersistencia(String cu) throws Exception {
        if (cu.equals("CU04")) {
            auditoriaService.registrar(USUARIO, "Integración CU04", "Prueba", null, "guardado");
        }
        if (cu.equals("CU15")) {
            mvc.perform(post("/historial-cambios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"" + USUARIO + "\",\"campo\":\"Integración CU15\","
                    + "\"anterior\":\"antiguo\",\"nuevo\":\"nuevo\"}"))
                .andExpect(status().isOk());
        }

        var respuesta = ejecutar(cu).andReturn().getResponse();
        int codigoEsperado = cu.equals("CU02") ? 401 : cu.equals("CU19") ? 400 : 200;
        assertEquals(codigoEsperado, respuesta.getStatus(),
            cu + " devolvió: " + respuesta.getContentAsString());

        String cuerpo = respuesta.getContentAsString();
        switch (cu) {
            case "CU01" -> assertTrue(asientoRepository.existsById(asientoId));
            case "CU02" -> assertTrue(auditoriaRepository.findByUsuario("no-existe@finantel.pe")
                .stream().anyMatch(a -> "Inicio de sesión fallido".equals(a.getAccion())));
            case "CU03" -> assertTrue(cuerpo.contains("admin"));
            case "CU04" -> assertTrue(cuerpo.contains("Integración CU04"));
            case "CU05" -> assertEquals("Inactivo", asientoRepository.findById(asientoId).orElseThrow().getEstado());
            case "CU06" -> assertTrue(asientoRepository.findAll().stream()
                .anyMatch(a -> "COPIA - Asiento de sistema y aceptación".equals(a.getDescripcion())));
            case "CU07" -> assertTrue(cuerpo.contains("Operación ordinaria"));
            case "CU08" -> assertTrue(cuerpo.contains("Asiento de sistema y aceptación"));
            case "CU09" -> assertTrue(cuerpo.startsWith("["));
            case "CU10" -> assertTrue(cuerpo.contains(ACCOUNT));
            case "CU11" -> assertTrue(cuerpo.contains("totalIngresos"));
            case "CU12" -> assertTrue(cuerpo.contains("Reporte PDF generado"));
            case "CU13" -> assertTrue(cuerpo.contains("totalAsientos"));
            case "CU14" -> assertTrue(cuerpo.contains("formato inválido"));
            case "CU15" -> assertTrue(cuerpo.contains("Integración CU15"));
            case "CU16" -> verify(copiaSeguridadRepository).findAll();
            case "CU17" -> verify(restauracionRepository).findAll();
            case "CU18" -> assertTrue(cuerpo.contains(USUARIO));
            case "CU19" -> assertTrue(cuerpo.contains("Error al procesar"));
            case "CU20" -> assertTrue(cuerpo.startsWith("["));
            case "CU21" -> assertTrue(cuerpo.contains("Asiento de sistema y aceptación"));
            case "CU22" -> {
                assertTrue(cuerpo.contains("\"compra\":3.75"));
                verify(tipoCambioSbsService).actualizar();
            }
            default -> throw new IllegalArgumentException("CU sin integración: " + cu);
        }
    }

    @DisplayName("Prueba de aceptación · {0}")
    @ParameterizedTest(name = "Aceptación {0}")
    @MethodSource("casosDeUso")
    void cadaCasoDeUsoCumpleElResultadoVisibleEsperado(String cu) throws Exception {
        String cuerpo = ejecutar(cu).andReturn().getResponse().getContentAsString();

        switch (cu) {
            case "CU01" -> assertTrue(cuerpo.contains("Asiento de sistema y aceptación"));
            case "CU02" -> assertTrue(cuerpo.contains("Credenciales incorrectas"));
            case "CU03" -> assertTrue(cuerpo.contains("admin"));
            case "CU04" -> assertTrue(cuerpo.startsWith("["));
            case "CU05" -> assertEquals("Inactivo", asientoRepository.findById(asientoId).orElseThrow().getEstado());
            case "CU06" -> {
                AsientoContable copia = asientoRepository.findAll().stream()
                    .filter(a -> "COPIA - Asiento de sistema y aceptación".equals(a.getDescripcion()))
                    .findFirst()
                    .orElseThrow();
                assertEquals(ACCOUNT, copia.getCuentaContable());
                assertEquals("PEN", copia.getMoneda());
                assertEquals(1, copia.getPeriodoId());
                assertEquals(25.0, copia.getDebe());
            }
            case "CU07" -> assertTrue(cuerpo.contains("Operación ordinaria"));
            case "CU08" -> assertTrue(cuerpo.contains("Asiento de sistema y aceptación"));
            case "CU09" -> assertTrue(cuerpo.startsWith("["));
            case "CU10" -> assertTrue(cuerpo.contains("9898"));
            case "CU11" -> assertTrue(cuerpo.contains("totalIngresos"));
            case "CU12" -> assertTrue(cuerpo.contains("Reporte PDF generado"));
            case "CU13" -> assertTrue(cuerpo.contains("totalAsientos"));
            case "CU14" -> assertTrue(cuerpo.contains("formato inválido"));
            case "CU15" -> assertTrue(cuerpo.startsWith("["));
            case "CU16" -> assertTrue(cuerpo.startsWith("["));
            case "CU17" -> assertTrue(cuerpo.startsWith("["));
            case "CU18" -> assertTrue(cuerpo.contains(USUARIO));
            case "CU19" -> assertTrue(cuerpo.contains("Error al procesar"));
            case "CU20" -> assertTrue(cuerpo.startsWith("["));
            case "CU21" -> assertTrue(cuerpo.contains("Asiento de sistema y aceptación"));
            case "CU22" -> assertTrue(cuerpo.contains("\"compra\":3.75"));
            default -> throw new IllegalArgumentException("Caso de uso sin aceptación: " + cu);
        }
    }

    private ResultActions ejecutar(String cu) throws Exception {
        AbstractMockHttpServletRequestBuilder<?> request = switch (cu) {
            case "CU01" -> get("/asientos");
            case "CU02" -> post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"no-existe@finantel.pe\",\"password\":\"incorrecta\"}");
            case "CU03" -> get("/admin");
            case "CU04" -> get("/auditoria");
            case "CU05" -> patch("/inactivar-asiento/{id}", asientoId)
                .principal(() -> USUARIO);
            case "CU06" -> get("/web/asientos/duplicar/{id}", asientoId)
                .principal(() -> USUARIO);
            case "CU07" -> get("/clasificar-asiento/tipos");
            case "CU08" -> get("/buscar-asiento/estado/Activo");
            case "CU09" -> get("/inconsistencias");
            case "CU10" -> get("/validar-cuentas");
            case "CU11" -> get("/estados-financieros/balance");
            case "CU12" -> get("/exportar-reportes/pdf");
            case "CU13" -> get("/reportes-graficos/resumen");
            case "CU14" -> multipart("/web/importar-excel")
                .file(new MockMultipartFile("archivo", "entrada.txt", "text/plain", "inválido".getBytes()))
                .principal(autenticacion);
            case "CU15" -> get("/historial-cambios");
            case "CU16" -> get("/copia-seguridad").principal(() -> USUARIO);
            case "CU17" -> get("/restaurar-bd");
            case "CU18" -> get("/usuarios");
            case "CU19" -> multipart("/ocr/leer")
                .file(new MockMultipartFile("archivo", "entrada.txt", "text/plain", "no es imagen".getBytes()));
            case "CU20" -> get("/conciliacion");
            case "CU21" -> get("/libro-contable");
            case "CU22" -> post("/web/tipocambio/actualizar");
            default -> throw new IllegalArgumentException("Caso de uso desconocido: " + cu);
        };
        return mvc.perform(request.with(authentication(autenticacion)));
    }

    private static Stream<String> casosDeUso() {
        return Stream.of(
            "CU01", "CU02", "CU03", "CU04", "CU05", "CU06", "CU07", "CU08",
            "CU09", "CU10", "CU11", "CU12", "CU13", "CU14", "CU15", "CU16",
            "CU17", "CU18", "CU19", "CU20", "CU21", "CU22"
        );
    }
}
