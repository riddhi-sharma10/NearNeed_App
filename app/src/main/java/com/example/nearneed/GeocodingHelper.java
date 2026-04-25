package com.example.nearneed;

import android.os.Handler;
import android.os.Looper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GeocodingHelper {

    private static final OkHttpClient httpClient = new OkHttpClient();

    public interface OnGeocodingResultListener {
        void onResults(List<SearchPredictionAdapter.GeocodingResult> results);
    }

    public static void performSearch(String query, OnGeocodingResultListener listener) {
        if (query == null || query.trim().length() < 3) {
            listener.onResults(new ArrayList<>());
            return;
        }

        new Thread(() -> {
            List<SearchPredictionAdapter.GeocodingResult> aggregated = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(2);

            // 1. Photon (OpenStreetMap)
            String photonUrl = "https://photon.komoot.io/api/?q=" + query + "&limit=5";
            Request photonReq = new Request.Builder().url(photonUrl).build();
            httpClient.newCall(photonReq).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { latch.countDown(); }
                @Override public void onResponse(Call call, Response r) {
                    try {
                        if (r.isSuccessful() && r.body() != null) {
                            JSONObject root = new JSONObject(r.body().string());
                            JSONArray features = root.getJSONArray("features");
                            for(int i=0; i<features.length(); i++) {
                                JSONObject feat = features.getJSONObject(i);
                                JSONObject props = feat.getJSONObject("properties");
                                JSONObject geom = feat.getJSONObject("geometry");
                                JSONArray coords = geom.getJSONArray("coordinates");
                                
                                String name = props.optString("name", "");
                                String city = props.optString("city", "");
                                String state = props.optString("state", "");
                                String sec = (city + (city.isEmpty() || state.isEmpty() ? "" : ", ") + state).trim();
                                
                                aggregated.add(new SearchPredictionAdapter.GeocodingResult(name, sec, coords.getDouble(1), coords.getDouble(0)));
                            }
                        }
                    } catch(Exception ignored) {}
                    finally { latch.countDown(); }
                }
            });

            // 2. Nominatim
            String nominatimUrl = "https://nominatim.openstreetmap.org/search?q=" + query + "&format=json&limit=5";
            Request nominatimReq = new Request.Builder().url(nominatimUrl).header("User-Agent", "NearNeedApp").build();
            httpClient.newCall(nominatimReq).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { latch.countDown(); }
                @Override public void onResponse(Call call, Response r) {
                    try {
                        if (r.isSuccessful() && r.body() != null) {
                            JSONArray arr = new JSONArray(r.body().string());
                            for(int i=0; i<arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                String[] parts = o.optString("display_name", "").split(",", 2);
                                String name = parts[0].trim();
                                String sec = parts.length > 1 ? parts[1].trim() : "";
                                aggregated.add(new SearchPredictionAdapter.GeocodingResult(name, sec, o.getDouble("lat"), o.getDouble("lon")));
                            }
                        }
                    } catch(Exception ignored) {}
                    finally { latch.countDown(); }
                }
            });

            try {
                latch.await(4, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}

            List<SearchPredictionAdapter.GeocodingResult> finalList = new ArrayList<>();
            for (SearchPredictionAdapter.GeocodingResult res : aggregated) {
                boolean dup = false;
                for (SearchPredictionAdapter.GeocodingResult existing : finalList) {
                    float[] results = new float[1];
                    android.location.Location.distanceBetween(res.lat, res.lng, existing.lat, existing.lng, results);
                    if (results[0] < 500) { 
                        dup = true; break;
                    }
                }
                if (!dup && finalList.size() < 6) finalList.add(res);
            }

            new Handler(Looper.getMainLooper()).post(() -> listener.onResults(finalList));
        }).start();
    }
}
