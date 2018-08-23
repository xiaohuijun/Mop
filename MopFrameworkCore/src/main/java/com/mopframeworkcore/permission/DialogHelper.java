package com.mopframeworkcore.permission;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.mopframeworkcore.R;
import com.mopframeworkcore.utils.Utils;

public class DialogHelper {
    public static void showRationaleDialog(final PermissionUtils.OnRationaleListener.ShouldRequest shouldRequest) {
        Activity topActivity = Utils.getTopActivity();
        if (topActivity == null) return;
        new AlertDialog.Builder(topActivity)
                .setTitle(R.string.permission_alert_title)
                .setMessage(R.string.permission_rationale_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shouldRequest.again(true);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shouldRequest.again(false);
                    }
                })
                .setCancelable(false)
                .create()
                .show();

    }

    public static void showOpenAppSettingDialog() {
        Activity topActivity = Utils.getTopActivity();
        if (topActivity == null) return;
        new AlertDialog.Builder(topActivity)
                .setTitle(R.string.open_setting_alert_title)
                .setMessage(R.string.permission_denied_forever_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtils.launchAppDetailsSettings();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }
}
