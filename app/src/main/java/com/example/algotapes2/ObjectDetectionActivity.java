package com.example.algotapes2;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.algotapes2.Utils.SearchResult;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObjectDetectionActivity extends AppCompatActivity {

    private ObjectDetector objectDetector;
    private ExecutorService cameraExecutor;
    private final CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build();
    // Declare RecyclerView and its adapter
    private RecyclerView recyclerView;
    private SearchResultAdapter searchResultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_object_detection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize ML Kit Object Detector
        objectDetector = ObjectDetection.getClient(getObjectDetectorOptions());

        // Initialize background executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Setup Camera
        setupCamera();

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerViewResults);
        searchResultAdapter = new SearchResultAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(searchResultAdapter);

        hideActionBar();
    }

    private ObjectDetectorOptions getObjectDetectorOptions() {
        return new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build();
    }

    private void setupCamera() {
        PreviewView previewView = findViewById(R.id.previewView);
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                if (cameraProvider != null) {
                    bindPreview(cameraProvider);
                }
            } catch (Exception e) {
                // Handle exceptions
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        PreviewView previewView = findViewById(R.id.previewView);

        // Create Preview use case
        Preview preview = new Preview.Builder().build();

        // Create ImageAnalysis use case for object detection
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                processObjectDetection(imageProxy);
            }
        });

        Camera camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis);

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processObjectDetection(ImageProxy imageProxy) {
        InputImage image = InputImage.fromMediaImage(
                Objects.requireNonNull(imageProxy.getImage()),
                imageProxy.getImageInfo().getRotationDegrees());

        objectDetector.process(image)
                .addOnSuccessListener(detectedObjects -> {
                    // Process the detected objects
                    StringBuilder labelInfo = new StringBuilder();
                    List<SearchResult> searchResults = new ArrayList<>();


                    // Iterate through detected objects
                    for (DetectedObject detectedObject : detectedObjects) {
                        List<DetectedObject.Label> labels = detectedObject.getLabels();

                        // Iterate through labels for each detected object
                        for (DetectedObject.Label label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();

                            // Append label information to StringBuilder
                            labelInfo.append("Label: ").append(text)
                                    .append(", Confidence: ").append(confidence)
                                    .append("\n");

                            // Retrieve bounding box information
                            Rect boundingBox = detectedObject.getBoundingBox();
                            float left = boundingBox.left;
                            float top = boundingBox.top;
                            float right = boundingBox.right;
                            float bottom = boundingBox.bottom;

                            // Map recognized labels to Vision API Product Search queries
                            List<String> productSearchQueries = mapLabelsToProductSearchQueries(labels);

                            // Create SearchResult object
                            float confidenceValue = label.getConfidence();
                            SearchResult result = new SearchResult(text, confidenceValue, "Details: ..."); // Replace confidenceValue with the actual float value
                            searchResults.add(result);
                        }
                    }

                    // Update the RecyclerView with the new search results
                    searchResultAdapter.setSearchResults(searchResults);

                    // TODO: Display bounding box in your UI (e.g., draw on a View)
                    // Note: You might need to create a custom View or use a graphic overlay for drawing.

                    // Update the UI using StringBuilder
                    if (!isFinishing()) {
                        runOnUiThread(() -> {
                            TextView classificationTextView = findViewById(R.id.classificationTextView);
                            classificationTextView.setText(labelInfo.toString());
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle object detection failure
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    // Create a custom adapter (you need to create SearchResultAdapter class)
    public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
        private List<SearchResult> searchResults;

        public void setSearchResults(List<SearchResult> searchResults) {
            this.searchResults = searchResults;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            // Create and return an instance of ViewHolder
            View itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_search_result, viewGroup, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            // Bind data to UI elements in the ViewHolder
            SearchResult result = searchResults.get(i);
            viewHolder.textViewProductName.setText(result.getLabel());
            viewHolder.textViewProductDetails.setText(result.getProductDetails());
        }

        @Override
        public int getItemCount() {
            return searchResults != null ? searchResults.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            // Declare UI elements for each item in the RecyclerView
            public TextView textViewProductName;
            public TextView textViewProductDetails;

            public ViewHolder(View itemView) {
                super(itemView);

                // Initialize UI elements in the constructor
                textViewProductName = itemView.findViewById(R.id.textViewProductName);
                textViewProductDetails = itemView.findViewById(R.id.textViewProductDetails);
                // Initialize more UI elements as needed
            }
        }
    }


    private List<String> mapLabelsToProductSearchQueries(List<DetectedObject.Label> labels) {
        // Implement your logic to map object labels to product search queries
        // This could be a simple mapping or a more complex logic based on your application's requirements
        List<String> productSearchQueries = new ArrayList<>();
        for (DetectedObject.Label label : labels) {
            // Example: Map label text to a query
            String query = "product:" + label.getText();
            productSearchQueries.add(query);
        }
        return productSearchQueries;
    }
    private void performAutomaticSearch(List<SearchResult> searchResults) {
        // Implement your search logic here
        // This could involve making network requests, querying a database, etc.

        // Update the UI indicating that a search is in progress
        runOnUiThread(() -> {
            TextView classificationTextView = findViewById(R.id.classificationTextView);
            classificationTextView.setText("Searching...");
        });

        // Example: Simulate a delay (replace this with your actual search logic)
        try {
            Thread.sleep(2000); // Simulate a 2-second search delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Update the UI with the actual search results
        runOnUiThread(() -> {
            TextView classificationTextView = findViewById(R.id.classificationTextView);
            classificationTextView.setText("Search results: ..."); // Update with actual search results
        });
    }

    // Hide action bar on top function
    private void hideActionBar() {
        ActionBar actionBar = ((AppCompatActivity) ObjectDetectionActivity.this).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
