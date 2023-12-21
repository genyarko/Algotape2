package com.example.algotapes2.ui.dashboard;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.algotapes2.R;
import com.example.algotapes2.Song;
import com.example.algotapes2.Utils.Posts;
import com.example.algotapes2.Utils.User;
import com.example.algotapes2.Utils.Users;
import com.example.algotapes2.databinding.FragmentDashboardBinding;
import com.example.algotapes2.ui.home.HomeFragment;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mUserRef, postRef, likeRef;
    private FirebaseStorage storage;
    private ImageView imageView, imagePoster;
    private TextView userNameHeader;
    private EditText inputAddPost;
    private Button sendPost;
    private MaterialCardView postCard;
    private static final int REQUEST_CODE = 101;
    private ArrayList<Posts> postsList = new ArrayList<>();
    private boolean isLoading = false;
    private Uri imageUri;
    private ProgressDialog mLoadingBar;

    Date date = new Date();
    private FirebaseRecyclerAdapter<Posts, PostsViewHolder> adapter;
    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    final String postDate = format.format(date);

    // Define a variable to keep track of the last visible item in the RecyclerView
    private int lastVisibleItem = 0;
    private int totalItemCount = 0;

    private boolean loading = false;
    private User user = new User();
    private final int visibleThreshold = 5; // Number of items to load before the end of the list

    private Handler backgroundHandler;
    private StorageReference postImageRef;
    private Posts clickedPost;

    // Define a callback interface
    interface UserFetchListener {
        void onUserFetched(User user);
        void onFailure(String errorMessage);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("user");

        postRef = FirebaseDatabase.getInstance().getReference().child("post");
        postImageRef = FirebaseStorage.getInstance().getReference().child("postImages");
        storage = FirebaseStorage.getInstance();
        // Fetch user data
        DatabaseReference currentUserRef = mUserRef.child(mUser.getUid());


        // Initialize other UI components
        imagePoster = binding.imagePost;
        inputAddPost = binding.inputAddPost;
        sendPost = binding.sendPostBtn;

        mLoadingBar = new ProgressDialog(requireContext());


        // Set up FirebaseRecyclerAdapter
        // Configure FirebaseRecyclerOptions
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(postRef, Posts.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {
                // Bind the data to the view holder
                Log.d("FirebaseAdapter", "Data at position " + position + ": " + model);

                try {
                    // Data is a Song object, proceed with binding

                    // Fetch user details based on userId
                    fetchUserDetails(model.getUserId(), new UserFetchListener() {
                        @Override
                        public void onUserFetched(User user) {
                            // Check if the post is liked by the user and update the like button state
                            boolean isLikedByUser = isPostLikedByUser(model.getPostId()); // Replace with your actual method
                            int likeIconResource = isLikedByUser ? R.drawable.baseline_thumb_up_24 : R.drawable.baseline_thumb_up_off_alt_24;
                            holder.postLike.setImageResource(likeIconResource);
                            // Set a click listener for the postImage
                            holder.postImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onLikeButtonClicked(v, holder.getBindingAdapterPosition());
                                }
                            });




                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            // Handle the case where user details couldn't be fetched
                            Log.e("UserFetchError", errorMessage);
                        }
                    });

                } catch (ClassCastException e) {
                    // Log the unexpected data
                    Log.e("FirebaseAdapter", "Unexpected data at position " + position + ": " + model);

                    // Skip processing this item or handle it as needed
                }
            }



            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create and return a new ViewHolder
                // Implement as needed based on your PostsViewHolder
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_view_post,parent,false);
                //attention, continue here.
                //set clicklisteners here

                return new PostsViewHolder(view);
            }
        };



        // Set up click listener for sending posts
        sendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performBackgroundWork();
            }
        });

        // Set up click listener for image posting
        imagePoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE); // Use the REQUEST_CODE constant
                showSuccessToast("Select image");
            }
        });


        // Initialize background handler
        backgroundHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 1) {
                    // The background task has finished
                    mLoadingBar.dismiss();
                    showSuccessToast("Post added");
                    inputAddPost.setText("");
                    imageUri = null;
                    imagePoster.setImageResource(R.drawable.ic_menu_camera);
                    LoadPosts(); // You should call LoadPosts here
                }
            }
        };

        // Hide action bar
        hideActionBar();

        // Load posts into recyclerview

        LoadPosts();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        backgroundHandler.removeCallbacksAndMessages(null);

    }

    // Hide action bar on top function
    private void hideActionBar() {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }


    private void performBackgroundWork() {
        String postText = inputAddPost.getText().toString().trim();
        if (imageUri == null || postText.isEmpty()) {
            showErrorToast("Please select an image and enter a post");
            return;
        }
        mLoadingBar.setTitle("Adding post...");
        mLoadingBar.show();
        // Perform the time-consuming task in a background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                addPostToFirebase(postText);
            }
        }).start();
    }
    // Interface to listen for user details fetch
    // Method to fetch user details based on userId
    private void fetchUserDetails(String userId, UserFetchListener listener) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        listener.onUserFetched(user);
                    } else {
                        listener.onFailure("User is null");
                    }
                } else {
                    listener.onFailure("User data does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                listener.onFailure("Error loading user data: " + databaseError.getMessage());
            }
        });
    }
    // when a user likes a post
    public void onLikeButtonClicked(View view, int position) {
        // Get the post object from the adapter
        Posts post = adapter.getItem(position);

        // Get the like count from the non-null post
        int initialLikeCount = post.getLikeCount();

        // Update like count and set the post value
        post.setLikeCount(initialLikeCount + 1);

        // Update the post in the Firebase Realtime Database
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("post").child(post.getPostId());
        postRef.setValue(post);

        // You can also update the UI here if needed
        showSuccessToast("Post liked");
    }
    private void toggleLikeState(String postId, boolean isLiked, int position) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("likes").child(postId).child(mUser.getUid());

        if (likesRef.getKey() != null) {
            // User already liked the post, remove the like
            likesRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Like removed successfully
                        // Update like count in the post node
                        decrementLikeCount(postId);

                        // Get the reference to the holder from the adapter
                        PostsViewHolder holder = (PostsViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                        if (holder != null) {
                            // Set the "baseline_thumb_up_off_alt_24" drawable
                            holder.postLike.setImageResource(R.drawable.baseline_thumb_up_off_alt_24);
                        }

                        showSuccessToast("Post unliked");
                    } else {
                        // Handle errors
                        String errorMessage = "Failed to remove like";
                        Log.e("ToggleLikeState", errorMessage, task.getException());
                        showErrorToast(errorMessage);
                    }
                }
            });
        } else {
            // User hasn't liked the post, add the like
            likesRef.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Like added successfully
                        // Update like count in the post node
                        incrementLikeCount(postId);

                        // Get the reference to the holder from the adapter
                        PostsViewHolder holder = (PostsViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                        if (holder != null) {
                            // Set the "baseline_thumb_up_24" drawable
                            holder.postLike.setImageResource(R.drawable.baseline_thumb_up_24);
                        }

                        showSuccessToast("Post liked");
                    } else {
                        // Handle errors
                        String errorMessage = "Failed to like post";
                        Log.e("ToggleLikeState", errorMessage, task.getException());
                        showErrorToast(errorMessage);
                    }
                }
            });
        }
    }

    private void incrementLikeCount(String postId) {
        DatabaseReference postLikeCountRef = FirebaseDatabase.getInstance().getReference().child("post").child(postId).child("likeCount");
        postLikeCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) {
                    // Post like count does not exist yet
                    currentData.setValue(1);
                } else {
                    // Increment the existing like count
                    long currentLikeCount = (long) currentData.getValue();
                    currentData.setValue(currentLikeCount + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed && currentData != null) {
                    // Like count updated successfully
                    // You can perform additional actions if needed
                } else {
                    // Handle errors
                    String errorMessage = "Failed to update like count";
                    Log.e("IncrementLikeCount", errorMessage, error.toException());
                    showErrorToast(errorMessage);
                }
            }
        });
    }

    private void decrementLikeCount(String postId) {
        DatabaseReference postLikeCountRef = FirebaseDatabase.getInstance().getReference().child("post").child(postId).child("likeCount");
        postLikeCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) {
                    // Post like count does not exist yet (shouldn't happen, but handle it just in case)
                    currentData.setValue(0);
                } else {
                    // Decrement the existing like count
                    long currentLikeCount = (long) currentData.getValue();
                    if (currentLikeCount > 0) {
                        currentData.setValue(currentLikeCount - 1);
                    }
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed && currentData != null) {
                    // Like count updated successfully
                    // You can perform additional actions if needed
                } else {
                    // Handle errors
                    String errorMessage = "Failed to update like count";
                    Log.e("DecrementLikeCount", errorMessage, error.toException());
                    showErrorToast(errorMessage);
                }
            }
        });
    }

    private boolean isPostLikedByUser(String postId) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("likes").child(postId).child(mUser.getUid());
        return likesRef.getKey() != null; // Check if the like node exists for the user
    }

    private void addPostToFirebase(String postText) {
        // Fetch user details
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if (mUser == null) {
            Log.e(TAG, "addPostToFirebase: Current user is null");
            return;
        }

        userRef.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null) {
                        // Fetched user details successfully, proceed to add the post

                        // Compress the image before uploading to Firebase Storage
                        byte[] compressedImage = compressImage(imageUri);

                        // Save the post to your Firebase Realtime Database under a "post" node
                        String postId = postRef.push().getKey();
                        String postKey = postId + date;
                        HashMap<String, Object> postMap = new HashMap<>();
                        postMap.put("postDate", postDate); // Post date (you can format this as needed)
                        postMap.put("posts", postText);
                        postMap.put("likeCount", 0); // Initial like count
                        postMap.put("commentCount", 0); // Initial comment count
                        postMap.put("postImageUrl", ""); // URL of the uploaded image
                        postMap.put("postId", postId); // generate postId based on user and random number
                        postMap.put("timeStamp", System.currentTimeMillis()); // timestamp
                        postMap.put("postKey", postKey); //

                        // Include user details in the post
                        postMap.put("userId", mUser.getUid());
                        postMap.put("profilepic", user.getProfilepic());
                        postMap.put("userName", user.getUserName());

                        postRef.child(postId).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Upload the compressed image to Firebase Storage
                                    StorageReference imageRef = postImageRef.child(postId + ".jpg");
                                    imageRef.putBytes(compressedImage).addOnSuccessListener(taskSnapshot -> {
                                        // Update the post image URL in the post node
                                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                            postRef.child(postId).child("postImageUrl").setValue(uri.toString());
                                            Message message = Message.obtain();
                                            message.what = 1;
                                            backgroundHandler.sendMessage(message);
                                        });
                                    }).addOnFailureListener(e -> {
                                        // Handle errors
                                        mLoadingBar.dismiss();
                                        showErrorToast("Failed to upload post image");
                                    });
                                } else {
                                    // Handle errors
                                    mLoadingBar.dismiss();
                                    showErrorToast("Failed to add post");
                                }
                            }
                        });
                    } else {
                        Log.e(TAG, "addPostToFirebase: User is null");
                    }
                } else {
                    Log.e(TAG, "addPostToFirebase: User data does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e(TAG, "addPostToFirebase: Error loading user data", databaseError.toException());
            }
        });
    }


    private byte[] compressImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            return stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // LoadPosts method
    private void LoadPosts() {
        // Query Firebase Database to fetch the last 12 posts, ordered by timestamp in descending order
        Query query = postRef.orderByChild("postDate").limitToLast(12);

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(query, Posts.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {
                // Bind data to the view holder
                // Assuming your PostsViewHolder has appropriate views, you can do something like:
                Glide.with(holder.itemView.getContext())
                        .load(model.getPostImageUrl()) // Replace getPostImageUrl() with your actual method to get the post image URL
                        .placeholder(R.drawable.baseline_image_24) // You can set a placeholder image
                        .into(holder.postImage);

                Glide.with(holder.itemView.getContext()).
                        load(model.getProfilepic()).
                        placeholder(R.drawable.baseline_person_24).
                        into(holder.profileImagePost);
                holder.postDate.setText(model.getPostDate());
                holder.postDescription.setText(model.getPosts());
                holder.postUsername.setText(model.getUserName());





            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create and return a new ViewHolder
                // Implement as needed based on your PostsViewHolder
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_view_post, parent, false);

                PostsViewHolder viewHolder = new PostsViewHolder(view);


                return new PostsViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);

        // Start listening for data changes
        adapter.startListening();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                // Scroll to the top when new items are added
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }


    // After an image is selected
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePoster.setImageURI(imageUri); // Use imageUri directly
        }
    }



    // ViewHolder class for FirebaseRecyclerAdapter
    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView profileImagePost;
        private GestureDetector gestureDetector;
        public TextView postUsername;
        public TextView postDate;
        public TextView postDescription;
        public ImageView postImage;
        public ImageView postLike;
        public TextView likeCountAmount;
        public TextView postComment;
        public ImageView commentImage;
        public View commentView;
        public RecyclerView recyclerView2;
        public EditText inputComment;
        public Button sendCommentBtn;
        public MaterialCardView postCard;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize your views here
            profileImagePost = itemView.findViewById(R.id.profileImagePost);
            postUsername = itemView.findViewById(R.id.postUsername);
            postDate = itemView.findViewById(R.id.postDate);
            postDescription = itemView.findViewById(R.id.postDescription);
            postImage = itemView.findViewById(R.id.postImage);
            postCard = itemView.findViewById(R.id.postCard);

        }

    }
    // Helper method to show a success Toast message
    private void showSuccessToast(String message) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showErrorToast(String message) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}