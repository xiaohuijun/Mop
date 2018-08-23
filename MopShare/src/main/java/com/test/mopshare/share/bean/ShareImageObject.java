package com.test.mopshare.share.bean;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class ShareImageObject {

    private Bitmap mBitmap;

    private String mPathOrUrl;

    private ArrayList<String> mPathList;

    public ShareImageObject(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public ShareImageObject(String pathOrUrl) {
        mPathOrUrl = pathOrUrl;
    }

    public ShareImageObject(ArrayList<String> paths) {
        mPathList = paths;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public String getPathOrUrl() {
        return mPathOrUrl;
    }

    public void setPathOrUrl(String pathOrUrl) {
        mPathOrUrl = pathOrUrl;
    }

    public ArrayList<String> getmPathList() {
        return mPathList;
    }

    public void setmPathList(ArrayList<String> mPathList) {
        this.mPathList = mPathList;
    }

    @Override
    public String toString() {
        return "ShareImageObject{" +
                "mBitmap=" + mBitmap +
                ", mPathOrUrl='" + mPathOrUrl + '\'' +
                ", mPathList=" + mPathList +
                '}';
    }
}
