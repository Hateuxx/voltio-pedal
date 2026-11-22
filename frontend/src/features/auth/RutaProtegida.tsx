import { Navigate } from "react-router-dom";
import type { ReactNode } from "react";
import { obtenerToken, obtenerUsuario } from "./sesion";
import type { RolUsuario } from "./authApi";

function RutaProtegida({ children, roles }: { children: ReactNode; roles?:RolUsuario[]}) {
    if (!obtenerToken()) {
        return <Navigate to='/login' replace />
    }

    const usuario = obtenerUsuario()
    if (roles && (!usuario || !roles.includes(usuario.rol))) {
        return <div className='alert alert-warning'>No tienes permiso para ver esta sección.</div>
    }
    return children
}

export default RutaProtegida