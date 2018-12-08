package io.dancmc.dsync;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONObject;

public abstract class MediaApiCallback {
    public abstract void success(JSONObject jsonResponse);

    public void failure(Context context, JSONObject jsonResponse){
        Toast.makeText(context, jsonResponse.optString("error_message", "Server returned unknown failure"), Toast.LENGTH_SHORT).show();
    }

    public void networkFailure(Context context, int code){
        Toast.makeText(context, "Network Failure - HTTP "+code+" error", Toast.LENGTH_SHORT).show();
    }
}