package com.example.algotapes2.Utils;
import java.io.Serializable;
import java.util.List;

public class Users implements Serializable {
    private String profilepic;
    private String mail;
    private String userName;
    private String password;
    private String userId;
    private String lastMessage;
    private String status;
    private String city;
    private String country;
    private String profession;
    private List<String> friendList; // List of friend UIDs

    // Default constructor
    public Users() {
    }

    // Constructor with necessary fields
    public Users(String userId, String userName, String mail, String password, String profilepic, String status) {
        this.userId = userId;
        this.userName = userName;
        this.mail = mail;
        this.password = password;
        this.profilepic = profilepic;
        this.status = status;
    }

    // Constructor with additional fields
    public Users(String userId, String userName, String mail, String password, String profilepic, String status,
                 String city, String country, String profession, List<String> friendList) {
        this.userId = userId;
        this.userName = userName;
        this.mail = mail;
        this.password = password;
        this.profilepic = profilepic;
        this.status = status;
        this.city = city;
        this.country = country;
        this.profession = profession;
        this.friendList = friendList;
    }

    public Users(String id, String namee, String emaill, String password, String imageuri, String status, String city, String profession, String country) {
    }

    // Getters and setters for all fields
    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
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

    public List<String> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<String> friendList) {
        this.friendList = friendList;
    }
}


