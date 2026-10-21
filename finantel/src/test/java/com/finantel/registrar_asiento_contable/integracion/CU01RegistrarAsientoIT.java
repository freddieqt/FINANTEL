package com.finantel.registrar_asiento_contable.integracion;

import com.finantel.validar_cuentas.dominio.entity.CuentaContable;
import com.finantel.validar_cuentas.dominio.repository.ValidarCuentasRepository;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import com.finantel.registrar_usuario.dominio.entity.Usuario;
import com.finantel.registrar_usuario.dominio.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class CU01RegistrarAsientoIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AsientoContableRepository asientoRepo;

    @Autowired
    private ValidarCuentasRepository cuentaRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    private static final String USUARIO = "admin@finantel.pe";

    private static final String JSON_VALIDO = """
        {"tipo":"Operación ordinaria","descripcion":"Cobro cuota crédito",
         "debe":100.0,"haber":100.0,"cuentaContable":"1011","moneda":"PEN",
         "tipoCambio":1.0,"periodoId":1}
        """;

    private ResultActions enviar(String json) throws Exception {
        return mvc.perform(post("/asientos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .principal(() -> USUARIO));
    }

    @DisplayName("CP-24 · Asiento válido por POST se guarda con el usuario autenticado")
    @Test
    void cp24_asientoValido_seGuardaConUsuario() throws Exception {
        if (usuarioRepo.findByEmail(USUARIO).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNombre("Usuario de prueba");
            usuario.setEmail(USUARIO);
            usuario.setPassword("password-de-prueba");
            usuario.setRol("Administrador");
            usuario.setEstado("Activo");
            usuario.setIntentos(0);
            usuarioRepo.saveAndFlush(usuario);
        }

        if (cuentaRepo.findById("1011").isEmpty()) {
            CuentaContable activa = new CuentaContable();
            activa.setCodigo("1011");
            activa.setDescripcion("Cuenta de prueba activa");
            activa.setClasificacion("Activo Corriente");
            activa.setTipo("Débito");
            activa.setEstado("Activa");
            cuentaRepo.saveAndFlush(activa);
        }

        long antes = asientoRepo.count();

        var respuesta = enviar(JSON_VALIDO).andReturn().getResponse();
        assertEquals(200, respuesta.getStatus(), respuesta.getContentAsString());

        assertEquals(antes + 1, asientoRepo.count());
        var guardados = asientoRepo.findByUsuario(USUARIO);
        assertFalse(guardados.isEmpty());
        assertTrue(guardados.stream().anyMatch(a -> "1011".equals(a.getCuentaContable())));
    }

    @DisplayName("CP-25 · Asiento descuadrado por POST se rechaza y no se guarda")
    @Test
    void cp25_asientoDescuadrado_seRechazaYNoSeGuarda() throws Exception {
        long antes = asientoRepo.count();
        String json = """
            {"tipo":"Operación ordinaria","descripcion":"Cobro cuota crédito",
             "debe":100.0,"haber":80.0,"cuentaContable":"1011","moneda":"PEN","periodoId":1}
            """;

        enviar(json)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("no cuadra")));

        assertEquals(antes, asientoRepo.count());
    }

    @DisplayName("CP-26 · Cuenta inexistente por POST se rechaza y no se guarda")
    @Test
    void cp26_cuentaInexistente_seRechazaYNoSeGuarda() throws Exception {
        long antes = asientoRepo.count();
        String json = """
            {"tipo":"Operación ordinaria","descripcion":"Cobro cuota crédito",
             "debe":100.0,"haber":100.0,"cuentaContable":"9999","moneda":"PEN","periodoId":1}
            """;

        enviar(json)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("no existe")));

        assertEquals(antes, asientoRepo.count());
    }

    @DisplayName("CP-27 · Cuenta inactiva por POST se rechaza y no se guarda")
    @Test
    void cp27_cuentaInactiva_seRechazaYNoSeGuarda() throws Exception {
        CuentaContable inactiva = new CuentaContable();
        inactiva.setCodigo("1099");
        inactiva.setDescripcion("Cuenta de prueba inactiva");
        inactiva.setClasificacion("Activo Corriente");
        inactiva.setTipo("Débito");
        inactiva.setEstado("Inactiva");
        cuentaRepo.saveAndFlush(inactiva);

        long antes = asientoRepo.count();
        String json = """
            {"tipo":"Operación ordinaria","descripcion":"Cobro cuota crédito",
             "debe":100.0,"haber":100.0,"cuentaContable":"1099","moneda":"PEN","periodoId":1}
            """;

        enviar(json)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("inactiva")));

        assertEquals(antes, asientoRepo.count());
    }

    @DisplayName("CP-28 · Tipo inválido por POST se rechaza y no se guarda")
    @Test
    void cp28_tipoInvalido_seRechazaYNoSeGuarda() throws Exception {
        long antes = asientoRepo.count();
        String json = """
            {"tipo":"Desembolso de Crédito","descripcion":"Cobro cuota crédito",
             "debe":100.0,"haber":100.0,"cuentaContable":"1011","moneda":"PEN","periodoId":1}
            """;

        enviar(json)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("Tipo inválido")));

        assertEquals(antes, asientoRepo.count());
    }

    @DisplayName("CP-29 · Moneda omitida por POST se rechaza (el POST no asigna PEN por defecto)")
    @Test
    void cp29_monedaOmitida_seRechaza() throws Exception {
        long antes = asientoRepo.count();
        String json = """
            {"tipo":"Operación ordinaria","descripcion":"Cobro cuota crédito",
             "debe":100.0,"haber":100.0,"cuentaContable":"1011","periodoId":1,"tipoCambio":1.0}
            """;

        enviar(json)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("La moneda es obligatoria")));

        assertEquals(antes, asientoRepo.count());
    }
}