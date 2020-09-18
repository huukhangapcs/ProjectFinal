package com.apcs2.helperapp.repository;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.apcs2.helperapp.entity.DirectPath;
import com.example.helperapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DirectPathRepository {
    Context context;
    ArrayList<DirectPath> directPaths;
    GoogleMap mMap;

    public void setmMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    public  DirectPathRepository(Context context){
        this.context = context;
        directPaths = new ArrayList<>();
    }

    public void removeAllPolylineExceptAtPostion(int position) {
        for (int i = 0; i < directPaths.size(); i++) {
            if (position != i) {
                ArrayList<Polyline> tmpPolylines = directPaths.get(i).getDirectPath();

                for (int j = 0; j < tmpPolylines.size(); j++) {
                    tmpPolylines.get(j).remove();
                }

            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void requestDirection(String url, final int postion) {
        RequestQueue mQueue = Volley.newRequestQueue(context);
        JsonObjectRequest mJsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject route = response
                                    .getJSONArray(context.getString(R.string.route))
                                    .getJSONObject(0);
                            ArrayList<LatLng> listPointRoute = decodePoly(route
                                    .getString
                                            (context.getString
                                                    (R.string.geometry)));
                            drawPolyline(listPointRoute,
                                    context.getString(R.string.colorPolyLine),
                                    10, postion);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Log.v(TAG, getString(R.string.on_respone));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Log.v(TAG, getString(R.string.request_url_err));
            }
        });
        mQueue.add(mJsonObjectRequest);
    }
    public ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
    public void drawPolyline(ArrayList<LatLng> listPointRoute, String color, int width, int position) {
        ArrayList<Polyline> tmpArr = new ArrayList<>();

        for (int i = 0; i < listPointRoute.size() - 1; i++) {
            LatLng src = listPointRoute.get(i),
                    des = listPointRoute.get(i + 1);
            Polyline singleLine = mMap.addPolyline(
                    new PolylineOptions().add(
                            new LatLng(src.latitude, src.longitude),
                            new LatLng(des.latitude, des.longitude)
                    )
                            .color(Color.parseColor(color))
                            .geodesic(true)
                            .width(width)
            );
            tmpArr.add(singleLine);
        }
        //  landMarkRepository.getByPosition(position).setPolyLines(tmpArr);
        update(tmpArr, position);

    }
    public boolean add(DirectPath directPath){

        directPaths.add(directPath);
        return true;
    }
    public boolean update(ArrayList<Polyline> directPath, int positon){
        directPaths.get(positon).setDirectPath(directPath);
        return true;
    }
    public boolean delete(int positon){
        directPaths.remove(positon);
        return true;
    }
}
