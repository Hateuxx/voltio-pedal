import { obtenerUsuario } from "../auth/sesion";

function InicioPage() {
    const usuario = obtenerUsuario()
    return (
        <div>
            <h1 className='h3'>Bienvenido{usuario ? `, ${usuario.nombres}` : ''}</h1>
            <p className='text-muted'>Elige una opción del menú.</p>
        </div>
    )
}

export default InicioPage