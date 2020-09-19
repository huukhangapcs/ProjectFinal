package com.apcs2.helperapp.controller;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionValues;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
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

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.apcs2.helperapp.entity.DirectPath;
import com.apcs2.helperapp.entity.LandMark;
import com.apcs2.helperapp.entity.Message;
import com.apcs2.helperapp.entity.MessageAdapter;
import com.apcs2.helperapp.main.MainActivity;
import com.apcs2.helperapp.main.MapsActivity;
import com.apcs2.helperapp.repository.DirectPathRepository;
import com.apcs2.helperapp.repository.LandMarkRepository;
import com.example.helperapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class LandMarkController {
    private GoogleMap mMap;
    LandMarkRepository landMarkRepository;
    FirebaseDataController firebaseDataController;
    FireBaseStorageController fireBaseStorageController;
    private ArrayList<Marker> mMarkers;
    private DirectPathRepository directPathRepository;
    LatLng currentLatLng;
    Marker marker;
    int idNotify;
    int newNearRequest;
    Context context;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    String userId;

    public String getUserId() {
        return userId;
    }

    public ArrayList<Marker> getmMarkers() {
        return mMarkers;
    }

    public void setmMarkers(ArrayList<Marker> mMarkers) {
        this.mMarkers = mMarkers;
    }

    public FirebaseDataController getFirebaseDataController() {
        return firebaseDataController;
    }

    public LandMarkRepository getLandMarkRepository() {
        return landMarkRepository;
    }

    public LandMarkController(Context context) {
        landMarkRepository = new LandMarkRepository();
        mMarkers = new ArrayList<>();
        idNotify = 0;
        newNearRequest = 0;
        this.context = context;
        directPathRepository = new DirectPathRepository(context);
        firebaseDataController = new FirebaseDataController(landMarkRepository, this, directPathRepository, context);
        fireBaseStorageController = new FireBaseStorageController(context, this);
    }

    public void gotoLocation(LatLng tmpLatLng) {
        CameraPosition newCameraPosition = new CameraPosition.Builder()
                .target(tmpLatLng) // Sets the center of the map to Mountain View
                .zoom(15)                      // Sets the zoom
                .bearing(90)                   // Sets the orientation of the camera to east
                .tilt(30)                      // Sets the tilt of the camera to 30 degrees
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
    }

    private boolean isPhoneNumber(String phoneNum) {
        if (phoneNum.length() > 12 || phoneNum.length() < 10)
            return false;
        // Log.d(TAG, "Line 797: phone: " + phoneNum.substring(1, 2));
        if (phoneNum.substring(1, 2).equals("0") || !phoneNum.startsWith("0")) {
            return false;
        }
        return true;
    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {

        View customMarkerView = new View(context);
//        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.img_demo);
//        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(100, 100,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    public void drawMarker(LandMark landMark) {

        new LoadImageAsyncTask(landMark).execute();
    }

    public class LoadImageAsyncTask extends AsyncTask<Void, Void, Drawable> {
        LandMark landMark;

        public LoadImageAsyncTask(LandMark landMark) {
            this.landMark = landMark;
        }

        @Override
        protected Drawable doInBackground(Void... voids) {


            Drawable bmp = getDrawableImageFromUrl(landMark.getImgUrl());

            return bmp;

        }

        @Override
        protected void onPostExecute(Drawable img) {
            LatLng latLng = landMark.getLatLng();
            LatLng base = new LatLng(0, 0);
            String description = landMark.getDescription();
            String title = landMark.getName();
            String phone = landMark.get_phone();
            String category = landMark.getCategory();
            String curDateTime = landMark.get_startDate();
            String uId = landMark.getUserId();
            String address = landMark.getAddress();
            String price = landMark.getPrice();
            String imgUrl = landMark.getImgUrl();
            if (!latLng.equals(base) && !description.equals("") && !title.equals("") && !phone.equals("")) {
                String spitSign = "~";
                String containerStr = description + spitSign + phone + spitSign + category + spitSign + curDateTime + spitSign + uId + spitSign + address + spitSign + price + spitSign + imgUrl;
                Marker mMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(createMaker(context, img, uId)))
                        .title(title)
                        .snippet(containerStr));

                mMarkers.add(mMarker);
                landMarkRepository.addALandmark(landMark);
                directPathRepository.add(new DirectPath());
            }
        }
    }

    public Bitmap createMaker(Context context, Drawable resource, String curUId) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker, null);

        CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        markerImage.setImageDrawable(resource);
        ImageView wappedImage = marker.findViewById(R.id.img_wapper);
        if (curUId.equals(userId))
            wappedImage.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorGreen)));
        else
            wappedImage.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorRed)));

        //  TextView txt_name = (TextView) marker.findViewById(R.id.name);
        //   txt_name.setText(_name);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

    public Drawable getDrawableImageFromUrl(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            Bitmap myBitmap = BitmapFactory.decodeStream(input);

            myBitmap = Bitmap.createScaledBitmap(myBitmap, 350, 250, true);
            Drawable drawableImage = new BitmapDrawable(context.getResources(), myBitmap);

            return drawableImage;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }


    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), id, null);
        assert vectorDrawable != null;
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public LatLng getDeviceLocation() {

        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Getting Current Location
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return new LatLng(10.763166, 106.682225);
        }
        mMap.setMyLocationEnabled(true);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            // Getting latitude of the current location
            double latitude = location.getLatitude();
            // Getting longitude of the current location
            double longitude = location.getLongitude();
            // Creating a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);
            //   Log.d(TAG, "Line 176: current latitute " + String.valueOf(latitude));
            return latLng;
        }
        return new LatLng(10.763166, 106.682225);
    }

    public void setMarkerOnClickListener(final LinearLayout containerLayout, final ScrollView detailView, final CardView compleRequest, final CardView editRequest, final TextView detailTitle, final TextView detailPhone, final TextView detailDescription, final TextView detailEmergency, final TextView detailDateTime, final TextView detailLocation, final TextView detailPrice, final TextView tmpUrlImg) {
        final LandMarkController curClass = this;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                animateFadout(detailView);
                detailView.setVisibility(View.VISIBLE);
                TransitionManager.beginDelayedTransition(containerLayout);
                containerLayout.setGravity(Gravity.BOTTOM);
                detailTitle.setText(marker.getTitle());
                String spitSign = "~";
                String detailCurLatLng = String.valueOf(marker.getPosition().latitude) + "," + String.valueOf(marker.getPosition().longitude);
                String[] splitStr = marker.getSnippet().split(spitSign);
                detailDescription.setText("Description: " + splitStr[0]);
                detailPhone.setText("Phone: " + splitStr[1]);
                detailEmergency.setText("Category: " + splitStr[2]);
                detailDateTime.setText(splitStr[3]);
//                animateFadout(compleRequest);
//                animateFadout(editRequest);
                if (checkUserPermission(splitStr[4])) {
                    compleRequest.setVisibility(View.VISIBLE);
                    editRequest.setVisibility(View.VISIBLE);

                } else {
                    compleRequest.setVisibility(View.GONE);
                    editRequest.setVisibility(View.GONE);

                }
                detailLocation.setText("Location: " + splitStr[5]);
                detailPrice.setText("Price: " + splitStr[6]);
                tmpUrlImg.setText(splitStr[7]);
                ImageView imageView = ((Activity) context).findViewById(R.id.img_detail);
                Picasso.get().load(splitStr[7]).into(imageView);

                curClass.marker = marker;
                return true;
            }
        });
    }

    private boolean checkUserPermission(String curUid) {
        if (userId.equals(curUid))
            return true;
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getCurrentDateTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveMarker(String title, String description, String location, String phone, String category, String price,


                           Uri filePath) {
        Activity activity = (Activity) context;
        EditText eLocation = activity.findViewById(R.id.eLocation);

        LinearLayout containerLayout = activity.findViewById(R.id.container);
        try {
            Address curAddress = null;
            LatLng tmpLatLng = null;
            String addressStr = "";
            ((Activity) context).findViewById(R.id.footer).setVisibility(View.GONE);
            CheckBox cbCurrentLocation = activity.findViewById(R.id.current_location);
            TransitionManager.beginDelayedTransition((ViewGroup)cbCurrentLocation.getParent());

            if (cbCurrentLocation.isChecked()) {
                currentLatLng = getDeviceLocation();
                tmpLatLng = currentLatLng;
                addressStr = String.valueOf(tmpLatLng.latitude) + "N, " + String.valueOf(tmpLatLng.longitude) + "E";
                //   detailLocation.setText("Location: " + String.valueOf(tmpLatLng.latitude) + "N, " + String.valueOf(tmpLatLng.longitude) + "E");

            } else if (eLocation.isEnabled()) {
                Log.d("ADDress", String.valueOf(location));
                curAddress = getLocationOfForm(location);
                //    curAddress = getLocationOfForm(String.valueOf("Ben xe quan 8"), ((Activity) context));

                if (curAddress == null) {
                    Toast.makeText(context, "Address doesn't exist", Toast.LENGTH_SHORT).show();
                    return;
                }
                tmpLatLng = new LatLng(curAddress.getLatitude(), curAddress.getLongitude());
                addressStr = curAddress.getAddressLine(0);

            }
            String currentDateTime = "Can't estimating because SDK version is lesser than 26...";
            try {
                currentDateTime = getCurrentDateTime();

            } catch (DateTimeException e) {
                e.printStackTrace();
                //  Log.d(TAG, getString(R.string.cur_date_err));
            }
            LandMark temp_lm = new LandMark(title, description, phone, tmpLatLng, category, userId, currentDateTime, addressStr, price);
            ImageView previewImg = activity.findViewById(R.id.img_demo);
            String imgTag = (String) previewImg.getTag();
            if (!temp_lm.getName().equals("") && !temp_lm.getDescription().equals("") && !temp_lm.get_phone().equals("") && !temp_lm.getPrice().equals("") && !imgTag.equals("default")) {
                //CASE UPDATE
                if (!eLocation.isEnabled()) {

                    TextView tmp = (TextView) ((Activity) context).findViewById(R.id.detail_title);
                    String[] extractedStr = extractDetailForm(tmp);
                    int position = landMarkRepository.findByPositionAndTitle(extractedStr[0], extractedStr[1]);
                    temp_lm.set_locationID(landMarkRepository.getByPosition(position).get_locationID());
                    temp_lm.setLatLng(landMarkRepository.getByPosition(position).getLatLng());
                    temp_lm.set_address(landMarkRepository.getByPosition(position).getAddress());
                    temp_lm.setServer(landMarkRepository.getByPosition(position).getServer());
                    temp_lm.setImgUrl(landMarkRepository.getByPosition(position).getImgUrl());
                    temp_lm.setPrice(landMarkRepository.getByPosition(position).getPrice());
                    TextView img = activity.findViewById(R.id.staticLayout);
                    String oldUrlImg = (String) img.getText();
                    String newImgUrl = landMarkRepository.getByPosition(position).getImgUrl();
//                    if (oldUrlImg.equals(newImgUrl))
//                        Log.d("HAHA", "NAUUUU");
//                    else
                    fireBaseStorageController.uploadImage(filePath, temp_lm, 1);


                } else if (landMarkRepository.isMarkerExist(temp_lm.getLatLng())) {
                    Toast.makeText(context, context.getString(R.string.capturedLocation), Toast.LENGTH_SHORT).show();
                    return;

                }
                // CASE ADD
                else {
                    fireBaseStorageController.uploadImage(filePath, temp_lm, 0);
                }
                ScrollView requestForm = activity.findViewById(R.id.request_from);
                ImageButton logout = activity.findViewById(R.id.logoutButton);
                SearchView searchRequest = activity.findViewById(R.id.sr);
                EditText eTitle = activity.findViewById(R.id.eTitle);
                EditText eDescription = activity.findViewById(R.id.eDescription);
                EditText ePhone = activity.findViewById(R.id.ePhone);
                EditText ePrice = activity.findViewById(R.id.ePrice);

                clearForm(searchRequest, requestForm, containerLayout, eTitle, eLocation, eDescription, ePhone, ePrice);
                gotoLocation(temp_lm.getLatLng());
                //     gotoLocation(temp_lm.getLatLng());
            }


        } catch (Exception e) {
            e.printStackTrace();
            //    throwErrorWarning();
        }
    }

    public boolean addLandmark(LandMark landMark) {
        return true;
    }


    public Address getLocationOfForm(String location) {
        //   String location = String.valueOf(eLocation.getText());
        LatLng tmpLatLng = null;
        List<Address> addresses = null;


        Geocoder geocoder = new Geocoder(context);

        try {
            addresses = geocoder.getFromLocationName(location, 1);
        } catch (IOException e) {

            //   Log.d(TAG, getString(R.string.ADRESS_NOT_FOUND));
            e.printStackTrace();
        }

        Address address = null;

        if (addresses != null && addresses.size() > 0) {
            address = addresses.get(0);
            return address;
        }
        return null;

    }


    private void clearForm(final SearchView searchRequest, final ScrollView requestForm, final LinearLayout containerLayout, final EditText eTitle, final EditText eLocation, final EditText eDescription, final EditText ePhone, final EditText ePrice) {

        containerLayout.setGravity(Gravity.BOTTOM);
        animateFadout(requestForm);
        requestForm.setVisibility(View.GONE);
        eTitle.setText("");
        eDescription.setText("");
        eLocation.setText("");
        ePhone.setText("");
        ePrice.setText("");
        animateFadout(searchRequest);

        searchRequest.setVisibility(View.VISIBLE);
        animateFadout(((Activity) context).findViewById(R.id.footer));
        ((Activity) context).findViewById(R.id.footer).setVisibility(View.VISIBLE);

        ImageButton switchBtn = ((Activity) context).findViewById(R.id.swithButton);
        TransitionManager.beginDelayedTransition((ViewGroup)switchBtn.getParent());

        switchBtn.setVisibility(View.VISIBLE);

        eLocation.setEnabled(true);
        Spinner sEmergency = ((Activity) context).findViewById(R.id.s_emergency);
        ImageView imageView = ((Activity) context).findViewById(R.id.img_demo);
        imageView.setImageResource(R.drawable.image_24);
        imageView.setTag("default");
        sEmergency.setSelection(2);
        ((Activity) context).findViewById(R.id.current_location).setVisibility(View.VISIBLE);
    }

    public void closeForm(final Context context, final SearchView searchRequest, final ScrollView requestForm, final LinearLayout containerLayout, final EditText eTitle, final EditText eLocation, final EditText eDescription, final EditText ePhone) {

        TextView error = ((Activity) context).findViewById(R.id.error_from);

        error.setText("");
        EditText ePrice = ((Activity) context).findViewById(R.id.ePrice);
        clearForm(searchRequest, requestForm, containerLayout, eTitle, eLocation, eDescription, ePhone, ePrice);

    }

    public void logout() {
        Activity curActivity = ((Activity) context);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(curActivity, MainActivity.class);
        curActivity.startActivity(intent);
        curActivity.finish();
    }

    public String[] extractDetailForm(final TextView detailTitle) {
        String detailCurLatLng = String.valueOf(marker.getPosition().latitude) + "," + String.valueOf(marker.getPosition().longitude);
        String curTitle = (String) detailTitle.getText();
        return new String[]{detailCurLatLng, curTitle};
    }

    public String createDirectionUri(LatLng startPosition, LatLng desPositon, Context context) {
        String start = String.valueOf(startPosition.longitude) + ',' + String.valueOf(startPosition.latitude);
        String des = String.valueOf(desPositon.longitude) + ',' + String.valueOf(desPositon.latitude);
        return context.getString(R.string.MAPBOX_URL) + start + ';' + des + context.getString(R.string.ACCESS_TOKEN);
    }

    public void completeRequest() {

        String[] extractedStr = extractDetailForm((TextView) ((Activity) context).findViewById(R.id.detail_title));
        firebaseDataController.deleteMarker(extractedStr[0], extractedStr[1]);
        close_detail_form();
    }

    public void close_detail_form() {

        ScrollView detailView  = ((Activity) context).findViewById(R.id.detail_view);
        animateFadout(detailView);
        detailView.setVisibility(View.GONE);
    }
    public void animateFadout(Object o){
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) o).getParent();
        TransitionManager.beginDelayedTransition(viewGroup);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void directToCurrentPosition() {
        //    removeAllPolylineExceptAtPostion(-1);
        //  LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        directPathRepository.setmMap(mMap);
        directPathRepository.removeAllPolylineExceptAtPostion(-1);
        //  LatLng tmpLng = new LatLng(curLat, curLong);

        TextView tmp = (TextView) ((Activity) context).findViewById(R.id.detail_title);
        String[] extractedStr = extractDetailForm(tmp);
        //    int position = find(extractedStr[0], extractedStr[1]);
        int position = landMarkRepository.findByPositionAndTitle(extractedStr[0], extractedStr[1]);

        //      LandMark landmark = landmarks.get(position);
        LandMark landMark = landMarkRepository.getByPosition(position);
        currentLatLng = getDeviceLocation();
        String url = createDirectionUri(currentLatLng, landMark.getLatLng(), context);
        directPathRepository.requestDirection(url, position);
        // check if get position success
        if (currentLatLng.latitude != 0 && currentLatLng.longitude != 0) {
            //         gotoLocation(currentLatLng);
            gotoLocation(currentLatLng);
            close_detail_form();
        }
    }

    public void removeAMarker(int position) {
        mMarkers.get(position).remove();
        mMarkers.remove(position);
        landMarkRepository.remove(position);
    }

    public void searchLandMark(String location) {//tri viet
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(context.getApplicationContext());
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(location));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            Toast.makeText(context.getApplicationContext(),address.getLatitude()+" "+address.getLongitude(),Toast.LENGTH_LONG).show();
        }
    }

    public void setMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    public void showServer() {
        TextView tmp = (TextView) ((Activity) context).findViewById(R.id.detail_title);
        String[] extractedStr = extractDetailForm(tmp);
        final int position = landMarkRepository.findByPositionAndTitle(extractedStr[0], extractedStr[1]);
        final ListView listView = ((Activity) context).findViewById(R.id.messages);

        final MessageAdapter messageAdapter = new MessageAdapter(context, landMarkRepository.getServer(position), userId);
        listView.setAdapter(messageAdapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        messageAdapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView time = view.findViewById(R.id.time_mess);
                TransitionManager.beginDelayedTransition((ViewGroup)time.getParent());
                time.setVisibility(Math.abs(time.getVisibility() - 8));
            }
        });
    }


}
