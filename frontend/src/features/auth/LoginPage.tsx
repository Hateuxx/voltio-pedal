import { useState } from 'react'
import { login } from './authApi'
import { ErrorApi } from '../../api/cliente'

function LoginPage() {
    const [numeroDocumento, setNumeroDocumento] = useState('')
    const [contrasena, setContrasena] = useState('')
    const [error, setError] = useState<string | null>(null)
    const [cargando, setCargando] = useState(false)

    async function manejarEnvio(e: React.FormEvent) {
        e.preventDefault()
        setError(null)
        setCargando(true)

        try {
            const respuesta = await login({ tipoDocumento: 'DNI', numeroDocumento, contrasena})
            localStorage.setItem('token', respuesta.token)
            alert(`Bienvenido, ${respuesta.nombres} (${respuesta.rol})`) /* temporal: luego navegaremos */
        } catch (err) {
            if (err instanceof ErrorApi) {
                setError(err.problema.detail) /* 401, 429, 400... el mensaje del backend  */
            } else {
                setError('No se pudo conectar con el servidor.')
            }
        } finally {
            setCargando(false)
        }
    }

    return (
        <div className='container py-5' style={{ maxWidth: 420 }}>
            <h1 className='h3 mb-4 text-primary'>Voltio & Pedal</h1>

            <form onSubmit={manejarEnvio}>
                <div className='mb-3'>
                    <label htmlFor="documento" className='form-label'>DNI</label>
                    <input id='documento' className='form-control'
                        value={numeroDocumento}
                        onChange={e => setNumeroDocumento(e.target.value)} />
                </div>

                <div className='mb-3'>
                    <label htmlFor="contrasena" className='form-label'>Contraseña</label>
                    <input id='contrasena' type='password' className='form-control'
                        value={contrasena}
                        onChange={e => setContrasena(e.target.value)} />

                </div>

                {error && <div className='alert alert-danger'>{error}</div>}

                <button type='submit' className='btn btn-primary w-100' disabled={cargando}>
                    {cargando ? 'Ingresando...' : 'Ingresar'}
                </button>
            </form>
        </div>
    )
}

export default LoginPage