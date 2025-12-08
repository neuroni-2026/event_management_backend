import React from 'react';
import './Ticket.css';
import QR from '../Images/qr_code.png';
import AppleWallet from '../Images/a_wallet.png';
import GoogleWallet from '../Images/g_wallet.png';



const qrCodeImage = QR;

const appleWalletPlaceholder = AppleWallet;
const googleWalletPlaceholder = GoogleWallet;


const TicketModal = ({ onClose }) =>    { {

  const handleModalClick = (e) => {
    e.stopPropagation();
  };

  return (

    <div className="modal-overlay" onClick={onClose}>
      

      <div className="modal-content" onClick={handleModalClick}>
        

        <div className="modal-header relative-header">
          <button className="buton-back" onClick={onClose}>
             <span style={{marginRight: '4px'}}>‹</span> Back
          </button>
          <h3 className="modal-title">ASSIST OPEN DOORS 25</h3>
        </div>


        <div className="qr-container">
          <img src={qrCodeImage} alt="Event QR Code" className="qr-image" />
        </div>


        <div className="ticket-details">
          <p className="event-info-line">
            Aula Magna • 13/12/2025 • 09:00
          </p>
          

          <div className="modal-tags">
            <span className="tag-badge tag-green">FIESC</span>
            <span className="tag-badge tag-blue">ASSIST</span>
            <span className="tag-badge tag-yellow">OPPORTUNITY</span>
          </div>
        </div>

        <div className="wallet-actions">

          <img src={appleWalletPlaceholder} alt="Add to Apple Wallet" className="wallet-buton" />
          <img src={googleWalletPlaceholder} alt="Add to Google Wallet" className="wallet-buton" />
        </div>


        <button className="buton-export">
          EXPORT IN PDF
        </button>

      </div>
    </div>
  );
};
}

export default TicketModal;