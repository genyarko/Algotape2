package com.example.algotapes2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.algotapes2.Utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileSetting extends AppCompatActivity {

    private EditText usernameEditText, userStatusEditText, userEmailEditText,
            userCityEditText, userCountryEditText, userProfessionEditText;
    private TextView userSubscriptionTextView;
    MaterialButton saveButton;
    CircleImageView userImageView;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseStorage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile_setting);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username);
        userStatusEditText = findViewById(R.id.userStatus);
        userEmailEditText = findViewById(R.id.userEmail);
        userCityEditText = findViewById(R.id.userCity);
        userCountryEditText = findViewById(R.id.userCountry);
        userProfessionEditText = findViewById(R.id.userProfession);
        userSubscriptionTextView = findViewById(R.id.userSubscription);
        saveButton = findViewById(R.id.saveButton);
        userImageView = findViewById(R.id.userimg);

        //storage reference
        storage = FirebaseStorage.getInstance();


        // Set padding based on system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // hide Action bar
        hideActionBar();

        // Set an OnClickListener for the saveButton
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });

        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        // Retrieve user information from Firebase Realtime Database
        retrieveUserInfo();
    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image URI
            Uri imageUri = data.getData();

            // Update the UI with the selected image using Glide
            Glide.with(this)
                    .load(imageUri)
                    .into(userImageView);

            // Upload the image to Firebase Storage and update the user's profilepic field
            uploadImageToStorage(imageUri);
        }
    }
    private void uploadImageToStorage(Uri imageUri) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Upload").child(userId);

            // Upload the selected image to Firebase Storage
            storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String profileImageUrl = uri.toString();

                                // Update the user's profilepic field in the database with the new image URL
                                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("user").child(userId);
                                userReference.child("profilepic").setValue(profileImageUrl);

                                // Update the profile image in the UI using Glide
                                Glide.with(UserProfileSetting.this)
                                        .load(profileImageUrl)
                                        .into(userImageView);

                                // Display a message or handle the completion
                                Toast.makeText(UserProfileSetting.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Handle the error during image upload
                        Toast.makeText(UserProfileSetting.this, "Error uploading profile picture", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }





    private void retrieveUserInfo() {
        // Get the current authenticated user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Get the user ID
            String userId = currentUser.getUid();

            // Reference to the user node in the database
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("user").child(userId);

            // Attach a listener to read the data at the user reference
            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Check if the dataSnapshot exists
                    if (dataSnapshot.exists()) {
                        // Retrieve user data from the dataSnapshot
                        User user = dataSnapshot.getValue(User.class);

                        // Update UI with retrieved user data
                        if (user != null) {
                            usernameEditText.setText(user.getUserName());
                            userStatusEditText.setText(user.getStatus());
                            userEmailEditText.setText(user.getMail());
                            userCityEditText.setText(user.getCity());
                            userCountryEditText.setText(user.getCountry());
                            userProfessionEditText.setText(user.getProfession());
                            userSubscriptionTextView.setText(user.getSubscription());

                            // Load and display profile picture using Glide
                            String profilePicUrl = user.getProfilepic();
                            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                Glide.with(UserProfileSetting.this)
                                        .load(profilePicUrl)
                                        .into(userImageView);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle the error
                }
            });
        }
    }

    private void saveUserInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("user").child(userId);

            // Update the user information based on the current UI values
            userReference.child("userName").setValue(usernameEditText.getText().toString());
            userReference.child("status").setValue(userStatusEditText.getText().toString());
            userReference.child("mail").setValue(userEmailEditText.getText().toString());
            userReference.child("city").setValue(userCityEditText.getText().toString());
            userReference.child("country").setValue(userCountryEditText.getText().toString());
            userReference.child("profession").setValue(userProfessionEditText.getText().toString());
            // This is for the subscription field in the User class
            //userReference.child("subscription").setValue(userSubscriptionTextView.getText().toString());

            // Display a message or handle the completion
            Toast.makeText(UserProfileSetting.this, "User information updated", Toast.LENGTH_SHORT).show();
        }
    }
}