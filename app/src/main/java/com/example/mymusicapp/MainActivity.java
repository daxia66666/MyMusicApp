package com.example.mymusicapp;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final List<MusicItem> musicItems = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private TextView nowPlayingTextView;
    private ImageButton playButton;
    private int currentIndex = -1;
    private boolean isPrepared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nowPlayingTextView = findViewById(R.id.nowPlayingTextView);
        playButton = findViewById(R.id.playButton);
        ImageButton previousButton = findViewById(R.id.previousButton);
        ImageButton nextButton = findViewById(R.id.nextButton);
        ListView musicListView = findViewById(R.id.musicListView);

        initMusicItems();
        musicListView.setAdapter(new MusicAdapter());
        musicListView.setOnItemClickListener((parent, view, position, id) -> playSong(position));

        previousButton.setOnClickListener(view -> playPrevious());
        playButton.setOnClickListener(view -> togglePlayPause());
        nextButton.setOnClickListener(view -> playNext());
    }

    private void initMusicItems() {
        musicItems.add(new MusicItem("\u84dd\u8272\u6e05\u6668", "\u4e0a\u884c\u94a2\u7434\u98ce\u77ed\u65cb\u5f8b", R.raw.track_01));
        musicItems.add(new MusicItem("\u9633\u5149\u8282\u62cd", "\u660e\u5feb\u7535\u5b50\u8282\u594f", R.raw.track_02));
        musicItems.add(new MusicItem("\u6821\u56ed\u56de\u5fc6", "\u8212\u7f13\u4e0b\u884c\u65cb\u5f8b", R.raw.track_03));
        musicItems.add(new MusicItem("\u661f\u7a7a\u65cb\u5f8b", "\u94c3\u58f0\u97f3\u8272\u9ad8\u97f3\u65cb\u5f8b", R.raw.track_04));
        musicItems.add(new MusicItem("\u8f7b\u5feb\u811a\u6b65", "\u4f4e\u97f3\u8df3\u8dc3\u8282\u594f", R.raw.track_05));
        musicItems.add(new MusicItem("\u5348\u540e\u5fae\u98ce", "\u8f7b\u67d4\u62c9\u957f\u65cb\u5f8b", R.raw.track_06));
        musicItems.add(new MusicItem("\u57ce\u5e02\u706f\u5149", "\u5206\u89e3\u548c\u5f26\u5f0f\u65cb\u5f8b", R.raw.track_07));
        musicItems.add(new MusicItem("\u96e8\u540e\u8857\u9053", "\u5e26\u505c\u987f\u7684\u6e05\u8106\u94c3\u58f0", R.raw.track_08));
        musicItems.add(new MusicItem("\u6d77\u8fb9\u65c5\u884c", "\u8f7b\u5feb\u4e0a\u4e0b\u8d77\u4f0f\u65cb\u5f8b", R.raw.track_09));
        musicItems.add(new MusicItem("\u665a\u5b89\u5fc3\u66f2", "\u6162\u901f\u7761\u524d\u94c3\u58f0", R.raw.track_10));
    }

    private void playSong(int index) {
        if (index < 0 || index >= musicItems.size()) {
            return;
        }

        releasePlayer();
        currentIndex = index;
        MusicItem item = musicItems.get(index);
        mediaPlayer = MediaPlayer.create(this, item.audioResId);
        isPrepared = mediaPlayer != null;

        if (!isPrepared) {
            nowPlayingTextView.setText("\u64ad\u653e\u5931\u8d25\uff1a" + item.name);
            return;
        }

        mediaPlayer.setOnCompletionListener(player -> {
            playButton.setImageResource(R.drawable.ic_play_arrow);
            playNext();
        });
        mediaPlayer.start();
        playButton.setImageResource(R.drawable.ic_pause);
        nowPlayingTextView.setText("\u6b63\u5728\u64ad\u653e\uff1a" + item.name);
    }

    private void togglePlayPause() {
        if (currentIndex == -1) {
            playSong(0);
            return;
        }

        if (mediaPlayer == null || !isPrepared) {
            playSong(currentIndex);
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playButton.setImageResource(R.drawable.ic_play_arrow);
            nowPlayingTextView.setText("\u5df2\u6682\u505c\uff1a" + musicItems.get(currentIndex).name);
        } else {
            mediaPlayer.start();
            playButton.setImageResource(R.drawable.ic_pause);
            nowPlayingTextView.setText("\u6b63\u5728\u64ad\u653e\uff1a" + musicItems.get(currentIndex).name);
        }
    }

    private void playPrevious() {
        int targetIndex = currentIndex <= 0 ? musicItems.size() - 1 : currentIndex - 1;
        playSong(targetIndex);
    }

    private void playNext() {
        int targetIndex = currentIndex >= musicItems.size() - 1 ? 0 : currentIndex + 1;
        playSong(targetIndex);
    }

    private void releasePlayer() {
        isPrepared = false;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }

    private static class MusicItem {
        final String name;
        final String description;
        final int audioResId;

        MusicItem(String name, String description, int audioResId) {
            this.name = name;
            this.description = description;
            this.audioResId = audioResId;
        }
    }

    private class MusicAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return musicItems.size();
        }

        @Override
        public Object getItem(int position) {
            return musicItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_music, parent, false);
                holder = new ViewHolder();
                holder.nameTextView = convertView.findViewById(R.id.songNameTextView);
                holder.descriptionTextView = convertView.findViewById(R.id.songDescriptionTextView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MusicItem item = musicItems.get(position);
            holder.nameTextView.setText(item.name);
            holder.descriptionTextView.setText(item.description);
            return convertView;
        }
    }

    private static class ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
    }
}
