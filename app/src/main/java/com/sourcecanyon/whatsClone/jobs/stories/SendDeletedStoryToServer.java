package com.sourcecanyon.whatsClone.jobs.stories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;
import com.sourcecanyon.whatsClone.api.APIHelper;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.database.AppExecutors;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.UtilsTime;
import com.sourcecanyon.whatsClone.models.stories.StoryModel;
import com.sourcecanyon.whatsClone.models.users.Pusher;
import com.sourcecanyon.whatsClone.presenters.controllers.StoriesController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

import static com.sourcecanyon.whatsClone.app.AppConstants.EVENT_BUS_DELETE_STORIES_ITEM;

/**
 * Created by Abderrahim El imame on 5/8/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class SendDeletedStoryToServer extends ListenableWorker {

    public static final String TAG = SendDeletedStoryToServer.class.getSimpleName();


    private CompositeDisposable compositeDisposable;
    private int mPendingMessages = 0;
    private SettableFuture<Result> mFuture;


    public SendDeletedStoryToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        mFuture = SettableFuture.create();
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            compositeDisposable = new CompositeDisposable();

            AppExecutors.getInstance().diskIO().execute(() -> {

                try {
                    DateTime expire_date = new DateTime();

                    // minus a day
                    expire_date = expire_date.minusDays(1);
                    AppHelper.LogCat("Job deleteStories: " + expire_date.toString());
                    List<StoryModel> storyModels = StoriesController.getInstance().getAllExpiredStories(expire_date.toString());

                    mPendingMessages = storyModels.size();
                    AppHelper.LogCat("Job deleteStories: " + mPendingMessages);


                    if (storyModels.size() != 0) {

                        for (StoryModel storyModel : storyModels) {
                            AppHelper.LogCat("should delete Stories: " + storyModel.getDate());
                            if (expire_date.isAfter(UtilsTime.getCorrectDate(storyModel.getDate()))) {
                                AppHelper.LogCat("should delete Stories: " + expire_date);
                                String storyId = storyModel.get_id();
                                String ownerId = storyModel.getUserId();
                                if (ownerId.equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))) {

                                    compositeDisposable.add(APIHelper.initialApiUsersContacts().deleteStory(storyId).subscribe(statusResponse -> {
                                        if (statusResponse.isSuccess()) {


                                            //set story as deleted

                                            StoryModel storyModel2 = StoriesController.getInstance().getStoryById(storyId);
                                            storyModel2.setDeleted(true);
                                            StoriesController.getInstance().updateStoryModel(storyModel2);

                                            List<StoryModel> storyModels1 = StoriesController.getInstance().getAllStoryNotDeleted(ownerId);

                                            if (storyModels1.size() == 0) {
                                                AppHelper.LogCat("stories deleted successfully  ");
                                                EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, ownerId));
                                            } else {
                                                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, ownerId));
                                            }


                                            JSONObject updateMessage = new JSONObject();
                                            try {
                                                updateMessage.put("storyId", storyId);
                                                updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                                                //emit by mqtt to other user
                                                try {

                                                    WhatsCloneApplication.getInstance().getMqttClientManager().publishStory(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED, updateMessage);
                                                } catch (MqttException e) {
                                                    e.printStackTrace();
                                                }
                                            } catch (JSONException e) {
                                                // e.printStackTrace();
                                            }


                                            mPendingMessages--;


                                        } else {

                                            AppHelper.LogCat("delete story failed  " + statusResponse.getMessage());

                                        }


                                    }, throwable -> {

                                        AppHelper.LogCat("delete story failed  " + throwable.getMessage());
                                    }));
                                } else {


                                    //set story as deleted

                                    StoryModel storyModel2 = StoriesController.getInstance().getStoryById(storyId);
                                    storyModel2.setDeleted(true);
                                    StoriesController.getInstance().updateStoryModel(storyModel2);


                                    List<StoryModel> storyModels1 = StoriesController.getInstance().getAllStoryNotDeleted(ownerId);
                                    if (storyModels1.size() == 0) {
                                        EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, ownerId));
                                    } else {
                                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, ownerId));
                                    }
                                    AppHelper.LogCat("stories deleted successfully  ");


                                    JSONObject updateMessage = new JSONObject();
                                    try {
                                        updateMessage.put("storyId", storyId);
                                        updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                                        //emit by mqtt to other user
                                        try {

                                            WhatsCloneApplication.getInstance().getMqttClientManager().publishStory(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED, updateMessage);
                                        } catch (MqttException e) {
                                            e.printStackTrace();
                                        }
                                    } catch (JSONException e) {
                                        // e.printStackTrace();
                                    }
                                    mPendingMessages--;


                                }


                            }
                        }


                        if (mPendingMessages != 0)
                            mFuture.set(Result.retry());
                        else
                            mFuture.set(Result.success());
                    } else {
                        mFuture.set(Result.failure());
                    }
                } catch (Throwable throwable) {
                    mFuture.setException(throwable);
                }
            });

        } else {
            mFuture.set(Result.failure());
        }
        return mFuture;
    }


    @Override
    public void onStopped() {
        super.onStopped();

        mPendingMessages = 0;
        if (compositeDisposable != null) compositeDisposable.dispose();
    }


}
