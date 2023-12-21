package com.example.algotapes2;

import android.Manifest;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TextRecognitionActivity extends AppCompatActivity {

    private static final Set<String> REQUIRED_PERMISSIONS = new HashSet<>(Arrays.asList(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
            // Add other permissions as needed
    ));

    private TextView recognizedTextView;
    private Preview preview;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private static final String TAG = "TextRecognition";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    PreviewView cameraPreview;

    private final ActivityResultLauncher<String[]> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean permissionGranted = true;
                        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                            if (Arrays.asList(REQUIRED_PERMISSIONS).contains(entry.getKey()) && !entry.getValue()) {
                                permissionGranted = false;
                            }
                        }
                        if (!permissionGranted) {
                            Toast.makeText(this, "Permission request denied", Toast.LENGTH_SHORT).show();
                        } else {
                            startCamera();
                        }
                    });

    // Handle the selected image from the gallery
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    this::recognizeTextFromUri);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognition);

        ImageView previewImage = findViewById(R.id.previewImage);
        recognizedTextView = findViewById(R.id.recognizedTextView);
        Button cameraButton = findViewById(R.id.cameraButton);
        Button choosePicButton = findViewById(R.id.choosePic);
        cameraPreview = findViewById(R.id.cameraPreview);

        // Request camera and storage permissions
        requestCameraPermission();

        // Set click listener for the camera button
        cameraButton.setOnClickListener(v -> takePhoto());

        // Set click listener for the choosePic button
        choosePicButton.setOnClickListener(v -> choosePictureFromGallery());

        hideActionBar();
    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception exc) {
                Log.e(TAG, "Use case binding failed", exc);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        ImageCapture imageCapture = this.imageCapture;
        if (imageCapture == null) {
            return;
        }

        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis());
        ContentValues contentValues = createImageContentValues(name);

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
                .build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        Log.e(TAG, "Photo capture failed: " + exc.getMessage(), exc);
                    }

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Uri savedUri = createSavedUri(output);
                        String msg = "Photo capture succeeded: " + savedUri;
                        Toast.makeText(TextRecognitionActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);

                        // Recognize text from the captured image
                        recognizeTextFromUri(savedUri);
                    }
                }
        );
    }

    private void requestCameraPermission() {
        cameraPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
    }

    private void choosePictureFromGallery() {
        galleryLauncher.launch("image/*");
    }

    private ContentValues createImageContentValues(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourAppName");
        }
        return contentValues;
    }

    private Uri createSavedUri(ImageCapture.OutputFileResults output) {
        return Uri.fromFile(new File(Objects.requireNonNull(Objects.requireNonNull(output.getSavedUri()).getPath())));
    }

    private void recognizeTextFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Rotate the bitmap if needed
            int rotationDegree = 0; // You should calculate this based on the device orientation

            InputImage image = InputImage.fromBitmap(bitmap, rotationDegree);

            // Use TextRecognizer for different languages
            TextRecognizer recognizerEnglish = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            TextRecognizer recognizerChinese = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
//            TextRecognizer recognizerDevanagari = TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());
//            TextRecognizer recognizerJapanese = TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());
//            TextRecognizer recognizerKorean = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());

            recognizeText(image, recognizerEnglish);
            recognizeText(image, recognizerChinese);
//            recognizeText(image, recognizerDevanagari);
//            recognizeText(image, recognizerJapanese);
//            recognizeText(image, recognizerKorean);

        } catch (IOException e) {
            Log.e(TAG, "Error reading input stream", e);
        }
    }

    private void recognizeText(InputImage image, TextRecognizer recognizer) {
        recognizer.process(image)
                .addOnSuccessListener(this::processRecognizedText)
                .addOnFailureListener(e -> Log.e(TAG, "Text recognition failed", e));
    }

    private void processRecognizedText(Text visionText) {
        // Extract and handle recognized text as needed
        String resultText = visionText.getText();
        recognizedTextView.setText(resultText);

        // Iterate through blocks, lines, elements, and symbols if needed

        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String blockText = block.getText();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (Text.Element element : line.getElements()) {
                    String elementText = element.getText();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                    for (Text.Symbol symbol : element.getSymbols()) {
                        String symbolText = symbol.getText();
                        Point[] symbolCornerPoints = symbol.getCornerPoints();
                        Rect symbolFrame = symbol.getBoundingBox();
                    }
                }
            }
        }
    }
}


