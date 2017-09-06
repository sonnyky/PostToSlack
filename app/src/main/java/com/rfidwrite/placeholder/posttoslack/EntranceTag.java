package com.rfidwrite.placeholder.posttoslack;

/**
 * Created by sonny on 2017/08/04.
 */

public class EntranceTag {
    private int dbId;
    private String tagId;
    private String dateTimeDetected;
    private int inUse; // 0 : not used, 1 : in use

    public EntranceTag( String tagId, String dateTime, int inUseFlag){
        this.tagId = tagId;
        this.dateTimeDetected = dateTime;
        this.inUse = inUseFlag;
    }

    public int GetTagIdFromDb(){
        return dbId;
    }

    public String GetTagId(){
        return tagId;
    }

    public String GetTagDateTime(){
        return dateTimeDetected;
    }

    public int GetTagUsedFlag(){
        return inUse;
    }

    public void SetIdFromDb(int idToSet){
        dbId = idToSet;
    }

    public void SetDateTimeDetected(String dateTimeInput){
        this.dateTimeDetected = dateTimeInput;
    }

    public void SetUsedFlag(int flag){
        this.inUse = flag;
    }

}
