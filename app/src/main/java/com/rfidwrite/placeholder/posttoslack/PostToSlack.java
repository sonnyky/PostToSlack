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
    SlackWebApiClient mWebApiClient;

    public void SetUserName(String inputName){
        userName = inputName;
    }

    public void SetToken(String inputToken){
        token = inputToken;
        mWebApiClient = SlackClientFactory.createWebApiClient(token);
    }

    protected void onPreExecute(){

    }

    @Override
    protected  Void doInBackground(Void...params){
        mWebApiClient.postMessage("kintai", "ハローモグ", "yayoi_nagano", false);
        return null;
    }
}
