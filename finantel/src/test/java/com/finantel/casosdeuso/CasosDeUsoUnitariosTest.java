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
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_accion.dominio.entity.AuditoriaAccion;
import com.finantel.registrar_accion.dominio.repository.AuditoriaRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.registrar_historial_cambios.aplicacion.RegistrarHistorialCambiosService;
import com.finantel.registrar_historial_cambios.dominio.entity.HistorialCambio;
import com.finantel.registrar_historial_cambios.dominio.repository.HistorialCambiosRepository;
import com.finantel.registrar_usuario.aplicacion.UsuarioService;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;
import com.finantel.realizar_copia_seguridad.aplicacion.RealizarCopiaSeguridadService;
import com.finantel.realizar_copia_seguridad.dominio.entity.CopiaSeguridadEntity;
import com.finantel.realizar_copia_seguridad.dominio.repository.CopiaSeguridadRepository;
import com.finantel.restaurar_base_datos.aplicacion.RestaurarBaseDatosService;
import com.finantel.restaurar_base_datos.dominio.repository.RestaurarBaseDatosRepository;
import com.finantel.validar_cuentas.aplicacion.ValidarCuentasService;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import com.finantel.visualizar_reportes_graficos.aplicacion.VisualizarReportesGraficosService;
import com.finantel.visualizar_reportes_graficos.dominio.repository.ReportesGraficosRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CasosDeUsoUnitariosTest {

    @Mock private AuditoriaRepository auditoriaRepository;
    @Mock private AsientoContableRepository asientoRepository;
    @Mock private BuscarAsientoRepository buscarRepository;
    @Mock private InconsistenciaRepository inconsistenciaRepository;
    @Mock private ValidarCuentasRepository cuentasRepository;
    @Mock private EstadosFinancierosRepository estadosRepository;
    @Mock private ExportarReportesRepository reportesRepository;
    @Mock private LibroContableRepository libroRepository;
    @Mock private ReportesGraficosRepository graficosRepository;
    @Mock private ImportarDatosExcelRepository importRepository;
    @Mock private HistorialCambiosRepository historialRepository;
    @Mock private CopiaSeguridadRepository copiaRepository;
    @Mock private RestaurarBaseDatosRepository restaurarRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ConciliacionBancariaRepository movimientosRepository;

    @Test
    void cu02_credencialesValidasYLimitesDeCampos() {
        AutenticacionService service = new AutenticacionService();

        assertTrue(service.validarCredenciales("a@finantel.pe", "clave"));
        assertFalse(service.validarCredenciales("", "clave"));
        assertFalse(service.validarCredenciales("a@finantel.pe", ""));
        assertFalse(service.validarCredenciales(null, "clave"));
        assertTrue(service.estaActivo("Activo"));
        assertTrue(service.estaActivo("activo"));
        assertFalse(service.estaActivo("Inactivo"));
    }

    @Test
    void cu03_permisosPorRolYAccion() {
        GestorPermisos permisos = new GestorPermisos();

        assertTrue(permisos.tienePermiso("Administrador", "eliminar"));
        assertTrue(permisos.tienePermiso("Contador General", "crear"));
        assertFalse(permisos.tienePermiso("Contador General", "eliminar"));
        assertTrue(permisos.tienePermiso("Auditor", "ver"));
        assertFalse(permisos.tienePermiso("Auditor", "editar"));
        assertFalse(permisos.tienePermiso("Desconocido", "ver"));
        assertFalse(permisos.tienePermiso(null, "ver"));
        assertFalse(permisos.tienePermiso("Administrador", null));
    }

    @Test
    void cu04_registrarAccionPersisteValoresYFecha() {
        AuditoriaService service = new AuditoriaService(auditoriaRepository);

        service.registrar("usuario@finantel.pe", "Creación", "Asientos", null, "Alta");

        ArgumentCaptor<AuditoriaAccion> captor = ArgumentCaptor.forClass(AuditoriaAccion.class);
        verify(auditoriaRepository).save(captor.capture());
        assertEquals("usuario@finantel.pe", captor.getValue().getUsuario());
        assertEquals("Creación", captor.getValue().getAccion());
        assertEquals("Asientos", captor.getValue().getObjeto());
        assertNull(captor.getValue().getValorAnterior());
        assertEquals("Alta", captor.getValue().getValorNuevo());
        assertNotNull(captor.getValue().getFecha());
    }

    @Test
    void cu05_inactivarAsientoRegistraEstadoYRechazaPeriodoCerrado() {
        AsientoContable asiento = asiento(9, "Activo", 50, 50);
        when(asientoRepository.findById(9)).thenReturn(Optional.of(asiento));
        InactivarAsientoService service = new InactivarAsientoService(asientoRepository,
            new AuditoriaService(auditoriaRepository));

        service.inactivar(9);

        assertEquals("Inactivo", asiento.getEstado());
        verify(asientoRepository).save(asiento);
        verify(auditoriaRepository).save(any(AuditoriaAccion.class));

        asiento.setEstado("Cerrado");
        assertThrows(RuntimeException.class, () -> service.inactivar(9));
        verify(asientoRepository, times(1)).save(any(AsientoContable.class));
    }

    @Test
    void cu06_duplicarAsientoGeneraCopiaSinModificarOriginal() {
        AsientoContable original = asiento(2, "Activo", 80, 80);
        original.setDescripcion("Asiento fuente");
        when(asientoRepository.findById(2)).thenReturn(Optional.of(original));
        DuplicarAsientoService service = new DuplicarAsientoService(asientoRepository,
            new AuditoriaService(auditoriaRepository));

        service.duplicar(2, "editor@finantel.pe");

        ArgumentCaptor<AsientoContable> captor = ArgumentCaptor.forClass(AsientoContable.class);
        verify(asientoRepository).save(captor.capture());
        assertEquals("COPIA - Asiento fuente", captor.getValue().getDescripcion());
        assertEquals("Activo", captor.getValue().getEstado());
        assertEquals("editor@finantel.pe", captor.getValue().getUsuario());
        assertEquals("Asiento fuente", original.getDescripcion());
        verify(auditoriaRepository).save(any(AuditoriaAccion.class));
    }

    @Test
    void cu06_asientoInexistenteNoGeneraCopia() {
        when(asientoRepository.findById(404)).thenReturn(Optional.empty());
        DuplicarAsientoService service = new DuplicarAsientoService(asientoRepository,
            new AuditoriaService(auditoriaRepository));

        assertThrows(RuntimeException.class, () -> service.duplicar(404, "editor@finantel.pe"));
        verify(asientoRepository, never()).save(any(AsientoContable.class));
        verify(auditoriaRepository, never()).save(any(AuditoriaAccion.class));
    }

    @Test
    void cu07_clasificarFiltraSinDistinguirMayusculasYExponeTipos() {
        AsientoContable ordinario = asiento(1, "Activo", 10, 10);
        ordinario.setTipo("Operación ordinaria");
        AsientoContable apertura = asiento(2, "Activo", 20, 20);
        apertura.setTipo("Apertura");
        when(asientoRepository.findAll()).thenReturn(List.of(ordinario, apertura));
        ClasificarAsientoService service = new ClasificarAsientoService(asientoRepository);

        assertEquals(List.of(ordinario), service.listarPorTipo("operación ordinaria"));
        assertTrue(service.getTiposDisponibles().contains("Ajuste"));
    }

    @Test
    void cu08_buscarAsientosDelegaFiltrosAlRepositorioIncluyendoResultadosVacios() {
        AsientoContable asiento = asiento(1, "Activo", 20, 20);
        when(buscarRepository.findByTipo("Apertura")).thenReturn(List.of(asiento));
        when(buscarRepository.findByUsuario("sin-resultados")).thenReturn(List.of());
        BuscarAsientoService service = new BuscarAsientoService(buscarRepository);

        assertEquals(List.of(asiento), service.buscarPorTipo("Apertura"));
        assertTrue(service.buscarPorUsuario("sin-resultados").isEmpty());
        verify(buscarRepository).findByTipo("Apertura");
        verify(buscarRepository).findByUsuario("sin-resultados");
    }

    @Test
    void cu08_filtroVacioSeEntregaAlRepositorioSinCrearResultados() {
        when(buscarRepository.findByEstado("")).thenReturn(List.of());
        BuscarAsientoService service = new BuscarAsientoService(buscarRepository);

        assertTrue(service.buscarPorEstado("").isEmpty());
        verify(buscarRepository).findByEstado("");
    }

    @Test
    void cu09_detectarInconsistenciasBalanceYValorLimite() {
        AsientoContable balanceado = asiento(1, "Activo", 100, 100);
        AsientoContable descuadrado = asiento(2, "Activo", 100, 99.98);
        when(inconsistenciaRepository.findById(1)).thenReturn(Optional.of(balanceado));
        when(inconsistenciaRepository.findById(2)).thenReturn(Optional.of(descuadrado));
        when(inconsistenciaRepository.findAll()).thenReturn(List.of(balanceado, descuadrado));
        DetectarInconsistenciasService service = new DetectarInconsistenciasService(inconsistenciaRepository);

        assertEquals("Sin inconsistencias detectadas", service.detectarErrores(1));
        assertTrue(service.detectarErrores(2).contains("no está balanceado"));
        assertEquals(1, service.detectarTodos().size());
        assertThrows(RuntimeException.class, () -> service.detectarErrores(404));
    }

    @Test
    void cu09_diferenciaDeUnCentimoSeConsideraDentroDeTolerancia() {
        AsientoContable asiento = asiento(3, "Activo", 100.0, 99.99);
        when(inconsistenciaRepository.findById(3)).thenReturn(Optional.of(asiento));
        DetectarInconsistenciasService service = new DetectarInconsistenciasService(inconsistenciaRepository);

        assertEquals("Sin inconsistencias detectadas", service.detectarErrores(3));
    }

    @Test
    void cu10_validarCuentaYRechazarCodigoOClasificacionInvalidos() {
        CuentaContable cuenta = cuenta("1011", "Activa");
        when(cuentasRepository.findByCodigo("1011")).thenReturn(Optional.of(cuenta));
        when(cuentasRepository.findByCodigo("9999")).thenReturn(Optional.empty());
        when(cuentasRepository.findByEstado("Activo")).thenReturn(List.of(cuenta("1012", "Activo")));
        when(cuentasRepository.findByEstado("Activa")).thenReturn(List.of(cuenta));
        ValidarCuentasService service = new ValidarCuentasService(cuentasRepository);

        assertEquals("Cuenta válida y activa", service.validarCuenta("1011"));
        assertEquals("Cuenta no encontrada", service.validarCuenta("9999"));
        assertEquals(2, service.listarCuentasActivas().size());

        CuentaContable invalida = cuenta("10A", "Activa");
        invalida.setClasificacion("No válida");
        assertThrows(RuntimeException.class, () -> service.crear(invalida));
        verify(cuentasRepository, never()).save(any(CuentaContable.class));
    }

    @Test
    void cu10_cuentaInactivaNoSeValidaComoPermitida() {
        when(cuentasRepository.findByCodigo("1099")).thenReturn(Optional.of(cuenta("1099", "Inactiva")));
        ValidarCuentasService service = new ValidarCuentasService(cuentasRepository);

        assertEquals("Cuenta inactiva — no permitida", service.validarCuenta("1099"));
    }

    @Test
    void cu11_estadoFinancieroCalculaTotalesYUtilidadNeta() {
        when(estadosRepository.findTodosSaldos()).thenReturn(List.of(
            new Object[] {"5001", "Ingresos", "Ingresos", 100.0},
            new Object[] {"6001", "Costos", "Costos", 25.0},
            new Object[] {"6101", "Gastos", "Gastos", 15.0},
            new Object[] {"6501", "Intereses", "Gastos financieros", 5.0}
        ));
        GenerarEstadosFinancierosService service = new GenerarEstadosFinancierosService(estadosRepository);

        Map<String, Object> resultado = service.getBalanceGeneral();

        assertEquals(100.0, (double) resultado.get("totalIngresos"));
        assertEquals(25.0, (double) resultado.get("totalCostos"));
        assertEquals(55.0, (double) resultado.get("utilidadNeta"));
    }

    @Test
    void cu11_balanceGeneralRealCalculaPorcentajesYToleraSaldoNulo() {
        when(estadosRepository.findTodosSaldos()).thenReturn(List.of(
            new Object[] {"1011", "Caja", "Activo Corriente", 75.0},
            new Object[] {"1012", "Banco", "Activo Corriente", 25.0},
            new Object[] {"2011", "Proveedores", "Pasivo Corriente", null}
        ));
        GenerarEstadosFinancierosService service = new GenerarEstadosFinancierosService(estadosRepository);

        Map<String, Object> resultado = service.getBalanceGeneralReal();

        assertEquals(100.0, (double) resultado.get("totalActivo"));
        assertEquals(0.0, (double) resultado.get("totalPasivo"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> activos = (List<Map<String, Object>>) resultado.get("activos");
        assertEquals(75.0, (double) activos.get(0).get("porcentaje"));
    }

    @Test
    void cu11_sinSaldosDevuelveTotalesCero() {
        when(estadosRepository.findTodosSaldos()).thenReturn(List.of());
        GenerarEstadosFinancierosService service = new GenerarEstadosFinancierosService(estadosRepository);

        Map<String, Object> resultado = service.getBalanceGeneral();

        assertEquals(0.0, (double) resultado.get("totalIngresos"));
        assertEquals(0.0, (double) resultado.get("utilidadNeta"));
    }

    @Test
    void cu12_exportarReportesIncluyeCantidadInclusoSinAsientos() {
        when(reportesRepository.findAll()).thenReturn(List.of());
        ExportarReportesService service = new ExportarReportesService(reportesRepository);

        assertEquals("Reporte PDF generado con 0 asientos contables", service.exportarPDF());
        assertEquals("Reporte Excel generado con 0 asientos contables", service.exportarExcel());
    }

    @Test
    void cu13_resumenGraficoSumaMontosYAgrupaTipoAusente() {
        AsientoContable primero = asiento(1, "Activo", 10, 10);
        primero.setTipo("Apertura");
        AsientoContable segundo = asiento(2, "Activo", 20, 15);
        segundo.setTipo(null);
        when(graficosRepository.findAll()).thenReturn(List.of(primero, segundo));
        VisualizarReportesGraficosService service = new VisualizarReportesGraficosService(graficosRepository);

        Map<String, Object> resumen = service.generarResumen();

        assertEquals(2, resumen.get("totalAsientos"));
        assertEquals(30.0, (double) resumen.get("totalDebe"));
        assertEquals(25.0, (double) resumen.get("totalHaber"));
        assertEquals(-5.0, (double) resumen.get("resultado"));
        assertEquals(1L, service.agruparPorTipo().get("Sin tipo"));
    }

    @Test
    void cu13_sinAsientosLosIndicadoresSonCero() {
        when(graficosRepository.findAll()).thenReturn(List.of());
        VisualizarReportesGraficosService service = new VisualizarReportesGraficosService(graficosRepository);

        Map<String, Object> resumen = service.generarResumen();

        assertEquals(0, resumen.get("totalAsientos"));
        assertEquals(0.0, (double) resumen.get("totalDebe"));
        assertTrue(service.agruparPorTipo().isEmpty());
    }

    @Test
    void cu14_importarCsvValidoYRechazarArchivoVacioOFormatoNoPermitido() {
        ImportarDatosExcelService service = new ImportarDatosExcelService(importRepository,
            new AuditoriaService(auditoriaRepository));
        byte[] contenido = ("tipo,descripcion,debe,haber,cuenta,centro,moneda\n"
            + "Apertura,Saldo inicial,10,10,1011,General,PEN\n").getBytes(StandardCharsets.UTF_8);
        MockMultipartFile csv = new MockMultipartFile("archivo", "asientos.csv", "text/csv", contenido);

        Map<String, Object> importado = service.importar(csv, "usuario@finantel.pe");

        assertEquals(true, importado.get("exito"));
        assertEquals(1, importado.get("total"));
        verify(importRepository).saveAll(anyList());
        verify(auditoriaRepository).save(any(AuditoriaAccion.class));

        MockMultipartFile invalido = new MockMultipartFile("archivo", "asientos.txt", "text/plain", contenido);
        assertEquals(false, service.importar(invalido, "usuario@finantel.pe").get("exito"));
        MockMultipartFile vacio = new MockMultipartFile("archivo", "vacio.csv", "text/csv", new byte[0]);
        assertEquals(false, service.importar(vacio, "usuario@finantel.pe").get("exito"));
        verify(importRepository, times(1)).saveAll(anyList());
    }

    @Test
    void cu15_historialGuardaUsuarioCampoValoresYFecha() {
        RegistrarHistorialCambiosService service = new RegistrarHistorialCambiosService(historialRepository);

        service.registrar("auditor@finantel.pe", "estado", "Activo", "Inactivo");

        ArgumentCaptor<HistorialCambio> captor = ArgumentCaptor.forClass(HistorialCambio.class);
        verify(historialRepository).save(captor.capture());
        assertEquals("estado", captor.getValue().getCampo());
        assertEquals("Activo", captor.getValue().getValorAnterior());
        assertEquals("Inactivo", captor.getValue().getValorNuevo());
        assertNotNull(captor.getValue().getFecha());
    }

    @Test
    void cu15_historialAceptaValorAnteriorNulo() {
        RegistrarHistorialCambiosService service = new RegistrarHistorialCambiosService(historialRepository);

        service.registrar("usuario", "alta", null, "creado");

        ArgumentCaptor<HistorialCambio> captor = ArgumentCaptor.forClass(HistorialCambio.class);
        verify(historialRepository).save(captor.capture());
        assertNull(captor.getValue().getValorAnterior());
        assertEquals("creado", captor.getValue().getValorNuevo());
    }

    @Test
    void cu16_generarCopiaRegistraMetadatosYAuditoria() {
        RealizarCopiaSeguridadService service = new RealizarCopiaSeguridadService(copiaRepository,
            new AuditoriaService(auditoriaRepository));

        assertEquals("Copia de seguridad realizada correctamente", service.realizarCopia("admin"));

        ArgumentCaptor<CopiaSeguridadEntity> captor = ArgumentCaptor.forClass(CopiaSeguridadEntity.class);
        verify(copiaRepository).save(captor.capture());
        assertEquals("Manual", captor.getValue().getTipo());
        assertEquals("Completado", captor.getValue().getEstado());
        assertEquals("admin", captor.getValue().getUsuario());
        assertNotNull(captor.getValue().getFecha());
        verify(auditoriaRepository).save(any(AuditoriaAccion.class));
    }

    @Test
    void cu17_restaurarPuntoExistenteOInexistente() {
        CopiaSeguridadEntity punto = new CopiaSeguridadEntity();
        punto.setFecha(LocalDateTime.of(2025, 1, 1, 12, 0));
        when(restaurarRepository.findById(1)).thenReturn(Optional.of(punto));
        when(restaurarRepository.findById(99)).thenReturn(Optional.empty());
        RestaurarBaseDatosService service = new RestaurarBaseDatosService(restaurarRepository);

        assertTrue(service.restaurar(1, "admin").contains("restaurada al punto"));
        assertEquals("Punto de restauración no encontrado", service.restaurar(99, "admin"));
    }

    @Test
    void cu17_idNegativoNoRestauraNingunPunto() {
        when(restaurarRepository.findById(-1)).thenReturn(Optional.empty());
        RestaurarBaseDatosService service = new RestaurarBaseDatosService(restaurarRepository);

        assertEquals("Punto de restauración no encontrado", service.restaurar(-1, "admin"));
    }

    @Test
    void cu18_crearUsuarioValidaDniTelefonoYCodificaPassword() {
        when(usuarioRepository.findByEmail("nuevo@finantel.pe")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("1234567")).thenReturn("hash");
        UsuarioService service = new UsuarioService(usuarioRepository, passwordEncoder,
            new AuditoriaService(auditoriaRepository));
        Usuario usuario = new Usuario();
        usuario.setNombre("Nuevo");
        usuario.setEmail("nuevo@finantel.pe");
        usuario.setPassword("1234567");
        usuario.setRol("Auditor");
        usuario.setDni("12345678");
        usuario.setTelefono("912345678");

        service.crear(usuario);

        assertEquals("hash", usuario.getPassword());
        assertEquals("Activo", usuario.getEstado());
        assertEquals(0, usuario.getIntentos());
        verify(usuarioRepository).save(usuario);
        verify(auditoriaRepository).save(any(AuditoriaAccion.class));
    }

    @Test
    void cu18_rechazarDniFueraDelLimiteSinGuardar() {
        when(usuarioRepository.findByEmail("nuevo@finantel.pe")).thenReturn(Optional.empty());
        UsuarioService service = new UsuarioService(usuarioRepository, passwordEncoder,
            new AuditoriaService(auditoriaRepository));
        Usuario usuario = new Usuario();
        usuario.setNombre("Nuevo");
        usuario.setEmail("nuevo@finantel.pe");
        usuario.setPassword("1234567");
        usuario.setRol("Auditor");
        usuario.setDni("1234567");

        assertThrows(RuntimeException.class, () -> service.crear(usuario));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cu18_passwordDeSeisCaracteresNoAlcanzaElMinimo() {
        when(usuarioRepository.findByEmail("nuevo@finantel.pe")).thenReturn(Optional.empty());
        UsuarioService service = new UsuarioService(usuarioRepository, passwordEncoder,
            new AuditoriaService(auditoriaRepository));
        Usuario usuario = new Usuario();
        usuario.setEmail("nuevo@finantel.pe");
        usuario.setPassword("123456");
        usuario.setRol("Auditor");

        assertThrows(RuntimeException.class, () -> service.crear(usuario));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cu20_conciliacionComparaTotalesYRegistraResultado() {
        AsientoContable asiento = asiento(1, "Activo", 100, 100);
        MovimientoBancario movimiento = new MovimientoBancario();
        movimiento.setMonto(100.0);
        when(asientoRepository.findAll()).thenReturn(List.of(asiento));
        when(movimientosRepository.findAll()).thenReturn(List.of(movimiento));
        ConciliacionBancariaService service = new ConciliacionBancariaService(movimientosRepository,
            asientoRepository, new AuditoriaService(auditoriaRepository));

        Map<String, Object> resultado = service.conciliar("estado.csv");

        assertEquals("Conciliado", resultado.get("estado"));
        assertEquals(0.0, (double) resultado.get("diferencia"));
        verify(auditoriaRepository).save(any(AuditoriaAccion.class));

        movimiento.setMonto(100.02);
        assertEquals("Con diferencias", service.conciliar("estado.csv").get("estado"));
        movimiento.setMonto(100.009);
        assertEquals("Conciliado", service.conciliar("estado.csv").get("estado"));
    }

    @Test
    void cu21_generarLibroDevuelveTodosLosAsientos() {
        List<AsientoContable> asientos = List.of(asiento(1, "Activo", 12, 12));
        when(libroRepository.findAll()).thenReturn(asientos);
        GenerarLibroContableService service = new GenerarLibroContableService(libroRepository);

        assertEquals(asientos, service.generarLibro());
        verify(libroRepository).findAll();
    }

    @Test
    void cu21_libroSinAsientosDevuelveListaVacia() {
        when(libroRepository.findAll()).thenReturn(List.of());
        GenerarLibroContableService service = new GenerarLibroContableService(libroRepository);

        assertTrue(service.generarLibro().isEmpty());
    }

    private static AsientoContable asiento(int id, String estado, double debe, double haber) {
        AsientoContable asiento = new AsientoContable();
        asiento.setId(id);
        asiento.setEstado(estado);
        asiento.setDebe(debe);
        asiento.setHaber(haber);
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
}
