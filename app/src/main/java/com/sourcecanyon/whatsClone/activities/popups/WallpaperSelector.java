package com.sourcecanyon.whatsClone.activities.popups;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.images.PickerBuilder;
import com.sourcecanyon.whatsClone.animations.AnimationsUtil;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.helpers.Files.cache.ImageLoader;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.helpers.permissions.Permissions;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Abderrahim El imame on 1/15/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class WallpaperSelector extends Activity {


    @BindView(R.id.defaultBtnTxt)
    TextView defaultBtnTxt;
    @BindView(R.id.galleryBtnText)
    TextView galleryBtnText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_bottom_sheet_wallpaper);
        ButterKnife.bind(this);

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.defaultBtn)
    public void setDefaultBtn() {
        PreferenceManager.getInstance().setWallpaper(this, null);
        finish();

    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.galleryBtn)
    public void setGalleryBtn() {
        Permissions.with(WallpaperSelector.this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .ifNecessary()
                .withRationaleDialog(getString(R.string.app__requires_storage_permission_in_order_to_attach_media_information),
                        R.drawable.ic_folder_white_24dp)
                .withPermanentDenialDialog(getString(R.string.app__requires_storage_permission_in_order_to_attach_media_information))
                .onAllGranted(() -> {
                    new PickerBuilder(this, PickerBuilder.SELECT_FROM_GALLERY)
                            .setOnImageReceivedListener(imageUri -> {
                                Intent data = new Intent();
                                data.setData(imageUri);
                                String fileImagePath;
                                AppHelper.LogCat("new image SELECT_FROM_GALLERY" + imageUri);

                                fileImagePath = FilesManager.getPath(this.getApplicationContext(), data.getData());
                                File file;
                                if (fileImagePath != null) {
                                    file = new File(fileImagePath);

                                    //get filename from path
                                    String filename = fileImagePath.substring(fileImagePath.lastIndexOf("/") + 1);
                                    //remove extension
                                    if (filename.indexOf(".") > 0)
                                        filename = filename.substring(0, filename.lastIndexOf("."));

                                    PreferenceManager.getInstance().setWallpaper(this, filename);
                                    ImageLoader.DownloadOfflineImage(file, filename, this, PreferenceManager.getInstance().getID(this), AppConstants.USER, AppConstants.ROW_WALLPAPER);
                                    AppHelper.CustomToast(this, getString(R.string.wallpaper_is_set));
                                }
                                finish();

                            })
                            .setImageName(this.getString(R.string.app_name))
                            .setImageFolderName(this.getString(R.string.app_name))
                            .setCropScreenColor(R.color.colorPrimary)
                            .withTimeStamp(false)
                            .setOnPermissionRefusedListener(() -> {
                                //PermissionHandler.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                            })
                            .start();
                })
                .onAnyDenied(() -> {
                    Toast.makeText(WallpaperSelector.this, getString(R.string.all_permission_required), Toast.LENGTH_LONG).show();

                })
                .execute();

    }
}
