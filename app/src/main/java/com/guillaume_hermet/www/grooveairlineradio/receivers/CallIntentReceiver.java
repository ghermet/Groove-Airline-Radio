package com.guillaume_hermet.www.grooveairlineradio.receivers;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.guillaume_hermet.www.grooveairlineradio.R;
import com.guillaume_hermet.www.grooveairlineradio.services.MusicService;
import com.squareup.picasso.Picasso;

/**
 * Created by Guillaume on 11/21/16.
 */
public class CallIntentReceiver extends BroadcastReceiver {

    private static final String TAG = "PhoneStatReceiver";

    private static boolean incomingFlag = false;

    private static String incoming_number = null;
    private final Activity mContext;
    private final MusicService mServ;
    private String outgoing_number;

    public CallIntentReceiver(Activity activity, MusicService musicService) {
        this.mContext = activity;
        this.mServ = musicService;
    }

    public CallIntentReceiver() {
        mContext = null;
        mServ = null;
    }

    private void stopMusic() {
        if (mServ != null && mServ.getmPlayer() != null) {
            if (mServ.getmPlayer().isPlaying())
                mServ.stopMusic();
            if (mContext != null) {
                Picasso.with(mContext)
                        .load(R.mipmap.ic_play)
                        .error(R.mipmap.ic_play)
                        .into((ImageView) mContext.findViewById(R.id.button_play));
                mContext.findViewById(R.id.cover_img).setVisibility(View.GONE);
                mContext.findViewById(R.id.tv_title).setVisibility(View.GONE);
                mContext.findViewById(R.id.tv_artist).setVisibility(View.GONE);
            }

        }
    }

    private void startMusic() {
        if (mServ != null && mServ.getmPlayer() != null) {
            if (mServ.getmPlayer().isPlaying())
                mServ.resumeMusic();
            if (mContext != null) {
                Picasso.with(mContext)
                        .load(R.mipmap.ic_play)
                        .error(R.mipmap.ic_play)
                        .into((ImageView) mContext.findViewById(R.id.button_play));
                mContext.findViewById(R.id.cover_img).setVisibility(View.VISIBLE);
                mContext.findViewById(R.id.tv_title).setVisibility(View.VISIBLE);
                mContext.findViewById(R.id.tv_artist).setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            outgoing_number = intent.getStringExtra("outgoing_number");
            Log.i(TAG, "incoming call RINGING :" + outgoing_number);
            stopMusic();
        }
        else if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {

            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

            switch (tm.getCallState()) {

                case TelephonyManager.CALL_STATE_RINGING:
                    incomingFlag = true;
                    incoming_number = intent.getStringExtra("incoming_number");
                    Log.i(TAG, "incoming call RINGING :" + incoming_number);
                    stopMusic();
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:

                    if (incomingFlag) {
                        Log.i(TAG, "incoming call ACCEPTED :" + incoming_number);
                        stopMusic();
                    }

                    break;

                case TelephonyManager.CALL_STATE_IDLE:

                    if (incomingFlag) {
                        Log.i(TAG, "call ended");
                        startMusic();

                    }

                    break;

            }

        }

    }
}