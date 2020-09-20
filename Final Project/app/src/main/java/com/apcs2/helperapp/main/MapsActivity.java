package com.apcs2.helperapp.main;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import com.android.volley.toolbox.JsonObjectRequest;
import com.apcs2.helperapp.controller.FireBaseStorageController;
import com.apcs2.helperapp.controller.FirebaseDataController;
import com.apcs2.helperapp.controller.LandMarkController;
import com.apcs2.helperapp.controller.ServerController;
import com.apcs2.helperapp.entity.LandMark;
import com.apcs2.helperapp.entity.Message;
import com.apcs2.helperapp.entity.MessageAdapter;
import com.apcs2.helperapp.repository.FirebaseDataRepository;
import com.apcs2.helperapp.repository.LandMarkRepository;
import com.apcs2.helperapp.repository.MessageRepository;
import com.example.helperapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskara.widget.MaskEditText;
import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int CAMERA_REQUEST = 1010;
    private static final int MY_CAMERA_PERMISSION_CODE = 1011;
    private Marker mMarker;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private ArrayList<Marker> mMarkers;
    ImageButton logout;
    private GoogleMap mMap;
    CardView compleRequest;
    CardView editRequest;
    // private ArrayList<LandMark> landmarks;
    LandMarkRepository landMarkRepository;
    LandMarkController landMarkController;
    ServerController serverController;
    LinearLayout containerLayout;
    LinearLayout distanceLayout;
    ScrollView requestForm;
    ScrollView detailView;
    TextView detailTitle;
    TextView detailDescription;
    TextView detailPhone;
    TextView detailLocation;
    TextView detailPrice;
    EditText eTitle;
    EditText eDescription;
    EditText eLocation;
    MaskEditText ePhone;
    EditText ePrice;
    Spinner sEmergency;
    CheckBox cbCurrentLocation;
    TextView tEmergency;
    TextView detailEmergency;
    ArrayList<EditText> editFormText;
    DatabaseReference refHigh;
    DatabaseReference refModerate;
    DatabaseReference refLow;
    SearchView searchRequest;
    FirebaseAuth mFirebaseAuth;
    String userName;
    TextView detailDateTime;
    DatabaseReference drName;
    String nameOfUser;
    String detailCurLatLng;
    String TAG = "INFO";
    int idNotify;
    int newNearRequest;
    ImageView demoImg;
    TextView tmpUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initComponent();
        //showNumberNewUpdateReq();
    }
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private void initComponent() {
        tmpUrl = findViewById(R.id.staticLayout);
        demoImg = findViewById(R.id.img_demo);
        mMarkers = new ArrayList<>();
        detailView = findViewById(R.id.detail_view);
        detailTitle = findViewById(R.id.detail_title);
        detailDescription = findViewById(R.id.detail_description);
        detailPhone = findViewById(R.id.detail_phone);
        detailLocation = findViewById(R.id.detail_location);
        detailPrice = findViewById(R.id.detail_price);
        containerLayout = findViewById(R.id.container);
        requestForm = findViewById(R.id.request_from);

        eTitle = findViewById(R.id.eTitle);
        eDescription = findViewById(R.id.eDescription);
        eLocation = findViewById(R.id.eLocation);
        ePhone = findViewById(R.id.ePhone);
        ePrice = findViewById(R.id.ePrice);
        ePrice.addTextChangedListener(new MoneyTextWatcher(ePrice));
        sEmergency = findViewById(R.id.s_emergency);
        tEmergency = findViewById(R.id.t_emergency);
        cbCurrentLocation = findViewById(R.id.current_location);
        detailEmergency = findViewById(R.id.detail_emergency);
        ArrayAdapter<CharSequence> sEmergencyAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.category_arr, android.R.layout.simple_spinner_item);
        sEmergencyAdapter.setDropDownViewResource(R.layout.emergency_level_spinner);
        sEmergency.setAdapter(sEmergencyAdapter);
        sEmergency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getItemAtPosition(i);
                tEmergency.setText("Emergency Level: " + item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        searchRequest = findViewById(R.id.sr);
        setSearchOnSearchListener();
        mFirebaseAuth = FirebaseAuth.getInstance();
        //   landMarkController.setUserId("1");

        detailDateTime = findViewById(R.id.detail_dateTime);
//        nameOfUser = mFirebaseAuth.getCurrentUser().getEmail();
        distanceLayout = (LinearLayout)findViewById(R.id.linearLayoutShowDistance);
        nameOfUser = "LONG";

        Log.d(TAG + "username: ", nameOfUser);
        TextView textNameUser = findViewById(R.id.name_of_user);
        String nameOfUserArr[] = nameOfUser.split(getString(R.string.split_sign_email));
        textNameUser.setText("Welcome " + nameOfUserArr[0] + "!");
        idNotify = 0;
        newNearRequest = 0;

        containerLayout = findViewById(R.id.container);
        detailView = findViewById(R.id.detail_view);
        compleRequest = findViewById(R.id.cardView_complete);
        editRequest = findViewById(R.id.card_view_edit);
        landMarkController = new LandMarkController(this);
        Log.d("CCCCCCCCCCCC", getCurUserId());
        landMarkController.setUserId(getCurUserId());
        serverController = new ServerController(landMarkController, this);

    }

    private void setSearchOnSearchListener() {
        searchRequest.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                landMarkController.searchLandMark(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }


    public void showNumberNewUpdateReq() {
        try {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (newNearRequest > 0)
                        Toast.makeText(getApplicationContext(), String.valueOf(newNearRequest) + " " + getString(R.string.number_new_req), Toast.LENGTH_SHORT).show();

                }
            }, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        landMarkController.setMap(mMap);
        checkPermissionLocationAccess();
        LatLng currentLatLng;
        currentLatLng = landMarkController.getDeviceLocation();
        // gotoLocation(currentLatLng);
        landMarkController.gotoLocation(currentLatLng);
        landMarkController.setMarkerOnClickListener(containerLayout, detailView, compleRequest, editRequest, detailTitle, detailPhone, detailDescription, detailEmergency, detailDateTime, detailLocation, detailPrice, tmpUrl);
    }

    public boolean checkPermissionLocationAccess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void directToCurrentPosition(View view) {
        distanceLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.swithButton).setVisibility(View.GONE);
        landMarkController.directToCurrentPosition();
    }

    /////////////Utils onclick
    public void startDial(View view) {
        String phoneNumber = (String) detailPhone.getText().subSequence(7, detailPhone.getText().length());
        call(phoneNumber);
    }

    public void call(String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel: " + phoneNumber)));
    }

    public void sendSMS(String number) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));
    }

    public void startMessage(View view) {
        sendSMS((String) detailPhone.getText());
    }

    public void createForm(View view) {
        //  Toast.makeText(this, "Tong so landmark la: " + String.valueOf(landmarks.size()), Toast.LENGTH_SHORT).show();
        ImageButton button = findViewById(R.id.swithButton);
        button.setVisibility(View.GONE);
        searchRequest.setVisibility(View.GONE);
        containerLayout.setGravity(Gravity.CENTER);
        landMarkController.animateFadout(requestForm);
        requestForm.setVisibility(View.VISIBLE);
        eTitle.setText("");
        eDescription.setText("");
        eLocation.setText("");
        ePhone.setText("");
        findViewById(R.id.footer).setVisibility(View.GONE);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveMarker(View view) {
        String title = String.valueOf(eTitle.getText());
        String description = String.valueOf(eDescription.getText());
        String phone = String.valueOf(ePhone.getText());
        String category = (String) tEmergency.getText();
        category = category.substring(category.indexOf(":") + 2);
        String location = String.valueOf(eLocation.getText());
        String price = String.valueOf(ePrice.getText());

        landMarkController.saveMarker(title, description, location, phone, category, price, filePath);
    }

    public void choose_current_location(View view) {
        if (cbCurrentLocation.isChecked()) {
            hideLocationText(View.GONE);
        } else {
            hideLocationText(View.VISIBLE);
        }
    }

    private void hideLocationText(int state) {
        TransitionManager.beginDelayedTransition((ViewGroup)eLocation.getParent());
        eLocation.setVisibility(state);
    }

    public void close_form(View view) {
        landMarkController.closeForm(this, searchRequest, requestForm, containerLayout, eTitle, eLocation, eDescription, ePhone);
    }

    public void completeRequest(View view) {
        landMarkController.completeRequest();

    }

    public void close_detail_form(View view) {
        landMarkController.close_detail_form();
    }

    public void logoutAccount(View view) {
        landMarkController.logout();
    }

    public void openServer(View view) {
        detailView.setVisibility(View.GONE);
        searchRequest.setVisibility(View.GONE);
        this.findViewById(R.id.cur_server).setVisibility(View.VISIBLE);

        landMarkController.showServer();

        //      landMarkController.updateServer();
    }

    public String getCurUserId() {
        return mFirebaseAuth.getCurrentUser().getUid();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendMessage(View view) {
        TextView chatBox = findViewById(R.id.chat_box);
        Message message = new Message();
        // Log.d("TEXXXX", (String) chatBox.getText());
        message.setContent(String.valueOf(chatBox.getText()));
        message.setUserId(getCurUserId());
        message.setUserName(nameOfUser);
        serverController.saveMessage(message);
        chatBox.setText("");

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void editRequest(View view) {

        ImageButton button = findViewById(R.id.swithButton);
        //   TransitionManager.beginDelayedTransition((ViewGroup)button.getParent());
        button.setVisibility(View.GONE);
    //    landMarkController.animateFadout(searchRequest);
        searchRequest.setVisibility(View.GONE);

        LinearLayout footer  = findViewById(R.id.footer);
      //  landMarkController.animateFadout(footer);
        footer.setVisibility(View.GONE);



     //   landMarkController.animateFadout(containerLayout);

        landMarkController.animateFadout(detailView);
        detailView.setVisibility(View.GONE);

        containerLayout.setGravity(Gravity.CENTER);
        landMarkController.animateFadout(requestForm);
        requestForm.setVisibility(View.VISIBLE);
        cbCurrentLocation.setVisibility(View.GONE);

        //   close_detail_form(view);
        eTitle.setText(detailTitle.getText());
        String description = (String) detailDescription.getText();
        eDescription.setText(description.substring(description.indexOf(":") + 2));

        String phone = (String) detailPhone.getText();

        ePhone.setText(phone.substring(phone.indexOf(":") + 2));

        String location = (String) detailLocation.getText();

        eLocation.setText(location.substring(location.indexOf(":") + 2));
        String price = (String) detailPrice.getText();
        ePrice.setText(price.substring(price.indexOf(":") + 2));
        String category = (String) detailEmergency.getText();

        category = category.substring(category.indexOf(":") + 2);

        int index = -1;
        String[] arr = getResources().getStringArray(R.array.category_arr);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(category))
                index = i;
        }
        TextView staticLayout = findViewById(R.id.staticLayout);
        String imgUrl = (String) staticLayout.getText();
        sEmergency.setSelection(index);
        Picasso.get().load(imgUrl).into(demoImg);
        eLocation.setEnabled(false);
        demoImg.setTag("Edit");


    }


    public void closeServer(View view) {
        findViewById(R.id.cur_server).setVisibility(View.GONE);
        detailView.setVisibility(View.VISIBLE);
        searchRequest.setVisibility(View.VISIBLE);

    }

    int PICK_IMAGE_REQUEST = 1001;
    Uri filePath;
    // Select Image method

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);

                demoImg.setImageBitmap(bitmap);
                demoImg.setTag("change");
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
        else if (requestCode == CAMERA_REQUEST
                && resultCode == RESULT_OK
                && data != null){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            demoImg.setImageBitmap(photo);
            demoImg.setTag("change");
        }
    }

    public void selectImage(View view) {
        loadImg();
    }

    public void loadImg() {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }
    public void takePicture(View view)
    {
        cameraOn();
    }

    private void cameraOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            }
            else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void close_map_direction(View view) {
        distanceLayout.setVisibility(View.GONE);
        findViewById(R.id.swithButton).setVisibility(View.VISIBLE);
    }
}