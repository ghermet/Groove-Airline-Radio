package com.guillaume_hermet.www.grooveairlineradio.services;

/**
 * Created by Guillaume on 10/3/16.
 */

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

public class MusicService extends Service {

    private final IBinder mBinder = new ServiceBinder();
    private final Activity mActivity;

    public MusicService() {
        mActivity = null;
    }

    public MediaPlayer getmPlayer() {
        return mPlayer;
    }

    public void setmPlayer(MediaPlayer mPlayer) {
        this.mPlayer = mPlayer;
    }

    MediaPlayer mPlayer;


    public MusicService(Activity mActivity) {
        this.mActivity = mActivity;
        this.mPlayer = MediaPlayer.create(mActivity, Uri.parse("http://listen.radionomy.com/RADIOGROOVEAIRLINE"));
    }


    public class ServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (mPlayer == null)
            mPlayer = MediaPlayer.create(mActivity, Uri.parse("http://listen.radionomy.com/RADIOGROOVEAIRLINE"));
        mPlayer.prepareAsync();
        if (mPlayer != null) {
            mPlayer.setVolume(100, 100);
        }
        mPlayer.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int
                    extra) {
                onError(mPlayer, what, extra);
                return true;
            }
        });


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPlayer.start();
        return START_STICKY;
    }

    public void pauseMusic() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();

        }
    }

    public void resumeMusic() {
        if (mPlayer==null){
            mPlayer = MediaPlayer.create(mActivity, Uri.parse("http://listen.radionomy.com/RADIOGROOVEAIRLINE"));
            mPlayer.prepareAsync();
            if (mPlayer != null) {
                mPlayer.setVolume(100, 100);
            }
            mPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int
                        extra) {
                    onError(mPlayer, what, extra);
                    return true;
                }
            });
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                        mp.start();

                }
            });
        }else{
            if (!mPlayer.isPlaying()) {
                mPlayer.start();
            }
        }

    }

    public void stopMusic() {
        if (mPlayer.isPlaying() || mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }
    }


    //
}