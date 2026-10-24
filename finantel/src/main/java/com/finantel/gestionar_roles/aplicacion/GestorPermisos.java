package com.finantel.gestionar_roles.aplicacion;

import org.springframework.stereotype.Component;

@Component
public class GestorPermisos {

    public boolean tienePermiso(String rol, String accion) {
        if (rol == null || accion == null || rol.isBlank() || accion.isBlank()) {
            return false;
        }
        switch (rol) {
            case "Administrador":
                return true;
            case "Contador General":
                return accion.equals("crear") || accion.equals("editar") || accion.equals("ver");
            case "Auditor":
                return accion.equals("ver");
            case "Personal Autorizado":
                return accion.equals("ver");
            default:
                return false;
        }
    }
}