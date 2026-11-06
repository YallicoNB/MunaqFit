package com.munaqfit.backend.dto;

/**
 * Cuerpo que espera PUT /api/admin/usuarios/{id}/rol
 */
public class CambiarRolRequest {

    private String rol;

    public CambiarRolRequest() {}

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
