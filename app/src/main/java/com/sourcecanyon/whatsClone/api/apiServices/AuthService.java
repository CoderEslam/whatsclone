package com.sourcecanyon.whatsClone.api.apiServices;

import android.content.Context;

import com.sourcecanyon.whatsClone.BuildConfig;
import com.sourcecanyon.whatsClone.api.APIAuthentication;
import com.sourcecanyon.whatsClone.api.APIService;
import com.sourcecanyon.whatsClone.models.auth.JoinModelResponse;
import com.sourcecanyon.whatsClone.models.auth.LoginModel;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 10/4/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class AuthService {

    private APIAuthentication apiAuthentication;
    private Context mContext;
    private APIService mApiService;


    public AuthService(Context context, APIService mApiService) {
        this.mContext = context;
        this.mApiService = mApiService;
    }

    /**
     * method to initialize the api auth
     *
     * @return return value
     */
    private APIAuthentication initializeApiAuth() {
        if (apiAuthentication == null) {
            apiAuthentication = this.mApiService.AuthService(APIAuthentication.class, BuildConfig.BACKEND_BASE_URL);
        }
        return apiAuthentication;
    }


    public Observable<JoinModelResponse> join(LoginModel loginModel) {
        return initializeApiAuth().join(loginModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<JoinModelResponse> resend(String phone) {
        return initializeApiAuth().resend(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<JoinModelResponse> verifyUser(String code,String phone) {
        return initializeApiAuth().verifyUser(code,phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
