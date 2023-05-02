package com.pdetector.android.models;

public class Pothole {
    public String address;
    public long timeStamp;
    public double intensity;
    public double lat;
    public double lon;
    public String to;
    public String from;
    private String deviceid;
    private String glink;
    private String id;
    private String status;

    public Pothole() {
    }


    public Pothole(String address, long timeStamp, double intensity, double lat, double lon, String to, String from, String deviceid, String glink, String id, String status) {
        this.address = address;
        this.timeStamp = timeStamp;
        this.intensity = intensity;
        this.lat = lat;
        this.lon = lon;
        this.to = to;
        this.from = from;
        this.deviceid = deviceid;
        this.glink = glink;
        this.id = id;
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getGlink() {
        return glink;
    }

    public void setGlink(String glink) {
        this.glink = glink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
