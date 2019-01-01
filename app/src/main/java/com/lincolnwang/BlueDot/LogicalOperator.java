package com.lincolnwang.BlueDot;

public enum LogicalOperator {

    AND(0,"and"),
    OR(1,"or");

    int operator;
    String value;

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private LogicalOperator(int operator,String value)
    {
        this.operator = operator;
        this.value = value;
    }

}
