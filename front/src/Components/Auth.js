import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Auth.css';

const AuthPage = () => {
  const [isLogin, setIsLogin] = useState(true);
  const navigate = useNavigate();
  

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: ''
  });


  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const toggleMode = () => {
    setIsLogin(!isLogin);
    setMessage('');
    setError('');

    setFormData({ firstName: '', lastName: '', email: '', password: '', confirmPassword: '' });
  };


  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };


  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');


    if (!isLogin && formData.password !== formData.confirmPassword) {
      setError("Parolele nu coincid!");
      return;
    }


    const baseUrl = 'http://localhost:8080/api/auth';
    const url = isLogin ? `${baseUrl}/signin` : `${baseUrl}/signup`;


    const bodyData = isLogin 
      ? { email: formData.email, password: formData.password }
      : { 
          firstName: formData.firstName, 
          lastName: formData.lastName, 
          email: formData.email, 
          password: formData.password 
        };

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(bodyData),
      });

      const data = await response.json();

      if (!response.ok) {

        throw new Error(data.message || 'Ceva nu a mers bine.');
      }

      if (isLogin) {

        console.log("Login reusit:", data);

        localStorage.setItem('user', JSON.stringify(data)); 
        navigate('/home'); 
      } else {

        console.log(" Inregistrare reusita:", data);
        setMessage("Cont creat cu succes! Te rugam sa te autentifici.");
        setIsLogin(true); 
        setFormData({ firstName: '', lastName: '', email: '', password: '', confirmPassword: '' });
      }

    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        
        <h2 className="auth-title">
          {isLogin ? 'Bine ai venit!' : 'Creeaza cont'}
        </h2>
        <p className="auth-subtitle">
          {isLogin 
            ? 'Introdu datele pentru a te autentifica.' 
            : 'Completeaza detaliile pentru a te inregistra.'}
        </p>


        {error && <div style={{color: 'red', marginBottom: '10px'}}>{error}</div>}
        {message && <div style={{color: 'green', marginBottom: '10px'}}>{message}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          
          {!isLogin && (
            <>

              <div className="form-group">
                <label>Prenume</label>
                <input 
                  type="text" 
                  name="firstName" 
                  value={formData.firstName} 
                  onChange={handleChange} 
                  placeholder="Ex: Ion" 
                  required 
                />
              </div>
              <div className="form-group">
                <label>Nume</label>
                <input 
                  type="text" 
                  name="lastName" 
                  value={formData.lastName} 
                  onChange={handleChange} 
                  placeholder="Ex: Popescu" 
                  required 
                />
              </div>
            </>
          )}

          <div className="form-group">
            <label>Email</label>
            <input 
              type="email" 
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="nume@exemplu.com" 
              required 
            />
          </div>

          <div className="form-group">
            <label>Parolă</label>
            <input 
              type="password" 
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="••••••••" 
              required 
            />
          </div>

          {!isLogin && (
            <div className="form-group">
              <label>Confirmă Parola</label>
              <input 
                type="password" 
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="••••••••" 
                required 
              />
            </div>
          )}

          <button type="submit" className="buton-auth">
            {isLogin ? 'AUTENTIFICARE' : 'INREGISTRARE'}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            {isLogin ? 'Nu ai un cont?' : 'Ai deja un cont?'}
            <span className="toggle-link" onClick={toggleMode}>
              {isLogin ? ' Inregistreaza-te aici' : ' Logheaza-te aici'}
            </span>
          </p>
        </div>

      </div>
    </div>
  );
};

export default AuthPage;