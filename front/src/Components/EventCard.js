import React from 'react';
import './EventCard.css';
import { useNavigate } from 'react-router-dom';
import Assist from '../Images/ASSIST.png'



const EventCard = () => {
  const navigate = useNavigate();
  const handleClick = () => {
  navigate('/event_detalii');
  
};
  return (
    
    <div className="card">
      <div className="image-container">
        <img className="card-image" src={Assist} alt="Event" />
        <div className="date">13 Dec</div>
      </div>
      
      <div className="content">
        <div className="tags">
          <span className="tag green">FIESC</span>
          <span className="tag blue">ASSIST</span>
          <span className="tag yellow">OPPORTUNITY</span>
        </div>

        <h2 className="title">ASSIST OPEN DOORS 25</h2>
        <p className="description">
          Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam tortor arcu.
        </p>
        
        <div className="sep"></div>

        <div className="last">

            <div className="location">
              <span className="icon">📍</span> USV, Aula Magna
            </div>
          <button className="details" onClick={handleClick}>DETALII</button>
        </div>
      </div>
    </div>
  );
};

export default EventCard;