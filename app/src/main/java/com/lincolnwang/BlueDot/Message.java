package com.lincolnwang.BlueDot;

/**
 * Created by 11304 on 2019/1/1.
 */

public class Message {

    public static final int MESSAGE_CAR = 0;
    public static final int MESSAGE_MOTOR = 1;

    private int messageType;
    private String message;

    public Message(int messageType,String message){
        this.messageType = messageType;
        this.message = message;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
