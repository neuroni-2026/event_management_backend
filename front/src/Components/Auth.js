import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Auth.css';

const AuthPage = () => {
  const [isLogin, setIsLogin] = useState(true); 
  const navigate = useNavigate();


  const toggleMode = () => {
    setIsLogin(!isLogin);
  };

 
  const handleSubmit = (e) => {
    e.preventDefault();

    console.log("Formular trimis!");
    navigate('/'); 
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        

        <h2 className="auth-title">
          {isLogin ? 'Bine ai venit!' : 'Creează cont'}
        </h2>
        <p className="auth-subtitle">
          {isLogin 
            ? 'Introdu datele pentru a te autentifica.' 
            : 'Completeaza detaliile pentru a te inregistra.'}
        </p>


        <form onSubmit={handleSubmit} className="auth-form">
          

          {!isLogin && (
            <div className="form-group">
              <label>Nume complet</label>
              <input type="text" placeholder="Ex: Ion Popescu" required />
            </div>
          )}

          <div className="form-group">
            <label>Email</label>
            <input type="email" placeholder="nume@exemplu.com" required />
          </div>

          <div className="form-group">
            <label>Parolă</label>
            <input type="password" placeholder="••••••••" required />
          </div>


          {!isLogin && (
            <div className="form-group">
              <label>Confirmă Parola</label>
              <input type="password" placeholder="••••••••" required />
            </div>
          )}


          <button type="submit" className="buton-auth">
            {isLogin ? 'AUTENTIFICARE' : 'ÎNREGISTRARE'}
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