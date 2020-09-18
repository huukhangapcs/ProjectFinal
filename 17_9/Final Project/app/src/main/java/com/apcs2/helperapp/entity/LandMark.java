package com.apcs2.helperapp.entity;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.apcs2.helperapp.repository.MessageRepository;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class LandMark {


    public String get_locationID() {
        return _locationID;
    }

    public void set_locationID(String _locationID) {
        this._locationID = _locationID;
    }
    private String _locationID;
    private String _name;
    private String _description;
    private LatLng _latLng;
    private String _phone;
    private String category;//1 la bt , 2 la medium, 3 la high
    private String _userId;
    private String _startDate;
    private String _address;
    private  String imgUrl;
    private String price;

    // Use for chat box
    private MessageRepository server;
    public void setLatLng(LatLng latLng) {
        this._latLng = latLng;
    }

    public LatLng getLatLng() {
        return _latLng;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public LandMark(String name, String description, String phone, LatLng latLng, String category, String userId, String startDate, String address, String price) {
        this._name = name;
        this._description = description;
        this._phone = phone;
        this._latLng = latLng;
        this.category = category;
        this._userId = userId;
        this._startDate = startDate;
        this._address = address;
        this.server = new MessageRepository();
        this.imgUrl = "https://vanhoadoanhnghiepvn.vn/wp-content/uploads/2020/08/112815953-stock-vector-no-image-available-icon-flat-vector.jpg";
        this.price = price;
        //
        Message message = new Message();

        message.setTime(getCurrenTime());
        message.setContent("Welcome to server " + name);
        message.setUserId("0");
        message.setUserName("Messages from system");
        server.add(message);

//        Message message2 = new Message();
//        message2.setTime(getCurrenTime());
//        message2.setContent("Two");
//        message2.setUserId("2");
//        server.add(message2);

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getCurrenTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        return dtf.format(now);
    }
    public LandMark(String name, String description, String phone, LatLng latLng, String category, String userId, String startDate, String address, MessageRepository server, String imgUrl, String price) {
        this._name = name;
        this._description = description;
        this._phone = phone;
        this._latLng = latLng;
        this.category = category;
        this._userId = userId;
        this._startDate = startDate;
        this._address = address;
        this.server = server;
        this.imgUrl = imgUrl;
        this.price = price;
    }
    public void setName(String name) {
        this._name = name;
    }

    public void setDescription(String description) {
        this._description = description;
    }

    public String getName() {
        return _name;
    }

    public String getDescription() {
        return _description;
    }

    public String get_phone() {
        return _phone;
    }

    public void set_phone(String _phone) {
        this._phone = _phone;
    }






    public String getUserId() {
        return _userId;
    }

    public void setUserId(String userId) {
        this._userId = userId;
    }

    public String get_startDate() {
        return _startDate;
    }

    public void set_startDate(String _startDate) {
        this._startDate = _startDate;
    }

    public String getAddress() {
        return _address;
    }

    public void set_address(String address) {
        this._address = address;
    }

    public MessageRepository getServer() {
        return server;
    }

    public void setServer(MessageRepository server) {
        this.server = server;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
