import type { RolUsuario } from "./authApi";

export interface OpcionMenu {
    ruta: string
    texto: string
    roles: RolUsuario[]
}

/* Espejo de la tabla "Roles y permisos" del README */
export const MENU: OpcionMenu[] = [
    { ruta: '/proveedores', texto: 'Proveedores', roles: ['ADMINISTRADOR', 'OPERADOR_TIENDA']},
    { ruta: '/tipos-repuesto', texto: 'Tipos de repuesto', roles: ['ADMINISTRADOR', 'OPERADOR_TIENDA', 'TECNICO'] },
    { ruta: '/mercaderia', texto: 'Mercadería', roles: ['ADMINISTRADOR', 'OPERADOR_TIENDA', 'TECNICO'] },
    { ruta: '/ingresos', texto: 'Ingresos', roles: ['ADMINISTRADOR', 'OPERADOR_TIENDA'] },
    { ruta: '/conteos', texto: 'Conteos', roles: ['ADMINISTRADOR', 'OPERADOR_TIENDA'] },
    { ruta: '/clientes', texto: 'Clientes', roles: ['ADMINISTRADOR', 'OPERADOR_TIENDA', 'TECNICO'] },
    { ruta: '/ventas', texto: 'Ventas', roles: ['ADMINISTRADOR', 'OPERADOR_TIENDA'] },
    { ruta: '/servicios', texto: 'Servicio técnico', roles: ['ADMINISTRADOR', 'OPERADOR_TIENDA', 'TECNICO'] },
    { ruta: '/reportes', texto: 'Reportes', roles: ['ADMINISTRADOR'] },
    { ruta: '/auditoria', texto: 'Auditoría', roles: ['ADMINISTRADOR'] },
]

export function rolesDe(ruta: string): RolUsuario[] {
    return MENU.find(o => o.ruta === ruta)?.roles ?? []
}