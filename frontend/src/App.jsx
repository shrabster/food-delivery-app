import { NavLink, Route, HashRouter, Routes } from 'react-router-dom'
import CustomerPage from './pages/CustomerPage.jsx'
import RestaurantPage from './pages/RestaurantPage.jsx'
import CourierPage from './pages/CourierPage.jsx'

export default function App() {
  return (
    <HashRouter>
      <nav style={{ display: 'flex', gap: '1rem', padding: '1rem', borderBottom: '1px solid #ddd' }}>
        <NavLink to="/">Customer</NavLink>
        <NavLink to="/restaurant">Restaurant</NavLink>
        <NavLink to="/courier">Courier</NavLink>
      </nav>
      <main style={{ padding: '1rem', maxWidth: 720, margin: '0 auto' }}>
        <Routes>
          <Route path="/" element={<CustomerPage />} />
          <Route path="/restaurant" element={<RestaurantPage />} />
          <Route path="/courier" element={<CourierPage />} />
        </Routes>
      </main>
    </HashRouter>
  )
}
