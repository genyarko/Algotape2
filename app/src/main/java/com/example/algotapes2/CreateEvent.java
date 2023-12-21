package com.example.algotapes2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.algotapes2.Utils.Event;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.UUID;

public class CreateEvent extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private EditText titleEditText, categoryEditText, availableForEditText,
            locationEditText, priceEditText, addressEditText, dateEditText, descriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Initialize Views and Button
        titleEditText = findViewById(R.id.event_title_et);
        categoryEditText = findViewById(R.id.event_category_et);
        availableForEditText = findViewById(R.id.event_available_for_ET);
        locationEditText = findViewById(R.id.event_location_et);
        priceEditText = findViewById(R.id.event_price_et);
        addressEditText = findViewById(R.id.event_address_et);
        dateEditText = findViewById(R.id.event_date_et);
        descriptionEditText = findViewById(R.id.event_description_et);
        ImageView uploadImageView = findViewById(R.id.event_image_View);
        Button uploadEventBtn = findViewById(R.id.upload_event_btn);

        // Set Click Listener for Image Upload
        uploadImageView.setOnClickListener(view -> openFileChooser());

        // Set Click Listener for Event Upload
        uploadEventBtn.setOnClickListener(view -> uploadEvent());
    }

    // Open file chooser for image selection
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle result from file chooser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Display the selected image, if needed
        }
    }

    // Upload event data to Firebase
    private void uploadEvent() {
        if (imageUri != null) {
            // Create unique image filename
            String imageName = UUID.randomUUID().toString();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("event_images/" + imageName);

            // Create and show a ProgressDialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.setMessage("Uploading event data...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Upload image to Firebase Storage
            storageReference.putFile(imageUri)
                    .addOnProgressListener(taskSnapshot -> {
                        // Update progress in the ProgressDialog
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploading " + (int) progress + "%");
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded image
                        storageReference.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();
                                    Log.d("CreateEvent", "Image URL: " + imageUrl);

                                    // Create Event object
                                    Event event = new Event(
                                            titleEditText.getText().toString(),
                                            categoryEditText.getText().toString(),
                                            availableForEditText.getText().toString(),
                                            locationEditText.getText().toString(),
                                            priceEditText.getText().toString(),
                                            addressEditText.getText().toString(),
                                            dateEditText.getText().toString(),
                                            descriptionEditText.getText().toString(),
                                            uri.toString(), // URL of the uploaded image
                                            imageUrl,
                                            "", // Set other fields if they are part of your Event class
                                            "" // Set other fields if needed
                                    );

                                    Log.d("CreateEvent", "Event object created: " + event.toString());

                                    // Save event data to Firebase Realtime Database
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("events");
                                    String eventId = databaseReference.push().getKey();
                                    assert eventId != null;
                                    databaseReference.child(eventId).setValue(event);

                                    // Clear all EditText fields
                                    clearAllFields();

                                    // Dismiss the ProgressDialog
                                    progressDialog.dismiss();

                                    Toast.makeText(CreateEvent.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("CreateEvent", "Failed to get image download URL", e);
                                    // Handle failure to get download URL
                                    progressDialog.dismiss();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure to upload image
                        Log.e("CreateEvent", "Failed to upload image", e);
                        Toast.makeText(CreateEvent.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    });
        } else {
            Toast.makeText(this, "Select an image", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to clear all EditText fields
    private void clearAllFields() {
        titleEditText.getText().clear();
        categoryEditText.getText().clear();
        availableForEditText.getText().clear();
        locationEditText.getText().clear();
        priceEditText.getText().clear();
        addressEditText.getText().clear();
        dateEditText.getText().clear();
        descriptionEditText.getText().clear();
    }
}


