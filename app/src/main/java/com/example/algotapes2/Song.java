package com.example.algotapes2;



import java.io.Serializable;
import java.util.List;

public class Song implements Serializable {
    private String songTitle;
    private String artistName;
    private String features;
    private String albumTitle;
    private int duration;
    private String genre;
    private String key;
    private String releaseDate;
    private String imageUrl;
    private String videoUrl;
    private String audioUrl;
    private String albumType;
    private String producer;
    private String instrumentalist;
    private String label;
    private String composer;
    private String lyrics;
    private int year;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private List<String> imageUrls;
    private Boolean music;
    private Boolean image;
    private Boolean video;
    private String userName;

    public Song(String songTitle, String artistName, String features, String albumTitle, int duration, String genre, String key, String releaseDate, String imageUrl, String videoUrl, String audioUrl, String albumType, String producer, String instrumentalist, String label, String composer, String lyrics, int year, List<String> imageUrls, Boolean music, Boolean image, Boolean video, String userName, String userId) {
        this.songTitle = songTitle;
        this.artistName = artistName;
        this.features = features;
        this.albumTitle = albumTitle;
        this.duration = duration;
        this.genre = genre;
        this.key = key;
        this.releaseDate = releaseDate;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.audioUrl = audioUrl;
        this.albumType = albumType;
        this.producer = producer;
        this.instrumentalist = instrumentalist;
        this.label = label;
        this.composer = composer;
        this.lyrics = lyrics;
        this.year = year;
        this.imageUrls = imageUrls;
        this.music = music;
        this.image = image;
        this.video = video;
        this.userName = userName;
        this.userId = userId;
    }

    private String userId;

    public Song() {
    }

    public Song(String songTitle, String artistName, String videoUrl) {
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getAlbumType() {
        return albumType;
    }

    public void setAlbumType(String albumType) {
        this.albumType = albumType;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getInstrumentalist() {
        return instrumentalist;
    }

    public void setInstrumentalist(String instrumentalist) {
        this.instrumentalist = instrumentalist;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Boolean getMusic() {
        return music;
    }

    public void setMusic(Boolean music) {
        this.music = music;
    }

    public Boolean getImage() {
        return image;
    }

    public void setImage(Boolean image) {
        this.image = image;
    }

    public Boolean getVideo() {
        return video;
    }

    public void setVideo(Boolean video) {
        this.video = video;
    }

    @Override
    public String toString() {
        return "Song{" +
                "songTitle='" + songTitle + '\'' +
                ", artistName='" + artistName + '\'' +
                ", features='" + features + '\'' +
                ", albumTitle='" + albumTitle + '\'' +
                ", duration=" + duration +
                ", genre='" + genre + '\'' +
                ", key='" + key + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", audioUrl='" + audioUrl + '\'' +
                ", albumType='" + albumType + '\'' +
                ", producer='" + producer + '\'' +
                ", instrumentalist='" + instrumentalist + '\'' +
                ", label='" + label + '\'' +
                ", composer='" + composer + '\'' +
                ", lyrics='" + lyrics + '\'' +
                ", year=" + year +
                ", imageUrls=" + imageUrls +
                ", music=" + music +
                ", image=" + image +
                ", video=" + video +
                '}';
    }
}



