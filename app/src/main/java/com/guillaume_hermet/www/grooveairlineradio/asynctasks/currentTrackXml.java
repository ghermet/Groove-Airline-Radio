package com.guillaume_hermet.www.grooveairlineradio.asynctasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.guillaume_hermet.www.grooveairlineradio.R;
import com.guillaume_hermet.www.grooveairlineradio.activities.MainActivity;
import com.guillaume_hermet.www.grooveairlineradio.models.Track;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Guillaume on 11/24/16.
 */


public class currentTrackXml {

    private final MainActivity context;

    public currentTrackXml(MainActivity mainActivity) {
        this.context = mainActivity;
    }

    public void loadCurrentTrack() {
        String currentTrackUrl = context.getString(R.string.gar_radionomy_current_track_api_url);
        new currentTrackXmlTask().execute(currentTrackUrl);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private class currentTrackXmlTask extends AsyncTask<String, Void, String> {

        private final String TAG = this.getClass().getSimpleName();

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
                context.setCurrentTrack(new Track(title, artist, cover, callmeback));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (tracks != null) {
                Log.d(TAG, "Response: " + tracks.toString());
                if (context.getCurrentTrack() != null) {
                    ImageView mCover = (ImageView) context.findViewById(R.id.cover_img);
                    Picasso.with(context)
                            .load(context.getCurrentTrack().getCover())
                            .fit()
                            .error(R.mipmap.ic_launcher)
                            .into(mCover);
                    TextView mTitleText = (TextView) context.findViewById(R.id.tv_title);
                    mTitleText.setText(context.getCurrentTrack().getTitle());
                    TextView mArtistText = (TextView) context.findViewById(R.id.tv_artist);
                    mArtistText.setText(context.getCurrentTrack().getArtist());

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isNetworkConnected()) loadCurrentTrack();
                        }
                    }, context.getCurrentTrack().getCallmeback());

                    if (context.getNotification()!=null)
                        new currentTrackNotification(context).buildNotification();
                }


            }


        }


    }

}

