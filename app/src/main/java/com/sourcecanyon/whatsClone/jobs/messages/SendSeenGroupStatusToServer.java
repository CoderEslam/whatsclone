package com.sourcecanyon.whatsClone.jobs.messages;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.database.AppExecutors;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.models.messages.MessageModel;
import com.sourcecanyon.whatsClone.presenters.controllers.MessagesController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Abderrahim El imame on 5/8/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class SendSeenGroupStatusToServer extends ListenableWorker {

    public static final String TAG = SendSeenGroupStatusToServer.class.getSimpleName();

    private SettableFuture<Result> mFuture;
    private int mPendingMessages = 0;

    public SendSeenGroupStatusToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        AppHelper.LogCat("InitJob: " + "InitJob");
     }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        mFuture = SettableFuture.create();
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        String groupId = getInputData().getString("groupId");
        String conversationId = getInputData().getString("conversationId");

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                try {


                    List<MessageModel> messageModelList = MessagesController.getInstance().getGroupDeliveredMessages(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()), groupId);

                    mPendingMessages = messageModelList.size();
                    AppHelper.LogCat("size messagesModels  " + messageModelList.size());

                    if (messageModelList.size() != 0) {

                        for (MessageModel messagesModel1 : messageModelList) {
                            String messageId = messagesModel1.get_id();
                            String ownerId = messagesModel1.getSenderId();
                            String recipientId = PreferenceManager.getInstance().getID(getApplicationContext());



                            JSONObject updateMessage = new JSONObject();
                            try {
                                updateMessage.put("messageId", messageId);
                                updateMessage.put("ownerId", ownerId);
                                updateMessage.put("recipientId", recipientId);
                                updateMessage.put("groupId", groupId);
                                updateMessage.put("is_group", true);

                            } catch (JSONException e) {
                                // e.printStackTrace();
                            }
                            try {

                                WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsSeen(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage);


                                AppHelper.LogCat("--> Recipient mark message as  seen <--");
                                mPendingMessages--;

                            } catch (MqttException e) {
                                e.printStackTrace();
                                AppHelper.LogCat(" SendSeenStatusToServer MainService " + e.getMessage());
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


}
