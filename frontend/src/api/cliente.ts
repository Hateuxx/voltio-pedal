import { cerrarSesion, obtenerToken } from "../features/auth/sesion"

const API_URL = import.meta.env.VITE_API_URL

/* Forma de los errores del backend: el ProblemDetail de ManejadorGlobalExcepciones */
export interface ProblemaApi {
    status: number
    title: string
    detail: string
    errores?: Record<string, string> /* solo en los 400 de validación */
}

/* Error que lanzan todas las llamadas que fallan */
export class ErrorApi extends Error {
    problema: ProblemaApi

    constructor(problema: ProblemaApi) {
        super(problema.detail)
        this.problema = problema
    }
}

export async function api<T>(ruta: string, opciones: RequestInit = {}): Promise<T> {
    const token = obtenerToken()

    const respuesta = await fetch(`${API_URL}${ruta}`, {
        ...opciones,
        headers: {
            'Content-Type': 'application/json',
            ...(token ? {Authorization: `Bearer ${token}`} : {}),
            ...opciones.headers,
        },
    })

    /* Revisar el 401 (no depende de que exista el cuerpo JSON) */
    if (respuesta.status === 401 && token) {
        cerrarSesion()
        window.location.replace('/login')
        return new Promise<T>(() => {})
    }

    /* 204: Repuestas exitosas sin cuerpo (desactivar, reactivar, restablecer...)-(DELETE 204 No Content) */
    if (respuesta.status === 204) {
        return undefined as T
    }

    /* Recién aquí parseo el JSON (datos o ProblemDetail) */
    const cuerpo = await respuesta.json()

    if (!respuesta.ok) {
        throw new ErrorApi(cuerpo as ProblemaApi)
    }

    return cuerpo as T
}

/* Mensaje legible para cualquier error de api() */
export function mensajeDeError(err: unknown) {
    return err instanceof ErrorApi ? err.problema.detail : 'No se pudo conectar con el servidor.'
}