package com.example.lab8;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;

import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;


public class MainActivity extends AppCompatActivity {
    ListView dataTV;
    ArrayList<String> items;
    ArrayAdapter<String> adapter;
    private HttpsURLConnection urlConnection;
    private Uri uri;
    private String MA = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataTV = findViewById(R.id.theData);
        items = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, R.layout.items);
        dataTV.setAdapter(adapter);
        new HttpsGetTask().execute("https://mason.gmu.edu/~white/earthquakes.json");
        dataTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Item " + ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
                String str = ((TextView) view).getText().toString();
                int begin = str.indexOf('(');
                int end = str.indexOf(')');
                String substr = str.substring(++begin, end);
                Log.i(MA, "substr: " + substr);
                click(substr);
                //  uri = Uri.parse("geo:0,0?q="+ latlonginfo +"&z=3";);
            }
        });
    }

    private class HttpsGetTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                String token = "rbkY34HnL...";
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                //               urlConnection.setHostnameVerifier(new HostnameVerifier() {
                //                   @Override
                //                   public boolean verify(String hostname, SSLSession session) {
                //                       HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                //                       return hv.verify("mason.gmu.edu", session);
                //                   }
                //               });
                //               urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                urlConnection.connect();
                InputStream inputStream;
                if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    inputStream = urlConnection.getErrorStream();
                    return inputStream.toString();
                } else {
                    inputStream = urlConnection.getInputStream();
                    return readStream(inputStream);
                }
            } catch (Exception e) {
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            onFinishGetRequest(result);
        }
    }
    private void onFinishGetRequest(String result) {
        //   dataTV.setText(result);
        try {
            JSONArray earthquakes = (new JSONArray(result));
            int len = earthquakes.length();
            for (int i = 0;i<len;i++) {
                JSONObject quake = earthquakes.getJSONObject(i);
                String region = quake.getString("region");
                String mag = quake.getString("magnitude");
                String occurred = quake.getString("occurred_at");
                JSONObject location = quake.getJSONObject("location");
                String lat = location.getString("latitude");
                String longitude = location.getString("longitude");
                adapter.add(region + " (" + lat + "," + longitude +
                        ") with magnitude = " + mag + " on " + occurred);
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void click(String latlonginfo){
        // String str = "geo:0,0?q=" + latlonginfo + "&z=3";
        Uri loc = Uri.parse("geo:0,0?q=" + latlonginfo + "&z=3");
        Log.i(MA, "loc: " + loc);
        //  Uri loc = Uri.parse(“geo:0,0?q="+ latlonginfo +"&z=3";);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(loc);
        intent.setPackage("com.google.android.apps.maps");
     //   if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }

    private static String readStream(InputStream is) throws IOException {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("US-ASCII")));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            total.append(line);
        }
        if (reader != null) {
            reader.close();
        }
        return total.toString();
    }

}
