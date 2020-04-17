package models;


import java.util.List;
import java.util.UUID;

public interface Model {

    void createTrip(String trip_name, String destination, String hotel_name);
    List getAllHotels();
    void addHotel(String hotel_name, String trip_name);
    List<Restaurants> getAllRestaurants();


    List<Activities> getAllActivities();

    List getAllTrips();

}


