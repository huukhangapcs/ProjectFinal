package com.apcs2.helperapp.controller;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.apcs2.helperapp.entity.Message;
import com.apcs2.helperapp.repository.LandMarkRepository;
import com.example.helperapp.R;


public class ServerController {
    LandMarkRepository landMarkRepository;
    LandMarkController landMarkController;
    EditText chatBox;
    FirebaseDataController firebaseDataController;
    Context context;
    public ServerController(LandMarkController landMarkController, Context context){
        this.landMarkController = landMarkController;
        this.context = context;
        landMarkRepository = landMarkController.getLandMarkRepository();
        firebaseDataController = landMarkController.getFirebaseDataController();
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean saveMessage(Message msg) {
        msg.setTime(landMarkController.getCurrentDateTime());
        TextView tmp = (TextView) ((Activity) context).findViewById(R.id.detail_title);
        String[] extractedStr = landMarkController.extractDetailForm(tmp);
        int position = landMarkRepository.findByPositionAndTitle(extractedStr[0], extractedStr[1]);
        firebaseDataController.updateServerLandmark(landMarkRepository.getByPosition(position), msg);
        return true;
    }

}
