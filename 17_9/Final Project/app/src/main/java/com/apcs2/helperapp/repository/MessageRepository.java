package com.apcs2.helperapp.repository;

import com.apcs2.helperapp.entity.Message;

import java.util.ArrayList;

public class MessageRepository {
    ArrayList<Message> messages;

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
    public int size(){
        return messages.size();
    }
    public MessageRepository(){
        messages = new ArrayList<>();
    }
    public void add(Message message){
        messages.add(message);
    }
}
