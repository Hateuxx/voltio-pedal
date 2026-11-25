import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { cerrarSesion, obtenerUsuario } from "../features/auth/sesion";
import { MENU } from "../features/auth/permisos";

function Layout() {
    const navigate = useNavigate()
    const usuario = obtenerUsuario()
    const opciones = MENU.filter(o => usuario && o.roles.includes(usuario.rol))

    function salir() {
        cerrarSesion()
        navigate('/login', { replace: true })
    }

    return (
        <>
            <nav className="navbar navbar-expand bg-primary navbar-dark mb-4 d-print-none">
                <div className="container">
                    <NavLink to="/inicio" className="navbar-brand">Voltio & Pedal</NavLink>
                    <ul className="navbar-nav me-auto flex-wrap">
                        {opciones.map(o => (
                            <li className="nav-item" key={o.ruta}>
                                <NavLink to={o.ruta} className="nav-link">{o.texto}</NavLink>
                            </li>
                        ))}
                    </ul>
                    {usuario && <span className="navbar-text me-3">{usuario.nombres} ({usuario.rol})</span>}
                    <button className="btn btn-outline-light btn-sm" onClick={salir}>Cerrar sesión</button>
                </div>
            </nav>

            <main className="container">
                <Outlet />
            </main>
        </>
    )
}

export default Layout