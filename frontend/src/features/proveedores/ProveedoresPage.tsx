import { useNavigate } from 'react-router-dom'
import { cerrarSesion, obtenerUsuario } from '../auth/sesion'

function ProveedoresPage() {
    const navigate = useNavigate()
    const usuario = obtenerUsuario()

    function salir() {
        cerrarSesion()
        navigate('/login', { replace: true })
    }

    return (
        <div className="container py-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h1 className="h3 text-primary">Proveedores</h1>
                <div>
                    <span className="me-3 text-muted">{usuario?.nombres} ({usuario?.rol})</span>
                    <button className="btn btn-outline-secondary btn-sm" onClick={salir}>Cerrar sesión</button>
                </div>
            </div>
            <p>Aquí irá la tabla.</p>
        </div>
    )
}

export default ProveedoresPage