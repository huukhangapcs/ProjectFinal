package com.apcs2.helperapp.entity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.helperapp.R;

import java.util.List;

public class MessageAdapter extends BaseAdapter {
    private List<Message> messages;
    Context context;
    String curUserId;


    public MessageAdapter(Context context, List<Message> messages, String curUserId) {
        this.context = context;
        this.messages = messages;
        this.curUserId = curUserId;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewMessage viewMessage;
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            viewMessage = new ViewMessage();
        } else {
            viewMessage = (ViewMessage) view.getTag();
        }
        Message message = messages.get(i);
        if (message.getUserId().equals(curUserId)) {
            view = messageInflater.inflate(R.layout.message_from_me, null);
            viewMessage.messageBody = (TextView)view.findViewById(R.id.my_message_body);
            viewMessage.time = (TextView)view.findViewById(R.id.time_mess);
            view.setTag(viewMessage);
            viewMessage.messageBody.setText(message.getContent());
            viewMessage.time.setText(message.getTime());
        }
        else {
            view = messageInflater.inflate(R.layout.message_from_other, null);
            viewMessage.name = (TextView)view.findViewById(R.id.name);
            viewMessage.messageBody = (TextView) view.findViewById(R.id.message_body);
            viewMessage.time = (TextView)view.findViewById(R.id.time_mess);
            view.setTag(viewMessage);
            viewMessage.name.setText(message.getUserName());
            viewMessage.messageBody.setText(message.getContent());
            viewMessage.time.setText(message.getTime());
        }
        Log.d("NAUUU", String.valueOf(i));
        return view;
    }
}
