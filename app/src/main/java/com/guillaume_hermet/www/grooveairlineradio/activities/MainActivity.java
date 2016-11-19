package com.guillaume_hermet.www.grooveairlineradio.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Toast;

import com.guillaume_hermet.www.grooveairlineradio.HeadsetIntentReceiver;
import com.guillaume_hermet.www.grooveairlineradio.R;
import com.guillaume_hermet.www.grooveairlineradio.adapters.ButtonAdapter;
import com.guillaume_hermet.www.grooveairlineradio.models.ActionButton;
import com.guillaume_hermet.www.grooveairlineradio.models.Track;
import com.guillaume_hermet.www.grooveairlineradio.services.MusicService;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import co.dift.ui.SwipeToAction;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// TODO Widget (Bonus)


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
    private Track currentTrack;
    private Notification notification;
    private ImageView mLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusBarSetup(R.color.colorBlack);

        setUpLayoutComponents();
        findViewById(R.id.loader_progress).setVisibility(View.VISIBLE);
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
                        getCurrentTrack();


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
        String logoUrl = "https://static.wixstatic.com/media/3e030d_dfcd357eb0014915b7d6b2c911b10862.png/v1/fill/w_922,h_304,al_c,usm_0.66_1.00_0.01/3e030d_dfcd357eb0014915b7d6b2c911b10862.png";
        Picasso.with(getApplicationContext())
                .load(logoUrl)
                .error(R.mipmap.ic_launcher)
                .into(mLogo);
    }

    private void setUpRecyclerViewButtons() {
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
            Intent browserIntent;

            @Override
            public boolean swipeLeft(final ActionButton itemData) {
                //moveTaskToBack(false);
                switch (itemData.getTitle()) {
                    case "EVENTS":
                        Log.d(TAG, "Events/Prog");
                        /*
                        Intent progIntent = new Intent(getApplicationContext(), ProgrammeActivity.class);
                        startActivity(progIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        */
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gar_url_multimedia)));
                        startActivity(browserIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        getNotification();
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
                        getNotification();
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
                    case "EVENTS":
                        Log.d(TAG, "Events/Prog");
                        /*
                        Intent progIntent = new Intent(getApplicationContext(), ProgrammeActivity.class);
                        startActivity(progIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        */
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gar_url_multimedia)));
                        startActivity(browserIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        getNotification();
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
                        getNotification();
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
                    case "EVENTS":
                        Log.d(TAG, "Events/Prog");
                        /*
                        Intent progIntent = new Intent(getApplicationContext(), ProgrammeActivity.class);
                        startActivity(progIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        */
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gar_url_multimedia)));
                        startActivity(browserIntent);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        getNotification();
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
                        getNotification();
                        break;
                    default:

                }
            }

            @Override
            public void onLongClick(ActionButton itemData) {
            }
        });
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
                    IntentFilter receiverFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
                    HeadsetIntentReceiver receiver = new HeadsetIntentReceiver(MainActivity.this,mServ);
                    registerReceiver( receiver, receiverFilter );
                    mLoader.setVisibility(View.GONE);
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
                        new liveStreamAsync().execute();
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

        return cm.getActiveNetworkInfo() != null;
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
        if (mServ.getmPlayer().isPlaying()) {
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

    private void startMusic() {
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
                        if (mServ.getmPlayer() != null )
                            if (mServ.getmPlayer().isPlaying())stopMusic();
                        NotificationManager mNotificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.cancelAll();
                        System.exit(0);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        NotificationManager mNotificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.cancelAll();
                    }
                })
                .setCancelable(true)
                .show();

    }


    // 2.0 and above
    @Override
    public void onBackPressed() {
        if (mServ.getmPlayer() != null && mServ.getmPlayer().isPlaying())
            //getNotification();
            // moveTaskToBack(true);
            showDialog();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServ.getmPlayer() != null && mServ.getmPlayer().isPlaying())
            getNotification();
        else if(mServ.getmPlayer() != null && !mServ.getmPlayer().isPlaying()){
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();
        }

        // moveTaskToBack(false);
    }

    // Before 2.0
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mServ.getmPlayer() != null && mServ.getmPlayer().isPlaying()) getNotification();
                showDialog();
                return true;
            case KeyEvent.KEYCODE_HOME:
                Toast.makeText(getApplicationContext(), "Home button", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onKeyDown(keyCode, event);

        }
    }

    //Implementation of AsyncTask used to download XML feed from Radionomy
    class currentTrackXmlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return "Connection Error";
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                return "XML parsing Error";
            }

        }


        private String loadXmlFromNetwork(String url) throws IOException, XmlPullParserException {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }


        private JSONObject xmlToJson(String xml) {
            JSONObject jsonObj = null;
            try {
                jsonObj = XML.toJSONObject(xml);
            } catch (JSONException e) {
                /*Log.e("JSON exception", e.getMessage());*/
                e.printStackTrace();
            }
            return jsonObj;
        }


        @Override
        protected void onPostExecute(String result) {
            JSONObject json = xmlToJson(result);
            JSONObject tracks = null;
            String title;
            String artist;
            String cover;
            int callmeback;
            try {
                tracks = json.getJSONObject("tracks");
                title = tracks.getJSONObject("track").getString("title");
                artist = tracks.getJSONObject("track").getString("artists");
                cover = tracks.getJSONObject("track").getString("cover");
                callmeback = Integer.parseInt(tracks.getJSONObject("track").getString("callmeback"));
                currentTrack = new Track(title, artist, cover, callmeback);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (tracks != null) {
                Log.d(TAG, "Response: " + tracks.toString());
                assert currentTrack != null;
                //new getBitmapFromUrlAsync().execute(currentTrack.getCover());
                if (currentTrack != null){
                    mCover = (ImageView) findViewById(R.id.cover_img);
                    Picasso.with(getApplicationContext())
                            .load(currentTrack.getCover())
                            .fit()
                            .error(R.mipmap.ic_launcher)
                            .into(mCover);
                }
                mTitleText.setText(currentTrack.getTitle());
                mArtistText.setText(currentTrack.getArtist());
                if (notification != null) {
                    new getNotificationAsync().execute();
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isNetworkConnected()) getCurrentTrack();
                    }
                }, currentTrack.getCallmeback());
            }


        }


    }

    public void getCurrentTrack() {
        String currentTrackUrl = "http://api.radionomy.com/currentsong.cfm?radiouid=7406f2d9-06a3-46bc-9290-6b60269ed7ea&apikey=ea720980-3067-4c5b-8cd8-f279fef21c48&callmeback=yes&type=xml&cover=yes&previous=yes";
        new currentTrackXmlTask().execute(currentTrackUrl);
    }


    public void getNotification() {
        new getNotificationAsync().execute();
    }

    public class getNotificationAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            buildNotification();
            return null;
        }

        private void buildNotification() {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
// Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            resultIntent.setAction(Intent.ACTION_MAIN);
            resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, 0);
// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.

// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
           /* PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            */
            // Sets an ID for the notification, so it can be updated
            if (currentTrack != null) {
                NotificationCompat.Builder mBuilder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic_music_notification)
                                .setLargeIcon(getBitmapFromURL(currentTrack.getCover()))
                                .setContentTitle(currentTrack.getTitle())
                                .setContentText(currentTrack.getArtist())
                                .setAutoCancel(true)
                                .setOngoing(true);


                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.

                notification = mBuilder.build();
                int notifyID = 1;
                mNotificationManager.notify(notifyID, notification);
            }
        }


        private Bitmap getBitmapFromURL(String src) {
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                // Log exception
                Log.e(TAG, "failed to load large Icon for notification ");
                return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            }
        }
    }

    class liveStreamAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //mServ.stopMusic();
            mServ.setmPlayer(MediaPlayer.create(getApplicationContext(), Uri.parse("http://streaming.radionomy.com/GAR?lang=en-US")));
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
            startMusic();
        }
    }


}



