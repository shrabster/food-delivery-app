import { useEffect, useRef, useState } from 'react'
import { orderApi, restaurantApi } from '../api/client.js'

export default function CustomerPage() {
  const [restaurants, setRestaurants] = useState([])
  const [restaurantId, setRestaurantId] = useState('')
  const [menu, setMenu] = useState([])
  const [cart, setCart] = useState({})
  const [customerName, setCustomerName] = useState('')
  const [order, setOrder] = useState(null)
  const [error, setError] = useState('')
  const pollRef = useRef(null)

  useEffect(() => {
    restaurantApi.listRestaurants().then(setRestaurants).catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    if (!restaurantId) {
      setMenu([])
      return
    }
    setCart({})
    restaurantApi.getMenu(restaurantId).then(setMenu).catch((err) => setError(err.message))
  }, [restaurantId])

  useEffect(() => {
    return () => clearInterval(pollRef.current)
  }, [])

  function addToCart(item) {
    setCart((prev) => ({ ...prev, [item.id]: { item, quantity: (prev[item.id]?.quantity ?? 0) + 1 } }))
  }

  function removeFromCart(item) {
    setCart((prev) => {
      const next = { ...prev }
      const existing = next[item.id]
      if (!existing) return prev
      if (existing.quantity <= 1) {
        delete next[item.id]
      } else {
        next[item.id] = { item, quantity: existing.quantity - 1 }
      }
      return next
    })
  }

  const cartLines = Object.values(cart)
  const total = cartLines.reduce((sum, { item, quantity }) => sum + item.price * quantity, 0)

  async function placeOrder() {
    setError('')
    try {
      const placed = await orderApi.placeOrder({
        restaurantId: Number(restaurantId),
        customerName,
        items: cartLines.map(({ item, quantity }) => ({ name: item.name, price: item.price, quantity }))
      })
      setOrder(placed)
      setCart({})

      clearInterval(pollRef.current)
      pollRef.current = setInterval(async () => {
        const latest = await orderApi.getOrder(placed.id)
        setOrder(latest)
        if (latest.status === 'DELIVERED') {
          clearInterval(pollRef.current)
        }
      }, 3000)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div>
      <h2>Order food</h2>
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

      {menu.length > 0 && (
        <ul>
          {menu.map((item) => (
            <li key={item.id}>
              {item.name} — ${item.price.toFixed(2)}{' '}
              <button onClick={() => addToCart(item)}>Add</button>
              {cart[item.id] && (
                <>
                  {' '}
                  x{cart[item.id].quantity} <button onClick={() => removeFromCart(item)}>-</button>
                </>
              )}
            </li>
          ))}
        </ul>
      )}

      {cartLines.length > 0 && (
        <div>
          <h3>Cart — total ${total.toFixed(2)}</h3>
          <label>
            Your name:{' '}
            <input value={customerName} onChange={(e) => setCustomerName(e.target.value)} />
          </label>
          <div>
            <button disabled={!customerName} onClick={placeOrder}>
              Place order
            </button>
          </div>
        </div>
      )}

      {order && (
        <div style={{ marginTop: '1rem', padding: '1rem', border: '1px solid #ddd' }}>
          <h3>Order #{order.id}</h3>
          <p>Status: {order.status}</p>
          <p>Total: ${order.total.toFixed(2)}</p>
        </div>
      )}
    </div>
  )
}
