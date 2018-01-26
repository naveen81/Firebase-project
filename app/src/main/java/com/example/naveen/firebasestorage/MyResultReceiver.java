package com.example.naveen.firebasestorage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.twitter.sdk.android.tweetcomposer.TweetUploadService;

/**
 * Created by Naveen on 26-01-2018.
 */

public class MyResultReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(TweetUploadService.UPLOAD_SUCCESS.equals(intent.getAction())){
            //success
            //final Long tweetID = intentExtras.getLong()
        }
    }
}
