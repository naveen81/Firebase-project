package com.example.naveen.firebasestorage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.tweetcomposer.TweetUploadService;

/**
 * Created by Naveen on 26-01-2018.
 */

public class MyResultReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(TweetUploadService.UPLOAD_SUCCESS.equals(intent.getAction())){
            //success
            Toast.makeText(context, "Tweet shared Successfully", Toast.LENGTH_SHORT).show();
            Log.i("share","share success");
            //final Long tweetID = intentExtras.getLong()
        }
        else if(TweetUploadService.UPLOAD_FAILURE.equals(intent.getAction())) {
            Toast.makeText(context, "Tweet sharing Failed", Toast.LENGTH_SHORT).show();
            Log.i("share", "share fail");
        }else if(TweetUploadService.TWEET_COMPOSE_CANCEL.equals(intent.getAction())){
            Toast.makeText(context, "Tweet Cancelled", Toast.LENGTH_SHORT).show();
            Log.i("share", "tweet cancelled");
        }
    }

}
