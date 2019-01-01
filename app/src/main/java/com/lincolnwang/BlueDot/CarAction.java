package com.lincolnwang.BlueDot;

import android.hardware.*;
import android.widget.ListView;

import java.util.List;

/**
 * Created by 11304 on 2018/11/5.
 */

public class CarAction {

    private Module module;
    private ActionType actionType;
    private double len; // 距离
    private double degree; // 偏转角度
    // 动作完成后的状态
    private int x;
    private int y;
    private int carDegree; // 车头角度
    // 循环
    private CmdType cmdType;
    private List<SingleCondition> conditionList;
    private Sensor sensor;
    private List<CarAction> loopCarAction; // 循环动作

    private List<CarAction> conditionCarAction; // 条件动作
    private int numOfLoop=0; // 循环次数
    private int conditionValue = 0;
    private int pulse = 0;

    public CarAction(){

    }

    public double getLen() {
        return len;
    }

    public void setLen(double len) {
        this.len = len;
    }

    public double getDegree() {
        return degree;
    }

    public void setDegree(double degree) {
        this.degree = degree;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getCarDegree() {
        return carDegree;
    }

    public void setCarDegree(int carDegree) {
        this.carDegree = carDegree;
    }

    public List<CarAction> getLoopCarAction() {
        return loopCarAction;
    }

    public void setLoopCarAction(List<CarAction> loopCarAction) {
        this.loopCarAction = loopCarAction;
    }

    public int getNumOfLoop() {
        return numOfLoop;
    }

    public void setNumOfLoop(int numOfLoop) {
        this.numOfLoop = numOfLoop;
    }

    public CmdType getCmdType() {
        return cmdType;
    }

    public void setCmdType(CmdType cmdType) {
        this.cmdType = cmdType;
    }

    public List<SingleCondition> getCondition() {
        return conditionList;
    }

    public void setCondition(List<SingleCondition> condition) {
        this.conditionList = condition;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public int getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(int conditionValue) {
        this.conditionValue = conditionValue;
    }

    public List<CarAction> getConditionCarAction() {
        return conditionCarAction;
    }

    public void setConditionCarAction(List<CarAction> conditionCarAction) {
        this.conditionCarAction = conditionCarAction;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public int getPulse() {
        return pulse;
    }

    public void setPulse(int pulse) {
        this.pulse = pulse;
    }

    public boolean conditionJudge(SingleCondition singleCondition){

        switch (singleCondition.getOperatorType()){
            case LESS:
                return singleCondition.getSensor().getCurrentValue() < singleCondition.getValue();
            case EQUAL:
                return singleCondition.getSensor().getCurrentValue() == singleCondition.getValue();
            case GREATER:
                return singleCondition.getSensor().getCurrentValue() > singleCondition.getValue();
            default:
                break;
        }
        return false;
    }

    public boolean getIsEstablish() {
        boolean result = true;
        if(this.cmdType == CmdType.CONDITION){
            if(conditionList != null){
                for (SingleCondition singleCondition:
                        conditionList) {


                    if(singleCondition.getLogicalOperator() == LogicalOperator.AND){
                        result &= conditionJudge(singleCondition);
                    }
                    else {
                        result |= conditionJudge(singleCondition);
                    }
                }

            }
            return result;
        }
        else
            return true;
    }

}
