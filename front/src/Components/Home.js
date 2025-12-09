import EventCard from './EventCard';
import SearchIcon from '../Icons/icon-search.png';
import Circle from '../Icons/circle.png';

import './Home.css';
const Home=() => {
  return (
    <div>

        <div className= "Header">
          <h1>Event Manager</h1>
          <div className="user-info">
          <p>Nume & Prenume <br/> Statut</p>
          <img src={Circle} alt="circle" className="circle-icon"/>
          </div>
        </div>
        <div className="Search">
        <img src={SearchIcon} className="search-icon" alt="Search" />
        <input className="search-input" type="text" placeholder="Type here"/>
      </div>
      <div className="App">
        <div className="Grid">
        <EventCard />
        <EventCard/>
        <EventCard/>
        <EventCard/>
        <EventCard/>
        <EventCard/>
      </div>
    </div>
    </div>

  );
}

export default Home;