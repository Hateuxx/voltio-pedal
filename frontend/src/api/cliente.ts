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

    /* 204: backend no envía cuerpo (desactivar, reactivar, restablecer...) */
    if (respuesta.status === 204) {
        return undefined as T
    }

    const cuerpo = await respuesta.json()

    if (respuesta.status === 401 && token) {
        cerrarSesion()
        window.location.replace('/login')
    }

    if (!respuesta.ok) {
        throw new ErrorApi(cuerpo as ProblemaApi)
    }

    return cuerpo as T
}