package com.sourcecanyon.whatsClone.activities.settings;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.BaseActivity;
import com.sourcecanyon.whatsClone.activities.profile.EditProfileActivity;
import com.sourcecanyon.whatsClone.animations.AnimationsUtil;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.models.users.contacts.UsersModel;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.RateHelper;
import com.sourcecanyon.whatsClone.helpers.phone.UtilsPhone;
import com.sourcecanyon.whatsClone.helpers.UtilsString;
import com.sourcecanyon.whatsClone.helpers.glide.GlideApp;
import com.sourcecanyon.whatsClone.helpers.glide.GlideUrlHeaders;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.presenters.users.SettingsPresenter;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Abderrahim El imame on 27/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SettingsActivity extends BaseActivity {


    @BindView(R.id.userAvatar)
    AppCompatImageView userAvatar;

    @BindView(R.id.user_status)
    EmojiTextView userStatus;

    @BindView(R.id.settingsHead)
    AppCompatImageView settingsHead;

    @BindView(R.id.userName)
    TextView userName;

    @BindView(R.id.chats_settings_text)
    TextView chats;

    @BindView(R.id.account_settings_text)
    TextView account;

    @BindView(R.id.notifications_settings_text)
    TextView notifications;

    @BindView(R.id.lang_app_text)
    TextView lang_app_text;

    @BindView(R.id.mainSettings)
    NestedScrollView mView;
    private UsersModel mContactsModel;
    private SettingsPresenter mSettingsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setupToolbar();

        mSettingsPresenter = new SettingsPresenter(this);
        mSettingsPresenter.onCreate();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSettingsPresenter.onPause();
    }


    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.settingsHead)
    public void launchEditProfile(View v) {
        RateHelper.significantEvent(this);
        if (AppHelper.isAndroid5()) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    new Pair<>(userAvatar, "userAvatar"),
                    new Pair<>(userName, "userName"),
                    new Pair<>(userStatus, "userStatus"),
                    new Pair<>(settingsHead, "settingsHead")
            );
            Intent mIntent = new Intent(this, EditProfileActivity.class);
            startActivity(mIntent, options.toBundle());
        } else {
            AppHelper.LaunchActivity(this, EditProfileActivity.class);
        }
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.chats_settings)
    public void launchChatsSettings() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, ChatsSettingsActivity.class);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.account_settings)
    public void launchAccountSettings() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, AccountSettingsActivity.class);
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.notifications_settings)
    public void launchNotificationSettings() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, NotificationsSettingsActivity.class);
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.lang_app)
    public void launchLanguageActivity() {
        AppHelper.LaunchActivity(this, PreferenceLanguageActivity.class);
    }

    @SuppressLint("StaticFieldLeak")
    public void ShowContact(UsersModel contactsModels) {
        mContactsModel = contactsModels;
        try {

            String finalName = null;

            if (mContactsModel.getStatus() != null) {
                String status = UtilsString.unescapeJava(mContactsModel.getStatus().getBody());
                userStatus.setText(status);
            } else {
                userStatus.setText(getString(R.string.no_status));
            }
            if (mContactsModel.getUsername() != null) {
                userName.setText(mContactsModel.getUsername());
                finalName = mContactsModel.getUsername();
            } else {
                userName.setText(getString(R.string.no_username));
                String name = UtilsPhone.getContactName(mContactsModel.getPhone());
                if (name != null) {
                    finalName = name;
                } else {
                    finalName = mContactsModel.getPhone();
                }
            }
            Drawable drawable = AppHelper.getDrawable(this, R.drawable.holder_user);

            String ImageUrl = mContactsModel.getImage();
            String recipientId = mContactsModel.get_id();

            if (ImageUrl != null) {

                DrawableImageViewTarget target = new DrawableImageViewTarget(userAvatar) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        userAvatar.setImageDrawable(resource);

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        userAvatar.setImageDrawable(errorDrawable);
                    }


                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        userAvatar.setImageDrawable(placeHolderDrawable);
                    }
                };
                GlideApp.with(SettingsActivity.this)
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + recipientId + "/" + ImageUrl))
                        .signature(new ObjectKey(ImageUrl))
                        .centerCrop()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(drawable)
                        .error(drawable)
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(target);

            } else {
                userAvatar.setImageDrawable(drawable);
            }
        } catch (Exception e) {
            AppHelper.LogCat("" + e);
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

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
        mSettingsPresenter.onDestroy();
        EventBus.getDefault().unregister(this);

    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_USERNAME_PROFILE_UPDATED:
            case AppConstants.EVENT_BUS_UPDATE_CURRENT_SATUS:
            case AppConstants.EVENT_BUS_MINE_IMAGE_PROFILE_UPDATED:
                mSettingsPresenter.getContactLocal();
                break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

}
