package com.sourcecanyon.whatsClone.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.database.dao.CallsDao;
import com.sourcecanyon.whatsClone.database.dao.CallsInfoDao;
import com.sourcecanyon.whatsClone.database.dao.ChatsDao;
import com.sourcecanyon.whatsClone.database.dao.GroupDao;
import com.sourcecanyon.whatsClone.database.dao.MembersDao;
import com.sourcecanyon.whatsClone.database.dao.MessageStatusDao;
import com.sourcecanyon.whatsClone.database.dao.MessagesDao;
import com.sourcecanyon.whatsClone.database.dao.StatusDao;
import com.sourcecanyon.whatsClone.database.dao.StoriesDao;
import com.sourcecanyon.whatsClone.database.dao.StoriesDetailsDao;
import com.sourcecanyon.whatsClone.database.dao.StoriesMineDao;
import com.sourcecanyon.whatsClone.database.dao.StoriesSeenDao;
import com.sourcecanyon.whatsClone.database.dao.UpDownDao;
import com.sourcecanyon.whatsClone.database.dao.UserDao;
import com.sourcecanyon.whatsClone.database.dao.UsersBlockedDao;
import com.sourcecanyon.whatsClone.database.dao.UsersPrivacyDao;
import com.sourcecanyon.whatsClone.models.calls.CallsInfoModel;
import com.sourcecanyon.whatsClone.models.calls.CallsModel;
import com.sourcecanyon.whatsClone.models.messages.ConversationModel;
import com.sourcecanyon.whatsClone.models.groups.GroupModel;
import com.sourcecanyon.whatsClone.models.groups.MembersModel;
import com.sourcecanyon.whatsClone.models.messages.MessageModel;
import com.sourcecanyon.whatsClone.models.messages.MessageStatus;
import com.sourcecanyon.whatsClone.models.users.status.StatusModel;
import com.sourcecanyon.whatsClone.models.stories.StoriesHeaderModel;
import com.sourcecanyon.whatsClone.models.stories.StoriesModel;
import com.sourcecanyon.whatsClone.models.stories.StoryModel;
import com.sourcecanyon.whatsClone.models.stories.StorySeen;
import com.sourcecanyon.whatsClone.models.UploadInfo;
import com.sourcecanyon.whatsClone.models.users.contacts.UsersBlockModel;
import com.sourcecanyon.whatsClone.models.users.contacts.UsersModel;
import com.sourcecanyon.whatsClone.models.users.contacts.UsersPrivacyModel;


/**
 * Created by Abderrahim El imame on 2019-07-26.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@Database(entities =
        {UsersModel.class,
                UsersBlockModel.class,
                StatusModel.class,
                ConversationModel.class,
                MessageModel.class,
                GroupModel.class,
                MembersModel.class,
                CallsModel.class,
                CallsInfoModel.class,
                UsersPrivacyModel.class,
                StoriesModel.class,
                StoriesHeaderModel.class,
                StoryModel.class,
                StorySeen.class,
                UploadInfo.class,
                MessageStatus.class
        }, version = AppConstants.DatabaseVersion, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = AppConstants.DatabaseName;
    private static volatile AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
                synchronized (AppDatabase.class) {
                    if (sInstance == null) {
                        sInstance = Room.databaseBuilder(context.getApplicationContext(),
                                AppDatabase.class, AppDatabase.DATABASE_NAME)
                                .fallbackToDestructiveMigration()
                                .build();
                    }
            }
        }
        return sInstance;
    }


    public static void destroyInstance() {
        sInstance = null;
    }


    public abstract UserDao userDao();


    public abstract CallsDao callsDao();


    public abstract CallsInfoDao callsInfoDao();


    public abstract UsersPrivacyDao usersPrivacyDao();


    public abstract StoriesDao storiesDao();


    public abstract StoriesMineDao storiesMineDao();

    public abstract StoriesDetailsDao storiesDetailsDao();

    public abstract StoriesSeenDao storiesSeenDao();

    public abstract UpDownDao upDownDao();


    public abstract UsersBlockedDao usersBlockedDao();


    public abstract StatusDao statusDao();


    public abstract ChatsDao chatsDao();


    public abstract MessagesDao messagesDao();


    public abstract MessageStatusDao messageStatusDao();


    public abstract GroupDao groupDao();

    public abstract MembersDao membersDao();
}