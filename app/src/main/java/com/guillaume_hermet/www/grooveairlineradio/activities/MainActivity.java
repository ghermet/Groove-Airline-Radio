package com.guillaume_hermet.www.grooveairlineradio.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.guillaume_hermet.www.grooveairlineradio.R;
import com.guillaume_hermet.www.grooveairlineradio.adapters.ButtonAdapter;
import com.guillaume_hermet.www.grooveairlineradio.asynctasks.currentTrackNotification;
import com.guillaume_hermet.www.grooveairlineradio.asynctasks.currentTrackXml;
import com.guillaume_hermet.www.grooveairlineradio.asynctasks.liveStream;
import com.guillaume_hermet.www.grooveairlineradio.models.ActionButton;
import com.guillaume_hermet.www.grooveairlineradio.models.Track;
import com.guillaume_hermet.www.grooveairlineradio.receivers.CallIntentReceiver;
import com.guillaume_hermet.www.grooveairlineradio.receivers.HeadsetIntentReceiver;
import com.guillaume_hermet.www.grooveairlineradio.services.MusicService;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import co.dift.ui.SwipeToAction;



public class MainActivity extends AppCompatActivity implements ComponentCallbacks2 {
    private String TAG = this.getClass().getSimpleName();
    private ImageButton mPlayPause;
    private ProgressBar mLoader;
    private SeekBar mSoundBar;
    private TextView mVolumeText;
    private MusicService mServ;
    private ImageView mCover;
    private TextView mTitleText;
    private TextView mArtistText;

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    private Notification notification;
    private ImageView mLogo;
    private Track currentTrack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        statusBarSetup(R.color.colorBlack);
        setUpLayoutComponents();
        findViewById(R.id.loader_progress).setVisibility(View.VISIBLE);
        setUpCoverIntro();
        findViewById(R.id.logo_img).setVisibility(View.VISIBLE);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isNetworkConnected()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mServ != null && mServ.getmPlayer() != null) stopMusic();
                            findViewById(R.id.loader_progress).setVisibility(View.GONE);
                            findViewById(R.id.rl_no_internet).setVisibility(View.VISIBLE);
                        }
                    });

                } else {
                    if (mServ == null) {
                        setUpMusicService();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.rl_no_internet).setVisibility(View.GONE);
                                setUpCoverBackground();
                                setUpRecyclerViewButtons();

                            }
                        });
                        new currentTrackXml(MainActivity.this).loadCurrentTrack();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.rl_no_internet).setVisibility(View.GONE);
                            }
                        });

                    }
                }

            }
        }, 500, 1000);


    }

    private void statusBarSetup(int color) {
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        // finally change the color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(this.getResources().getColor(color));
        }
    }

    private void setUpLayoutComponents() {
        mPlayPause = (ImageButton) findViewById(R.id.button_play);
        mLoader = (ProgressBar) findViewById(R.id.loader_progress);
        mSoundBar = (SeekBar) findViewById(R.id.sound_bar);
        mVolumeText = (TextView) findViewById(R.id.volume_text);
        mCover = (ImageView) findViewById(R.id.cover_img);
        mTitleText = (TextView) findViewById(R.id.tv_title);
        mArtistText = (TextView) findViewById(R.id.tv_artist);
        mLogo = (ImageView) findViewById(R.id.logo_img);
        mSoundBar.setVisibility(View.GONE);
        mPlayPause.setVisibility(View.GONE);
        mVolumeText.setVisibility(View.GONE);
        mCover.setVisibility(View.GONE);
        mTitleText.setVisibility(View.GONE);
        mArtistText.setVisibility(View.GONE);
        mLoader.setVisibility(View.GONE);
    }

    private void setUpCoverBackground() {
        String logoUrl = "http://psgpassion.free.fr/GA/gap.jpg";
        Picasso.with(getApplicationContext())
                .load(logoUrl)
                .error(R.mipmap.ic_launcher)
                .into(mLogo);
    }

    private void setUpCoverIntro() {
        String logoUrl = "http://psgpassion.free.fr/GA/gac.jpg";
        Picasso.with(getApplicationContext())
                .load(logoUrl)
                .error(R.mipmap.ic_launcher)
                .into(mLogo);
    }

    private void setUpRecyclerViewButtons() {
        // RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        List<ActionButton> buttons = new ArrayList<>();
        populate(buttons);
        ButtonAdapter adapter = new ButtonAdapter(getApplicationContext(), buttons);
        recyclerView.setAdapter(adapter);

        new SwipeToAction(recyclerView, new SwipeToAction.SwipeListener<ActionButton>() {
            Intent browserIntent;

            @Override
            public boolean swipeLeft(final ActionButton itemData) {
                //moveTaskToBack(false);
                switch (itemData.getTitle()) {
                    case "PROGRAMME":
                        Log.d(TAG, "Events/Prog");
                        /*
                        Intent progIntent = new Intent(getApplicationContext(), ProgrammeActivity.class);
                        startActivity(progIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        */
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gar_url_multimedia)));
                        startActivity(browserIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        new currentTrackNotification(MainActivity.this).buildNotification();
                        break;
                    case "PODCAST":
                        Log.d(TAG, "Podcast");
                        /*
                        Intent podcastIntent = new Intent(getApplicationContext(), PodcastActivity.class);
                        startActivity(podcastIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        */
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gar_url_podcast)));
                        startActivity(browserIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        new currentTrackNotification(MainActivity.this).buildNotification();
                        break;
                    case "SKYPE":
                        if (mServ.getmPlayer() != null) stopMusic();
                        Log.d(TAG, "Skype");
                        String contactUserName = "groove.airline";
                        initiateSkypeUri(getApplicationContext(), contactUserName, "chat");
                        break;

                    default:

                }
                return true;
            }

            @Override
            public boolean swipeRight(ActionButton itemData) {
                // moveTaskToBack(false);
                switch (itemData.getTitle()) {
                    case "PROGRAMME":
                        Log.d(TAG, "Events/Prog");
                        /*
                        Intent progIntent = new Intent(getApplicationContext(), ProgrammeActivity.class);
                        startActivity(progIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        */
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gar_url_multimedia)));
                        startActivity(browserIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        new currentTrackNotification(MainActivity.this).buildNotification();
                        break;
                    case "PODCAST":
                        Log.d(TAG, "Podcast");
                        /*
                        Intent podcastIntent = new Intent(getApplicationContext(), PodcastActivity.class);
                        startActivity(podcastIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        */
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gar_url_podcast)));
                        startActivity(browserIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        new currentTrackNotification(MainActivity.this).buildNotification();
                        break;
                    case "SKYPE":
                        if (mServ.getmPlayer() != null) stopMusic();
                        Log.d(TAG, "Skype");
                        String contactUserName = getString(R.string.gar_username_skype);
                        initiateSkypeUri(getApplicationContext(), contactUserName, "call");
                        break;

                    default:

                }
                return true;
            }

            @Override
            public void onClick(ActionButton itemData) {
                // moveTaskToBack(false);
                switch (itemData.getTitle()) {
                    case "PROGRAMME":
                        Log.d(TAG, "Events/Prog");
                        /*
                        Intent progIntent = new Intent(getApplicationContext(), ProgrammeActivity.class);
                        startActivity(progIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        */
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.grooveairline.fr/programme"));
                        startActivity(browserIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        new currentTrackNotification(MainActivity.this).buildNotification();
                        break;
                    case "PODCAST":
                        Log.d(TAG, "Podcast");
                        /*
                        Intent podcastIntent = new Intent(getApplicationContext(), PodcastActivity.class);
                        startActivity(podcastIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        */
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gar_url_podcast)));
                        startActivity(browserIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        new currentTrackNotification(MainActivity.this).buildNotification();
                        break;
                    default:

                }
            }

            @Override
            public void onLongClick(ActionButton itemData) {
            }
        });
    }

    public MusicService getmServ() {
        return mServ;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    private void setUpMusicService() {
        mServ = new MusicService(this);
        if (mServ.getmPlayer() != null)
            mServ.getmPlayer().setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(final MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "onError(): " + what + " " + extra);
                    stopMusic();
                    return false;
                }
            });
        if (mServ.getmPlayer() != null)
            mServ.getmPlayer().setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {
                    Log.d(TAG, "onPrepared()");
                    IntentFilter noiseReceiverFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
                    HeadsetIntentReceiver noiseIntentReceiver = new HeadsetIntentReceiver(MainActivity.this, mServ);
                    registerReceiver(noiseIntentReceiver, noiseReceiverFilter);
                    IntentFilter callReceiverFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
                    CallIntentReceiver callIntentReceiver = new CallIntentReceiver(MainActivity.this, mServ);
                    registerReceiver(callIntentReceiver, callReceiverFilter);
                    IntentFilter incomingCallReceiverFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                    CallIntentReceiver incomingCallIntentReceiver = new CallIntentReceiver(MainActivity.this, mServ);
                    registerReceiver(incomingCallIntentReceiver, incomingCallReceiverFilter);
                    mLoader.setVisibility(View.GONE);
                    findViewById(R.id.recycler).setVisibility(View.VISIBLE);
                    volumeBarSetup();
                    playButtonSetup();

                }
            });
    }

    private void volumeBarSetup() {
        // Volume Bar
        final AudioManager audioManager =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final SeekBar mSoundBar = (SeekBar) findViewById(R.id.sound_bar);
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
        mSoundBar.setVisibility(View.VISIBLE);
        mVolumeText.setVisibility(View.VISIBLE);
    }

    private void playButtonSetup() {
        // Play/Pause Button
        Picasso.with(getApplicationContext())
                .load(R.mipmap.ic_play)
                .error(R.mipmap.ic_play)
                .into(mPlayPause);
        findViewById(R.id.button_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    if (mServ.getmPlayer() == null)
                        new liveStream(MainActivity.this).loadLiveStream();
                    else if (mServ.getmPlayer().isPlaying())
                        stopMusic();
                    else if (!mServ.getmPlayer().isPlaying()) {
                        startMusic();
                    }

                } else {
                    if (mServ != null && mServ.getmPlayer() != null) stopMusic();
                    findViewById(R.id.rl_no_internet).setVisibility(View.VISIBLE);
                }
            }
        });
        findViewById(R.id.button_play).setVisibility(View.VISIBLE);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return !cm.getActiveNetworkInfo().getDetailedState().equals(NetworkInfo.DetailedState.VERIFYING_POOR_LINK) && cm.getActiveNetworkInfo() != null;
    }



    private void populate(List<ActionButton> buttons) {
        buttons.add(new ActionButton(R.color.colorTeal, R.mipmap.ic_calendar_check, R.mipmap.ic_calendar_check, "PROGRAMME"));
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
        if (mServ.getmPlayer().isPlaying()) {
            clearNotifications();
            mServ.stopMusic();
            Picasso.with(getApplicationContext())
                    .load(R.mipmap.ic_play)
                    .error(R.mipmap.ic_play)
                    .into(mPlayPause);
            mCover.setVisibility(View.GONE);
            mTitleText.setVisibility(View.GONE);
            mArtistText.setVisibility(View.GONE);
        }
    }

    public void startMusic() {
        if (!mServ.getmPlayer().isPlaying()) {
            mServ.resumeMusic();
            Picasso.with(getApplicationContext())
                    .load(R.mipmap.ic_stop)
                    .error(R.mipmap.ic_stop)
                    .into(mPlayPause);
            mCover.setVisibility(View.VISIBLE);
            mTitleText.setVisibility(View.VISIBLE);
            mArtistText.setVisibility(View.VISIBLE);
        }
    }

    private void showDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Quit Application")
                .setMessage("Are you sure you want to quit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        if (mServ.getmPlayer() != null)
                            if (mServ.getmPlayer().isPlaying()) stopMusic();
                        clearNotifications();
                        System.exit(0);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        clearNotifications();
                    }
                })
                .setCancelable(true)
                .show();

    }

    private void clearNotifications() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    private void handleNotifications() {
        if (getmServ() != null) {
            if (getmServ().getmPlayer() != null && getmServ().getmPlayer().isPlaying())
                new currentTrackNotification(MainActivity.this).buildNotification();
            else if (getmServ().getmPlayer() != null && !getmServ().getmPlayer().isPlaying()) {
                clearNotifications();
            }
        }else {
            clearNotifications();
        }
    }

    public Notification getNotification() {
        return notification;
    }


    // 2.0 and above
    @Override
    public void onBackPressed() {
        handleNotifications();
        moveTaskToBack(true);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearNotifications();
        System.exit(0);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        handleNotifications();

    }

    // Before 2.0
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                handleNotifications();
                moveTaskToBack(true);
                return true;
            default:
                return super.onKeyDown(keyCode, event);

        }
    }


    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
    }


}



