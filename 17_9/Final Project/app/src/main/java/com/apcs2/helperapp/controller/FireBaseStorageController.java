package com.apcs2.helperapp.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.apcs2.helperapp.entity.LandMark;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class FireBaseStorageController {
    FirebaseDataController firebaseDataController;
    Context context;
    FirebaseStorage storage;
    StorageReference storageReference;
    int PICK_IMAGE_REQUEST = 1001;

    public FireBaseStorageController(Context context, LandMarkController landMarkController) {
        this.firebaseDataController = landMarkController.getFirebaseDataController();
        this.context = context;
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    public String uploadImage(Uri filePath, final LandMark landMark, final int option) {
        final String[] url = {null};
        if (filePath != null) {


            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog
                    = new ProgressDialog(context);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            final String imgId = UUID.randomUUID().toString();
            Log.d("NOO", imgId);
            final StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + imgId);

            // adding listeners on upload
            // or failure of image

            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot) {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();

                                    Toast
                                            .makeText(context,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            url[0] = uri.toString();
                                            Log.d("urla", url[0]);
                                            landMark.setImgUrl(url[0]);
                                            if (option == 0)
                                                firebaseDataController.pushLmToDB(landMark);
                                            if (option == 1)
                                                firebaseDataController.updateLandmark(landMark);



                                        }
                                    });
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(context,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int) progress + "%");
                                }
                            });
        }
        return url[0];
    }
}
