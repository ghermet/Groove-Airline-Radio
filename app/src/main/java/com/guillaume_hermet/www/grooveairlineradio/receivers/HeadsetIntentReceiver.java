package com.guillaume_hermet.www.grooveairlineradio.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.View;
import android.widget.ImageView;

import com.guillaume_hermet.www.grooveairlineradio.R;
import com.guillaume_hermet.www.grooveairlineradio.services.MusicService;
import com.squareup.picasso.Picasso;

/**
 * Created by Guillaume on 11/19/16.
 */

public class HeadsetIntentReceiver extends BroadcastReceiver {


    private final Activity context;
    private final MusicService mServ;

    public HeadsetIntentReceiver(Activity context, MusicService mServ) {
        this.context = context;
        this.mServ = mServ;
    }

    private void stopMusic() {
        if (mServ != null && mServ.getmPlayer()!=null && mServ.getmPlayer().isPlaying()) {
            mServ.stopMusic();
            if (context != null) {
                Picasso.with(context)
                        .load(R.mipmap.ic_play)
                        .error(R.mipmap.ic_play)
                        .into((ImageView) context.findViewById(R.id.button_play));
                context.findViewById(R.id.cover_img).setVisibility(View.GONE);
                context.findViewById(R.id.tv_title).setVisibility(View.GONE);
                context.findViewById(R.id.tv_artist).setVisibility(View.GONE);
            }

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            stopMusic();
        }
    }

}