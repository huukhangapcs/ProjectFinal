package com.apcs2.helperapp.repository;

import com.apcs2.helperapp.entity.LandMark;
import com.apcs2.helperapp.entity.Message;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class LandMarkRepository {
    private ArrayList<LandMark> landmarks;

    public LandMarkRepository() {
        landmarks = new ArrayList<>();
    }

    public void remove(int position) {
        landmarks.remove(position);
    }

    public LandMark getByPosition(int position) {
        if (position != -1)
            return landmarks.get(position);
        return null;
    }

    public int findByPositionAndTitle(String location, String title) {
        for (int i = 0; i < landmarks.size(); i++) {
            LandMark landmark = landmarks.get(i);
            String tmpLatLg = String.valueOf(landmark.getLatLng().latitude) + "," + String.valueOf(landmark.getLatLng().longitude);
            if (landmark.getName().equals(title) && location.equals(tmpLatLg)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isMarkerExist(LatLng curLatLng) {
        for (int i = 0; i < landmarks.size(); i++) {
            LatLng laLng = landmarks.get(i).getLatLng();
            if (curLatLng.latitude == laLng.latitude && curLatLng.longitude == laLng.longitude) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Message> getServer(int position) {
        LandMark landMark = landmarks.get(position);
        return landMark.getServer().getMessages();
    }

    public boolean addAMessage(Message message, int position) {
        landmarks.get(position).getServer().add(message);
        return true;
    }

    public LandMark findByLocationId(String locationId) {
        for (int i = 0; i < landmarks.size(); i++) {
            LandMark landMark = landmarks.get(i);
            if (landMark.get_locationID().equals(locationId)) {
                return landMark;
            }
        }
        return null;
    }
    public int findPositionByLocationId(String locationId) {
        for (int i = 0; i < landmarks.size(); i++) {
            LandMark landMark = landmarks.get(i);
            if (landMark.get_locationID().equals(locationId)) {
                return i;
            }
        }
        return -1;
    }
    public void addALandmark(LandMark landMark) {
        landmarks.add(landMark);
    }

    public int countItems() {
        return landmarks.size();
    }
}
