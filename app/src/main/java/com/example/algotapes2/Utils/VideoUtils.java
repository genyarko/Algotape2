package com.example.algotapes2.Utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.IOException;
import java.util.Objects;

public class VideoUtils {

    public static String getVideoDuration(Context context, Uri videoUri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        long durationMillis;

        try {
            retriever.setDataSource(context, videoUri);
            durationMillis = Long.parseLong(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
        } finally {
            retriever.release();
        }

        // Convert duration to a human-readable format (hh:mm:ss)
        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
}


