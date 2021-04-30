package com.example.atry;

public class Location {
    private int walk_id;
    private double latitude;
    private double longitude;

    public Location(int _walk_id, double _latitude, double _longitude)
    {
        walk_id = _walk_id;
        latitude = _latitude;
        longitude = _longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getWalk_id() {
        return walk_id;
    }
}

