package com.example.algotapes2;

import static com.example.algotapes2.TextRecognitionUtil.processImageForTextRecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.algotapes2.Utils.Event;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EventsFragment extends Fragment {

    private static final String EVENTS_REFERENCE = "events";
    private FirebaseRecyclerAdapter<Event, EventViewHolder> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(EVENTS_REFERENCE);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // Create a new adapter for the RecyclerView.
        adapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(getFirebaseRecyclerOptions(databaseReference)) {
            @Override
            protected void onBindViewHolder(@NonNull EventViewHolder holder, int position, @NonNull Event model) {
                // Bind data to ViewHolder
                holder.bind(model);
                // Ensure that position is valid before binding
                if (position != RecyclerView.NO_POSITION) {
                    // Bind data to ViewHolder
                    try {
                        holder.bind(model);
                    } catch (Exception e) {
                        // Handle any binding errors gracefully
                        e.printStackTrace();
                    }
                }
                holder.uploadEvent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CreateEvent.class);
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_event, parent, false);
                return new EventViewHolder(view);
            }
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                // Notify data set changes to keep UI in sync
                notifyDataSetChanged();
            }
        };


        recyclerView.setAdapter(adapter);
        hideActionBar();

        return view;
    }
    // Hide action bar on top function
    private void hideActionBar() {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private FirebaseRecyclerOptions<Event> getFirebaseRecyclerOptions(DatabaseReference databaseReference) {
        return new FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(databaseReference, Event.class)
                .build();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        public TextView eventTitleTextView;
        public TextView eventCategoryTextView;
        public TextView eventDateTextView;
        public TextView eventDescriptionTextView;
        public ImageView postImageView, uploadEvent;
        public TextView recognizedTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitleTextView = itemView.findViewById(R.id.eventTitle);
            eventCategoryTextView = itemView.findViewById(R.id.eventCategory);
            eventDateTextView = itemView.findViewById(R.id.eventDate);
            eventDescriptionTextView = itemView.findViewById(R.id.eventDescription);
            postImageView = itemView.findViewById(R.id.postImage);
            uploadEvent = itemView.findViewById(R.id.uploadEvent);
            recognizedTextView = itemView.findViewById(R.id.menu_recognize_text);
            recognizedTextView.setVisibility(View.GONE);

            postImageView.setOnLongClickListener(v -> {
                recognizedTextView.setVisibility(View.VISIBLE);
                recognizedTextView.setText("Processing...");

                Bitmap postImageBitmap = getBitmapFromDrawable(postImageView.getDrawable());
                if (postImageBitmap != null) {
                    processImageForTextRecognition(postImageBitmap, recognizedTextView);
                }
                return true;
            });

            postImageView.setOnClickListener(v -> recognizedTextView.setVisibility(View.GONE));
        }

        private Bitmap getBitmapFromDrawable(Drawable drawable) {
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
            return null;
        }

        public void bind(Event event) {
            if (event != null) {
                eventTitleTextView.setText(event.getEventTitle());
                eventCategoryTextView.setText(event.getEventCategory());
                eventDateTextView.setText(event.getDate());
                eventDescriptionTextView.setText(event.getDescription());

                Glide.with(itemView.getContext())
                        .load(event.getImageUrl())
                        .placeholder(R.drawable.baseline_add_location_24)
                        .error(R.drawable.baseline_notifications_24vermillion)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                if (e != null) {
                                    e.logRootCauses("Glide load failed");
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                Bitmap postImageBitmap = getBitmapFromDrawable(resource);
                                if (postImageBitmap != null) {
                                    recognizedTextView.setText("Processing...");
                                    processImageForTextRecognition(postImageBitmap, recognizedTextView);
                                }
                                return false;
                            }
                        })
                        .into(postImageView);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}





