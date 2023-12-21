package com.example.algotapes2.Utils;

import java.io.Serializable;

public class Event implements Serializable {
    private String eventTitle;
    private String eventCategory;
    private String availableFor;
    private String location;
    private String price;
    private String address;
    private String date;
    private String description;
    private String videoUrl; // The URL of the uploaded video

    private String imageUrl;
    private String profilepic;
    private String userName;

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // Constructors
    public Event() {
        // Default constructor required for Firebase Realtime Database
    }

    public Event(String eventTitle, String eventCategory, String availableFor, String location, String price,
                 String address, String date, String description, String videoUrl, String imageUrl, String profilepic, String userName) {
        this.eventTitle = eventTitle;
        this.eventCategory = eventCategory;
        this.availableFor = availableFor;
        this.location = location;
        this.price = price;
        this.address = address;
        this.date = date;
        this.description = description;
        this.videoUrl = videoUrl;
        this.imageUrl = imageUrl;
        this.profilepic = profilepic;
        this.userName = userName;
    }

    // Getters and setters for the fields

    public String getEventTitle() {
        return eventTitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getAvailableFor() {
        return availableFor;
    }

    public void setAvailableFor(String availableFor) {
        this.availableFor = availableFor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}

