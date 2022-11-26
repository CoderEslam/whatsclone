package com.sourcecanyon.whatsClone.activities.status;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.BaseActivity;
import com.sourcecanyon.whatsClone.adapters.recyclerView.StatusAdapter;
import com.sourcecanyon.whatsClone.animations.AnimationsUtil;
import com.sourcecanyon.whatsClone.models.users.status.StatusModel;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.UtilsString;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.presenters.users.StatusPresenter;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusActivity extends BaseActivity implements RewardedVideoAdListener {
    private RewardedVideoAd mAd;


    @BindView(R.id.currentStatus)
    EmojiTextView currentStatus;
    @BindView(R.id.editCurrentStatusBtn)
    AppCompatImageView editCurrentStatusBtn;
    @BindView(R.id.StatusList)
    RecyclerView StatusList;
    @BindView(R.id.ParentLayoutStatus)
    LinearLayout ParentLayoutStatus;


    private StatusAdapter mStatusAdapter;
    private StatusPresenter mStatusPresenter;
    private String statusID;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        ButterKnife.bind(this);

        if (PreferenceManager.getInstance().ShowVideoAds(this)) {
            if (PreferenceManager.getInstance().getUnitVideoAdsID(this) != null) {
                // Initialize the Mobile Ads SDK.
                MobileAds.initialize(this, PreferenceManager.getInstance().getAppVideoAdsID(this));

                // Use an activity context to get the rewarded video instance.
                mAd = MobileAds.getRewardedVideoAdInstance(this);
                mAd.setRewardedVideoAdListener(this);
                loadRewardedVideoAd();
            }
        }

        initializerView();
        mStatusPresenter = new StatusPresenter(this);
        mStatusPresenter.onCreate();
        setupToolbar();


    }

    private void showRewardedVideo() {
        if (mAd != null)
            if (mAd.isLoaded()) {
                mAd.show();
            }
    }

    private void loadRewardedVideoAd() {
        if (mAd != null)
            if (!mAd.isLoaded()) {
                mAd.loadAd(PreferenceManager.getInstance().getUnitVideoAdsID(this), new AdRequest.Builder().build());
            }


    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * method to initialize the view
     */
    public void initializerView() {
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mStatusAdapter = new StatusAdapter(this);
        StatusList.setLayoutManager(mLinearLayoutManager);
        StatusList.setAdapter(mStatusAdapter);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.editCurrentStatusBtn)
    void launchEditStatus(View v) {
        Intent mIntent = new Intent(this, EditStatusActivity.class);
        mIntent.putExtra("statusID", statusID);
        mIntent.putExtra("currentStatus", currentStatus.getText().toString().trim());
        startActivity(mIntent);
    }

    /**
     * method to show status list
     *
     * @param statusModels this is parameter for  ShowStatus   method
     */
    public void ShowStatus(List<StatusModel> statusModels) {
        mStatusAdapter.setStatus(statusModels);
        for (StatusModel statusModel : statusModels) {
            if (statusModel.isCurrent()) {
                ShowCurrentStatus(statusModel.getBody());
                break;
            }
        }
        mStatusPresenter.getCurrentStatus();
    }


    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(Pusher pusher) {
        mStatusPresenter.onEventPush(pusher);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.status_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                break;
            case R.id.deleteStatus:
                mStatusPresenter.DeleteAllStatus();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAd != null)
            mAd.destroy(this);
        mStatusPresenter.onDestroy();

    }

    /**
     * method to show the current status
     *
     * @param statusModel this is parameter for  ShowCurrentStatus   method
     */
    public void ShowCurrentStatus(String statusModel) {
        String status = UtilsString.unescapeJava(statusModel);
        currentStatus.setText(status);
    }

    /**
     * method to show the current status
     *
     * @param statusModel this is parameter for  ShowCurrentStatus   method
     */
    public void ShowCurrentStatus(StatusModel statusModel) {
        statusID = statusModel.getS_id();
        String status = UtilsString.unescapeJava(statusModel.getBody());
        currentStatus.setText(status);

    }

    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("status error" + throwable.getMessage());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAd != null)
            mAd.pause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAd != null)
            mAd.resume(this);
        mStatusPresenter.onResume();

    }


    public void deleteStatus(String statusID) {
        mStatusAdapter.DeleteStatusItem(statusID);
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        showRewardedVideo();
    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();// Preload the next video ad.

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

        loadRewardedVideoAd();// Preload the next video ad.
    }

}
