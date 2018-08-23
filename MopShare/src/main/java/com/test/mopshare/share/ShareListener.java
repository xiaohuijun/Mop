package com.test.mopshare.share;

public interface ShareListener {
    void shareSuccess();

    void shareFailure(Exception e);

    void shareCancel();
}
