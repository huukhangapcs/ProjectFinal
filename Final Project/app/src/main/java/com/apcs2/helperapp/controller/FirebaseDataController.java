package com.apcs2.helperapp.controller;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.apcs2.helperapp.entity.DirectPath;
import com.apcs2.helperapp.entity.Message;
import com.apcs2.helperapp.entity.MessageAdapter;
import com.apcs2.helperapp.repository.DirectPathRepository;
import com.apcs2.helperapp.repository.FirebaseDataRepository;
import com.apcs2.helperapp.entity.LandMark;
import com.apcs2.helperapp.repository.LandMarkRepository;
import com.apcs2.helperapp.repository.MessageRepository;
import com.example.helperapp.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FirebaseDataController {
    FirebaseDataRepository firebaseDataRepository;
    LandMarkRepository landMarkRepository;
    LandMarkController landMarkController;
    DirectPathRepository directPathRepository;
    Context context;
    private ArrayList<Marker> mMarkers;

    int idNotify;
    int newNearRequest = 0;

    public FirebaseDataController(LandMarkRepository landMarkRepository, LandMarkController landMarkController,
                                  DirectPathRepository directPathRepository, Context context) {
        this.landMarkRepository = landMarkRepository;
        this.landMarkController = landMarkController;
        this.directPathRepository = directPathRepository;

        firebaseDataRepository = new FirebaseDataRepository();
        newNearRequest = 0;
        idNotify = 0;
        this.context = context;
        mMarkers = landMarkController.getmMarkers();
        fireBaseAddEventListener(firebaseDataRepository.getRefRoot());
    }

    public FirebaseDataController(Context context) {
        firebaseDataRepository = new FirebaseDataRepository();
        this.context = context;
        fireBaseAddEventListener(firebaseDataRepository.getRefRoot());
    }

    public void deleteMarker(String location, String title) {
        //int position = find(location, title);
        int position = landMarkRepository.findByPositionAndTitle(location, title);
        if (position != -1) {
            //   LandMark landMark = landmarks.get(position);
            LandMark landMark = landMarkRepository.getByPosition(position);
            removeOnDataBase(landMark);
            directPathRepository.removeAllPolylineExceptAtPostion(-1);
            landMarkController.removeAMarker(position);
            //       Toast.makeText(this, "Xoa landmark thanh cong, Tong so landmark la: " + String.valueOf(landmarks.size()), Toast.LENGTH_SHORT).show();

            //-1 = clear all
        }
    }

    public void deleteMarkerByPosition(int position) {
        //int position = find(location, title);
        if (position != -1) {
            //   LandMark landMark = landmarks.get(position);
            LandMark landMark = landMarkRepository.getByPosition(position);
            removeOnDataBase(landMark);
            directPathRepository.removeAllPolylineExceptAtPostion(-1);
            //       Toast.makeText(this, "Xoa landmark thanh cong, Tong so landmark la: " + String.valueOf(landmarks.size()), Toast.LENGTH_SHORT).show();

            //-1 = clear all
        }
    }

    public void handleItemRemoved(@NonNull DataSnapshot snapshot) {
        Double newLat = snapshot.child("latLng").child("latitude").getValue(Double.class);
        Double newLong = snapshot.child("latLng").child("longitude").getValue(Double.class);
        String name = snapshot.child("name").getValue(String.class);
        String location = String.valueOf(newLat) + "," + String.valueOf(newLong);
        deleteMarker(location, name);
    }

    private void fireBaseAddEventListener(DatabaseReference drName) {
        drName.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                handleItemAdded(snapshot);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageRepository server = snapshot.child("server").getValue(MessageRepository.class);
                String locationId = snapshot.child("_locationID").getValue(String.class);
                String startDate = snapshot.child("_startDate").getValue(String.class);

                LandMark landMark = landMarkRepository.findByLocationId(locationId);
                // update Server
                if (startDate.equals(landMark.get_startDate())) {
                    landMark.setServer(server);
                    ListView listView = (ListView) ((Activity) context).findViewById(R.id.messages_view);
                    MessageAdapter messageAdapter = new MessageAdapter(context, server.getMessages(), landMarkController.getUserId());
                    listView.setAdapter(messageAdapter);
                    listView.setSelection(messageAdapter.getCount() - 1);
                    messageAdapter.notifyDataSetChanged();
                    return;
                }
                // update whole landmark
                handleItemUpdated(snapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                handleItemRemoved(snapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void handleItemAdded(DataSnapshot child) {

        Double newLatitude = child.child("latLng").child("latitude").getValue(Double.class);
        Double newLongitude = child.child("latLng").child("longitude").getValue(Double.class);
//        if (isMarkerExist(new LatLng(lat, lLong)))
//            return;
        String currentDateTime = child.child("_startDate").getValue(String.class);
        String curUserId = child.child("userId").getValue(String.class);
        String category = child.child("category").getValue(String.class);
        String locationId = child.child("_locationID").getValue(String.class);
        String phone = child.child("_phone").getValue(String.class);
        String description = child.child("description").getValue(String.class);
        String name = child.child("name").getValue(String.class);
        String address = child.child("address").getValue(String.class);
        MessageRepository server = child.child("server").getValue(MessageRepository.class);
        String imgUrl = child.child("imgUrl").getValue(String.class);
        String price = child.child("price").getValue(String.class);

//        makeNotification(newLatitude, newLongitude, description, name, Integer.valueOf(emLevel));
        LandMark landMark = new LandMark(name, description, phone, new LatLng(newLatitude, newLongitude), category, curUserId, currentDateTime, address, server, imgUrl, price);
        landMark.set_locationID(locationId);
        landMarkController.drawMarker(landMark);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void handleItemUpdated(DataSnapshot child) {
        String locationId = child.child("_locationID").getValue(String.class);
        Double newLatitude = child.child("latLng").child("latitude").getValue(Double.class);
        Double newLongitude = child.child("latLng").child("longitude").getValue(Double.class);
//        if (isMarkerExist(new LatLng(lat, lLong)))
//            return;
        String currentDateTime = landMarkController.getCurrentDateTime();
        String curUserId = child.child("userId").getValue(String.class);
        String category = child.child("category").getValue(String.class);
        String phone = child.child("_phone").getValue(String.class);
        String description = child.child("description").getValue(String.class);
        String name = child.child("name").getValue(String.class);
        String address = child.child("address").getValue(String.class);
        String imgUrl = child.child("imgUrl").getValue(String.class);
        String price = child.child("price").getValue(String.class);

        MessageRepository server = child.child("server").getValue(MessageRepository.class);

        LandMark landMark = landMarkRepository.findByLocationId(locationId);
        landMark.setName(name);
        landMark.setDescription(description);
        landMark.set_phone(phone);
        landMark.setLatLng(new LatLng(newLatitude, newLongitude));
        landMark.setCategory(category);
        landMark.setUserId(curUserId);
        landMark.set_startDate(currentDateTime);
        landMark.set_address(address);
        landMark.setServer(server);
        //new LandMark(name, description, phone, new LatLng(newLatitude, newLongitude), emLevel, curUserId, currentDateTime, address, server);
        landMark.set_locationID(locationId);
        landMark.setImgUrl(imgUrl);
        landMark.setPrice(price);
        // Update current marker
        int position = landMarkRepository.findPositionByLocationId(locationId);
        String spitSign = "~";
        String containerStr = description + spitSign + phone + spitSign + category + spitSign + currentDateTime + spitSign + curUserId + spitSign + address + spitSign + price + spitSign + imgUrl;
        new UpdateImageAsyncTask(position, name, containerStr, imgUrl).execute();

    }

    public class UpdateImageAsyncTask extends AsyncTask<Void, Void, Drawable> {
        int position;
        String name;
        String containerStr;
        String imgUrl;

        public UpdateImageAsyncTask(int position, String name, String containerStr, String imgUrl) {

            this.position = position;
            this.name = name;
            this.containerStr = containerStr;
            this.imgUrl = imgUrl;
        }

        @Override
        protected Drawable doInBackground(Void... voids) {


            Drawable bmp = landMarkController.getDrawableImageFromUrl(imgUrl);

            return bmp;

        }

        @Override
        protected void onPostExecute(Drawable img) {


            mMarkers.get(position).setIcon(BitmapDescriptorFactory.fromBitmap(landMarkController.createMaker(context, img, landMarkController.getUserId())));
            mMarkers.get(position).setTitle(name);
            mMarkers.get(position).setSnippet(containerStr);


        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void makeNotification(Double lat, Double lLong, String description, String name, int emergencyLevel) {
        // check if new Request near 5km
        LatLng currentLatLng = landMarkController.getDeviceLocation();
        if (calculateDistance(currentLatLng, new LatLng(lat, lLong)) < 5) {
            showNotif(description, name, emergencyLevel, context);
            newNearRequest++;
        }
    }

    public double calculateDistance(LatLng laLg1, LatLng laLg2) {
        double lat1 = laLg1.latitude;
        double lat2 = laLg2.latitude;
        double lon1 = laLg1.longitude;
        double lon2 = laLg2.longitude;
        double p = 0.017453292519943295;    // Math.PI / 180
        double a = 0.5 - Math.cos((lat2 - lat1) * p) / 2 +
                Math.cos(lat1 * p) * Math.cos(lat2 * p) *
                        (1 - Math.cos((lon2 - lon1) * p)) / 2;
        return 12742 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km
    }

    private void removeOnDataBase(LandMark landMark) {
        DatabaseReference dref = FirebaseDatabase.getInstance().getReference().getRoot().child(landMark.get_locationID());
        dref.removeValue();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotif(String description, String name, int emergencyLevel, Context context) {
//        int priorityLevel = getPriorityLevelOnEmergencyLevel(emergencyLevel);
//        int importantLevel = getImportantLevelOnEmergencyLevel(emergencyLevel);
//        String channelId = context.getString(R.string.app_name);
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), channelId)
//                .setSmallIcon(R.drawable.warning)
//                .setContentTitle(name)
//                .setContentText(description)
//                .setPriority(priorityLevel);
//        NotificationChannel channel = new NotificationChannel(channelId, description, importantLevel);
//        channel.setDescription(description);
//        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
//        notificationManager.createNotificationChannel(channel);
//        notificationManager.notify(autoIncrementId(idNotify), mBuilder.build());
    }


    public void pushLmToDB(LandMark temp_lm) {
        String locationID;


        locationID = firebaseDataRepository.getRefRoot().push().getKey();
        assert locationID != null;
        temp_lm.set_locationID(locationID);
        firebaseDataRepository.getRefRoot().child(locationID).setValue(temp_lm);
        //          firebaseDataRepository.getRefRoot().child(locationID).child("server").setValue(new Message());

        Toast.makeText(context, "Đã đăng thành công!!!", Toast.LENGTH_LONG).show();

    }

    public void updateServerLandmark(LandMark temp_lm, Message newMsg) {
        String locationID = temp_lm.get_locationID();
        String key = String.valueOf(temp_lm.getServer().size());

        firebaseDataRepository.getRefRoot().child(locationID).child("server").child("messages").child(key).setValue(newMsg);


    }

    public void updateLandmark(LandMark temp_lm) {
        String locationID = temp_lm.get_locationID();
        Map<String, Object> taskMap = new HashMap<>();

        assert locationID != null;
        temp_lm.set_locationID(locationID);
        taskMap.put(locationID, temp_lm);
        firebaseDataRepository.getRefRoot().updateChildren(taskMap);

    }
}
