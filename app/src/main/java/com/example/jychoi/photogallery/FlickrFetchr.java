package com.example.jychoi.photogallery;

import android.net.Uri;
import android.util.Log;

import com.example.jychoi.photogallery.model.GalleryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jychoi on 2017. 12. 17..
 */

public class FlickrFetchr {
    private static final String TAG = FlickrFetchr.class.getSimpleName();
    private static final String API_KEY = "836bbb983607bd116ec8754b027433cb";
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {

                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                        || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    Log.e("FlickrFetchr", "Changed  = " + connection.getHeaderField("Location"));
                }
                throw new IOException(connection.getResponseMessage() + " : with "
                + urlSpec);

            }
            int bytesRead = 0;
            byte [] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0 ) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();

        }finally {
            connection.disconnect();
        }
    }
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }
    public List<GalleryItem> fetchItems() {
        List<GalleryItem> items = new ArrayList<>();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();

            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON = " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items" , ioe);
        } catch (JSONException jsone) {
            Log.e(TAG, "Failed to parse json" , jsone);
        }
        return items;
    }
    private void parseItems(List<GalleryItem> items, JSONObject jsonBody)
    throws  IOException, JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i<photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();

            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            if (!photoJsonObject.has("url_s")) {
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }

}
