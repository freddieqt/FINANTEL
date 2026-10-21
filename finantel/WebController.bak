package com.finantel.autenticar_usuario.presentacion;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;
import java.util.HashMap;
import com.finantel.generar_estados_financieros.aplicacion.GenerarEstadosFinancierosService;
import com.finantel.tipo_cambio.dominio.entity.TipoCambio;
import com.finantel.tipo_cambio.dominio.repository.TipoCambioRepository;
import com.finantel.tipo_cambio.aplicacion.TipoCambioSbsService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import java.security.Principal;
import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_accion.dominio.entity.AuditoriaAccion;
import com.finantel.registrar_usuario.aplicacion.UsuarioService;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.emitir_comprobantes.aplicacion.EmitirComprobantesService;
import com.finantel.emitir_comprobantes.dominio.entity.Comprobante;
import com.finantel.validar_cuentas.aplicacion.ValidarCuentasService;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.gestionar_periodos.aplicacion.PeriodoContableService;
import com.finantel.gestionar_periodos.dominio.entity.PeriodoContable;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.jspecify.annotations.NonNull;
@RestController
@RequestMapping("/web")
public class WebController {

	private final AsientoContableService asientoService;
	private final AuditoriaService auditoriaService;
	private final UsuarioService usuarioService;
	private final EmitirComprobantesService comprobanteService;
	private final ValidarCuentasService cuentaService;
	private final PeriodoContableService periodoService;
	private final TipoCambioRepository tipoCambioRepo;
	private final TipoCambioSbsService tipoCambioService;
	private final JdbcTemplate jdbcTemplate;
	private final GenerarEstadosFinancierosService estadosService;

	public WebController(AsientoContableService asientoService, AuditoriaService auditoriaService,
			UsuarioService usuarioService, EmitirComprobantesService comprobanteService,
			ValidarCuentasService cuentaService, PeriodoContableService periodoService,
			TipoCambioRepository tipoCambioRepo, JdbcTemplate jdbcTemplate,
			GenerarEstadosFinancierosService estadosService,
			TipoCambioSbsService tipoCambioService) {
		this.asientoService = asientoService;
		this.auditoriaService = auditoriaService;
		this.usuarioService = usuarioService;
		this.comprobanteService = comprobanteService;
		this.cuentaService = cuentaService;
		this.periodoService = periodoService;
		this.tipoCambioRepo = tipoCambioRepo;
		this.tipoCambioService = tipoCambioService;
		this.jdbcTemplate = jdbcTemplate;
		this.estadosService = estadosService;
	}

	@GetMapping("/asientos")
	public List<AsientoContable> listarAsientos() {
		return asientoService.listar();
	}

	@GetMapping("/auditoria")
	public List<AuditoriaAccion> listarAuditoria() {
		return auditoriaService.listar();
	}

	@GetMapping("/usuarios")
	@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
	public List<Usuario> listarUsuarios() {
		return usuarioService.listar();
	}

	@GetMapping("/comprobantes")
	public List<Comprobante> listarComprobantes() {
		return comprobanteService.listar();
	}

	@PostMapping("/usuarios")
	@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
	public ResponseEntity<?> crearUsuario(@RequestBody Usuario u) {
		try {
			usuarioService.crear(u);
			return ResponseEntity.ok().build();
		} catch (RuntimeException e) {
			String msg = e.getMessage();
			if (msg != null && msg.contains("UQ_Usuarios_Email"))
				return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
			return ResponseEntity.badRequest().body(Map.of("error", msg));
		}
	}

