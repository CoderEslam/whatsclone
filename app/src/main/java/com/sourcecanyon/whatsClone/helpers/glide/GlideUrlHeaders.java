package com.sourcecanyon.whatsClone.helpers.glide;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;

/**
 * Created by Abderrahim El imame on 10/4/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class GlideUrlHeaders {



   public static GlideUrl getUrlWithHeaders(String url){
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", PreferenceManager.getInstance().getToken(WhatsCloneApplication.getInstance()))
                .build());
    }
}
