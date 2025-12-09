import React from 'react';
import { useNavigate } from 'react-router-dom';
import './EventCardDetails.css'; 
import { useState } from 'react';
import usv from '../Images/usv.jpg';
import TicketModal from './Ticket';
import Circle from '../Icons/circle.png';




const EventDetails = () => {
    const navigate = useNavigate();
    const handleClick = () => {
    navigate('/');
    };
    const [showTicket, setShowTicket] = useState(false);
  const handleParticipa = () => {
    setShowTicket(true);
  };
  const handleClose = () => {
    setShowTicket(false);
  };

const [esteInscris, setEsteInscris] = useState(false);

  return (
    <div className="event-pagina">
       <div className= "Header">
                      <h1>Event Manager</h1>
                      <div className="user-info">
                      <p>Nume & Prenume <br/> Statut</p>
                      <img src={Circle} alt="circle" className="circle-icon"/>
                      </div>
                    </div>
      <div className="card-detalii">
        
        <div className="header">
          <button className="back" onClick={handleClick}>&lt;Back</button>
          <h1 className="event-title">ASSIST OPEN DOORS 25</h1>
        </div>


        <div className="content-card">
          

          <div className="card-imagine">
            <img 
              src={usv}
              alt="Event Crowd" 
              className="event-imagine"
            />
          </div>


          <div className="info-panel">
            <div className="info-group">
              <label>Organizator:</label>
              <div className="info-value">Assist Software</div>
            </div>
            
            <div className="info-group">
              <label>Locatie:</label>
              <div className="info-value">Aula Magna</div>
            </div>

            <div className="info-group">
              <label>Data:</label>
              <div className="info-value">13 decembrie 2025</div>
            </div>

            <div className="info-group">
              <label>Ora:</label>
              <div className="info-value">09:00 A.M.</div>
            </div>

            <div className="info-group">
              <label>Deadline pentru a aplica:</label>
              <div className="info-value">09:00 13/12/2025</div>
            </div>

            <button onClick={handleParticipa} className="buton-participare">PARTICIP</button>
          </div>
          
        </div>


        <div className="description">
          <p>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam tortor arcu, tempus et mi eget, congue ultrices nisi. Proin ipsum eros, malesuada eu venenatis in, viverra tristique quam. Vestibulum ut aliquet nisi. Duis nec dignissim tellus. Duis ultrices sed libero a dictum. Donec pellentesque vulputate magna, eu commodo nisi posuere at. Aenean posuere augue vel fringilla pretium.
          </p>
        </div>

      </div>
      {showTicket && <TicketModal onClose={handleClose} />}
    </div>
  );
};

export default EventDetails;