package com.finantel.casosdeuso;

import com.finantel.autenticar_usuario.aplicacion.AutenticacionService;
import com.finantel.buscar_asiento.aplicacion.BuscarAsientoService;
import com.finantel.buscar_asiento.dominio.repository.BuscarAsientoRepository;
import com.finantel.clasificar_asiento.aplicacion.ClasificarAsientoService;
import com.finantel.conciliacion_bancaria.aplicacion.ConciliacionBancariaService;
import com.finantel.conciliacion_bancaria.dominio.entity.MovimientoBancario;
import com.finantel.conciliacion_bancaria.dominio.repository.ConciliacionBancariaRepository;
import com.finantel.detectar_inconsistencias.aplicacion.DetectarInconsistenciasService;
import com.finantel.detectar_inconsistencias.dominio.repository.InconsistenciaRepository;
import com.finantel.duplicar_asiento.aplicacion.DuplicarAsientoService;
import com.finantel.exportar_reportes.aplicacion.ExportarReportesService;
import com.finantel.exportar_reportes.dominio.repository.ExportarReportesRepository;
import com.finantel.generar_estados_financieros.aplicacion.GenerarEstadosFinancierosService;
import com.finantel.generar_estados_financieros.dominio.repository.EstadosFinancierosRepository;
import com.finantel.generar_libro_contable.aplicacion.GenerarLibroContableService;
import com.finantel.generar_libro_contable.dominio.repository.LibroContableRepository;
import com.finantel.gestionar_roles.aplicacion.GestorPermisos;
import com.finantel.importar_datos_excel.aplicacion.ImportarDatosExcelService;
import com.finantel.importar_datos_excel.dominio.repository.ImportarDatosExcelRepository;
import com.finantel.inactivar_asiento.aplicacion.InactivarAsientoService;
import com.finantel.ocr_documentos.aplicacion.OcrDocumentosService;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_accion.dominio.entity.AuditoriaAccion;
import com.finantel.registrar_accion.dominio.repository.AuditoriaRepository;
import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.registrar_historial_cambios.aplicacion.RegistrarHistorialCambiosService;
import com.finantel.registrar_usuario.aplicacion.UsuarioService;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;
import com.finantel.realizar_copia_seguridad.aplicacion.RealizarCopiaSeguridadService;
import com.finantel.realizar_copia_seguridad.dominio.repository.CopiaSeguridadRepository;
import com.finantel.restaurar_base_datos.aplicacion.RestaurarBaseDatosService;
import com.finantel.restaurar_base_datos.dominio.repository.RestaurarBaseDatosRepository;
import com.finantel.tipo_cambio.aplicacion.TipoCambioSbsService;
import com.finantel.validar_cuentas.aplicacion.ValidarCuentasService;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import com.finantel.visualizar_reportes_graficos.aplicacion.VisualizarReportesGraficosService;
import com.finantel.visualizar_reportes_graficos.dominio.repository.ReportesGraficosRepository;
import net.sourceforge.tess4j.Tesseract;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CasosDeUsoEquivalenciaLimitesTest {

    private static final String USER = "prueba@finantel.pe";
    private static final String API_URL = "https://tipo-cambio.test/api";

    @ParameterizedTest(name = "Equivalencia {0}")
    @MethodSource("casosDeUso")
    void verificaClasesDeEquivalencia(String cu) throws Exception {
        switch (cu) {
            case "CU01" -> {
                var repo = mock(AsientoContableRepository.class);
                var cuentas = mock(ValidarCuentasRepository.class);
                when(repo.count()).thenReturn(0L);
                when(repo.existsByNumeroCorrelativo(anyString())).thenReturn(false);
                when(cuentas.findById("1011")).thenReturn(Optional.of(cuenta("1011", "Activa")));
                var service = asientoService(repo, cuentas);
                AsientoContable valido = asientoValido();
                service.crear(valido);
                assertEquals("Activo", valido.getEstado());
                AsientoContable tipoNoValido = asientoValido();
                tipoNoValido.setTipo("Tipo no registrado");
                assertThrows(RuntimeException.class, () -> service.crear(tipoNoValido));
                verify(repo).save(valido);
                verify(repo, times(1)).save(any(AsientoContable.class));
            }
            case "CU02" -> {
                var service = new AutenticacionService();
                assertTrue(service.validarCredenciales(USER, "ClaveValida"));
                assertFalse(service.validarCredenciales(USER, ""));
            }
            case "CU03" -> {
                var service = new GestorPermisos();
                assertTrue(service.tienePermiso("Administrador", "eliminar"));
                assertFalse(service.tienePermiso("Auditor", "eliminar"));
            }
            case "CU04" -> {
                var repo = mock(AuditoriaRepository.class);
                var service = new AuditoriaService(repo);
                service.registrar(USER, "Edición", "Asientos", "Anterior", "Nuevo");
                var captor = ArgumentCaptor.forClass(AuditoriaAccion.class);
                verify(repo).save(captor.capture());
                assertEquals("Anterior", captor.getValue().getValorAnterior());
                assertEquals("Nuevo", captor.getValue().getValorNuevo());
            }
            case "CU05" -> {
                var repo = mock(AsientoContableRepository.class);
                var asiento = asiento(1, "Activo", 10, 10);
                when(repo.findById(1)).thenReturn(Optional.of(asiento));
                new InactivarAsientoService(repo, new AuditoriaService(mock(AuditoriaRepository.class))).inactivar(1);
                assertEquals("Inactivo", asiento.getEstado());
                when(repo.findById(2)).thenReturn(Optional.empty());
                assertThrows(RuntimeException.class, () -> new InactivarAsientoService(
                    repo, new AuditoriaService(mock(AuditoriaRepository.class))).inactivar(2));
            }
            case "CU06" -> {
                var repo = mock(AsientoContableRepository.class);
                var original = asiento(1, "Activo", 10, 10);
                original.setDescripcion("Original");
                when(repo.findById(1)).thenReturn(Optional.of(original));
                new DuplicarAsientoService(repo, new AuditoriaService(mock(AuditoriaRepository.class)))
                    .duplicar(1, USER);
                var captor = ArgumentCaptor.forClass(AsientoContable.class);
                verify(repo).save(captor.capture());
                assertEquals("COPIA - Original", captor.getValue().getDescripcion());
                assertEquals("Activo", captor.getValue().getEstado());
            }
            case "CU07" -> {
                var repo = mock(AsientoContableRepository.class);
                var apertura = asiento(1, "Activo", 10, 10);
                apertura.setTipo("Apertura");
                var cierre = asiento(2, "Activo", 10, 10);
                cierre.setTipo("Cierre");
                when(repo.findAll()).thenReturn(List.of(apertura, cierre));
                var service = new ClasificarAsientoService(repo);
                assertEquals(List.of(apertura), service.listarPorTipo("apertura"));
                assertTrue(service.listarPorTipo("Ajuste").isEmpty());
            }
            case "CU08" -> {
                var repo = mock(BuscarAsientoRepository.class);
                var encontrado = asiento(1, "Activo", 10, 10);
                when(repo.findByEstado("Activo")).thenReturn(List.of(encontrado));
                when(repo.findByEstado("Inactivo")).thenReturn(List.of());
                var service = new BuscarAsientoService(repo);
                assertEquals(List.of(encontrado), service.buscarPorEstado("Activo"));
                assertTrue(service.buscarPorEstado("Inactivo").isEmpty());
            }
            case "CU09" -> {
                var repo = mock(InconsistenciaRepository.class);
                when(repo.findById(1)).thenReturn(Optional.of(asiento(1, "Activo", 50, 50)));
                when(repo.findById(2)).thenReturn(Optional.of(asiento(2, "Activo", 50, 45)));
                var service = new DetectarInconsistenciasService(repo);
                assertEquals("Sin inconsistencias detectadas", service.detectarErrores(1));
                assertTrue(service.detectarErrores(2).contains("no está balanceado"));
            }
            case "CU10" -> {
                var repo = mock(ValidarCuentasRepository.class);
                when(repo.findByCodigo("1011")).thenReturn(Optional.of(cuenta("1011", "Activa")));
                when(repo.findByCodigo("1099")).thenReturn(Optional.of(cuenta("1099", "Inactiva")));
                when(repo.findByCodigo("9999")).thenReturn(Optional.empty());
                var service = new ValidarCuentasService(repo);
                assertEquals("Cuenta válida y activa", service.validarCuenta("1011"));
                assertEquals("Cuenta inactiva — no permitida", service.validarCuenta("1099"));
                assertEquals("Cuenta no encontrada", service.validarCuenta("9999"));
            }
            case "CU11" -> {
                var repo = mock(EstadosFinancierosRepository.class);
                when(repo.findTodosSaldos()).thenReturn(List.of(
                    new Object[] {"5001", "Venta", "Ingresos", 10.0},
                    new Object[] {"6001", "Costo", "Costos", 4.0}
                ));
                Map<String, Object> balance = new GenerarEstadosFinancierosService(repo).getBalanceGeneral();
                assertEquals(10.0, (double) balance.get("totalIngresos"));
                assertEquals(4.0, (double) balance.get("totalCostos"));
            }
            case "CU12" -> {
                var repo = mock(ExportarReportesRepository.class);
                when(repo.findAll()).thenReturn(List.of(asiento(1, "Activo", 10, 10)));
                var service = new ExportarReportesService(repo);
                assertTrue(service.exportarPDF().contains("PDF"));
                assertTrue(service.exportarExcel().contains("Excel"));
            }
            case "CU13" -> {
                var repo = mock(ReportesGraficosRepository.class);
                var apertura = asiento(1, "Activo", 10, 10);
                apertura.setTipo("Apertura");
                var cierre = asiento(2, "Activo", 20, 20);
                cierre.setTipo("Cierre");
                when(repo.findAll()).thenReturn(List.of(apertura, cierre));
                assertEquals(Map.of("Apertura", 1L, "Cierre", 1L),
                    new VisualizarReportesGraficosService(repo).agruparPorTipo());
            }
            case "CU14" -> {
                var repo = mock(ImportarDatosExcelRepository.class);
                var service = new ImportarDatosExcelService(repo,
                    new AuditoriaService(mock(AuditoriaRepository.class)));
                var csv = csv("Apertura,Saldo,1,1,1011,General,PEN\n");
                assertEquals(true, service.importar(csv, USER).get("exito"));
                var txt = new MockMultipartFile("archivo", "datos.txt", "text/plain", new byte[] {1});
                assertEquals(false, service.importar(txt, USER).get("exito"));
                verify(repo).saveAll(anyList());
            }
            case "CU15" -> {
                var repo = mock(com.finantel.registrar_historial_cambios.dominio.repository.HistorialCambiosRepository.class);
                new RegistrarHistorialCambiosService(repo).registrar(USER, "estado", "Activo", "Inactivo");
                var captor = ArgumentCaptor.forClass(
                    com.finantel.registrar_historial_cambios.dominio.entity.HistorialCambio.class);
                verify(repo).save(captor.capture());
                assertEquals("Activo", captor.getValue().getValorAnterior());
                assertEquals("Inactivo", captor.getValue().getValorNuevo());
            }
            case "CU16" -> {
                var repo = mock(CopiaSeguridadRepository.class);
                var service = new RealizarCopiaSeguridadService(repo,
                    new AuditoriaService(mock(AuditoriaRepository.class)));
                assertTrue(service.realizarCopia(USER).contains("correctamente"));
                var captor = ArgumentCaptor.forClass(
                    com.finantel.realizar_copia_seguridad.dominio.entity.CopiaSeguridadEntity.class);
                verify(repo).save(captor.capture());
                assertEquals("Completado", captor.getValue().getEstado());
            }
            case "CU17" -> {
                var repo = mock(RestaurarBaseDatosRepository.class);
                var punto = new com.finantel.realizar_copia_seguridad.dominio.entity.CopiaSeguridadEntity();
                punto.setFecha(java.time.LocalDateTime.of(2026, 1, 1, 0, 0));
                when(repo.findById(1)).thenReturn(Optional.of(punto));
                when(repo.findById(2)).thenReturn(Optional.empty());
                var service = new RestaurarBaseDatosService(repo);
                assertTrue(service.restaurar(1, USER).contains("restaurada"));
                assertEquals("Punto de restauración no encontrado", service.restaurar(2, USER));
            }
            case "CU18" -> {
                var repo = mock(UsuarioRepository.class);
                var encoder = mock(PasswordEncoder.class);
                when(repo.findByEmail(USER)).thenReturn(Optional.empty());
                when(encoder.encode("Clave123")).thenReturn("hash");
                Usuario usuario = usuario(USER, "Clave123", "Auditor", "12345678", "912345678");
                new UsuarioService(repo, encoder, new AuditoriaService(mock(AuditoriaRepository.class))).crear(usuario);
                assertEquals("hash", usuario.getPassword());
                verify(repo).save(usuario);
            }
            case "CU19" -> {
                var image = png("imagen.png");
                try (var tess = mockConstruction(Tesseract.class,
                    (mock, context) -> when(mock.doOCR(any(java.io.File.class))).thenReturn("Texto reconocido"))) {
                    assertEquals("Texto reconocido", new OcrDocumentosService().extraerTexto(image));
                    assertEquals(1, tess.constructed().size());
                }
                var noImage = new MockMultipartFile("archivo", "documento.txt", "text/plain", "texto".getBytes());
                assertThrows(IllegalArgumentException.class, () -> new OcrDocumentosService().extraerTexto(noImage));
            }
            case "CU20" -> {
                var movimientos = mock(ConciliacionBancariaRepository.class);
                var asientos = mock(AsientoContableRepository.class);
                var movimiento = new MovimientoBancario();
                movimiento.setMonto(100.0);
                when(movimientos.findAll()).thenReturn(List.of(movimiento));
                when(asientos.findAll()).thenReturn(List.of(asiento(1, "Activo", 100, 100)));
                var service = new ConciliacionBancariaService(movimientos, asientos,
                    new AuditoriaService(mock(AuditoriaRepository.class)));
                assertEquals("Conciliado", service.conciliar("banco.csv").get("estado"));
                movimiento.setMonto(102.0);
                assertEquals("Con diferencias", service.conciliar("banco.csv").get("estado"));
            }
            case "CU21" -> {
                var repo = mock(LibroContableRepository.class);
                var item = asiento(1, "Activo", 10, 10);
                when(repo.findAll()).thenReturn(List.of(item));
                assertEquals(List.of(item), new GenerarLibroContableService(repo).generarLibro());
            }
            case "CU22" -> {
                var rest = new RestTemplate();
                var server = MockRestServiceServer.createServer(rest);
                var jdbc = mock(JdbcTemplate.class);
                server.expect(requestTo(API_URL)).andRespond(
                    withSuccess("{\"compra\":3.7,\"venta\":3.8}", MediaType.APPLICATION_JSON));
                var tasas = new TipoCambioSbsService(rest, jdbc, API_URL).actualizar();
                assertEquals(3.7, tasas.get("compra"));
                assertEquals(3.8, tasas.get("venta"));
                server.verify();
            }
            default -> fail("CU no cubierto: " + cu);
        }
    }

    @ParameterizedTest(name = "Límite {0}")
    @MethodSource("casosDeUso")
    void verificaValoresLimite(String cu) throws Exception {
        switch (cu) {
            case "CU01" -> {
                var repo = mock(AsientoContableRepository.class);
                var cuentas = mock(ValidarCuentasRepository.class);
                when(repo.count()).thenReturn(0L);
                when(repo.existsByNumeroCorrelativo(anyString())).thenReturn(false);
                when(cuentas.findById("1011")).thenReturn(Optional.of(cuenta("1011", "Activa")));
                var centimo = asientoValido();
                centimo.setDebe(0.01);
                centimo.setHaber(0.01);
                asientoService(repo, cuentas).crear(centimo);
                assertThrows(RuntimeException.class, () -> {
                    var cero = asientoValido();
                    cero.setDebe(0.0);
                    cero.setHaber(0.0);
                    asientoService(repo, cuentas).crear(cero);
                });
            }
            case "CU02" -> {
                var service = new AutenticacionService();
                assertFalse(service.validarCredenciales(null, "x"));
                assertFalse(service.validarCredenciales("", "x"));
                assertFalse(service.validarCredenciales(USER, ""));
            }
            case "CU03" -> {
                var service = new GestorPermisos();
                assertFalse(service.tienePermiso(null, "ver"));
                assertFalse(service.tienePermiso("Administrador", null));
                assertFalse(service.tienePermiso("Desconocido", "ver"));
            }
            case "CU04" -> {
                var repo = mock(AuditoriaRepository.class);
                new AuditoriaService(repo).registrar("", "", "", null, "");
                var captor = ArgumentCaptor.forClass(AuditoriaAccion.class);
                verify(repo).save(captor.capture());
                assertEquals("", captor.getValue().getUsuario());
                assertNull(captor.getValue().getValorAnterior());
            }
            case "CU05" -> {
                var repo = mock(AsientoContableRepository.class);
                when(repo.findById(1)).thenReturn(Optional.of(asiento(1, "Cerrado", 1, 1)));
                var service = new InactivarAsientoService(repo, new AuditoriaService(mock(AuditoriaRepository.class)));
                assertThrows(RuntimeException.class, () -> service.inactivar(1));
                verify(repo, never()).save(any(AsientoContable.class));
            }
            case "CU06" -> {
                var repo = mock(AsientoContableRepository.class);
                when(repo.findById(Integer.MAX_VALUE)).thenReturn(Optional.empty());
                var service = new DuplicarAsientoService(repo,
                    new AuditoriaService(mock(AuditoriaRepository.class)));
                assertThrows(RuntimeException.class, () -> service.duplicar(Integer.MAX_VALUE, USER));
                verify(repo, never()).save(any(AsientoContable.class));
            }
            case "CU07" -> {
                var repo = mock(AsientoContableRepository.class);
                when(repo.findAll()).thenReturn(List.of());
                assertTrue(new ClasificarAsientoService(repo).listarPorTipo("").isEmpty());
            }
            case "CU08" -> {
                var repo = mock(BuscarAsientoRepository.class);
                when(repo.findByEstado("")).thenReturn(List.of());
                assertTrue(new BuscarAsientoService(repo).buscarPorEstado("").isEmpty());
                verify(repo).findByEstado("");
            }
            case "CU09" -> {
                var repo = mock(InconsistenciaRepository.class);
                when(repo.findById(1)).thenReturn(Optional.of(asiento(1, "Activo", 100, 99.99)));
                when(repo.findById(2)).thenReturn(Optional.of(asiento(2, "Activo", 100, 99.98)));
                var service = new DetectarInconsistenciasService(repo);
                assertEquals("Sin inconsistencias detectadas", service.detectarErrores(1));
                assertTrue(service.detectarErrores(2).contains("no está balanceado"));
            }
            case "CU10" -> {
                var repo = mock(ValidarCuentasRepository.class);
                when(repo.findByCodigo("")).thenReturn(Optional.empty());
                var service = new ValidarCuentasService(repo);
                assertEquals("Cuenta no encontrada", service.validarCuenta(""));
                var invalida = cuenta("A", "Activa");
                invalida.setClasificacion("No válida");
                assertThrows(RuntimeException.class, () -> service.crear(invalida));
            }
            case "CU11" -> {
                var repo = mock(EstadosFinancierosRepository.class);
                when(repo.findTodosSaldos()).thenReturn(List.of());
                var balance = new GenerarEstadosFinancierosService(repo).getBalanceGeneral();
                assertEquals(0.0, (double) balance.get("totalIngresos"));
                assertEquals(0.0, (double) balance.get("utilidadNeta"));
            }
            case "CU12" -> {
                var repo = mock(ExportarReportesRepository.class);
                when(repo.findAll()).thenReturn(List.of());
                var service = new ExportarReportesService(repo);
                assertTrue(service.exportarPDF().contains("0 asientos"));
                assertTrue(service.exportarExcel().contains("0 asientos"));
            }
            case "CU13" -> {
                var repo = mock(ReportesGraficosRepository.class);
                when(repo.findAll()).thenReturn(List.of());
                var service = new VisualizarReportesGraficosService(repo);
                assertEquals(0, service.generarResumen().get("totalAsientos"));
                assertTrue(service.agruparPorTipo().isEmpty());
            }
            case "CU14" -> {
                var repo = mock(ImportarDatosExcelRepository.class);
                var service = new ImportarDatosExcelService(repo,
                    new AuditoriaService(mock(AuditoriaRepository.class)));
                assertEquals(false, service.importar(null, USER).get("exito"));
                var empty = new MockMultipartFile("archivo", "vacio.csv", "text/csv", new byte[0]);
                assertEquals(false, service.importar(empty, USER).get("exito"));
                var headerOnly = csv("");
                assertEquals(0, service.importar(headerOnly, USER).get("total"));
            }
            case "CU15" -> {
                var repo = mock(com.finantel.registrar_historial_cambios.dominio.repository.HistorialCambiosRepository.class);
                new RegistrarHistorialCambiosService(repo).registrar(USER, "alta", null, "creado");
                var captor = ArgumentCaptor.forClass(
                    com.finantel.registrar_historial_cambios.dominio.entity.HistorialCambio.class);
                verify(repo).save(captor.capture());
                assertNull(captor.getValue().getValorAnterior());
            }
            case "CU16" -> {
                var repo = mock(com.finantel.realizar_copia_seguridad.dominio.repository.CopiaSeguridadRepository.class);
                doThrow(new IllegalStateException("BD no disponible")).when(repo).save(any());
                var service = new RealizarCopiaSeguridadService(repo,
                    new AuditoriaService(mock(AuditoriaRepository.class)));
                assertThrows(IllegalStateException.class, () -> service.realizarCopia(USER));
            }
            case "CU17" -> {
                var repo = mock(RestaurarBaseDatosRepository.class);
                when(repo.findById(Integer.MIN_VALUE)).thenReturn(Optional.empty());
                assertEquals("Punto de restauración no encontrado",
                    new RestaurarBaseDatosService(repo).restaurar(Integer.MIN_VALUE, USER));
            }
            case "CU18" -> {
                var repo = mock(UsuarioRepository.class);
                when(repo.findByEmail(USER)).thenReturn(Optional.empty());
                var user = usuario(USER, "123456", "Auditor", "1234567", "912345678");
                var service = new UsuarioService(repo, mock(PasswordEncoder.class),
                    new AuditoriaService(mock(AuditoriaRepository.class)));
                assertThrows(RuntimeException.class, () -> service.crear(user));
                verify(repo, never()).save(any(Usuario.class));
            }
            case "CU19" -> {
                var service = new OcrDocumentosService();
                var empty = new MockMultipartFile("archivo", "vacio.png", "image/png", new byte[0]);
                assertThrows(IllegalArgumentException.class, () -> service.extraerTexto(empty));
                var badImage = new MockMultipartFile("archivo", "texto.txt", "text/plain", new byte[] {1});
                assertThrows(IllegalArgumentException.class, () -> service.extraerTexto(badImage));
            }
            case "CU20" -> {
                var movimientos = mock(ConciliacionBancariaRepository.class);
                var asientos = mock(AsientoContableRepository.class);
                var movimiento = new MovimientoBancario();
                movimiento.setMonto(100.009);
                when(movimientos.findAll()).thenReturn(List.of(movimiento));
                when(asientos.findAll()).thenReturn(List.of(asiento(1, "Activo", 100, 100)));
                var service = new ConciliacionBancariaService(movimientos, asientos,
                    new AuditoriaService(mock(AuditoriaRepository.class)));
                assertEquals("Conciliado", service.conciliar("limite.csv").get("estado"));
                movimiento.setMonto(100.01);
                assertEquals("Con diferencias", service.conciliar("limite.csv").get("estado"));
            }
            case "CU21" -> {
                var repo = mock(LibroContableRepository.class);
                when(repo.findAll()).thenReturn(List.of());
                assertTrue(new GenerarLibroContableService(repo).generarLibro().isEmpty());
            }
            case "CU22" -> {
                for (String body : List.of(
                    "{}",
                    "{\"compra\":0,\"venta\":3.8}",
                    "{\"compra\":3.7,\"venta\":-1}",
                    "{\"compra\":1e309,\"venta\":3.8}"
                )) {
                    var rest = new RestTemplate();
                    var server = MockRestServiceServer.createServer(rest);
                    var jdbc = mock(JdbcTemplate.class);
                    server.expect(requestTo(API_URL)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
                    var service = new TipoCambioSbsService(rest, jdbc, API_URL);
                    assertThrows(IllegalStateException.class, service::actualizar);
                    verify(jdbc, never()).update(anyString(), any(Object[].class));
                }
            }
            default -> fail("CU sin prueba de límites: " + cu);
        }
    }

    private static Stream<String> casosDeUso() {
        return Stream.of(
            "CU01", "CU02", "CU03", "CU04", "CU05", "CU06", "CU07", "CU08",
            "CU09", "CU10", "CU11", "CU12", "CU13", "CU14", "CU15", "CU16",
            "CU17", "CU18", "CU19", "CU20", "CU21", "CU22"
        );
    }

    private static AsientoContableService asientoService(
        AsientoContableRepository repo,
        ValidarCuentasRepository cuentas
    ) {
        return new AsientoContableService(repo, mock(AuditoriaService.class), cuentas);
    }

    private static AsientoContable asientoValido() {
        AsientoContable asiento = asiento(1, "Activo", 100, 100);
        asiento.setTipo("Operación ordinaria");
        asiento.setDescripcion("Asiento equivalente");
        asiento.setCuentaContable("1011");
        asiento.setMoneda("PEN");
        return asiento;
    }

    private static AsientoContable asiento(int id, String estado, double debe, double haber) {
        AsientoContable asiento = new AsientoContable();
        asiento.setId(id);
        asiento.setEstado(estado);
        asiento.setDebe(debe);
        asiento.setHaber(haber);
        asiento.setTipo("Operación ordinaria");
        asiento.setDescripcion("Asiento de prueba");
        asiento.setCuentaContable("1011");
        asiento.setMoneda("PEN");
        asiento.setTipoCambio(1.0);
        asiento.setPeriodoId(1);
        asiento.setUsuario(USER);
        return asiento;
    }

    private static CuentaContable cuenta(String codigo, String estado) {
        CuentaContable cuenta = new CuentaContable();
        cuenta.setCodigo(codigo);
        cuenta.setEstado(estado);
        cuenta.setClasificacion("Activo Corriente");
        cuenta.setTipo("Débito");
        return cuenta;
    }

    private static Usuario usuario(String email, String password, String rol, String dni, String telefono) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de prueba");
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setRol(rol);
        usuario.setDni(dni);
        usuario.setTelefono(telefono);
        return usuario;
    }

    private static MockMultipartFile csv(String rows) {
        String content = "tipo,descripcion,debe,haber,cuenta,centro,moneda\n" + rows;
        return new MockMultipartFile("archivo", "asientos.csv", "text/csv",
            content.getBytes(StandardCharsets.UTF_8));
    }

    private static MockMultipartFile png(String filename) throws Exception {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", content);
        return new MockMultipartFile("archivo", filename, "image/png", content.toByteArray());
    }
}
