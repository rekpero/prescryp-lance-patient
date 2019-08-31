package com.prescryp.lance.Model;

public class RideHistoryItem {
    private String rideId;
    private Long timestamp;
    private String dateOfRide;
    private String driverProfileImageUrl;
    private String driverName;
    private String ambulanceType;
    private String rideRating;
    private String distance;
    private String pickupLocationName;
    private String destinationLocationName;

    public RideHistoryItem(String rideId, Long timestamp, String dateOfRide, String driverProfileImageUrl, String driverName, String ambulanceType, String rideRating, String distance, String pickupLocationName, String destinationLocationName) {
        this.rideId = rideId;
        this.timestamp = timestamp;
        this.dateOfRide = dateOfRide;
        this.driverProfileImageUrl = driverProfileImageUrl;
        this.driverName = driverName;
        this.ambulanceType = ambulanceType;
        this.rideRating = rideRating;
        this.distance = distance;
        this.pickupLocationName = pickupLocationName;
        this.destinationLocationName = destinationLocationName;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateOfRide() {
        return dateOfRide;
    }

    public void setDateOfRide(String dateOfRide) {
        this.dateOfRide = dateOfRide;
    }

    public String getDriverProfileImageUrl() {
        return driverProfileImageUrl;
    }

    public void setDriverProfileImageUrl(String driverProfileImageUrl) {
        this.driverProfileImageUrl = driverProfileImageUrl;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getAmbulanceType() {
        return ambulanceType;
    }

    public void setAmbulanceType(String ambulanceType) {
        this.ambulanceType = ambulanceType;
    }

    public String getRideRating() {
        return rideRating;
    }

    public void setRideRating(String rideRating) {
        this.rideRating = rideRating;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getPickupLocationName() {
        return pickupLocationName;
    }

    public void setPickupLocationName(String pickupLocationName) {
        this.pickupLocationName = pickupLocationName;
    }

    public String getDestinationLocationName() {
        return destinationLocationName;
    }

    public void setDestinationLocationName(String destinationLocationName) {
        this.destinationLocationName = destinationLocationName;
    }
}
