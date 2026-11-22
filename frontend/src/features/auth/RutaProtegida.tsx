import { Navigate } from "react-router-dom";
import type { ReactNode } from "react";
import { obtenerToken } from "./sesion";

function RutaProtegida({ children }: { children: ReactNode}) {
    if (!obtenerToken()) {
        return <Navigate to='/login' replace />
    }
    return children
}

export default RutaProtegida