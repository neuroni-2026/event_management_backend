import Home from './Components/Home';
import EventCardDetails from './Components/EventCardDetails';
import AuthPage from './Components/Auth';
import './App.css';
import { Routes, Route } from 'react-router-dom';


function App() {
  return (
    <div>
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/event_detalii" element={<EventCardDetails />} />
      <Route path="/login" element={<AuthPage />} />
    </Routes>
      
  </div>
  );
}

export default App;
