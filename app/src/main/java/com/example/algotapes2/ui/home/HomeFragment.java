package com.example.algotapes2.ui.home;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.algotapes2.EnhancedRecommendationEngine;
import com.example.algotapes2.LoginActivity;
import com.example.algotapes2.R;
import com.example.algotapes2.UploadVideo;
import com.example.algotapes2.Song;
import com.example.algotapes2.databinding.FragmentHomeBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Song, SongViewHolder> adapter;
    private Handler videoPreloadHandler;
    private int preloadedPosition;
    private Parcelable recyclerViewState;
    private String currentUserId;
    private String currentUserUsername;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Check if the user is authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is authenticated, retrieve user information
            currentUserId = currentUser.getUid();
            currentUserUsername = currentUser.getDisplayName();
        } else {
            // User is not authenticated, handle accordingly (e.g., redirect to login)
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }

        // Initialize Firebase Realtime Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("videos");

        // Initialize the recommendation engine
        EnhancedRecommendationEngine recommendationEngine = new EnhancedRecommendationEngine();
        recommendationEngine.initialize(); // Load data and configure the engine

        // Configure FirebaseRecyclerOptions
        FirebaseRecyclerOptions<Song> options =
                new FirebaseRecyclerOptions.Builder<Song>()
                        .setQuery(databaseReference, Song.class)
                        .build();

        // Initialize the adapter
        adapter = new FirebaseRecyclerAdapter<Song, SongViewHolder>(options) {
            @NonNull
            @Override
            public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Always create a new view holder
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_single, parent, false);
                return new SongViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull SongViewHolder holder, int position, @NonNull Song model) {
                Log.d("FirebaseAdapter", "onBindViewHolder - Position: " + position + ", Model: " + model);

                // Get the total item count outside onBindViewHolder
                int itemCount = getItemCount();

                // Check if the position is valid
                if (position >= 0 && position < itemCount) {
                    try {
                        // Data is a Song object, proceed with binding
                        holder.bind(model);

                        // Preload the next video when the user is viewing the current one
                        preloadNextVideo();
                    } catch (ClassCastException e) {
                        // Log the unexpected data
                        Log.e("FirebaseAdapter", "onBindViewHolder - Unexpected data at position " + position + ": " + model, e);

                        // Skip processing this item or handle it as needed
                    } catch (Exception ex) {
                        // Log any other unexpected exceptions during binding
                        Log.e("FirebaseAdapter", "onBindViewHolder - Exception at position " + position + ": " + ex.getMessage(), ex);
                    }
                } else {
                    // Log an error for invalid position
                    Log.e("FirebaseAdapter", "onBindViewHolder - Invalid position: " + position);
                }
            }

        };

        recyclerView.setAdapter(adapter);

        hideActionBar();

        videoPreloadHandler = new Handler();
        preloadedPosition = -1;

        // Delayed video preloading to allow time for RecyclerView to stabilize
        recyclerView.postDelayed(this::preloadNextVideos, 1000);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (videoPreloadHandler != null) {
            videoPreloadHandler.removeCallbacksAndMessages(null);
        }
        adapter = null; // Release the adapter
    }

    private void hideActionBar() {
        ActionBar actionBar = Objects.requireNonNull(((AppCompatActivity) requireActivity())).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();

        if (recyclerViewState != null) {
            Objects.requireNonNull(recyclerView.getLayoutManager()).onRestoreInstanceState(recyclerViewState);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    // Save RecyclerView state on configuration changes
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (recyclerView.getLayoutManager() != null) {
            recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
            outState.putParcelable("recycler_state", recyclerViewState);
        }
    }

    // Restore RecyclerView state on configuration changes
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            recyclerViewState = savedInstanceState.getParcelable("recycler_state");
        }
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView uploadVideo;
        TextView txtTitle;
        TextView txtDesc, uploadedBy;
        VideoView videoView;
        ProgressBar progressBar;
        boolean isPlaying;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            uploadVideo = itemView.findViewById(R.id.uploadVideo);
            txtTitle = itemView.findViewById(R.id.txtTitle_single);
            txtDesc = itemView.findViewById(R.id.txtDesc_single);
            videoView = itemView.findViewById(R.id.videoView_single);
            progressBar = itemView.findViewById(R.id.progressBar_single);
            uploadedBy = itemView.findViewById(R.id.uploadedBy);

            isPlaying = false;

            // Toggle video playback on touch
            itemView.setOnClickListener(v -> togglePlayPause());

            // Check if the uploadVideo Image is null
            if (uploadVideo == null) {
                // Set the helpCard to a non-null value
                uploadVideo = new ImageView(requireContext());
            }

            // Go to upload video activity when the image is clicked
            uploadVideo.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), UploadVideo.class);
                startActivity(intent);
            });
        }

        public void bind(Song model) {
            txtTitle.setText(model.getSongTitle());
            txtDesc.setText(model.getArtistName());

            String uploaderId = model.getUserId();
            if (uploaderId != null && !uploaderId.isEmpty()) {
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("user").child(uploaderId);
                userReference.child("userName").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userName = Objects.requireNonNull(task.getResult()).getValue(String.class);
                        uploadedBy.setText(userName);
                    } else {
                        Log.e("SongViewHolder", "Error fetching userName", task.getException());
                    }
                });
            }


            String videoUrl = model.getVideoUrl();
            if (videoUrl != null && !videoUrl.isEmpty()) {
                try {
                    videoView.setVideoPath(videoUrl);
                } catch (Exception e) {
                    Log.e("SongViewHolder", "Error setting video path", e);
                }

                videoView.requestFocus();
                progressBar.setVisibility(View.VISIBLE);

                videoView.setOnPreparedListener(mp -> {
                    progressBar.setVisibility(View.GONE);
                    if (isPlaying) {
                        mp.start();
                    }
                });

                videoView.setOnCompletionListener(mp -> {
                    if (isPlaying) {
                        mp.start();
                    }
                });
            }
        }

        // Toggle play/pause state
        private void togglePlayPause() {
            if (isPlaying) {
                videoView.pause();
            } else {
                videoView.start();
            }
            isPlaying = !isPlaying;
        }
    }

    // Improved preloadNextVideos method
    private void preloadNextVideos() {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
            int videosToPreload = 3;

            // Preload videos within the visible range and additional videos ahead
            for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition + videosToPreload; i++) {
                if (i >= 0 && i < adapter.getItemCount() && i != preloadedPosition) {
                    preloadedPosition = i;

                    try {
                        // Your existing preload logic...
                        recyclerView.findViewHolderForAdapterPosition(i);
                        Log.d("FirebaseAdapter", "Preloaded video at position: " + i);
                    } catch (Exception e) {
                        Log.e("FirebaseAdapter", "Error during preload at position: " + i, e);
                        // Implement your error handling logic
                    }
                }
            }
        }
    }

    // Modify the existing preloadNextVideo method
    private void preloadNextVideo() {
        videoPreloadHandler.removeCallbacksAndMessages(null);

        videoPreloadHandler.postDelayed(this::preloadNextVideos, 1000); // Delay in milliseconds
    }

}







