import { useEffect, useState } from 'react'
import { deliveryApi } from '../api/client.js'

export default function CourierPage() {
  const [couriers, setCouriers] = useState([])
  const [courierId, setCourierId] = useState('')
  const [deliveries, setDeliveries] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    deliveryApi.listCouriers().then(setCouriers).catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    if (!courierId) {
      setDeliveries([])
      return
    }
    const load = () => deliveryApi.listDeliveries(courierId).then(setDeliveries).catch((err) => setError(err.message))
    load()
    const interval = setInterval(load, 3000)
    return () => clearInterval(interval)
  }, [courierId])

  async function updateStatus(deliveryId, action) {
    setError('')
    try {
      if (action === 'picked-up') {
        await deliveryApi.markPickedUp(deliveryId)
      } else {
        await deliveryApi.markDelivered(deliveryId)
      }
      const updated = await deliveryApi.listDeliveries(courierId)
      setDeliveries(updated)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div>
      <h2>My deliveries</h2>
      {error && <p style={{ color: 'crimson' }}>{error}</p>}

      <label>
        Courier:{' '}
        <select value={courierId} onChange={(e) => setCourierId(e.target.value)}>
          <option value="">Select a courier</option>
          {couriers.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name} {c.available ? '' : '(busy)'}
            </option>
          ))}
        </select>
      </label>

      <ul>
        {deliveries.map((delivery) => (
          <li key={delivery.id} style={{ marginBottom: '0.5rem' }}>
            Order #{delivery.orderId} — {delivery.status}
            {delivery.status === 'ASSIGNED' && (
              <button onClick={() => updateStatus(delivery.id, 'picked-up')}>Mark picked up</button>
            )}
            {delivery.status === 'PICKED_UP' && (
              <button onClick={() => updateStatus(delivery.id, 'delivered')}>Mark delivered</button>
            )}
          </li>
        ))}
      </ul>
    </div>
  )
}
