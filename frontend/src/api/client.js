const ORDER_SERVICE_URL = import.meta.env.VITE_ORDER_SERVICE_URL || 'http://localhost:8081'
const RESTAURANT_SERVICE_URL = import.meta.env.VITE_RESTAURANT_SERVICE_URL || 'http://localhost:8082'
const DELIVERY_SERVICE_URL = import.meta.env.VITE_DELIVERY_SERVICE_URL || 'http://localhost:8083'

async function request(url, options) {
  const response = await fetch(url, options)
  if (!response.ok) {
    throw new Error(`Request to ${url} failed with status ${response.status}`)
  }
  if (response.status === 204) {
    return null
  }
  return response.json()
}

export const restaurantApi = {
  listRestaurants: () => request(`${RESTAURANT_SERVICE_URL}/restaurants`),
  getMenu: (restaurantId) => request(`${RESTAURANT_SERVICE_URL}/restaurants/${restaurantId}/menu`),
  getQueue: (restaurantId) => request(`${RESTAURANT_SERVICE_URL}/restaurants/${restaurantId}/queue`),
  markReady: (orderId) =>
    request(`${RESTAURANT_SERVICE_URL}/queue/${orderId}/ready`, { method: 'POST' })
}

export const orderApi = {
  placeOrder: (payload) =>
    request(`${ORDER_SERVICE_URL}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    }),
  getOrder: (orderId) => request(`${ORDER_SERVICE_URL}/orders/${orderId}`)
}

export const deliveryApi = {
  listCouriers: () => request(`${DELIVERY_SERVICE_URL}/couriers`),
  listDeliveries: (courierId) => request(`${DELIVERY_SERVICE_URL}/couriers/${courierId}/deliveries`),
  markPickedUp: (deliveryId) =>
    request(`${DELIVERY_SERVICE_URL}/deliveries/${deliveryId}/picked-up`, { method: 'POST' }),
  markDelivered: (deliveryId) =>
    request(`${DELIVERY_SERVICE_URL}/deliveries/${deliveryId}/delivered`, { method: 'POST' })
}
