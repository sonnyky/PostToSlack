package com.rfidwrite.placeholder.rfidwrite;

/**
 * Created by sonny on 2017/08/04.
 */

public class EntranceTag {
    private int dbId;
    private String tagId;
    private String dateDetected;
    private String timeDetected;
    private int inUse;

    public EntranceTag( String tagId, String date, String time, int inUseFlag){
        this.dbId = dbId;
        this.tagId = tagId;
        this.dateDetected = date;
        this.timeDetected = time;
        this.inUse = inUseFlag;
    }

    public int GetTagIdFromDb(){
        return dbId;
    }

    public String GetTagId(){
        return tagId;
    }

    public String GetTagDate(){
        return dateDetected;
    }


    public String GetTagTime(){
        return timeDetected;
    }

    public int GetTagUsedFlag(){
        return inUse;
    }

    public void SetIdFromDb(int idToSet){
        dbId = idToSet;
    }

    public void SetDateDetected(String dateInput){
        this.dateDetected = dateInput;
    }

    public void SetTimeDetected(String timeInput){
        this.timeDetected = timeInput;
    }

    public void SetUsedFlag(int flag){
        this.inUse = flag;
    }

}
