package com.sourcecanyon.whatsClone.adapters.recyclerView.stories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.UtilsString;
import com.sourcecanyon.whatsClone.helpers.glide.GlideApp;
import com.sourcecanyon.whatsClone.helpers.glide.GlideUrlHeaders;
import com.sourcecanyon.whatsClone.helpers.phone.UtilsPhone;
import com.sourcecanyon.whatsClone.models.stories.StorySeen;
import com.sourcecanyon.whatsClone.models.users.contacts.UsersModel;
import com.sourcecanyon.whatsClone.presenters.controllers.UsersController;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

;

/**
 * Created by Abderrahim El imame on 12/31/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class SeenListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<StorySeen> usersModels;
    private LayoutInflater mInflater;


    public SeenListAdapter(Activity mActivity) {
        this.mActivity = mActivity;
        this.usersModels = new ArrayList<>();
        mInflater = LayoutInflater.from(mActivity);

    }


    public void setUsersList(List<StorySeen> usersModels) {
        this.usersModels = usersModels;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_seen_list, parent, false);
        return new UsersViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final UsersViewHolder usersViewHolder = (UsersViewHolder) holder;
        final StorySeen storySeen = this.usersModels.get(position);
        final UsersModel usersModel = UsersController.getInstance().getUserById(storySeen.getUserId());
        try {
            String username;
            String name = UtilsPhone.getContactName(usersModel.getPhone());
            if (name != null) {
                username = name;
            } else {
                username = usersModel.getPhone();
            }


            usersViewHolder.setUsername(username);
            if (usersModel.getStatus() != null) {
                usersViewHolder.setStatus(usersModel.getStatus().getBody());
            }

            usersViewHolder.setUserImage(usersModel.getImage(), usersModel.get_id());

        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        }


    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getItemCount() {
        if (usersModels != null) {
            return usersModels.size();
        } else {
            return 0;
        }
    }


    class UsersViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_image)
        AppCompatImageView userImage;

        @BindView(R.id.username)
        TextView username;

        @BindView(R.id.status)
        EmojiTextView status;

        UsersViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            status.setSelected(true);

        }


        @SuppressLint("StaticFieldLeak")
        void setUserImage(String ImageUrl, String recipientId) {
            Drawable drawable = AppHelper.getDrawable(mActivity, R.drawable.holder_user);
            if (ImageUrl != null) {


                DrawableImageViewTarget target = new DrawableImageViewTarget(userImage) {


                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        userImage.setImageDrawable(resource);
                    }


                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        userImage.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        userImage.setImageDrawable(placeHolderDrawable);
                    }
                };
                GlideApp.with(mActivity.getApplicationContext())
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + recipientId + "/" + ImageUrl))
                        .signature(new ObjectKey(ImageUrl))
                        .centerCrop().apply(RequestOptions.circleCropTransform())
                        .placeholder(drawable)
                        .error(drawable)
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(target);
            } else {
                userImage.setImageDrawable(drawable);
            }
        }

        void setUsername(String phone) {
            username.setText(phone);
        }

        void setStatus(String Status) {
            String finalStatus = UtilsString.unescapeJava(Status);
            status.setText(finalStatus);
        }
    }


}