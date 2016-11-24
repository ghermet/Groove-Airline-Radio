package com.guillaume_hermet.www.grooveairlineradio.asynctasks;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.guillaume_hermet.www.grooveairlineradio.R;
import com.guillaume_hermet.www.grooveairlineradio.activities.MainActivity;

/**
 * Created by Guillaume on 11/24/16.
 */

public class liveStream {

    private final ImageButton mPlayPause;
    private final ProgressBar mLoader;
    private final SeekBar mSoundBar;
    private final TextView mVolumeText;
    private final ImageView mCover;
    private final TextView mTitleText;
    private final TextView mArtistText;
    private final ImageView mLogo;
    private MainActivity context;

    public liveStream(MainActivity context) {
        this.context = context;
        mPlayPause = (ImageButton) context.findViewById(R.id.button_play);
        mLoader = (ProgressBar) context.findViewById(R.id.loader_progress);
        mSoundBar = (SeekBar) context.findViewById(R.id.sound_bar);
        mVolumeText = (TextView) context.findViewById(R.id.volume_text);
        mCover = (ImageView) context.findViewById(R.id.cover_img);
        mTitleText = (TextView) context.findViewById(R.id.tv_title);
        mArtistText = (TextView) context.findViewById(R.id.tv_artist);
        mLogo = (ImageView) context.findViewById(R.id.logo_img);

    }

    public void loadLiveStream() {
        new liveStreamAsync().execute();
    }

    private class liveStreamAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            context.getmServ().setmPlayer(MediaPlayer.create(context, Uri.parse("http://streaming.radionomy.com/GAR?lang=en-US")));
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPlayPause.setVisibility(View.GONE);
            mCover.setVisibility(View.GONE);
            mTitleText.setVisibility(View.GONE);
            mArtistText.setVisibility(View.GONE);
            mVolumeText.setVisibility(View.GONE);
            mSoundBar.setVisibility(View.GONE);
            mLoader.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mPlayPause.setVisibility(View.VISIBLE);
            mVolumeText.setVisibility(View.VISIBLE);
            mSoundBar.setVisibility(View.VISIBLE);
            mLoader.setVisibility(View.GONE);
            context.startMusic();
        }
    }

}