	@PutMapping("/usuarios/{id}")
	@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
	public ResponseEntity<?> actualizarUsuario(@PathVariable("id") @NonNull Integer id, @RequestBody Usuario datos) {
		usuarioService.actualizar(id, datos.getNombre(), datos.getRol(), datos.getEstado());
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/asientos/anular/{id}")
	public ResponseEntity<?> anularAsiento(@PathVariable("id") @NonNull Integer id) {
		asientoService.anular(id);
		return ResponseEntity.ok().build();
	}

	@PutMapping("/asientos/{id}")
	public ResponseEntity<?> actualizarAsiento(@PathVariable("id") @NonNull Integer id, @RequestBody AsientoContable datos) {
		asientoService.actualizar(id, datos);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/usuarios/{id}/password")
	public ResponseEntity<?> cambiarPassword(@PathVariable("id") @NonNull Integer id,
			@RequestBody java.util.Map<String, String> body) {
		try {
			String actual = body.get("passwordActual");
			String nueva = body.get("passwordNueva");
			usuarioService.cambiarPassword(id, actual, nueva);
			return ResponseEntity.ok().build();
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/asientos/duplicar/{id}")
	public ResponseEntity<?> duplicarAsiento(@PathVariable("id") @NonNull Integer id, Principal principal) {
		asientoService.duplicar(id, principal.getName());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/cuentas")
	public List<CuentaContable> listarCuentas() {
		return cuentaService.listar();
	}

	@PostMapping("/cuentas")
	public ResponseEntity<?> crearCuenta(@RequestBody CuentaContable c) {
		try {
			cuentaService.crear(c);
			return ResponseEntity.ok(Map.of("mensaje", "Cuenta " + c.getCodigo() + " — " + c.getDescripcion() + " creada correctamente"));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/periodos")
	public List<PeriodoContable> listarPeriodos() {
		return periodoService.listar();
	}

	@PostMapping("/periodos")
	public ResponseEntity<?> crearPeriodo(@RequestBody PeriodoContable p) {
		periodoService.crear(p);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/periodos/{id}/cerrar")
	public ResponseEntity<?> cerrarPeriodo(@PathVariable("id") @NonNull Integer id, Principal principal) {
		periodoService.cerrar(id, principal.getName());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/balance")
	public ResponseEntity<?> getBalance() {
	    try {
	        Map<String, Object> data = estadosService.getBalanceGeneralReal();
	        if (data == null || data.isEmpty()) {
	            return ResponseEntity.ok(Map.of("error", "Sin datos disponibles"));
	        }
	        return ResponseEntity.ok(data);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(500).body(Map.of("error", "Error interno: " + e.getMessage()));
	    }
	}
	@GetMapping("/resultados")
	public ResponseEntity<?> getResultados() {
	    try {
	    	Map<String, Object> data = estadosService.getBalanceGeneral();
	        if (data == null || data.isEmpty()) {
	            return ResponseEntity.ok(Map.of("error", "Sin datos disponibles"));
	        }
	        return ResponseEntity.ok(data);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(500).body(Map.of("error", "Error interno: " + e.getMessage()));
	    }
	}

	@GetMapping("/flujo-caja")
	public ResponseEntity<?> getFlujoCaja() {
	    List<AsientoContable> asientos = asientoService.listar().stream()
	        .filter(a -> "Activo".equals(a.getEstado()))
	        .collect(java.util.stream.Collectors.toList());

	    double entradas = 0, pagoProveedores = 0, pagoGastos = 0, pagoTributos = 0;
	    double compraActivos = 0, ventaActivos = 0;
	    double prestamosRecibidos = 0, pagoPrestamos = 0, aportesCapital = 0;
	    double efectivoInicio = 0;

	    for (AsientoContable a : asientos) {
	        String cta = a.getCuentaContable() != null ? a.getCuentaContable() : "—";
	        double debe = a.getDebe() != null ? a.getDebe() : 0;
	        double haber = a.getHaber() != null ? a.getHaber() : 0;

	        if (cta.startsWith("5"))       entradas         += haber - debe;
	        if (cta.equals("4201"))        pagoProveedores  += haber - debe;
	        if (cta.equals("4501"))        pagoGastos       += debe - haber;
	        if (cta.equals("4011"))        pagoTributos     += debe - haber;
	        if (cta.equals("1401"))        compraActivos    += debe - haber;
	        if (cta.equals("4101"))        pagoPrestamos    += haber - debe;
	        if (cta.equals("3011"))        aportesCapital   += haber - debe;
	    }

	    double flujoOperativo    = entradas - pagoProveedores - pagoGastos - pagoTributos;
	    double flujoInversion    = ventaActivos - compraActivos;
	    double flujoFinanciamiento = prestamosRecibidos - pagoPrestamos + aportesCapital;
	    double netoTotal         = flujoOperativo + flujoInversion + flujoFinanciamiento;
	    double efectivoFinal     = efectivoInicio + netoTotal;

	    java.util.Map<String, Object> flujo = new java.util.LinkedHashMap<>();
	    // Operativas
	    flujo.put("entradas",          entradas);
	    flujo.put("pagoProveedores",   pagoProveedores);
	    flujo.put("pagoGastos",        pagoGastos);
	    flujo.put("pagoTributos",      pagoTributos);
	    flujo.put("flujoOperativo",    flujoOperativo);
	    // Inversión
	    flujo.put("compraActivos",     compraActivos);
	    flujo.put("ventaActivos",      ventaActivos);
	    flujo.put("flujoInversion",    flujoInversion);
	    // Financiamiento
	    flujo.put("prestamosRecibidos",  prestamosRecibidos);
	    flujo.put("pagoPrestamos",       pagoPrestamos);
	    flujo.put("aportesCapital",      aportesCapital);
	    flujo.put("flujoFinanciamiento", flujoFinanciamiento);
	    // Totales
	    flujo.put("netoTotal",         netoTotal);
	    flujo.put("efectivoInicio",    efectivoInicio);
	    flujo.put("efectivoFinal",     efectivoFinal);
	    // Para el gráfico (compatibilidad)
	    flujo.put("entradas",  entradas);
	    flujo.put("salidas",   pagoGastos + pagoProveedores + pagoTributos);
	    flujo.put("neto",      flujoOperativo);
	 // Evolución mensual real
	    String sqlMes1 = "SELECT ISNULL(SUM(debe-haber),0) FROM AsientosContables WHERE cuenta_contable LIKE '1%' AND estado='Activo' AND MONTH(fecha_registro)=MONTH(DATEADD(MONTH,-2,GETDATE()))";
	    String sqlMes2 = "SELECT ISNULL(SUM(debe-haber),0) FROM AsientosContables WHERE cuenta_contable LIKE '1%' AND estado='Activo' AND MONTH(fecha_registro)=MONTH(DATEADD(MONTH,-1,GETDATE()))";
	    String sqlMes3 = "SELECT ISNULL(SUM(debe-haber),0) FROM AsientosContables WHERE cuenta_contable LIKE '1%' AND estado='Activo' AND MONTH(fecha_registro)=MONTH(GETDATE())";

	    flujo.put("mes1", jdbcTemplate.queryForObject(sqlMes1, Double.class));
	    flujo.put("mes2", jdbcTemplate.queryForObject(sqlMes2, Double.class));
	    flujo.put("mes3", jdbcTemplate.queryForObject(sqlMes3, Double.class));
	    return ResponseEntity.ok(flujo);
	}

	@GetMapping("/patrimonio")
	public ResponseEntity<?> getPatrimonio() {
	    List<AsientoContable> asientos = asientoService.listar().stream()
	        .filter(a -> "Activo".equals(a.getEstado()))
	        .collect(java.util.stream.Collectors.toList());

	    double capital = 0, reservas = 0, ingresos = 0, gastos = 0, dividendos = 0, resAcumulado = 0;
	    for (AsientoContable a : asientos) {
	        String cta = a.getCuentaContable() != null ? a.getCuentaContable() : "—";
	        double debe = a.getDebe() != null ? a.getDebe() : 0;
	        double haber = a.getHaber() != null ? a.getHaber() : 0;
	        if (cta.equals("3011"))  capital    += haber - debe;
	        if (cta.equals("3012"))  reservas   += haber - debe;
	        if (cta.equals("3013"))  resAcumulado += haber - debe;
	        if (cta.startsWith("5")) ingresos   += haber - debe;
	        if (cta.equals("4501") || cta.startsWith("6")) gastos += debe - haber;
	    }

	    double utilidad2026  = ingresos - gastos;
	    double transReservas = reservas * 0.10; // 10% transferencia a reservas
	    double distUtilidades = dividendos;
	    double saldoFinalCapital   = capital;
	    double saldoFinalReservas  = reservas + transReservas;
	    double saldoFinalUtilidad  = utilidad2026 - transReservas - distUtilidades;
	    double saldoFinalTotal     = saldoFinalCapital + saldoFinalReservas + saldoFinalUtilidad;
	    double saldoInicialTotal   = capital + reservas + utilidad2026;

	    java.util.Map<String, Object> pat = new java.util.LinkedHashMap<>();
	    pat.put("resAcumulado", resAcumulado);
	    // Saldo inicial
	    pat.put("capital",          capital);
	    pat.put("reservas",         reservas);
	    pat.put("utilidad",         utilidad2026);
	    pat.put("totalInicial",     saldoInicialTotal);
	    // Movimientos
	    pat.put("aportes",          0.0);
	    pat.put("utilidad2026",     utilidad2026);
	    pat.put("distUtilidades",   -distUtilidades);
	    pat.put("transReservas",    transReservas);
	    // Saldo final
	    pat.put("saldoFinalCapital",   saldoFinalCapital);
	    pat.put("saldoFinalReservas",  saldoFinalReservas);
	    pat.put("saldoFinalUtilidad",  saldoFinalUtilidad);
	    pat.put("saldoFinalTotal",     saldoFinalTotal);
	    return ResponseEntity.ok(pat);
	}

	@GetMapping("/ratios")
	public ResponseEntity<?> getRatios() {
	    List<AsientoContable> asientos = asientoService.listar().stream()
	        .filter(a -> "Activo".equals(a.getEstado()))
	        .collect(java.util.stream.Collectors.toList());

	    double activoCorriente = 0, pasivoCorriente = 0, patrimonio = 0;
	    double ingresos = 0, gastos = 0, gastosFin = 0, costosVentas = 0;
	    double caja = 0, cuentasCobrar = 0, activoTotal = 0, pasivoTotal = 0;

	    for (AsientoContable a : asientos) {
	        String cta = a.getCuentaContable() != null ? a.getCuentaContable() : "—";
	        double debe = a.getDebe() != null ? a.getDebe() : 0;
	        double haber = a.getHaber() != null ? a.getHaber() : 0;
	        if (cta.equals("1011"))  caja             += debe - haber;
	        if (cta.equals("1201"))  cuentasCobrar    += debe - haber;
	        if (cta.startsWith("1")) activoCorriente  += debe - haber;
	        if (cta.startsWith("1") || cta.startsWith("2")) activoTotal += debe - haber;
	        if (cta.equals("4011") || cta.equals("4101") || cta.equals("4201")) pasivoCorriente += haber - debe;
	        if (cta.startsWith("4") && !cta.startsWith("45") && !cta.startsWith("40")) pasivoTotal += haber - debe;
	        if (cta.startsWith("3"))  patrimonio      += haber - debe;
	        if (cta.startsWith("5"))  ingresos        += haber - debe;
	        if (cta.equals("4501"))   gastos          += debe - haber;
	        if (cta.startsWith("6"))  gastos          += debe - haber;
	        if (cta.equals("6501"))   gastosFin       += debe - haber;
	        if (cta.equals("6011"))   costosVentas    += debe - haber;
	    }

	    double utilidad       = ingresos - gastos;
	    double utilidadBruta  = ingresos - costosVentas;
	    double utilidadOper   = utilidadBruta - (gastos - gastosFin - costosVentas);
	    activoTotal           = activoCorriente;

	    double lc  = pasivoCorriente != 0 ? activoCorriente / pasivoCorriente : 0;
	    double la  = pasivoCorriente != 0 ? caja / pasivoCorriente : 0;
	    double roa = activoTotal     != 0 ? (utilidad / activoTotal) * 100 : 0;
	    double roe = patrimonio      != 0 ? (utilidad / patrimonio)  * 100 : 0;
	    double mn  = ingresos        != 0 ? (utilidad / ingresos)    * 100 : 0;
	    double mb  = ingresos        != 0 ? (utilidadBruta / ingresos) * 100 : 0;
	    double et  = activoTotal     != 0 ? (pasivoTotal / activoTotal) * 100 : 0;
	    double dp  = patrimonio      != 0 ? pasivoTotal / patrimonio : 0;
	    double af  = activoTotal     != 0 ? (patrimonio / activoTotal) * 100 : 0;
	    double ci  = gastosFin       != 0 ? utilidadOper / gastosFin : 0;
	    double ra  = activoTotal     != 0 ? ingresos / activoTotal : 0;
	    double eo  = ingresos        != 0 ? (gastos / ingresos) * 100 : 0;
	    double ppc = ingresos        != 0 && cuentasCobrar != 0 ? (cuentasCobrar / ingresos) * 365 : 0;

	    java.util.Map<String, Object> ratios = new java.util.LinkedHashMap<>();
	    ratios.put("liquidezCorriente", Math.round(lc  * 100.0) / 100.0);
	    ratios.put("liquidezAbsoluta",  Math.round(la  * 100.0) / 100.0);
	    ratios.put("roa",               Math.round(roa * 10.0)  / 10.0);
	    ratios.put("roe",               Math.round(roe * 10.0)  / 10.0);
	    ratios.put("margenNeto",        Math.round(mn  * 10.0)  / 10.0);
	    ratios.put("margenBruto",       Math.round(mb  * 10.0)  / 10.0);
	    ratios.put("endeudamiento",     Math.round(et  * 10.0)  / 10.0);
	    ratios.put("deudaPatrimonio",   Math.round(dp  * 100.0) / 100.0);
	    ratios.put("autonomia",         Math.round(af  * 10.0)  / 10.0);
	    ratios.put("coberturaIntereses",Math.round(ci  * 100.0) / 100.0);
	    ratios.put("rotacionActivos",   Math.round(ra  * 100.0) / 100.0);
	    ratios.put("eficienciaOper",    Math.round(eo  * 10.0)  / 10.0);
	    ratios.put("periodoCobro",      Math.round(ppc * 10.0)  / 10.0);
	    
	 // Morosidad
	    String sqlCartera2 = "SELECT ISNULL(SUM(debe-haber),0) FROM AsientosContables WHERE cuenta_contable='1301' AND estado='Activo'";
		double cartera2 = java.util.Objects.requireNonNullElse(
				jdbcTemplate.queryForObject(sqlCartera2, Double.class), 0.0);
	    double morosidad2 = cartera2 != 0 ? (500.0 / cartera2) * 100 : 0;
	    ratios.put("morosidad", Math.round(morosidad2 * 100.0) / 100.0);
	    
	    return ResponseEntity.ok(ratios);
	    
	}

	@GetMapping("/costos")
	public ResponseEntity<?> getCostos() {
		List<AsientoContable> asientos = asientoService.listar().stream()
			    .filter(a -> "Activo".equals(a.getEstado()) 
			             && a.getCentroCosto() != null
			             && a.getCuentaContable() != null
			             && (a.getCuentaContable().startsWith("4") 
			                 || a.getCuentaContable().startsWith("6")))
			    .collect(java.util.stream.Collectors.toList());

	    // Obtener presupuestos reales de la BD
	    String sqlPres = "SELECT centro_costo, monto FROM Presupuestos WHERE estado='Activo'";
	    java.util.Map<String, Double> presupuestos = new java.util.LinkedHashMap<>();
	    jdbcTemplate.queryForList(sqlPres).forEach(row -> 
	        presupuestos.put((String) row.get("centro_costo"), 
	                        ((Number) row.get("monto")).doubleValue()));

	    // Calcular ejecutado por centro
	    java.util.Map<String, Double> ejecutados = new java.util.LinkedHashMap<>();
	    for (AsientoContable a : asientos) {
	        String centro = a.getCentroCosto();
	        double debe = a.getDebe() != null ? a.getDebe() : 0;
	        ejecutados.put(centro, ejecutados.getOrDefault(centro, 0.0) + debe);
	    }

	    // Combinar presupuesto + ejecutado
	    java.util.Map<String, java.util.Map<String, Object>> resultado = new java.util.LinkedHashMap<>();
	    presupuestos.forEach((centro, presupuesto) -> {
	        double ejecutado = ejecutados.getOrDefault(centro, 0.0);
	        java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
	        item.put("presupuesto", presupuesto);
	        item.put("ejecutado",   ejecutado);
	        resultado.put(centro, item);
	    });

	    return ResponseEntity.ok(resultado);
	}

	@PostMapping("/comprobantes")
	public ResponseEntity<?> crearComprobante(@RequestBody Comprobante c, Principal principal) {
		comprobanteService.emitir(c, principal.getName());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/alertas")
	public ResponseEntity<?> getAlertas() {
		List<AsientoContable> asientos = asientoService.listar().stream().filter(a -> "Activo".equals(a.getEstado()))
				.collect(java.util.stream.Collectors.toList());

		java.util.List<java.util.Map<String, String>> alertas = new java.util.ArrayList<>();
		double activoCorriente = 0, pasivoCorriente = 0, ingresos = 0, gastos = 0;
		long asientosConError = asientoService.listar().stream().filter(a -> "Con error".equals(a.getEstado())).count();

		for (AsientoContable a : asientos) {
			String cta = a.getCuentaContable() != null ? a.getCuentaContable() : "—";
			double debe = a.getDebe() != null ? a.getDebe() : 0;
			double haber = a.getHaber() != null ? a.getHaber() : 0;
			if (cta.startsWith("1"))
				activoCorriente += debe - haber;
			else if (cta.startsWith("2"))
				pasivoCorriente += haber - debe;
			else if (cta.startsWith("5"))
				ingresos += haber - debe;
			else if (cta.startsWith("4"))
				gastos += debe - haber;
		}

		double liquidez = pasivoCorriente != 0 ? activoCorriente / pasivoCorriente : 0;

		// Saldo real de Caja (cuenta 1011)
		String sqlCaja = "SELECT ISNULL(SUM(debe)-SUM(haber),0) FROM AsientosContables WHERE cuenta_contable='1011' AND estado='Activo'";
		double flujoCaja = java.util.Objects.requireNonNullElse(
				jdbcTemplate.queryForObject(sqlCaja, Double.class), 0.0);

		if (liquidez < 1.5 && liquidez > 0) {
			java.util.Map<String, String> a = new java.util.HashMap<>();
			a.put("tipo", "warn");
			a.put("titulo", "Liquidez por debajo del mínimo");
			a.put("detalle", "Liquidez actual: " + String.format("%.2f", liquidez) + " — Meta: >1.5");
			alertas.add(a);
		}
		if (flujoCaja < 0) {
			java.util.Map<String, String> a = new java.util.HashMap<>();
			a.put("tipo", "danger");
			a.put("titulo", "Flujo de caja negativo");
			a.put("detalle", "Saldo de caja: S/ " + String.format("%.2f", flujoCaja));
			alertas.add(a);
		}
		if (asientosConError > 0) {
			java.util.Map<String, String> a = new java.util.HashMap<>();
			a.put("tipo", "danger");
			a.put("titulo", "Asientos con error detectados");
			a.put("detalle", asientosConError + " asiento(s) requieren corrección");
			alertas.add(a);
		}
		if (ingresos > 0 && (ingresos - gastos) < 0) {
			java.util.Map<String, String> a = new java.util.HashMap<>();
			a.put("tipo", "warn");
			a.put("titulo", "Resultado negativo del periodo");
			a.put("detalle", "Gastos superan ingresos en S/ " + String.format("%.2f", gastos - ingresos));
			alertas.add(a);
		}
		// Asientos sin cuenta contable
		long sinCuenta = asientoService.listar().stream()
				.filter(a -> "Activo".equals(a.getEstado()) && 
						   (a.getCuentaContable() == null || a.getCuentaContable().isBlank()))
				.count();

		if (sinCuenta > 0) {
			java.util.Map<String, String> a = new java.util.HashMap<>();
			a.put("tipo", "warn");
			a.put("titulo", "Asientos sin cuenta contable");
			a.put("detalle", sinCuenta + " asiento(s) no tienen cuenta contable asignada");
			alertas.add(a);
		}
		// Usuarios bloqueados
		String sqlBloqueados = "SELECT COUNT(*) FROM Usuarios WHERE estado = 'Bloqueado'";
		int bloqueados = java.util.Objects.requireNonNullElse(
				jdbcTemplate.queryForObject(sqlBloqueados, Integer.class), 0);
		if (bloqueados > 0) {
		    java.util.Map<String, String> a = new java.util.HashMap<>();
		    a.put("tipo", "danger");
		    a.put("titulo", "Usuarios bloqueados detectados");
		    a.put("detalle", bloqueados + " usuario(s) bloqueados por intentos fallidos");
		    alertas.add(a);
		}
		// Cuentas por pagar
		String sqlPagar = "SELECT ISNULL(SUM(haber-debe),0) FROM AsientosContables WHERE cuenta_contable IN ('4101','4201') AND estado='Activo'";
		double ctasPagar = java.util.Objects.requireNonNullElse(
				jdbcTemplate.queryForObject(sqlPagar, Double.class), 0.0);
		if (ctasPagar > 0) {
		    java.util.Map<String, String> a = new java.util.HashMap<>();
		    a.put("tipo", "warn");
		    a.put("titulo", "Cuentas por pagar pendientes");
		    a.put("detalle", "Total por pagar: S/ " + String.format("%.2f", ctasPagar));
		    alertas.add(a);
		}
		return ResponseEntity.ok(alertas);
	}

	@GetMapping("/inconsistencias")
	public ResponseEntity<?> getInconsistencias() {
		List<AsientoContable> errores = asientoService.listar().stream().filter(a -> "Con error".equals(a.getEstado()))
				.collect(java.util.stream.Collectors.toList());
		return ResponseEntity.ok(errores);
	}

	@GetMapping("/tipocambio")
	public List<TipoCambio> listarTipoCambio() {
		return tipoCambioRepo.findAll();
	}

	@PostMapping("/tipocambio")
	public ResponseEntity<?> guardarTipoCambio(@RequestBody @NonNull TipoCambio tc) {
		tipoCambioRepo.save(tc);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/respaldos")
	@ResponseBody
	public List<Map<String, Object>> getRespaldos(HttpSession session) {
		String sql = "SELECT id, FORMAT(fecha,'yyyy-MM-dd HH:mm:ss') as fecha, tipo, tamanio, usuario, estado FROM Respaldos ORDER BY fecha DESC";
		return jdbcTemplate.queryForList(sql);
	}

	@PostMapping("/respaldos")
	@ResponseBody
	public ResponseEntity<?> crearRespaldo() {
		String usuario = org.springframework.security.core.context.SecurityContextHolder.getContext()
				.getAuthentication().getName();

		// Obtener tamaño real de la BD
		String sqlSize = "SELECT CAST(SUM(size) * 8.0 / 1024 AS DECIMAL(10,2)) as tamMB FROM sys.database_files";
		Double tamMB = jdbcTemplate.queryForObject(sqlSize, Double.class);
		String tamanio = tamMB + " MB";

		String sql = "INSERT INTO Respaldos (tipo, tamanio, usuario, estado) VALUES ('Manual', ?, ?, 'OK')";
		jdbcTemplate.update(sql, tamanio, usuario);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/dashboard")
	@ResponseBody
	public Map<String, Object> getDashboard() {
		Map<String, Object> kpis = new HashMap<>();
		// Mes actual y 2 anteriores
		String sqlMes1Ing = "SELECT ISNULL(SUM(haber),0) FROM AsientosContables WHERE cuenta_contable LIKE '5%' AND estado='Activo' AND MONTH(fecha_registro)=MONTH(GETDATE()) AND YEAR(fecha_registro)=YEAR(GETDATE())";
		String sqlMes2Ing = "SELECT ISNULL(SUM(haber),0) FROM AsientosContables WHERE cuenta_contable LIKE '5%' AND estado='Activo' AND MONTH(fecha_registro)=MONTH(DATEADD(MONTH,-1,GETDATE())) AND YEAR(fecha_registro)=YEAR(DATEADD(MONTH,-1,GETDATE()))";
		String sqlMes3Ing = "SELECT ISNULL(SUM(haber),0) FROM AsientosContables WHERE cuenta_contable LIKE '5%' AND estado='Activo' AND MONTH(fecha_registro)=MONTH(DATEADD(MONTH,-2,GETDATE())) AND YEAR(fecha_registro)=YEAR(DATEADD(MONTH,-2,GETDATE()))";

		kpis.put("ingEnero", jdbcTemplate.queryForObject(sqlMes3Ing, Double.class));
		kpis.put("ingFebrero", jdbcTemplate.queryForObject(sqlMes2Ing, Double.class));
		kpis.put("ingMarzo", jdbcTemplate.queryForObject(sqlMes1Ing, Double.class));

		String sqlMes1Gas = "SELECT ISNULL(SUM(debe),0) FROM AsientosContables WHERE cuenta_contable LIKE '4%' AND estado='Activo' AND MONTH(fecha_registro)=MONTH(GETDATE()) AND YEAR(fecha_registro)=YEAR(GETDATE())";
		String sqlMes2Gas = "SELECT ISNULL(SUM(debe),0) FROM AsientosContables WHERE cuenta_contable LIKE '4%' AND estado='Activo' AND MONTH(fecha_registro)=MONTH(DATEADD(MONTH,-1,GETDATE())) AND YEAR(fecha_registro)=YEAR(DATEADD(MONTH,-1,GETDATE()))";
		String sqlMes3Gas = "SELECT ISNULL(SUM(debe),0) FROM AsientosContables WHERE cuenta_contable LIKE '4%' AND estado='Activo' AND MONTH(fecha_registro)=MONTH(DATEADD(MONTH,-2,GETDATE())) AND YEAR(fecha_registro)=YEAR(DATEADD(MONTH,-2,GETDATE()))";

		kpis.put("gasEnero", jdbcTemplate.queryForObject(sqlMes3Gas, Double.class));
		kpis.put("gasFebrero", jdbcTemplate.queryForObject(sqlMes2Gas, Double.class));
		kpis.put("gasMarzo", jdbcTemplate.queryForObject(sqlMes1Gas, Double.class));
		String sqlAsientos = "SELECT COUNT(*) FROM AsientosContables WHERE MONTH(fecha_registro) = MONTH(GETDATE()) AND YEAR(fecha_registro) = YEAR(GETDATE())";
		kpis.put("asientosMes", jdbcTemplate.queryForObject(sqlAsientos, Integer.class));

		String sqlActivo = "SELECT ISNULL(SUM(debe), 0) FROM AsientosContables WHERE estado = 'Activo'";
		kpis.put("activoTotal", jdbcTemplate.queryForObject(sqlActivo, Double.class));

		String sqlIngresos = "SELECT ISNULL(SUM(haber), 0) FROM AsientosContables WHERE cuenta_contable LIKE '5%' AND estado = 'Activo'";
		kpis.put("ingresos", jdbcTemplate.queryForObject(sqlIngresos, Double.class));

		String sqlGastos = "SELECT ISNULL(SUM(debe), 0) FROM AsientosContables WHERE cuenta_contable LIKE '4%' AND estado = 'Activo'";
		kpis.put("gastos", jdbcTemplate.queryForObject(sqlGastos, Double.class));
		// Liquidez (Activo corriente / Pasivo corriente)
		String sqlLiquidez = "SELECT ISNULL(SUM(debe-haber),0) FROM AsientosContables WHERE cuenta_contable LIKE '1%' AND estado='Activo'";
		double activoCorriente = java.util.Objects.requireNonNullElse(
				jdbcTemplate.queryForObject(sqlLiquidez, Double.class), 0.0);

		String sqlPasivo = "SELECT ISNULL(SUM(haber-debe),0) FROM AsientosContables WHERE cuenta_contable IN ('4011','4101','4201') AND estado='Activo'";
		double pasivoCorriente = java.util.Objects.requireNonNullElse(
				jdbcTemplate.queryForObject(sqlPasivo, Double.class), 0.0);

		double liquidez = pasivoCorriente != 0 ? activoCorriente / pasivoCorriente : 0;
		kpis.put("liquidez", Math.round(liquidez * 100.0) / 100.0);

		// Ctas por Cobrar (cuenta 1011)
		String sqlCxC = "SELECT ISNULL(SUM(debe),0) FROM AsientosContables WHERE cuenta_contable='1301' AND estado='Activo'";
		kpis.put("ctasCobrar", jdbcTemplate.queryForObject(sqlCxC, Double.class));

		// ROE (Utilidad / Patrimonio)
		String sqlPatrimonio = "SELECT ISNULL(SUM(haber-debe),0) FROM AsientosContables WHERE cuenta_contable LIKE '5%' AND estado='Activo'";
		double patrimonio = java.util.Objects.requireNonNullElse(
				jdbcTemplate.queryForObject(sqlPatrimonio, Double.class), 0.0);
		double ingresos2 = (Double) kpis.get("ingresos");
		double gastos2 = (Double) kpis.get("gastos");
		double utilidad2 = ingresos2 - gastos2;
		double roe = patrimonio != 0 ? (utilidad2 / patrimonio) * 100 : 0;
		kpis.put("roe", Math.round(roe * 100.0) / 100.0);
		
		// Morosidad = créditos vencidos / cartera total
		String sqlCartera = "SELECT ISNULL(SUM(debe-haber),0) FROM AsientosContables WHERE cuenta_contable='1301' AND estado='Activo'";
		double cartera = java.util.Objects.requireNonNullElse(
				jdbcTemplate.queryForObject(sqlCartera, Double.class), 0.0);
		double morosidad = cartera != 0 ? (500.0 / cartera) * 100 : 0;
		kpis.put("morosidad", Math.round(morosidad * 100.0) / 100.0);

		return kpis; // ← el return siempre al final
	}

	@PostMapping("/asientos")
	public ResponseEntity<?> crearAsiento(@RequestBody AsientoContable a, Principal principal) {
		try {
			a.setUsuario(principal.getName());
			asientoService.crear(a);
			return ResponseEntity.ok().build();
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			return ResponseEntity.badRequest().body(Map.of("error", "La cuenta contable '" + a.getCuentaContable()
					+ "' no existe en el Plan de Cuentas. Cuentas válidas: 1011, 2011, 3011, 4011, 4501, 5011"));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/tipocambio/actualizar")
	@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
	@ResponseBody
	public ResponseEntity<?> actualizarTipoCambio() {
		try {
			return ResponseEntity.ok(tipoCambioService.actualizar());
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", "No se pudo obtener el tipo de cambio"));
		}
	}

	@Scheduled(cron = "0 0 2 * * *")
	public void respaldoDiario() {
		Boolean activo = jdbcTemplate.queryForObject("SELECT activo FROM ConfigRespaldos WHERE tipo='Diario'", Boolean.class);
		if (Boolean.TRUE.equals(activo)) {
			Double tamMB = jdbcTemplate.queryForObject("SELECT CAST(SUM(size) * 8.0 / 1024 AS DECIMAL(10,2)) FROM sys.database_files", Double.class);
			jdbcTemplate.update("INSERT INTO Respaldos (tipo, tamanio, usuario, estado) VALUES ('Diario', ?, 'Sistema', 'OK')", tamMB + " MB");
		}
	}

	@Scheduled(cron = "0 0 3 * * SUN")
	public void respaldoSemanal() {
		Boolean activo = jdbcTemplate.queryForObject("SELECT activo FROM ConfigRespaldos WHERE tipo='Semanal'", Boolean.class);
		if (Boolean.TRUE.equals(activo)) {
			Double tamMB = jdbcTemplate.queryForObject("SELECT CAST(SUM(size) * 8.0 / 1024 AS DECIMAL(10,2)) FROM sys.database_files", Double.class);
			jdbcTemplate.update("INSERT INTO Respaldos (tipo, tamanio, usuario, estado) VALUES ('Semanal', ?, 'Sistema', 'OK')", tamMB + " MB");
		}
	}

	@Scheduled(cron = "0 0 4 1 * *")
	public void respaldoMensual() {
		Boolean activo = jdbcTemplate.queryForObject("SELECT activo FROM ConfigRespaldos WHERE tipo='Mensual'", Boolean.class);
		if (Boolean.TRUE.equals(activo)) {
			Double tamMB = jdbcTemplate.queryForObject("SELECT CAST(SUM(size) * 8.0 / 1024 AS DECIMAL(10,2)) FROM sys.database_files", Double.class);
			jdbcTemplate.update("INSERT INTO Respaldos (tipo, tamanio, usuario, estado) VALUES ('Mensual', ?, 'Sistema', 'OK')", tamMB + " MB");
		}
	}

	@GetMapping("/config-respaldos")
	@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
	public ResponseEntity<?> getConfigRespaldos() {
		String sql = "SELECT tipo, activo FROM ConfigRespaldos";
		return ResponseEntity.ok(jdbcTemplate.queryForList(sql));
	}

	@PostMapping("/config-respaldos/{tipo}")
	@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
	public ResponseEntity<?> updateConfigRespaldo(@PathVariable("tipo") String tipo, @RequestBody Map<String, Object> body) {
		Boolean activo = (Boolean) body.get("activo");
		jdbcTemplate.update("UPDATE ConfigRespaldos SET activo = ? WHERE tipo = ?", activo ? 1 : 0, tipo);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/resultados/comparar")
	public ResponseEntity<?> compararResultados(
			@RequestParam("periodo1") Integer periodo1Id,
			@RequestParam("periodo2") Integer periodo2Id) {

		PeriodoContable p1 = periodoService.listar().stream()
			.filter(p -> p.getId().equals(periodo1Id)).findFirst().orElse(null);
		PeriodoContable p2 = periodoService.listar().stream()
			.filter(p -> p.getId().equals(periodo2Id)).findFirst().orElse(null);

		if (p1 == null || p2 == null) return ResponseEntity.badRequest().body("Periodo no encontrado");

		List<AsientoContable> todos = asientoService.listar().stream()
			.filter(a -> "Activo".equals(a.getEstado()))
			.collect(java.util.stream.Collectors.toList());

		java.util.function.Function<PeriodoContable, java.util.Map<String, Double>> calcular = periodo -> {
			java.time.LocalDate ini = periodo.getFechaInicio();
			java.time.LocalDate fin = periodo.getFechaCierre();
			java.util.Map<String, Double> saldos = new java.util.LinkedHashMap<>();
			todos.stream()
				.filter(a -> a.getFechaRegistro() != null &&
					!a.getFechaRegistro().toLocalDate().isBefore(ini) &&
					!a.getFechaRegistro().toLocalDate().isAfter(fin))
				.forEach(a -> {
					String cta = a.getCuentaContable() != null ? a.getCuentaContable() : "—";
					char tipo = cta.charAt(0);
					if (tipo != '4' && tipo != '5') return;
					double debe = a.getDebe() != null ? a.getDebe() : 0;
					double haber = a.getHaber() != null ? a.getHaber() : 0;
					double delta = tipo == '5' ? haber - debe : debe - haber;
					saldos.put(cta, saldos.getOrDefault(cta, 0.0) + delta);
				});
			return saldos;
		};

		java.util.Map<String, Double> s1 = calcular.apply(p1);
		java.util.Map<String, Double> s2 = calcular.apply(p2);

		java.util.Set<String> cuentas = new java.util.LinkedHashSet<>();
		cuentas.addAll(s1.keySet());
		cuentas.addAll(s2.keySet());

		List<CuentaContable> cuentasList = cuentaService.listar();
		java.util.Map<String, String> descCuentas = new java.util.HashMap<>();
		for (CuentaContable c : cuentasList) descCuentas.put(c.getCodigo(), c.getDescripcion());

		java.util.List<java.util.Map<String, Object>> resultado = new java.util.ArrayList<>();
		for (String cta : cuentas) {
			double m1 = s1.getOrDefault(cta, 0.0);
			double m2 = s2.getOrDefault(cta, 0.0);
			double varS = m1 - m2;
			double varPct = m2 != 0 ? (varS / Math.abs(m2)) * 100 : 0;
			java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
			row.put("cta", cta);
			row.put("descripcion", descCuentas.getOrDefault(cta, cta));
			row.put("tipo", String.valueOf(cta.charAt(0)));
			row.put("monto1", m1);
			row.put("monto2", m2);
			row.put("varS", varS);
			row.put("varPct", Math.round(varPct * 100.0) / 100.0);
			resultado.add(row);
		}

		java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
		resp.put("periodo1", p1.getPeriodo());
		resp.put("periodo2", p2.getPeriodo());
		resp.put("filas", resultado);
		return ResponseEntity.ok(resp);
	}
	@GetMapping("/asientos/{id}/partidas")
	public ResponseEntity<?> getPartidas(@PathVariable("id") Integer id) {
	    String sql = "SELECT p.id, p.cuenta_contable, cc.descripcion, p.descripcion as glosa, p.debe, p.haber " +
	                 "FROM PartidasAsiento p " +
	                 "LEFT JOIN CuentasContables cc ON p.cuenta_contable = cc.codigo " +
	                 "WHERE p.asiento_id = ?";
	    return ResponseEntity.ok(jdbcTemplate.queryForList(sql, id));
	}
}