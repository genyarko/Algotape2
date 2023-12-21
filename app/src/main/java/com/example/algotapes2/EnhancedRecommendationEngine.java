package com.example.algotapes2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

public class EnhancedRecommendationEngine {

    // Simulated music genres
    private static final List<String> GENRES = Arrays.asList(
            "Pop", "Rock", "Hip Hop", "Electronic", "Jazz", "Classical", "Country", "R&B"
    );

    public void initialize() {
    }

    // Simulated user class with attributes
    static class User {
        private List<String> preferredGenres;
        private List<String> playHistory;

        public List<String> getPreferredGenres() {
            return preferredGenres;
        }

        public void setPreferredGenres(List<String> preferredGenres) {
            this.preferredGenres = preferredGenres;
        }

        public List<String> getPlayHistory() {
            return playHistory;
        }

        public void setPlayHistory(List<String> playHistory) {
            this.playHistory = playHistory;
        }
    }

    // Simulated song class with attributes
    static class Song {
        private String title;
        private String artist;
        private List<String> genres;

        public Song(String title, String artist, List<String> genres) {
            this.title = title;
            this.artist = artist;
            this.genres = genres;
        }

        public String getTitle() {
            return title;
        }

        public String getArtist() {
            return artist;
        }

        public List<String> getGenres() {
            return genres;
        }
    }

    public static List<Song> generateRecommendations(User user, List<Song> songDatabase) {
        List<Song> recommendations = new ArrayList<>();

        // Step 1: Filter songs based on user's preferred genres
        List<Song> preferredGenreSongs = new ArrayList<>();
        for (Song song : songDatabase) {
            if (user.getPreferredGenres().stream().anyMatch(song.getGenres()::contains)) {
                preferredGenreSongs.add(song);
            }
        }

        // Step 2: Filter out songs user has already played
        List<Song> unplayedSongs = new ArrayList<>();
        for (Song song : preferredGenreSongs) {
            if (!user.getPlayHistory().contains(song.getTitle())) {
                unplayedSongs.add(song);
            }
        }

        // Step 3: Sort songs by popularity (for example, by number of times played)
        unplayedSongs.sort(Comparator.comparingInt(song -> Collections.frequency(user.getPlayHistory(), song.getTitle())));

        // Step 4: Select top recommendations (e.g., top 5)
        recommendations.addAll(unplayedSongs.subList(0, Math.min(5, unplayedSongs.size())));

        return recommendations;
    }

    public static void main(String[] args) {
        // Simulated user data
        User user = new User();
        user.setPreferredGenres(Arrays.asList("Pop", "Rock"));
        user.setPlayHistory(Arrays.asList("Song1", "Song2", "Song3"));

        // Simulated song database
        List<Song> songDatabase = Arrays.asList(
                new Song("Song1", "Artist1", Arrays.asList("Pop")),
                new Song("Song2", "Artist2", Arrays.asList("Rock")),
                new Song("Song3", "Artist3", Arrays.asList("Pop", "Rock")),
                new Song("Song4", "Artist4", Arrays.asList("Electronic")),
                new Song("Song5", "Artist5", Arrays.asList("Pop", "Electronic"))
                // Add more songs...
        );

        // Generate recommendations
        List<Song> recommendations = generateRecommendations(user, songDatabase);

        // Print recommendations
        System.out.println("Recommendations:");
        for (Song song : recommendations) {
            System.out.println(song.getTitle() + " by " + song.getArtist());
        }
    }
}


