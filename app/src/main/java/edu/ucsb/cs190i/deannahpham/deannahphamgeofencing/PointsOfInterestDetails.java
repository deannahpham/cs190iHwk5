package edu.ucsb.cs190i.deannahpham.deannahphamgeofencing;

/**
 * Created by Deanna on 5/24/17.
 */

public class PointsOfInterestDetails {
    public double latitude, longitude;
    public String placeId, name;

    public PointsOfInterestDetails() {
        super();
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude () {
        return latitude;
    }

    public double getLongitude () {
        return longitude;
    }

    public String getPlaceId () {
        return placeId;
    }

    public String getName(){
        return name;
    }
}
