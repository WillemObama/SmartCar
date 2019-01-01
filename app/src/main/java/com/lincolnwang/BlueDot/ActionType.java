package com.lincolnwang.BlueDot;

/**
 * Created by 11304 on 2018/11/5.
 */

public enum ActionType {

    FORWARD(0,"前进"),
    BACK(1,"后退"),
    TURN_LEFT(2,"左转"),
    TURN_RIGHT(3,"右转"),
    STOP(4,"停止"),
    CAMERA(5,"拍照"),
    LIGHT(6,"光照"),
    RECORDER(7,"分贝"),
    MAGNETIC(8,"磁力计"),
    ACCEX(9,"加X轴"),
    ACCEY(10,"加Y轴"),
    ACCEZ(11,"加Z轴"),
    ORIENTATION(12,"指南针");

    String value;
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    int action;

    private ActionType(int action,String value) {
        this.action=action;
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static String getName(int action) {
        for (ActionType a : ActionType.values()) {
            if (a.getAction() == action) {
                return a.value;
            }
        }
        return null;
    }

}
