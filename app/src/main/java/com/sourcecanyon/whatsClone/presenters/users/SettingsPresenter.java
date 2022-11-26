package com.sourcecanyon.whatsClone.presenters.users;


import com.sourcecanyon.whatsClone.activities.settings.SettingsActivity;
import com.sourcecanyon.whatsClone.api.APIHelper;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.interfaces.Presenter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Abderrahim El imame on 20/02/2016. Email : abderrahim.elimame@gmail.com
 */
public class SettingsPresenter implements Presenter {
    private final SettingsActivity view;

    private CompositeDisposable mDisposable;


    public SettingsPresenter(SettingsActivity settingsActivity) {
        this.view = settingsActivity;


    }

    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {
        mDisposable = new CompositeDisposable();
        getContactLocal();
    }


    public void getContactLocal() {

        mDisposable.add(APIHelper.initialApiUsersContacts()
                .getUserInfo(PreferenceManager.getInstance().getID(view), mDisposable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(usersModel -> {
                    AppHelper.LogCat("usersModel " + usersModel.toString());
                    view.ShowContact(usersModel);

                }, throwable -> {
                    AppHelper.LogCat("usersModel " + throwable.getMessage());

                }));
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        if (mDisposable != null) mDisposable.dispose();
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onStop() {

    }
}