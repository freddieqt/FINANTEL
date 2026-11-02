package com.finantel.registrar_usuario.sistema;

import com.finantel.pantalla.PantallaBase;
import org.junit.jupiter.api.Test;

public class ValidacionesUsuarioPantallaSistemaTest extends PantallaBase {

    private void abrirNuevoUsuario() {
        iniciarComoAdmin();
        page.evaluate("openModalUsuario()");
        page.waitForSelector("#modal-usuario.open");
    }

    private void crear() {
        page.click("#modal-usuario button:has-text('Crear Usuario')");
    }

    // ENTRADA: [CU18 pantalla] nombre = "" (vacío); email = nuevo@finantel.pe; contraseña = 123456; rol = válido
    // RESULTADO: Se muestra el mensaje "Nombre es obligatorio"
    @Test
    void nombre_vacio_muestra_error() {
        abrirNuevoUsuario();
        page.locator("#modal-usuario input[type=text]").first().fill("");
        page.fill("#modal-usuario input[type=email]", "nuevo@finantel.pe");
        page.fill("#modal-usuario input[type=password]", "123456");
        page.evaluate("() => { const s = document.querySelector('#modal-usuario select'); s.selectedIndex = Array.from(s.options).findIndex(o => o.value !== ''); }");
        crear();
        assertToastContiene("Nombre es obligatorio");
    }

    // ENTRADA: [CU18 pantalla] nombre = "Prueba"; email = prueba.finantel.pe (sin @); contraseña = 123456; rol = válido
    // RESULTADO: Se muestra el mensaje "Email inválido"
    @Test
    void email_sin_arroba_muestra_error() {
        abrirNuevoUsuario();
        page.locator("#modal-usuario input[type=text]").first().fill("Prueba");
        page.fill("#modal-usuario input[type=email]", "prueba.finantel.pe");
        page.fill("#modal-usuario input[type=password]", "123456");
        page.evaluate("() => { const s = document.querySelector('#modal-usuario select'); s.selectedIndex = Array.from(s.options).findIndex(o => o.value !== ''); }");
        crear();
        assertToastContiene("Email inválido");
    }

    // ENTRADA: [CU18 pantalla] nombre = "Prueba"; email = nuevo@finantel.pe; contraseña = 123456; rol = opción vacía
    // RESULTADO: Se muestra el mensaje "Selecciona un rol"
    @Test
    void rol_sin_seleccionar_muestra_error() {
        abrirNuevoUsuario();
        page.locator("#modal-usuario input[type=text]").first().fill("Prueba");
        page.fill("#modal-usuario input[type=email]", "nuevo@finantel.pe");
        page.fill("#modal-usuario input[type=password]", "123456");
        page.evaluate("() => { document.querySelector('#modal-usuario select').selectedIndex = -1; }");
        crear();
        assertToastContiene("Selecciona un rol");
    }

    // ENTRADA: [CU18 pantalla] nombre = "Prueba"; email = nuevo@finantel.pe; contraseña = 12345 (5 caracteres); rol = válido
    // RESULTADO: Se muestra el mensaje "Contraseña mín. 6 caracteres"
    @Test
    void contrasena_de_5_caracteres_muestra_error() {
        abrirNuevoUsuario();
        page.locator("#modal-usuario input[type=text]").first().fill("Prueba");
        page.fill("#modal-usuario input[type=email]", "nuevo@finantel.pe");
        page.fill("#modal-usuario input[type=password]", "12345");
        page.evaluate("() => { const s = document.querySelector('#modal-usuario select'); s.selectedIndex = Array.from(s.options).findIndex(o => o.value !== ''); }");
        crear();
        assertToastContiene("Contraseña mín. 6 caracteres");
    }

    // ENTRADA: [Cambiar contraseña pantalla] contraseña actual = "" (vacía); nueva = 123456; confirmar = 123456
    // RESULTADO: Se muestra el mensaje "Ingresa tu contraseña actual"
    @Test
    void contrasena_actual_vacia_muestra_error() {
        iniciarComoAdmin();
        page.evaluate("cambiarPassword(1)");
        page.waitForSelector("#cp-actual");
        page.fill("#cp-actual", "");
        page.fill("#cp-nueva", "123456");
        page.fill("#cp-confirmar", "123456");
        page.click("button[onclick^='confirmarCambioPassword']");
        assertToastContiene("Ingresa tu contraseña actual");
    }

    // ENTRADA: [Cambiar contraseña pantalla] contraseña actual = "clave-actual"; nueva = 12345 (5 caracteres); confirmar = 12345
    // RESULTADO: Se muestra el mensaje "La nueva contraseña debe tener mínimo 6 caracteres"
    @Test
    void contrasena_nueva_de_5_caracteres_muestra_error() {
        iniciarComoAdmin();
        page.evaluate("cambiarPassword(1)");
        page.waitForSelector("#cp-actual");
        page.fill("#cp-actual", "clave-actual");
        page.fill("#cp-nueva", "12345");
        page.fill("#cp-confirmar", "12345");
        page.click("button[onclick^='confirmarCambioPassword']");
        assertToastContiene("La nueva contraseña debe tener mínimo 6 caracteres");
    }

    // ENTRADA: [Cambiar contraseña pantalla] contraseña actual = "clave-actual"; nueva = 123456; confirmar = 123457
    // RESULTADO: Se muestra el mensaje "Las contraseñas no coinciden"
    @Test
    void confirmacion_distinta_muestra_error() {
        iniciarComoAdmin();
        page.evaluate("cambiarPassword(1)");
        page.waitForSelector("#cp-actual");
        page.fill("#cp-actual", "clave-actual");
        page.fill("#cp-nueva", "123456");
        page.fill("#cp-confirmar", "123457");
        page.click("button[onclick^='confirmarCambioPassword']");
        assertToastContiene("Las contraseñas no coinciden");
    }

    // ENTRADA: [Cambiar contraseña pantalla] contraseña actual = "clave-actual"; nueva = 123456; confirmar = 123456
    // RESULTADO: No se muestra ningún error de validación de la pantalla
    @Test
    void contrasena_nueva_de_6_caracteres_pasa_la_validacion() {
        iniciarComoAdmin();
        page.evaluate("cambiarPassword(1)");
        page.waitForSelector("#cp-actual");
        page.fill("#cp-actual", "clave-actual");
        page.fill("#cp-nueva", "123456");
        page.fill("#cp-confirmar", "123456");
        page.click("button[onclick^='confirmarCambioPassword']");
        assertNoToastContiene("mínimo 6 caracteres");
        assertNoToastContiene("no coinciden");
    }
}
