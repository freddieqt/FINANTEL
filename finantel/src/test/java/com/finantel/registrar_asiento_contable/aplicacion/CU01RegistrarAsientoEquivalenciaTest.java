package com.finantel.registrar_asiento_contable.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CU01RegistrarAsientoEquivalenciaTest {

    @Mock
    private AsientoContableRepository repo;

    @Mock
    private AuditoriaService auditoria;

    @Mock
    private ValidarCuentasRepository cuentaRepo;

    @InjectMocks
    private AsientoContableService service;

    @BeforeEach
    void configurar() {
        when(repo.count()).thenReturn(0L);
        when(repo.existsByNumeroCorrelativo(anyString())).thenReturn(false);
        cuentaActiva("1011");
    }

    // ---------- Helpers ----------

    private AsientoContable asientoValido() {
        AsientoContable a = new AsientoContable();
        a.setTipo("Operación ordinaria");
        a.setDescripcion("Cobro cuota crédito");
        a.setDebe(100.0);
        a.setHaber(100.0);
        a.setCuentaContable("1011");
        a.setMoneda("PEN");
        return a;
    }

    private void cuentaActiva(String codigo) {
        CuentaContable c = new CuentaContable();
        c.setCodigo(codigo);
        c.setEstado("Activa");
        when(cuentaRepo.findById(codigo)).thenReturn(Optional.of(c));
    }

    private void cuentaInactiva(String codigo) {
        CuentaContable c = new CuentaContable();
        c.setCodigo(codigo);
        c.setEstado("Inactiva");
        when(cuentaRepo.findById(codigo)).thenReturn(Optional.of(c));
    }

    private void debeSerAceptado(AsientoContable a) {
        assertDoesNotThrow(() -> service.crear(a));
        verify(repo).save(any(AsientoContable.class));
    }

    private void debeSerRechazado(AsientoContable a, String fragmentoEsperado) {
        RuntimeException e = assertThrows(RuntimeException.class, () -> service.crear(a));
        assertTrue(e.getMessage().contains(fragmentoEsperado),
            "Mensaje esperado con '" + fragmentoEsperado + "', recibido: " + e.getMessage());
        verify(repo, never()).save(any());
    }

    // ---------- CP-15 a CP-17: tipo ----------

    @DisplayName("CP-15 · Tipo válido (clase válida): se acepta")
    @ParameterizedTest
    @ValueSource(strings = {"Apertura", "Operación ordinaria", "Ajuste", "Cierre", "Reversión"})
    void cp15_tipoValido_seAcepta(String tipo) {
        AsientoContable a = asientoValido();
        a.setTipo(tipo);
        debeSerAceptado(a);
    }

    @DisplayName("CP-16 · Tipo inválido (clases inválidas): se rechaza")
    @ParameterizedTest
    @ValueSource(strings = {"Operacion ordinaria", "operación ordinaria", "Desembolso de Crédito"})
    void cp16_tipoInvalido_seRechaza(String tipo) {
        AsientoContable a = asientoValido();
        a.setTipo(tipo);
        debeSerRechazado(a, "Tipo inválido");
    }

    @DisplayName("CP-17 · Tipo nulo: se rechaza")
    @Test
    void cp17_tipoNulo_seRechaza() {
        AsientoContable a = asientoValido();
        a.setTipo(null);
        debeSerRechazado(a, "Tipo inválido");
    }

    // ---------- CP-18: descripción ----------

    @DisplayName("CP-18 · Descripción de un carácter (límite inferior válido): se acepta")
    @Test
    void cp18_descripcionUnCaracter_seAcepta() {
        AsientoContable a = asientoValido();
        a.setDescripcion("A");
        debeSerAceptado(a);
    }

    @DisplayName("CP-18b · Descripción vacía o solo espacios (inválidas): se rechaza")
    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void cp18b_descripcionVaciaOEspacios_seRechaza(String descripcion) {
        AsientoContable a = asientoValido();
        a.setDescripcion(descripcion);
        debeSerRechazado(a, "Descripción es obligatoria");
    }

    @DisplayName("CP-18c · Descripción nula: se rechaza")
    @Test
    void cp18c_descripcionNula_seRechaza() {
        AsientoContable a = asientoValido();
        a.setDescripcion(null);
        debeSerRechazado(a, "Descripción es obligatoria");
    }

    // ---------- CP-19 a CP-20: debe y haber (valores límite) ----------

    @DisplayName("CP-19 · Debe y haber en 0,01 (límite inferior válido): se acepta")
    @Test
    void cp19_debeYHaberEnUnCentimo_seAcepta() {
        AsientoContable a = asientoValido();
        a.setDebe(0.01);
        a.setHaber(0.01);
        debeSerAceptado(a);
    }

    @DisplayName("CP-19b · Debe igual a 0 (límite): se rechaza")
    @Test
    void cp19b_debeCero_seRechaza() {
        AsientoContable a = asientoValido();
        a.setDebe(0.0);
        a.setHaber(0.0);
        debeSerRechazado(a, "'debe' debe ser mayor a 0");
    }

    @DisplayName("CP-19c · Debe negativo: se rechaza")
    @Test
    void cp19c_debeNegativo_seRechaza() {
        AsientoContable a = asientoValido();
        a.setDebe(-5.0);
        debeSerRechazado(a, "'debe' debe ser mayor a 0");
    }

    @DisplayName("CP-19d · Debe nulo: se rechaza")
    @Test
    void cp19d_debeNulo_seRechaza() {
        AsientoContable a = asientoValido();
        a.setDebe(null);
        debeSerRechazado(a, "'debe' debe ser mayor a 0");
    }

    @DisplayName("CP-20 · Haber igual a 0 (límite): se rechaza")
    @Test
    void cp20_haberCero_seRechaza() {
        AsientoContable a = asientoValido();
        a.setHaber(0.0);
        debeSerRechazado(a, "'haber' debe ser mayor a 0");
    }

    @DisplayName("CP-20b · Haber negativo y nulo: se rechazan")
    @Test
    void cp20b_haberNegativoONulo_seRechazan() {
        AsientoContable negativo = asientoValido();
        negativo.setHaber(-5.0);
        debeSerRechazado(negativo, "'haber' debe ser mayor a 0");

        AsientoContable nulo = asientoValido();
        nulo.setHaber(null);
        debeSerRechazado(nulo, "'haber' debe ser mayor a 0");
    }

    // ---------- CP-21: cuadre (valores límite de la tolerancia) ----------

    @DisplayName("CP-21a · Diferencia cero: se acepta")
    @Test
    void cp21a_diferenciaCero_seAcepta() {
        debeSerAceptado(asientoValido());
    }

    @DisplayName("CP-21b · Diferencia exacta de 0,01 (límite de tolerancia): se acepta")
    @Test
    void cp21b_diferenciaDeUnCentimo_seAcepta() {
        AsientoContable a = asientoValido();
        a.setDebe(100.00);
        a.setHaber(99.99);
        debeSerAceptado(a);
    }

    @DisplayName("CP-21c · Diferencia de 0,02 (fuera de tolerancia): se rechaza")
    @Test
    void cp21c_diferenciaDeDosCentimos_seRechaza() {
        AsientoContable a = asientoValido();
        a.setDebe(100.00);
        a.setHaber(99.98);
        debeSerRechazado(a, "no cuadra");
    }

    @DisplayName("CP-21d · Diferencia grande (100 contra 80): se rechaza")
    @Test
    void cp21d_diferenciaGrande_seRechaza() {
        AsientoContable a = asientoValido();
        a.setDebe(100.0);
        a.setHaber(80.0);
        debeSerRechazado(a, "no cuadra");
    }

    // ---------- CP-22: cuenta contable ----------

    @DisplayName("CP-22 · Cuenta nula, vacía o solo espacios: se rechaza")
    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void cp22_cuentaVaciaOEspacios_seRechaza(String cuenta) {
        AsientoContable a = asientoValido();
        a.setCuentaContable(cuenta);
        debeSerRechazado(a, "La cuenta contable es obligatoria");
    }

    @DisplayName("CP-22b · Cuenta nula: se rechaza")
    @Test
    void cp22b_cuentaNula_seRechaza() {
        AsientoContable a = asientoValido();
        a.setCuentaContable(null);
        debeSerRechazado(a, "La cuenta contable es obligatoria");
    }

    @DisplayName("CP-22c · Cuenta inexistente: se rechaza")
    @Test
    void cp22c_cuentaInexistente_seRechaza() {
        when(cuentaRepo.findById("9999")).thenReturn(Optional.empty());
        AsientoContable a = asientoValido();
        a.setCuentaContable("9999");
        debeSerRechazado(a, "no existe");
    }

    @DisplayName("CP-22d · Cuenta inactiva: se rechaza")
    @Test
    void cp22d_cuentaInactiva_seRechaza() {
        cuentaInactiva("1011");
        debeSerRechazado(asientoValido(), "inactiva");
    }

    @DisplayName("CP-22e · Cuenta con espacios alrededor (1011 con espacios): se acepta y se recorta")
    @Test
    void cp22e_cuentaConEspacios_seAceptaYSeRecorta() {
        AsientoContable a = asientoValido();
        a.setCuentaContable("  1011  ");
        debeSerAceptado(a);
        assertEquals("1011", a.getCuentaContable());
    }

    // ---------- CP-23: moneda ----------

    @DisplayName("CP-23 · Moneda válida (PEN, USD): se acepta")
    @ParameterizedTest
    @ValueSource(strings = {"PEN", "USD"})
    void cp23_monedaValida_seAcepta(String moneda) {
        AsientoContable a = asientoValido();
        a.setMoneda(moneda);
        debeSerAceptado(a);
    }

    @DisplayName("CP-23b · Moneda inválida (EUR, minúscula pen): se rechaza")
    @ParameterizedTest
    @ValueSource(strings = {"EUR", "pen"})
    void cp23b_monedaInvalida_seRechaza(String moneda) {
        AsientoContable a = asientoValido();
        a.setMoneda(moneda);
        debeSerRechazado(a, "La moneda es obligatoria");
    }

    @DisplayName("CP-23c · Moneda nula: se rechaza")
    @Test
    void cp23c_monedaNula_seRechaza() {
        AsientoContable a = asientoValido();
        a.setMoneda(null);
        debeSerRechazado(a, "La moneda es obligatoria");
    }
}