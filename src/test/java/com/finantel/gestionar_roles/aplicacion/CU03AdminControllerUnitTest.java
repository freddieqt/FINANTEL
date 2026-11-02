package com.finantel.gestionar_roles.aplicacion;

import com.finantel.gestionar_roles.presentacion.AdminController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CU03AdminControllerUnitTest {

    @Test
    void admin_devuelve_la_vista_admin() {
        assertEquals("admin", new AdminController().admin());
    }
}