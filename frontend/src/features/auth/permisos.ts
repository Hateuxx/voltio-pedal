import type { RolUsuario } from "./authApi";

export interface OpcionMenu {
    ruta: string
    texto: string
    roles: RolUsuario[]
}

/* Espejo de la tabla "Roles y permisos" del README */
export const MENU: OpcionMenu[] = [
    { ruta: '/proveedores', texto: 'Proveedores', roles: ['ADMINISTRADOR', 'OPERADOR_TIENDA']},
]

export function rolesDe(ruta: string): RolUsuario[] {
    return MENU.find(o => o.ruta === ruta)?.roles ?? []
}