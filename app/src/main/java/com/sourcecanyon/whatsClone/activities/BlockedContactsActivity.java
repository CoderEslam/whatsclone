package com.sourcecanyon.whatsClone.activities;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.adapters.recyclerView.contacts.BlockedContactsAdapter;
import com.sourcecanyon.whatsClone.animations.AnimationsUtil;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.models.users.contacts.UsersBlockModel;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.presenters.users.SelectContactsPresenter;
import com.sourcecanyon.whatsClone.ui.RecyclerViewFastScroller;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class BlockedContactsActivity extends BaseActivity {
    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.empty)
    LinearLayout emptyContacts;

    private BlockedContactsAdapter mSelectContactsAdapter;
    private SelectContactsPresenter mContactsPresenter;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initializerView();
        mContactsPresenter = new SelectContactsPresenter(this);
        mContactsPresenter.onCreate();
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_blocked_contacts));

        }
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mSelectContactsAdapter = new BlockedContactsAdapter(this);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        ContactsList.setAdapter(mSelectContactsAdapter);

        // set recyclerView to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();

        super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    /**
     * method to show blocked contacts list
     *
     * @param usersBlockModels this is parameter for ShowContacts  method
     */
    public void ShowContacts(List<UsersBlockModel> usersBlockModels) {

        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle("" + usersBlockModels.size() + getResources().getString(R.string.of) + PreferenceManager.getInstance().getContactSize(this));


        if (usersBlockModels.size() != 0) {
            fastScroller.setVisibility(View.VISIBLE);
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);
            mSelectContactsAdapter.setContacts(usersBlockModels);
        } else {
            fastScroller.setVisibility(View.GONE);
            ContactsList.setVisibility(View.GONE);
            emptyContacts.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactsPresenter.onDestroy();
        EventBus.getDefault().unregister(this);
    }



    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_REFRESH_BLOCKED_LIST:
                mContactsPresenter.onCreate();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
