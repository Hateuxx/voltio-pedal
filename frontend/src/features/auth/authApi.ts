import { api } from '../../api/cliente'

export type RolUsuario = 'ADMINISTRADOR' | 'OPERADOR_TIENDA' | 'TECNICO'

/* Espejo de LoginRequest.java */
export interface LoginRequest {
    tipoDocumento: 'DNI' | 'CE' | 'PASAPORTE'
    numeroDocumento: string
    contrasena: string
}

/* Espejo de LoginResponse.java */
export interface LoginResponse {
    token: string
    tipoToken: string
    fechaExpiracion: string
    nombres: string
    apellidos: string
    rol: RolUsuario
}

export function login(datos: LoginRequest) {
    return api<LoginResponse>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(datos)
    })
}