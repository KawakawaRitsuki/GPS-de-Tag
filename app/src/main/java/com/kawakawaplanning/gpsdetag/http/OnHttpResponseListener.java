package com.kawakawaplanning.gpsdetag.http;

import java.util.EventListener;

/**
 * Created by KP on 15/08/14.
 */
public interface OnHttpResponseListener extends EventListener {

    void onPostResponse(String response);

}