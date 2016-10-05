package com.guillaume_hermet.www.grooveairlineradio.activities;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.guillaume_hermet.www.grooveairlineradio.R;
import com.guillaume_hermet.www.grooveairlineradio.adapters.ButtonAdapter;
import com.guillaume_hermet.www.grooveairlineradio.models.ActionButton;
import com.guillaume_hermet.www.grooveairlineradio.services.MusicService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.dift.ui.SwipeToAction;

// TODO Slide Appel Skype
// TODO Widget Play/Pause LockScreen
// TODO Music on OffScreen
// TODO Offline Mode (No internet)


// TODO Widget (Bonus)


public class MainActivity extends AppCompatActivity {
    private String TAG = this.getClass().getSimpleName();
    private ImageButton mPlayPause;
    private ProgressBar mLoader;
    private MediaPlayer mp;
    private SeekBar mSoundBar;
    private TextView mVolumeText;
    private MusicService mServ;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(this.getResources().getColor(R.color.colorBlack));
        ImageView mLogo = (ImageView) findViewById(R.id.logo_img);
        Picasso.with(getApplicationContext())
                .load("https://static.wixstatic.com/media/3e030d_dfcd357eb0014915b7d6b2c911b10862.png/v1/fill/w_922,h_304,al_c,usm_0.66_1.00_0.01/3e030d_dfcd357eb0014915b7d6b2c911b10862.png")
                .error(R.mipmap.ic_launcher)
                .into(mLogo);
        mPlayPause = (ImageButton) findViewById(R.id.button_play);
        mLoader = (ProgressBar) findViewById(R.id.loader_progress);
        mSoundBar = (SeekBar) findViewById(R.id.sound_bar);
        mVolumeText = (TextView) findViewById(R.id.volume_text);
        mSoundBar.setVisibility(View.GONE);
        mPlayPause.setVisibility(View.GONE);
        mVolumeText.setVisibility(View.GONE);
        mLoader.setVisibility(View.VISIBLE);
        // Media PLayer
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mp.setDataSource(getBaseContext(), Uri.parse("http://streaming.radionomy.com/GAR?lang=fr"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.prepareAsync();
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPrepared(final MediaPlayer mp) {
                Log.d(TAG, "onPrepared()");
                mPlayPause.setVisibility(View.VISIBLE);
                mSoundBar.setVisibility(View.VISIBLE);
                mVolumeText.setVisibility(View.VISIBLE);
                mLoader.setVisibility(View.GONE);
                // start music service
                mServ = new MusicService(mp, mPlayPause);
                Picasso.with(getApplicationContext())
                        .load(R.mipmap.ic_play)
                        .error(R.mipmap.ic_play)
                        .into(mPlayPause);
                // Volume Bar
                final AudioManager audioManager =
                        (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                mSoundBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                mSoundBar.setProgress(audioManager
                        .getStreamVolume(AudioManager.STREAM_MUSIC));
                mSoundBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(), 0);

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(), 0);
                    }
                });
                // Play/PAuse Button
                mPlayPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mp.isPlaying()) stopMusic();
                        else startMusic();
                    }
                });

            }
        });
        // Programme / Evenements Button
        Button mEventsButton = (Button) findViewById(R.id.button_events);
        mEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Events/Prog");
                Intent progIntent = new Intent(getApplicationContext(), ProgrammeActivity.class);
                startActivity(progIntent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                //finish();
            }
        });
        // Podcast Button
        Button mPodcastButton = (Button) findViewById(R.id.button_podcast);
        mPodcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Podcast");
                Intent podcastIntent = new Intent(getApplicationContext(), PodcastActivity.class);
                startActivity(podcastIntent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                // finish();


            }
        });
        // Skype Button
        Button mSkypeButton = (Button) findViewById(R.id.button_skype);
        mSkypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws NullPointerException {
                if (mp.isPlaying()) stopMusic();
                Log.d(TAG, "Skype");
                String contactUserName = "live:guillaume.hermet";
                initiateSkypeUri(getApplicationContext(), contactUserName, "call");

            }
        });

        // RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        List<ActionButton> buttons = new ArrayList<>();
        populate(buttons);
        ButtonAdapter adapter = new ButtonAdapter(getApplicationContext(), buttons);
        recyclerView.setAdapter(adapter);

        final SwipeToAction swipeToAction = new SwipeToAction(recyclerView, new SwipeToAction.SwipeListener<ActionButton>() {
            @Override
            public boolean swipeLeft(final ActionButton itemData) {
                switch (itemData.getTitle()) {
                    case "EVENTS":
                        Log.d(TAG, "Events/Prog");
                        Intent progIntent = new Intent(getApplicationContext(), ProgrammeActivity.class);
                        startActivity(progIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;
                    case "PODCAST":
                        Log.d(TAG, "Podcast");
                        Intent podcastIntent = new Intent(getApplicationContext(), PodcastActivity.class);
                        startActivity(podcastIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;
                    case "SKYPE":
                        if (mp.isPlaying()) stopMusic();
                        Log.d(TAG, "Skype");
                        String contactUserName = "live:guillaume.hermet";
                        initiateSkypeUri(getApplicationContext(), contactUserName, "chat");
                        break;

                    default:

                }
                return true;
            }

            @Override
            public boolean swipeRight(ActionButton itemData) {
                switch (itemData.getTitle()) {
                    case "EVENTS":
                        Log.d(TAG, "Events/Prog");
                        Intent progIntent = new Intent(getApplicationContext(), ProgrammeActivity.class);
                        startActivity(progIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;
                    case "PODCAST":
                        Log.d(TAG, "Podcast");
                        Intent podcastIntent = new Intent(getApplicationContext(), PodcastActivity.class);
                        startActivity(podcastIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;
                    case "SKYPE":
                        if (mp.isPlaying()) stopMusic();
                        Log.d(TAG, "Skype");
                        String contactUserName = "live:guillaume.hermet";
                        initiateSkypeUri(getApplicationContext(), contactUserName, "call");
                        break;

                    default:

                }
                return true;
            }

            @Override
            public void onClick(ActionButton itemData) {
                switch (itemData.getTitle()) {
                    case "EVENTS":
                        Log.d(TAG, "Events/Prog");
                        Intent progIntent = new Intent(getApplicationContext(), ProgrammeActivity.class);
                        startActivity(progIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;
                    case "PODCAST":
                        Log.d(TAG, "Podcast");
                        Intent podcastIntent = new Intent(getApplicationContext(), PodcastActivity.class);
                        startActivity(podcastIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;
                    default:

                }
            }

            @Override
            public void onLongClick(ActionButton itemData) {
            }
        });


    }

    private void populate(List<ActionButton> buttons) {
        buttons.add(new ActionButton(R.color.colorTeal, R.mipmap.ic_calendar_check, R.mipmap.ic_calendar_check, "EVENTS"));
        buttons.add(new ActionButton(R.color.colorOrange, R.mipmap.ic_music, R.mipmap.ic_music, "PODCAST"));
        buttons.add(new ActionButton(R.color.colorBlueSkype, R.mipmap.ic_message, R.mipmap.ic_call, "SKYPE"));
    }

    public void initiateSkypeUri(Context myContext, String mySkypeUri, String action) {
        Log.d(TAG, "initiateSkypeUri()");
        // Make sure the Skype for Android client is installed.
        if (!isSkypeClientInstalled(myContext)) {
            Log.d(TAG, "Skype not installed");
            goToMarket(myContext);
        } else {
            Log.d(TAG, "Skype already installed");
            // Create the Intent from our Skype URI.
            Uri skypeUri = Uri.parse("skype:" + mySkypeUri + "?" + action);
            Intent myIntent = new Intent(Intent.ACTION_VIEW, skypeUri);
            // Restrict the Intent to being handled by the Skype for Android client only.
            myIntent.setComponent(new ComponentName("com.skype.raider", "com.skype.raider.Main"));
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            // Initiate the Intent. It should never fail because you've already established the
            // presence of its handler (although there is an extremely minute window where that
            // handler can go away).
            myContext.startActivity(myIntent);
        }
    }


    public boolean isSkypeClientInstalled(Context myContext) {
        PackageManager myPackageMgr = myContext.getPackageManager();
        try {
            myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            return (false);
        }
        return (true);
    }

    public void goToMarket(Context myContext) {
        Log.d(TAG, "gotToMarket()");
        final String appPackageName = "com.skype.raider";
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            myContext.startActivity(i);
        } catch (android.content.ActivityNotFoundException anfe) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            myContext.startActivity(i);
        }

    }

    private void stopMusic() {
        mServ.pauseMusic();
        if (!mServ.getmPlayer().isPlaying()) {
            Picasso.with(getApplicationContext())
                    .load(R.mipmap.ic_play)
                    .error(R.mipmap.ic_play)
                    .into(mPlayPause);
        }
    }

    private void startMusic() {
        mServ.resumeMusic();
        if (mServ.getmPlayer().isPlaying()) {
            Picasso.with(getApplicationContext())
                    .load(R.mipmap.ic_pause)
                    .error(R.mipmap.ic_pause)
                    .into(mPlayPause);
        }
      /*
       MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(getBaseContext(), Uri.parse("http://listen.radionomy.com/gar.m3u"));
        String artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        Log.d(TAG,artist);
        */
    }

    @Override
    public void onResume() {
        super.onResume();
          Log.d(TAG,"OnResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"OnPause()");
        //stopMusic();
        //new AsyncMusic().cancel(true);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        mServ.stopMusic();
    }


}






