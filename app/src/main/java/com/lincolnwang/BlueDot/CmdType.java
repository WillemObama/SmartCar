package com.lincolnwang.BlueDot;

/**
 * Created by 11304 on 2018/11/5.
 */

public enum CmdType {

    SEQUEN(0,"顺序"),
    LOOP(1,"循环"),
    CONDITION(2,"条件");

    String value;
    int cmd;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    private CmdType(int action,String value) {
        this.cmd=action;
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static String getName(int cmd) {
        for (CmdType a : CmdType.values()) {
            if (a.getCmd() == cmd) {
                return a.value;
            }
        }
        return null;
    }

}
