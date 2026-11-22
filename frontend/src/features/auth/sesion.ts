import type { LoginResponse, RolUsuario } from "./authApi";

const CLAVE_TOKEN = 'token'
const CLAVE_USUARIO = 'usuario'

export interface UsuarioSesion {
    nombres: string
    rol: RolUsuario
}

export function guardarSesion(respuesta: LoginResponse) {
    localStorage.setItem(CLAVE_TOKEN, respuesta.token)
    localStorage.setItem(
        CLAVE_USUARIO,
        JSON.stringify({ nombres: respuesta.nombres, rol: respuesta.rol })
    )
}

export function obtenerToken(): string | null {
    return localStorage.getItem(CLAVE_TOKEN)
}

export function obtenerUsuario(): UsuarioSesion | null {
    const texto = localStorage.getItem(CLAVE_USUARIO)
    return texto ? JSON.parse(texto) : null
}

export function cerrarSesion() {
    localStorage.removeItem(CLAVE_TOKEN)
    localStorage.removeItem(CLAVE_USUARIO)
}