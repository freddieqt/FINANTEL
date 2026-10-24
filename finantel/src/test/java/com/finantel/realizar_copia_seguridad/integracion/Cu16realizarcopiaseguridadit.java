package com.finantel.realizar_copia_seguridad.integracion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultHandler;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CU16RealizarCopiaSeguridadIT {

    private static final String URL_BD =
        "jdbc:sqlserver://localhost:1433;databaseName=FINANTEL_TEST;encrypt=false;trustServerCertificate=true";

    @Autowired
    private MockMvc mvc;

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection c = DriverManager.getConnection(URL_BD, "sa", "admin098");
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM RespaldosBD WHERE usuario = ? AND tipo = 'Manual'")) {
            ps.setString(1, "admin@finantel.pe");
            ps.executeUpdate();
        }
    }

    @Test
    void CP_I1_listar_copias_responde_un_arreglo() throws Exception {
    	mvc.perform(get("/copia-seguridad")
    	        .with(user("admin@finantel.pe")))
    	    .andDo(print())
    	    .andExpect(status().isOk())
    	    .andExpect(jsonPath("$").isArray());
    }

 

	@Test
    void CP_I2_realizar_copia_responde_con_confirmacion() throws Exception {
        mvc.perform(post("/copia-seguridad")
        		.with(user("admin@finantel.pe").roles("ADMINISTRADOR"))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("realizada correctamente")));
    }
}