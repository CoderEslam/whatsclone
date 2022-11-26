package com.sourcecanyon.whatsClone.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.danikula.videocache.HttpProxyCacheServer;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.sourcecanyon.whatsClone.BuildConfig;
import com.sourcecanyon.whatsClone.database.AppDatabase;
import com.sourcecanyon.whatsClone.database.AppExecutors;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.ExceptionHandler;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.helpers.ForegroundRuning;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.UserAgentHeadersInjector;
import com.sourcecanyon.whatsClone.helpers.glide.GlideApp;
import com.sourcecanyon.whatsClone.jobs.WorkJobsManager;
import com.sourcecanyon.whatsClone.jobs.mqtt.MqttClientManager;
import com.sourcecanyon.whatsClone.jobs.mqtt.MqttMessageService;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class WhatsCloneApplication extends MultiDexApplication implements ServiceConnection {
    public static volatile Handler applicationHandler = null;
    static WhatsCloneApplication mInstance;

    private HttpProxyCacheServer proxy;


    private MqttClientManager mqttClientManager;
    private MqttAndroidClient mqttAndroidClient;


    public static synchronized WhatsCloneApplication getInstance() {
        return mInstance;
    }

    public void setmInstance(WhatsCloneApplication mInstance) {
        WhatsCloneApplication.mInstance = mInstance;
    }

    public void preInitMqtt() {

        if (AppHelper.isAndroid8()) {

            Intent intent = new Intent(getApplicationContext(), MqttMessageService.class);
            bindService(intent, this, Context.BIND_AUTO_CREATE);
        } else {

            Intent service = new Intent(getApplicationContext(), MqttMessageService.class);
            startService(service);
        }
        mqttClientManager = new MqttClientManager();
        mqttAndroidClient = mqttClientManager.initializeMqttClient(getApplicationContext(), BuildConfig.MQTT_BROKER_URL, AppConstants.CLIENT_ID);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        setmInstance(this);


        if (AppConstants.CRASH_LYTICS)
            WhatsCloneApplication.setupCrashlytics();
        if (PreferenceManager.getInstance().getToken(this) != null) {
            preInitMqtt();


        }

        if (AppConstants.DEBUGGING_MODE) {
            PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                    //    .methodCount(0)         // (Optional) How many method line to show. Default 2
                    //.methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                    //  .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                    .tag(AppConstants.TAG)   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                    .build();
            Logger.addLogAdapter(new AndroidLogAdapter() {
                @Override
                public boolean isLoggable(int priority, String tag) {
                    return true;
                }
            });
            //  strictMode();
        }
        applicationHandler = new Handler(getApplicationContext().getMainLooper());

        ForegroundRuning.init(this);


        ForegroundRuning.get().addListener(new ForegroundRuning.Listener() {
            @Override
            public void onBecameForeground() {
                AppHelper.LogCat("onBecameForeground ");
                initializerApplication();
            }

            @Override
            public void onBecameBackground() {
                AppHelper.LogCat("onBecameBackground ");
                WorkJobsManager.getInstance().cancelAllJob();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                initializerApplication();
            }
        });


        if (AppConstants.ENABLE_CRASH_HANDLER)
            Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        RxJavaPlugins.setErrorHandler(throwable -> {
            AppHelper.LogCat("throwable " + throwable.getMessage());
            AppHelper.LogCat(throwable);
        });

        if (!PreferenceManager.getInstance().getLanguage(this).equals(""))
            setDefaultLocale(this, new Locale(PreferenceManager.getInstance().getLanguage(this)));
        else {
            if (Locale.getDefault().toString().startsWith("en_")) {
                PreferenceManager.getInstance().setLanguage(this, "en");
            }
        }
        EmojiManager.install(new IosEmojiProvider());

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);


    }

    public MqttClientManager getMqttClientManager() {
        return mqttClientManager;
    }

    public MqttAndroidClient getClient() {
        return mqttAndroidClient;
    }

    public static void setupCrashlytics() {
        try {

            Crashlytics crashlyticsKit = new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder()
                            .disabled(AppConstants.DEBUGGING_MODE)
                            .build())
                    .build();
            Fabric.with(mInstance, crashlyticsKit, new Crashlytics());
            Crashlytics.setUserEmail(PreferenceManager.getInstance().getMobileNumber(getInstance()));
            Crashlytics.setUserName(PreferenceManager.getInstance().getMobileNumber(getInstance()));
            Crashlytics.setUserIdentifier(PreferenceManager.getInstance().getID(getInstance()));
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        }

    }

    protected void strictMode() {
        /**
         * Doesn't enable anything on the main thread that related
         * to resource access.
         */
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskWrites()
                .permitDiskReads()
                .detectNetwork()
                .detectCustomSlowCalls()
                .penaltyLog()
                .penaltyFlashScreen()
                .penaltyDeath()
                .build());

        /**
         * Doesn't enable any leakage of the application's components.
         */
        final StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.detectLeakedRegistrationObjects();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }
        builder.detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
        ;
        StrictMode.setVmPolicy(builder.build());
    }


    @SuppressWarnings("deprecation")
    protected void setDefaultLocale(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration appConfig = new Configuration();
        appConfig.locale = locale;
        context.getResources().updateConfiguration(appConfig, context.getResources().getDisplayMetrics());

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void initializerApplication() {

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null && !PreferenceManager.getInstance().isNeedProvideInfo(this)) {


            WorkJobsManager.getInstance().initializerApplicationService();
            WorkJobsManager.getInstance().syncingContactsWithServerWorker();
            WorkJobsManager.getInstance().sendUserMessagesToServer();
            WorkJobsManager.getInstance().sendUserStoriesToServer();
            WorkJobsManager.getInstance().sendDeliveredStatusToServer();
            WorkJobsManager.getInstance().sendDeliveredGroupStatusToServer();
            WorkJobsManager.getInstance().sendDeletedStoryToServer();


        }

    }


    public void DeleteDatabaseInstance() {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(getInstance()).clearAllTables());
    }


    public static HttpProxyCacheServer getProxy(Context context) {
        WhatsCloneApplication app = (WhatsCloneApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .maxCacheSize(1024 * 1024 * 1024)       // 1 Gb for video cache
                .headerInjector(new UserAgentHeadersInjector())
                .cacheDirectory(FilesManager.getFileDataCached(getInstance(), "cache"))
                .build();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        GlideApp.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        GlideApp.get(this).trimMemory(level);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
