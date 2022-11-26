package com.sourcecanyon.whatsClone.api;

import com.sourcecanyon.whatsClone.BuildConfig;
import com.sourcecanyon.whatsClone.api.apiServices.AuthService;
import com.sourcecanyon.whatsClone.api.apiServices.ConversationsService;
import com.sourcecanyon.whatsClone.api.apiServices.GroupsService;
import com.sourcecanyon.whatsClone.api.apiServices.MessagesService;
import com.sourcecanyon.whatsClone.api.apiServices.UsersService;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.app.WhatsCloneApplication;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.giph.model.GiphyResponse;
import com.sourcecanyon.whatsClone.helpers.giph.net.GiphyLoader;
import com.sourcecanyon.whatsClone.jobs.utils.DownloadProgressResponseBody;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 4/11/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class APIHelper {

    public static UsersService initialApiUsersContacts() {
        APIService mApiService = APIService.with(WhatsCloneApplication.getInstance());
        return new UsersService(WhatsCloneApplication.getInstance(), mApiService);
    }


    public static FilesUploadService initializeUploadFiles() {
        APIService mApiService = APIService.with(WhatsCloneApplication.getInstance());
        return mApiService.UploadService(FilesUploadService.class, BuildConfig.BACKEND_BASE_URL);
    }


    public static FilesDownloadService initializeDownloadFiles(DownloadProgressResponseBody.DownloadProgressListener listener) {
        APIService mApiService = APIService.with(WhatsCloneApplication.getInstance());
        return mApiService.DownloadService(FilesDownloadService.class, BuildConfig.BACKEND_BASE_URL, listener);


    }


    public static GroupsService initializeApiGroups() {
        APIService mApiService = APIService.with(WhatsCloneApplication.getInstance());
        return new GroupsService(WhatsCloneApplication.getInstance(), mApiService);
    }

    public static ConversationsService initializeConversationsService() {
        APIService mApiService = APIService.with(WhatsCloneApplication.getInstance());
        return new ConversationsService(mApiService);
    }

    public static MessagesService initializeMessagesService() {
        return new MessagesService();
    }

    public static AuthService initializeAuthService() {
        APIService mApiService = APIService.with(WhatsCloneApplication.getInstance());
        return new AuthService(WhatsCloneApplication.getInstance(), mApiService);
    }

    public static Observable<GiphyResponse> getGiphy(int offset, String query) {
        APIService apiService = new APIService(WhatsCloneApplication.getInstance());
        APIContact apiUsers = apiService.ApiService(APIContact.class, EndPoints.GET_GIF_BASE);
        return apiUsers.getGiphy(PreferenceManager.getInstance().getGiphyKey(WhatsCloneApplication.getInstance()), offset, GiphyLoader.PAGE_SIZE, query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<GiphyResponse> getGiphy(int offset) {
        APIService apiService = new APIService(WhatsCloneApplication.getInstance());
        APIContact apiUsers = apiService.ApiService(APIContact.class, EndPoints.GET_GIF_BASE);
        return apiUsers.getGiphy(PreferenceManager.getInstance().getGiphyKey(WhatsCloneApplication.getInstance()), offset, GiphyLoader.PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
