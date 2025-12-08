import EventCard from './EventCard';
import SearchIcon from '../Icons/icon-search.png';

import './Home.css';
const Home=() => {
  return (
    <div>
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