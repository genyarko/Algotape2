package com.example.algotapes2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.algotapes2.Utils.User;
import com.example.algotapes2.Utils.VideoUtils;
import com.example.algotapes2.databinding.ActivityUploadVideoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class UploadVideo extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private static final int PICK_FILE_REQUEST_CODE = 1;
    private EditText videoTitleEditText;
    private EditText videoArtistEditText;
    private EditText videoEditText;
    private EditText videoProducerEditText;
    private EditText videoInstrumentalistEditText;
    private EditText videoLabelEditText;

    private Uri selectedFileUri;
    private String selectedFileType;

    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private ActivityUploadVideoBinding binding;
    private String currentUserId;

    private User user = new User();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_video);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Check if the user is authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // User is authenticated, retrieve user information
            currentUserId = currentUser.getUid();



        } else {
            // User is not authenticated, handle accordingly (e.g., redirect to login)
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }

        hideActionBar();
        initializeViews();
        setupFirebase();
    }

    private void initializeViews() {
        binding = ActivityUploadVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupProgressDialog();

        binding.musicVideo.setOnClickListener(view -> launchFilePicker("video"));

        videoTitleEditText = binding.videoTitleEt;
        videoArtistEditText = binding.videoArtistEt;
        videoEditText = binding.genreET;
        videoProducerEditText = binding.videoProducerEt;
        videoInstrumentalistEditText = binding.videoInstrumentalistEt;
        videoLabelEditText = binding.videoLabelEt;

        // Set the click listener for the "Upload" button
        binding.uploadMusicBtn.setOnClickListener(view -> uploadMusic());
    }


    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(UploadVideo.this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
    }

    private void hideActionBar() {
        ActionBar actionBar = Objects.requireNonNull(((AppCompatActivity) this).getSupportActionBar());

        actionBar.hide();
    }

    private void launchFilePicker(String fileType) {
        Intent intent = createFilePickerIntent(fileType);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple selections
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    private Intent createFilePickerIntent(String fileType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        // Set the MIME type based on the selected fileType
        String mimeType = "*/" + fileType;
        switch (fileType) {
            case "audio":
                mimeType = "audio/*";
                break;
            case "image":
                mimeType = "image/*";
                break;
            case "video":
                mimeType = "video/*";
                break;
        }

        intent.setType(mimeType);
        return intent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            if (requestCode == PICK_FILE_REQUEST_CODE) {
                String fileType = getFileType(data.getData());
                if (fileType != null) {
                    selectedFileUri = data.getData();
                    selectedFileType = fileType;
                }
            }
        }
    }

    private String getFileType(Uri fileUri) {
        String mimeType = this.getContentResolver().getType(fileUri);
        if (mimeType != null) {
            if (mimeType.startsWith("audio/")) {
                return "audio";
            } else if (mimeType.startsWith("image/")) {
                return "image";
            } else if (mimeType.startsWith("video/")) {
                return "video";
            }
        }
        return null;
    }

    private void setupFirebase() {
        databaseRef = FirebaseDatabase.getInstance().getReference("videos");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    private void uploadMusic() {
        String title = videoTitleEditText.getText().toString();
        String artist = videoArtistEditText.getText().toString();
        String producer = videoProducerEditText.getText().toString();
        String instrumentalist = videoInstrumentalistEditText.getText().toString();
        String label = videoLabelEditText.getText().toString();

        if (title.isEmpty() || artist.isEmpty() || videoEditText.getText().toString().isEmpty()) {
            // Show an error message indicating missing fields
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFileUri != null && selectedFileType != null && selectedFileType.equals("video")) {
            // Use try-catch to handle potential exceptions during duration retrieval
            try {
                // Get video duration synchronously
                String videoDuration = VideoUtils.getVideoDuration(this, selectedFileUri);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    String date = getCurrentDate();

                    // Now you have both video duration and date, proceed with the upload
                    progressDialog.show();
                    uploadFileToStorage(selectedFileUri, selectedFileType, title, artist, producer, instrumentalist, label, videoDuration, currentUserId);
                } else {
                    // Handle the case where Android version is not Oreo or later
                    Toast.makeText(UploadVideo.this, "Unsupported Android version", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                // Handle exception for failed duration retrieval
                e.printStackTrace();
                Toast.makeText(this, "Error retrieving video duration", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Show a message indicating that a video file must be selected
            Toast.makeText(this, "Select a video file", Toast.LENGTH_SHORT).show();
        }
    }




    private void uploadFileToStorage(Uri fileUri, String fileType, String title, String artist,
                                     String producer, String instrumentalist, String label,
                                     String videoDuration, String currentUserId) {
        String storagePath = "videoData/" + fileType + "/" + title + "." + getFileExtension(fileUri);
        StorageReference fileRef = storageRef.child(storagePath);

        UploadTask uploadTask = fileRef.putFile(fileUri);
        uploadTask.addOnProgressListener(snapshot -> {
            // Calculate the upload progress and update the ProgressDialog
            int progress = (int) (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
            progressDialog.setProgress(progress);
        });
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // File uploaded successfully
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Create a VideoData object with metadata and file URL
                Song videoData = new Song();
                videoData.setVideoUrl(uri.toString());
                user.setUserId(currentUserId);  // Add userId


                switch (fileType) {
                    case "audio":
                        videoData.setMusic(true);
                        videoData.setImage(false);
                        videoData.setVideo(false);
                        // Set the audio URL to the Firebase Storage URL
                        videoData.setAudioUrl(uri.toString());
                        break;
                    case "image":
                        videoData.setMusic(false);
                        videoData.setImage(true);
                        videoData.setVideo(false);
                        // Set the image URL to the Firebase Storage URL
                        videoData.setImageUrl(uri.toString());
                        break;
                    case "video":
                        videoData.setMusic(false);
                        videoData.setImage(false);
                        videoData.setVideo(true);
                        break;
                }

                videoData.setSongTitle(title);
                videoData.setArtistName(artist);
                videoData.setProducer(producer);
                videoData.setInstrumentalist(instrumentalist);
                videoData.setLabel(label);
                videoData.setUserId(currentUserId);

                // Set the video duration (convert formatted string to integer)
                int convertedDuration = convertVideoDuration(videoDuration);
                videoData.setDuration(convertedDuration);

                // Set the year based on the current date
                String currentDate = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    currentDate = getCurrentDate();
                }
                assert currentDate != null;
                int convertedDate = Integer.parseInt(currentDate);
                videoData.setYear(convertedDate);

                // Upload the video metadata to the Realtime Database
                String videoKey = databaseRef.push().getKey();
                assert videoKey != null;
                databaseRef.child(videoKey).setValue(videoData)
                        .addOnSuccessListener(aVoid -> {
                            progressDialog.dismiss();
                            clearInputFields();
                            Toast.makeText(UploadVideo.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(UploadVideo.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            // Log the error for further investigation
                            Log.e("FirebaseUpload", "Error uploading data", e);
                        });

            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            // Handle file upload failure
            Toast.makeText(UploadVideo.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Utility method to convert video duration from formatted string to integer
    private int convertVideoDuration(String formattedDuration) {
        String[] parts = formattedDuration.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }


    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getCurrentDate() {
        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Format the date as a string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return currentDate.format(formatter);
    }


    private void clearInputFields() {
        videoTitleEditText.setText("");
        videoArtistEditText.setText("");
        videoEditText.setText("");
        videoProducerEditText.setText("");
        videoInstrumentalistEditText.setText("");
        videoLabelEditText.setText("");
    }
}
