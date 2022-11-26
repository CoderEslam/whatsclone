package com.sourcecanyon.whatsClone.services.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;

/**
 * Created by Abderrahim El imame on 2019-12-31.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context mContext, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            if (PreferenceManager.getInstance().getToken(mContext) != null) {
                WhatsCloneApplication.getInstance().preInitMqtt();
            }

        }
    }
}