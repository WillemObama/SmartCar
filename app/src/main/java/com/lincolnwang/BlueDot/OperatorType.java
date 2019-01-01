package com.lincolnwang.BlueDot;

/**
 * Created by 11304 on 2018/11/13.
 */

public enum OperatorType {

    EQUAL(0,"=="),
    GREATER(1,">"),
    LESS(2,"<");

    String value;
    int operatorType;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getoperatorType() {
        return operatorType;
    }

    public void setoperatorType(int operatorType) {
        this.operatorType = operatorType;
    }

    private OperatorType(int operatorType,String value) {
        this.operatorType = operatorType;
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
