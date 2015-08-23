package com.kawakawaplanning.gpsdetag.list;

import android.graphics.Bitmap;

public class CustomData {
    private Bitmap imageData_;
    private String textData_;
 
    public void setImagaData(Bitmap image) {
        imageData_ = image;
    }
 
    public Bitmap getImageData() {
        return imageData_;
    }
 
    public void setTextData(String text) {
        textData_ = text;
    }
 
    public String getTextData() {
        return textData_;
    }
}