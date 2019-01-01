package com.lincolnwang.BlueDot;

/**
 * Created by 11304 on 2018/12/2.
 */

public enum Module {

    Car(0,"Car"),
    Motor(1,"Motor");

    int action;
    String value;

    private Module(int action,String value){
        this.action = action;
        this.value = value;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
