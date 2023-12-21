package com.example.algotapes2.Utils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String userName;
    private String mail;
    private String password; // Note: Storing passwords in plaintext is not recommended for production, consider hashing and salting
    private String profilepic;
    private String status;
    private String city;
    private String country;
    private String profession;
    private String subscription; // Added field for subscription

    // Default constructor required for Firebase
    public User() {
    }

    // Parameterized constructor
    public User(String userId, String userName, String mail, String password, String profilepic,
                String status, String city, String country, String profession, String subscription) {
        this.userId = userId;
        this.userName = userName;
        this.mail = mail;
        this.password = password;
        this.profilepic = profilepic;
        this.status = status;
        this.city = city;
        this.country = country;
        this.profession = profession;
        this.subscription = subscription;
    }

    // Getters and setters for all fields

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }
}


