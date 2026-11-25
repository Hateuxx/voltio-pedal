/* Formatos compartidos (solo para MOSTRAR: los cálculos de dinero los hace el backend con BigDecimal) */
export const soles = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })

export function fechaHora(iso: string | null | undefined) {
    return iso ? new Date(iso).toLocaleString('es-PE', { dateStyle: 'short', timeStyle: 'short' }) : '—'
}

/* "2026-11-25" -> "25/11/2026" sin pasar por zonas horarias */
export function fecha(iso: string | null | undefined) {
    if (!iso) return '—'
    const [a, m, d] = iso.slice(0, 10).split('-')
    return `${d}/${m}/${a}`
}

/* Fecha local de hoy en formato ISO (yyyy-mm-dd) para los <input type="date"> */
export function hoyIso(desplazamientoDias = 0) {
    const d = new Date()
    d.setDate(d.getDate() + desplazamientoDias)
    const z = (n: number) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${z(d.getMonth() + 1)}-${z(d.getDate())}`
}
