package com.apcs2.helperapp.entity;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.helperapp.R;

import java.util.List;

public class MessageAdapter extends BaseAdapter {
    private List<Message> messages;
    Context context;
    LayoutInflater layoutInflater;
    LinearLayout chatSegment;
    String curUserId;



    public MessageAdapter(Context context, List<Message> messages, String curUserId) {
        this.context = context;
        this.messages = messages;
        layoutInflater = LayoutInflater.from(context);
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
        if (view == null) {
            view = layoutInflater.inflate(R.layout.message, null);
            viewMessage = new ViewMessage();
            viewMessage.message = view.findViewById(R.id.single_mess);
            viewMessage.username = view.findViewById(R.id.username);
            viewMessage.chatSegment = view.findViewById(R.id.chat_segment);
            viewMessage.time = view.findViewById(R.id.time_mess);
            view.setTag(viewMessage);
        } else {
            viewMessage = (ViewMessage) view.getTag();
        }
        Message message = messages.get(i);
        if (message.getUserId().equals(curUserId)) {
            viewMessage.chatSegment.setGravity(Gravity.RIGHT);
            viewMessage.username.setText("Me");
            viewMessage.message.setBackgroundColor(Color.parseColor("#008EFF"));
            viewMessage.message.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            viewMessage.chatSegment.setGravity(Gravity.LEFT);
            viewMessage.username.setText(message.userName);
            viewMessage.message.setBackgroundColor(Color.parseColor("#D3D3D3"));
            viewMessage.message.setTextColor(Color.parseColor("#4C4848"));
        }
        viewMessage.time.setText(message.time);
        viewMessage.message.setText(message.content);
        Log.d("NAUUU", String.valueOf(i));
        return view;
    }
}
