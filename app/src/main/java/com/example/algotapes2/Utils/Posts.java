package com.example.algotapes2.Utils;

import java.io.Serializable;
import java.util.List;

public class Posts implements Serializable {

    private String userId;
    private String userName;
    private String userProfileImage;

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public Posts(String userId, String userName, String userProfileImage, String postDate, String profilepic, String posts, String postKey, String postId, Users user, String postImageUrl, int likeCount, int commentCount, Long timeStamp) {
        this.userId = userId;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
        this.postDate = postDate;
        this.profilepic = profilepic;
        this.posts = posts;
        this.postKey = postKey;
        this.postId = postId;
        this.user = user;
        this.postImageUrl = postImageUrl;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.timeStamp = timeStamp;
    }

    private String postDate;
    private String profilepic;
    private String posts; // Rename to match the Firebase Realtime Database field name

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    private String postKey;
    private String postId;
    private Users user; // Add this field

    // Constructors, getters, and setters...

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Posts(String userId, String userName, String userProfileImage, String postDate, String posts, String postKey, String postId, String postImageUrl, int likeCount, int commentCount,  Long timeStamp) {
        this.userId = userId;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
        this.postDate = postDate;
        this.posts = posts;
        this.postKey = postKey;
        this.postId = postId;
        this.postImageUrl = postImageUrl;
        this.likeCount = likeCount;
        this.commentCount = commentCount;

        this.timeStamp = timeStamp;
    }

    private String postImageUrl;

    private int likeCount;
    private int commentCount;


    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    private Long timeStamp;

    public Posts(String userId, String userName, String userProfileImage, String postDate, String posts, String postId, String postImageUrl, int likeCount, int commentCount, Long timeStamp) {
        this.userId = userId;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
        this.postDate = postDate;
        this.posts = posts;
        this.postId = postId;
        this.postImageUrl = postImageUrl;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.timeStamp = timeStamp;
    }

    public Posts() {
        // Default constructor required for Firebase
    }

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

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getPosts() { // Rename to match the Firebase Realtime Database field name
        return posts;
    }

    public void setPosts(String posts) { // Rename to match the Firebase Realtime Database field name
        this.posts = posts;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImage(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public Posts(String userId, String userName, String userProfileImage, String postDate, String posts, String postId, String postImageUrl, int likeCount, int commentCount) {
        this.userId = userId;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
        this.postDate = postDate;
        this.posts = posts;
        this.postId = postId;
        this.postImageUrl = postImageUrl;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    // Getters and setters for all fields
    // (You can generate these automatically in your IDE)

    // Additional methods as needed
}

