package com.apcs2.helperapp.repository;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDataRepository {
    DatabaseReference refRoot;
    public FirebaseDataRepository(){
        refRoot = FirebaseDatabase.getInstance().getReference().getRoot();
    }
    public DatabaseReference getRefRoot() {
        return refRoot;
    }

    public void settRefRoot(DatabaseReference refRoot) {
        this.refRoot = refRoot;
    }




}


