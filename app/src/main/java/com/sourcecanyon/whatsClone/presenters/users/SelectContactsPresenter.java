package com.sourcecanyon.whatsClone.presenters.users;


import com.sourcecanyon.whatsClone.activities.BlockedContactsActivity;
import com.sourcecanyon.whatsClone.activities.messages.TransferMessageContactsActivity;
import com.sourcecanyon.whatsClone.api.APIHelper;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.interfaces.Presenter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SelectContactsPresenter implements Presenter {
    private TransferMessageContactsActivity transferMessageContactsActivity;
    private BlockedContactsActivity blockedContactsActivity;

    private CompositeDisposable mDisposable;

    public SelectContactsPresenter(TransferMessageContactsActivity transferMessageContactsActivity) {
        this.transferMessageContactsActivity = transferMessageContactsActivity;

    }

    public SelectContactsPresenter(BlockedContactsActivity blockedContactsActivity) {
        this.blockedContactsActivity = blockedContactsActivity;

    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        mDisposable = new CompositeDisposable();
        if (transferMessageContactsActivity != null) {


            mDisposable.add(APIHelper.initialApiUsersContacts()
                    .getLinkedContacts()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(transferMessageContactsActivity::ShowContacts, throwable -> {
                AppHelper.LogCat("Error contacts selector " + throwable.getMessage());
            }))
            ;
        } else {


            mDisposable.add(APIHelper.initialApiUsersContacts()
                    .getBlockedContacts()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(blockedContactsActivity::ShowContacts, throwable -> {
                AppHelper.LogCat("Error contacts selector " + throwable.getMessage());
            }))
            ;
        }

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