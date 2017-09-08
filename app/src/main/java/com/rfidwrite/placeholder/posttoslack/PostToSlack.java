package com.rfidwrite.placeholder.posttoslack;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.webapi.SlackWebApiClient;

/**
 * Created by sonny on 2017/09/06.
 */

public class PostToSlack extends AsyncTask<Void, Void, Void> {

    private String userName = "";
    private String token = "";
    private String value = "";
    SlackWebApiClient mWebApiClient;

    public void SetUserName(String inputName){
        userName = inputName;
    }

    public void SetToken(String inputToken){
        token = inputToken;
        mWebApiClient = SlackClientFactory.createWebApiClient(token);
    }

    public void SetMessage(String inVal){
        value = inVal;
    }

    protected void onPreExecute(){

    }

    @Override
    protected  Void doInBackground(Void...params){
        mWebApiClient.postMessage("kintai", value, "spasuzuking", true);
        return null;
    }
}
