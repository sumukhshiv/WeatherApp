package io.github.sumukhshiv.weatherapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    TextView textViewTemperature;
    TextView textViewRain;
    TextView textViewDescription;
    ImageView icon;
    String temperature;
    String formattedTemperature;
    String description;
    int rainMinute;
    Boolean willRainInCurrentHour;
    String formattedTime;
    String stringLatitude;
    String stringLongitude;
    String iconType;
    final ArrayList<Double> latlong = new ArrayList<>();
    final ArrayList<String> location = new ArrayList<>();
    public ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refresh();
//        progressDialog.dismiss();

        ((ImageButton) findViewById(R.id.imageButtonRefresh)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });



    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    refresh();
                } else {

                    progressDialog.dismiss();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void refresh() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        textViewTemperature = (TextView) findViewById(R.id.textViewTemperature);
        textViewRain = (TextView) findViewById(R.id.textViewRain);
        textViewDescription = (TextView) findViewById(R.id.textViewDescription);
        textViewDescription = (TextView) findViewById(R.id.textViewDescription);
        icon = (ImageView) findViewById(R.id.icon);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                    0);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            progressDialog.dismiss();
            return;
        }
        try {
            Location location2 = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            final double longitude = location2.getLongitude();
            final double latitude = location2.getLatitude();
            latlong.clear();
            latlong.add(latitude);
            latlong.add(longitude);
            location.clear();
            location.add(latlong.get(0) + "," + latlong.get(1));
        } catch (Exception e) {
            Log.d("BAD", "unable to pull location");
        }

        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... voids) {
                try {
                    URL url;
                    if (location.size() == 0) {
                        url = new URL("https://api.darksky.net/forecast/5711edd00f69e7c3d178667848166542/" + "37.85267,-122.4233");
                    } else {
                        url = new URL("https://api.darksky.net/forecast/5711edd00f69e7c3d178667848166542/" + location.get(0));
                    }
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    String response = convertStreamToString(in);
                    JSONObject json = new JSONObject(response);
                    Log.d("DEBUG", json.toString());
                    temperature = json.getJSONObject("currently").getString("temperature");
                    double value = Double.parseDouble(temperature);
                    int temp = (int) Math.round(value);

                    formattedTemperature = Integer.toString(temp);


                    description = json.getJSONObject("currently").getString("summary");
                    iconType = json.getJSONObject("currently").getString("icon");
                    JSONArray minuteData = json.getJSONObject("minutely").getJSONArray("data");
                    willRainInCurrentHour = false;

                    willRainInCurrentHour = false;
                    for (int i = 0; i < 60; i ++) {
                        if (minuteData.getJSONObject(i).getInt("precipProbability") > 0) {
                            willRainInCurrentHour =  true;
//                            rainMinute = minuteData.getJSONObject(i).getInt("time");
                            long unixSeconds = minuteData.getJSONObject(i).getInt("time");
                            Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
                            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a"); // the format of your date
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT-8")); // give a timezone reference for formating (see comment at the bottom
                            formattedTime = sdf.format(date);
                            break;
                        }
                    }



                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.e("bad", "url");
                } catch (ProtocolException p) {
                    p.printStackTrace();
                    Log.e("bad", "protocol");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("bad", "io");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("bad", e.getMessage());
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {            }

            @Override
            protected void onPostExecute(Void aVoid) {

                progressDialog.dismiss();

                textViewTemperature.setText(formattedTemperature + "°");
                textViewDescription.setText(description);
                if (willRainInCurrentHour) {

                    textViewRain.setText("It will rain at " + formattedTime);
                }

                if (!willRainInCurrentHour) {
                    textViewRain.setText("It will not rain in the next hour!");
                }

                if (iconType.equals("clear-day")) {
                    icon.setImageResource(R.drawable.ic_brightness_high_black_24dp);
                }
                else if (iconType.equals("clear-night")) {
                    icon.setImageResource(R.drawable.ic_brightness_2_black_24dp);
                }
                else {
                    icon.setImageResource(R.drawable.ic_cloud_queue_black_24dp);
                }
//                progressDialog = null;
            }
        }.execute();

    }
}
