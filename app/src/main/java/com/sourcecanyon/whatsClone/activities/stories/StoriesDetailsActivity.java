package com.sourcecanyon.whatsClone.activities.stories;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.BaseActivity;
import com.sourcecanyon.whatsClone.adapters.others.StoriesPagerAdapter;
import com.sourcecanyon.whatsClone.models.stories.StoriesModel;
import com.sourcecanyon.whatsClone.helpers.PreferenceManager;
import com.sourcecanyon.whatsClone.presenters.controllers.StoriesController;
import com.sourcecanyon.whatsClone.ui.stories.CustomViewPager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

/**
 * Created by Abderrahim El imame on 7/10/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesDetailsActivity extends BaseActivity {


    @BindView(R.id.viewpager)
    CustomViewPager pager;

    private final String KEY_SELECTED_PAGE = "KEY_SELECTED_PAGE";
    int selectedPage = 0;
    int currentStoryPosition = 0;

    public List<StoriesModel> storyModels;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(FLAG_TRANSLUCENT_NAVIGATION, FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedPage = savedInstanceState.getInt(KEY_SELECTED_PAGE);
        }

        setContentView(R.layout.activity_story_details);
        ButterKnife.bind(this);
        pager = findViewById(R.id.viewpager);
        selectedPage = getIntent().getIntExtra("position", 0);
        if (getIntent().hasExtra("currentStoryPosition"))
            currentStoryPosition = getIntent().getIntExtra("currentStoryPosition", 0);
        String storyId = getIntent().getStringExtra("storyId");

        StoriesPagerAdapter mAdapter;
        if (storyId.equals(PreferenceManager.getInstance().getID(this))) {
            mAdapter = new StoriesPagerAdapter(getSupportFragmentManager(), 1, storyId, currentStoryPosition);
        } else {
            storyModels = StoriesController.getInstance().getAllStoriesModelList();
            mAdapter = new StoriesPagerAdapter(getSupportFragmentManager(), storyModels.size(), storyId);

        }


        pager.setAdapter(mAdapter);
        /// pager.setPageTransformer(false, new CubeOutTransformer());

        pager.setCurrentItem(selectedPage);
        pager.setOffscreenPageLimit(0);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_PAGE, pager.getCurrentItem());
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(FLAG_TRANSLUCENT_NAVIGATION, FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
