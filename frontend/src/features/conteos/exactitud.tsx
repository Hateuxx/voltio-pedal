/* Badge de exactitud: verde ≥ 95 %, amarillo ≥ 80 %, rojo por debajo */
export default function BadgeExactitud({ valor }: { valor: number | null }) {
    if (valor === null) return <span className="text-muted">—</span>
    const color = valor >= 95 ? 'text-bg-success' : valor >= 80 ? 'text-bg-warning' : 'text-bg-danger'
    return <span className={`badge ${color}`}>{valor}%</span>
}