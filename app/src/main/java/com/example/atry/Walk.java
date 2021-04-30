package com.example.atry;

public class Walk {
    private int id;
    private String duration;
    private double distance;
    private double speed;
    private String start_time;

    public Walk(int _id, String _start_time, String _duration, double _distance, double _speed)
    {
        id = _id;
        start_time = _start_time;
        duration = _duration;
        distance = _distance;
        speed = _speed;
    }

    public double getDistance() {
        return distance;
    }

    public String getStart_time() {
        return start_time;
    }

    public int getId() {
        return id;
    }

    public double getSpeed() {
        return speed;
    }

    public String getDuration() {
        return duration;
    }
}
