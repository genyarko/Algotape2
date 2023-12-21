package com.example.algotapes2.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.algotapes2.HelpSettings;
import com.example.algotapes2.LoginActivity;
import com.example.algotapes2.ObjectDetectionActivity;
import com.example.algotapes2.R;
import com.example.algotapes2.SubscriptionActivity;
import com.example.algotapes2.TextRecognitionActivity;
import com.example.algotapes2.UserProfileSetting;
import com.example.algotapes2.databinding.FragmentNotificationsBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        // Set hooks
        MaterialCardView profileCard = root.findViewById(R.id.profileCard);
        MaterialCardView helpCard = root.findViewById(R.id.helpCard);
        MaterialCardView subscribeCard = root.findViewById(R.id.subscribeCard);
        ImageView textRecognition = root.findViewById(R.id.textRecognition);
        MaterialButton logOutBtn = root.findViewById(R.id.logOutButton);
        ImageView liveLabel = root.findViewById(R.id.liveLabeling);

        // Set up log out button
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);

            }
        });


        // check if the text rec button is not null
        if (textRecognition == null) {
            // Set the helpCard to a non-null value
            textRecognition = new ImageView(requireContext());
        }

        //set the on click listener for button text recog.
        textRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TextRecognitionActivity.class);
                startActivity(intent);
            }
        });

        // check if live label button is not null
        if (liveLabel == null) {
            // Set the helpCard to a non-null value
            liveLabel = new ImageView(requireContext());
        }

        // Set up on click listener for live labeling
        liveLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ObjectDetectionActivity.class);
                startActivity(intent);
            }
        });

        // Check if the profileCard is null
        if (profileCard == null) {
            // Set the helpCard to a non-null value
            profileCard= new MaterialCardView(requireContext());
        }

        // Set the onClickListener for the profileCard
        profileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the UserProfileSetting activity
                Intent intent = new Intent(getActivity(), UserProfileSetting.class);
                startActivity(intent);
            }
        });
        // Check if the profileCard is null
        if (subscribeCard == null) {
            // Set the helpCard to a non-null value
            subscribeCard= new MaterialCardView(requireContext());
        }

        // Set the onClickListener for the profileCard
        subscribeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the UserProfileSetting activity
                Intent intent = new Intent(getActivity(), SubscriptionActivity.class);
                startActivity(intent);
            }
        });

        // Check if the helpCard is null
        if (helpCard == null) {
            // Set the helpCard to a non-null value
            helpCard= new MaterialCardView(requireContext());
        }

        // Set the onClickListener for helpCard
        helpCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the HelpActivity
                Intent intent = new Intent(getActivity(), HelpSettings.class);
                startActivity(intent);
            }
        });

        hideActionBar();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void hideActionBar() {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }
}