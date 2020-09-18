package com.apcs2.helperapp.entity;

import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class DirectPath {
    ArrayList<Polyline> directPath;
    public DirectPath(){
        directPath = new ArrayList<>();
    }
    public ArrayList<Polyline> getDirectPath() {
        return directPath;
    }

    public void setDirectPath(ArrayList<Polyline> directPath) {
        this.directPath = directPath;
    }

}
