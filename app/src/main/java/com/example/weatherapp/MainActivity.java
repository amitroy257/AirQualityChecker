package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    EditText etCity;
    Button btnFetch;
    TextView tvResult;

    String API_KEY = "d1f2c42a74c74b511216893a6cdf11fe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etCity = findViewById(R.id.etCity);
        btnFetch = findViewById(R.id.btnFetch);
        tvResult = findViewById(R.id.tvResult);

        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = etCity.getText().toString().trim();
                if (!city.isEmpty()) {
                    fetchCoordinates(city);
                } else {
                    Toast.makeText(MainActivity.this, "Enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchCoordinates(String city) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String geoUrl = "https://api.openweathermap.org/geo/1.0/direct?q=" +
                        URLEncoder.encode(city, "UTF-8") +
                        "&limit=1&appid=" + API_KEY;

                URL url = new URL(geoUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONArray arr = new JSONArray(result.toString());
                if (arr.length() > 0) {
                    JSONObject loc = arr.getJSONObject(0);
                    double lat = loc.getDouble("lat");
                    double lon = loc.getDouble("lon");

                    fetchAirQuality(lat, lon, city);
                } else {
                    runOnUiThread(() -> tvResult.setText("City not found."));
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> tvResult.setText("Error fetching coordinates."));
            }
        });
    }

    private void fetchAirQuality(double lat, double lon, String city) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String apiUrl = "https://api.openweathermap.org/data/2.5/air_pollution?lat=" +
                        lat + "&lon=" + lon + "&appid=" + API_KEY;

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                JSONArray list = json.getJSONArray("list");
                JSONObject data = list.getJSONObject(0);

                int aqi = data.getJSONObject("main").getInt("aqi");
                JSONObject components = data.getJSONObject("components");

                String message = "City: " + city + "\n\n" +
                        "AQI Level: " + getAqiLabel(aqi) + " (" + aqi + ")\n" +
                        "CO: " + components.getDouble("co") + " µg/m³\n" +
                        "NO: " + components.getDouble("no") + " µg/m³\n" +
                        "NO₂: " + components.getDouble("no2") + " µg/m³\n" +
                        "O₃: " + components.getDouble("o3") + " µg/m³\n" +
                        "SO₂: " + components.getDouble("so2") + " µg/m³\n" +
                        "PM2.5: " + components.getDouble("pm2_5") + " µg/m³\n" +
                        "PM10: " + components.getDouble("pm10") + " µg/m³\n" +
                        "NH₃: " + components.getDouble("nh3") + " µg/m³";

                runOnUiThread(() -> tvResult.setText(message));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> tvResult.setText("Error fetching air quality."));
            }
        });
    }

    private String getAqiLabel(int aqi) {
        switch (aqi) {
            case 1: return "Good";
            case 2: return "Fair";
            case 3: return "Moderate";
            case 4: return "Poor";
            case 5: return "Very Poor";
            default: return "Unknown";
        }
    }
}
