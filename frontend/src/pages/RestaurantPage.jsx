import { useEffect, useState } from 'react'
import { restaurantApi } from '../api/client.js'

export default function RestaurantPage() {
  const [restaurants, setRestaurants] = useState([])
  const [restaurantId, setRestaurantId] = useState('')
  const [queue, setQueue] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    restaurantApi.listRestaurants().then(setRestaurants).catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    if (!restaurantId) {
      setQueue([])
      return
    }
    const load = () => restaurantApi.getQueue(restaurantId).then(setQueue).catch((err) => setError(err.message))
    load()
    const interval = setInterval(load, 3000)
    return () => clearInterval(interval)
  }, [restaurantId])

  async function markReady(orderId) {
    setError('')
    try {
      await restaurantApi.markReady(orderId)
      const updated = await restaurantApi.getQueue(restaurantId)
      setQueue(updated)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div>
      <h2>Kitchen queue</h2>
      {error && <p style={{ color: 'crimson' }}>{error}</p>}

      <label>
        Restaurant:{' '}
        <select value={restaurantId} onChange={(e) => setRestaurantId(e.target.value)}>
          <option value="">Select a restaurant</option>
          {restaurants.map((r) => (
            <option key={r.id} value={r.id}>
              {r.name}
            </option>
          ))}
        </select>
      </label>

      <ul>
        {queue.map((entry) => (
          <li key={entry.orderId} style={{ marginBottom: '0.5rem' }}>
            <strong>Order #{entry.orderId}</strong> for {entry.customerName} — {entry.status}
            <ul>
              {entry.items.map((item, idx) => (
                <li key={idx}>
                  {item.quantity}x {item.name}
                </li>
              ))}
            </ul>
            {entry.status === 'PENDING' && <button onClick={() => markReady(entry.orderId)}>Mark ready</button>}
          </li>
        ))}
      </ul>
    </div>
  )
}
