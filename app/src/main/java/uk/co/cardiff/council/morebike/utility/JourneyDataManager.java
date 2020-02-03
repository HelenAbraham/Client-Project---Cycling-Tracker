package uk.co.cardiff.council.morebike.utility;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.*;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import uk.co.cardiff.council.morebike.localdb.tables.journey.Journey;
import uk.co.cardiff.council.morebike.localdb.tables.journey.JourneyDao;
import uk.co.cardiff.council.morebike.localdb.tables.journey.JourneyDatabase;

public class JourneyDataManager {

    private String baseUrl;
    private static final GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson;
    private final WifiManager wifiManager;
    private final static HttpClient httpClient = HttpClientBuilder.create().build();
    private static JourneyDao db;
    private static final String TAG_NAME = "JourneyDataManager";

    public JourneyDataManager(Context context, String baseUrl) {
        this.baseUrl = baseUrl;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        db = JourneyDatabase.getDatabase(context).journeyDao();
        gson = gsonBuilder.create();
    }

    public void sendUnsentJourneys() {
        if(!isWifiOnAndConnected())
            throw new IllegalStateException("WiFi is not enabled or not connected.");

        new PostJourneysTask().execute(baseUrl);


    }


    // Adapted from: https://stackoverflow.com/questions/3841317/how-do-i-see-if-wi-fi-is-connected-on-android [Accessed: 04/04/2019]
    private boolean isWifiOnAndConnected() {
        if (wifiManager.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getNetworkId() != -1;
        }
        else return false; // Wi-Fi adapter is OFF
    }

    private static class PostJourneysTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... url) {
            if (url.length != 1) throw new IllegalArgumentException("Url must be defined for post request.");
            List<Journey> nonSentJourneys = db.getAllUnsent();

            if(nonSentJourneys.size() > 0) {
                try {
                    HttpPost post = new HttpPost(url[0]);
                    StringEntity postString = new StringEntity(gson.toJson(nonSentJourneys));
                    post.setEntity(postString);
                    post.setHeader("Content-type", "application/json");
                    HttpResponse response = httpClient.execute(post);

                    for (Journey journey : nonSentJourneys) {
                        db.setSentFor(journey.getId());
                    }

                } catch (Exception e) {
                    Log.e("JourneyDataManager", e.toString());
                }
            }

            return null;
        }
    }
}
