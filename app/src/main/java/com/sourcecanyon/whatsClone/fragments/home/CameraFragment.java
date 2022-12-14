package com.sourcecanyon.whatsClone.fragments.home;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.sourcecanyon.whatsClone.R;
import com.sourcecanyon.whatsClone.activities.main.MainActivity;
import com.sourcecanyon.whatsClone.activities.media.ImageEditActivity;
import com.sourcecanyon.whatsClone.activities.media.VideoEditorActivity;
import com.sourcecanyon.whatsClone.adapters.recyclerView.picker.MediaFilesAdapter;
import com.sourcecanyon.whatsClone.adapters.recyclerView.picker.RecentMediaAdapter;
import com.sourcecanyon.whatsClone.app.AppConstants;
import com.sourcecanyon.whatsClone.database.AppExecutors;
import com.sourcecanyon.whatsClone.helpers.AppHelper;
import com.sourcecanyon.whatsClone.helpers.Files.FilesManager;
import com.sourcecanyon.whatsClone.helpers.permissions.Permissions;
import com.sourcecanyon.whatsClone.helpers.picker.Constants;
import com.sourcecanyon.whatsClone.helpers.picker.HeaderItemDecoration;
import com.sourcecanyon.whatsClone.helpers.picker.MediaFetcher;
import com.sourcecanyon.whatsClone.helpers.picker.PickerHelper;
import com.sourcecanyon.whatsClone.interfaces.picker.FastScrollStateChangeListener;
import com.sourcecanyon.whatsClone.interfaces.picker.OnSelectionListener;
import com.sourcecanyon.whatsClone.models.MediaPicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import butterknife.ButterKnife;

;


/**
 * Created by Abderrahim El imame on 2/11/19.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class CameraFragment extends Fragment implements View.OnTouchListener, SurfaceHolder.Callback {

    private static final int sBubbleAnimDuration = 1000;
    private static final int sScrollbarHideDelay = 1000;
    private static final String SELECTION = "selection";
    private static final String REQUEST_CODE = "requestCode";
    private static final int sTrackSnapRange = 5;
    public static float TOP_BAR_HEIGHT;
    private int colorPrimaryDark;

    //camera
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Handler customHandler = new Handler();
    private int flag = 0;
    private File tempFile = null;
    private Camera.PictureCallback jpegCallback;
    private int MAX_VIDEO_SIZE_UPLOAD = 100; //MB
    private File folder = null;
    private float zoom = 0.0f;
    private float dist = 0.0f;
    private AppCompatImageView flashChildAt;
    private MediaRecorder mediaRecorder;
    private OrientationEventListener myOrientationEventListener;
    private int iOrientation = 0;
    private int mOrientation = 90;
    private TextView textCounter;
    private TextView hintTextView;
    private SurfaceView mCamera;
    ///camera
    private Handler handler = new Handler();
    private FastScrollStateChangeListener mFastScrollStateChangeListener;


    private RecyclerView mediaPickerRecyclerView, recentMediaRecyclerView;
    private BottomSheetBehavior mBottomSheetBehavior;
    private RecentMediaAdapter recentMediaAdapter;
    private GridLayoutManager mLayoutManager;
    private View status_bar_bg, mScrollbar, topbar, mainFrameLayout, bottomButtons, sendButton;
    private TextView mBubbleView, selection_ok, img_count;
    private AppCompatImageView mHandleView, capture_btn, selection_back, selection_check;
    private ViewPropertyAnimator mScrollbarAnimator;
    private ViewPropertyAnimator mBubbleAnimator;
    private Set<MediaPicker> selectionList = new HashSet<>();
    private Runnable mScrollbarHider = this::hideScrollbar;
    private MediaFilesAdapter mediaFilesAdapter;
    private float mViewHeight;
    private boolean mHideScrollbar = true;
    private boolean cameraVisible = false;
    private boolean LongSelection = false;
    private int SelectionCount = 1;
    private boolean forStory = false;
    private View mView;

    private boolean isFullScreen = false;

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (!mHandleView.isSelected() && recyclerView.isEnabled()) {
                setViewPositions(getScrollProportion(recyclerView));
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (recyclerView.isEnabled()) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        handler.removeCallbacks(mScrollbarHider);
                        PickerHelper.cancelAnimation(mScrollbarAnimator);
                        if (!PickerHelper.isViewVisible(mScrollbar) && (recyclerView.computeVerticalScrollRange() - mViewHeight > 0)) {
                            mScrollbarAnimator = PickerHelper.showScrollbar(mScrollbar, getActivity());
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if (mHideScrollbar && !mHandleView.isSelected()) {
                            handler.postDelayed(mScrollbarHider, sScrollbarHideDelay);
                        }
                        break;
                }
            }
        }
    };
    private TextView selection_count;
    private OnSelectionListener onSelectionListener = new OnSelectionListener() {
        @Override
        public void OnClick(MediaPicker mediaPicker, View view, int position) {
            //Log.e("OnClick", "OnClick");
            if (LongSelection) {
                if (selectionList.contains(mediaPicker)) {
                    selectionList.remove(mediaPicker);
                    recentMediaAdapter.select(false, position);
                    mediaFilesAdapter.select(false, position);
                } else {
                    if (SelectionCount <= selectionList.size()) {
                        Toast.makeText(getActivity(), String.format(getResources().getString(R.string.selection_limiter), selectionList.size()), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mediaPicker.setPosition(position);
                    selectionList.clear();//i have add because i want just to add one file
                    selectionList.add(mediaPicker);
                    recentMediaAdapter.select(true, position);
                    mediaFilesAdapter.select(true, position);
                }
                if (selectionList.size() == 0) {
                    LongSelection = false;
                    selection_check.setVisibility(View.VISIBLE);
                    DrawableCompat.setTint(selection_back.getDrawable(), colorPrimaryDark);
                    topbar.setBackgroundColor(Color.parseColor("#ffffff"));
                    Animation anim = new ScaleAnimation(
                            1f, 0f, // Start and end values for the X axis scaling
                            1f, 0f, // Start and end values for the Y axis scaling
                            Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                            Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
                    anim.setFillAfter(true); // Needed to keep the result of the animation
                    anim.setDuration(300);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            sendButton.setVisibility(View.GONE);
                            sendButton.clearAnimation();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    sendButton.startAnimation(anim);
                }
                selection_count.setText(String.format(Locale.getDefault(), "%s%d", getString(R.string.media_selected), selectionList.size()));
                img_count.setText(String.valueOf(selectionList.size()));
            } else {
                mediaPicker.setPosition(position);
                selectionList.clear();
                selectionList.add(mediaPicker);
                getResults();
                DrawableCompat.setTint(selection_back.getDrawable(), colorPrimaryDark);
                topbar.setBackgroundColor(Color.parseColor("#ffffff"));
            }
        }

        @Override
        public void OnLongClick(MediaPicker mediaPicker, View view, int position) {
            if (SelectionCount > 1) {
                PickerHelper.vibe(getActivity(), 50);
                //Log.e("OnLongClick", "OnLongClick");
                LongSelection = true;
                if (selectionList.size() == 0) {
                    if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                        sendButton.setVisibility(View.VISIBLE);
                        Animation anim = new ScaleAnimation(
                                0f, 1f, // Start and end values for the X axis scaling
                                0f, 1f, // Start and end values for the Y axis scaling
                                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
                        anim.setFillAfter(true); // Needed to keep the result of the animation
                        anim.setDuration(300);
                        sendButton.startAnimation(anim);
                    }
                }
                if (selectionList.contains(mediaPicker)) {
                    selectionList.remove(mediaPicker);
                    recentMediaAdapter.select(false, position);
                    mediaFilesAdapter.select(false, position);
                } else {
                    mediaPicker.setPosition(position);
                    selectionList.clear();
                    selectionList.add(mediaPicker);
                    recentMediaAdapter.select(true, position);
                    mediaFilesAdapter.select(true, position);
                }
                selection_check.setVisibility(View.GONE);
                topbar.setBackgroundColor(colorPrimaryDark);
                selection_count.setText(String.format(Locale.getDefault(), "%s%d", getString(R.string.media_selected), selectionList.size()));
                img_count.setText(String.valueOf(selectionList.size()));
                DrawableCompat.setTint(selection_back.getDrawable(), Color.parseColor("#ffffff"));
            }

        }
    };
    private FrameLayout flash;
    private AppCompatImageView front;


    public static CameraFragment newInstance() {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putInt(SELECTION, 1);
        args.putString("tag", CameraFragment.class.getSimpleName());
        args.putInt(REQUEST_CODE, AppConstants.PICK_CAMERA_GALLERY_STORY);
        args.putBoolean(AppConstants.MediaConstants.EXTRA_FOR_STORY, true);
        cameraFragment.setArguments(args);
        return cameraFragment;
    }

    private void hideScrollbar() {
        float transX = getResources().getDimensionPixelSize(R.dimen.fastscroll_scrollbar_padding_end);
        mScrollbarAnimator = mScrollbar.animate().translationX(transX).alpha(0f)
                .setDuration(Constants.sScrollbarAnimDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mScrollbar.setVisibility(View.GONE);
                        mScrollbarAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        mScrollbar.setVisibility(View.GONE);
                        mScrollbarAnimator = null;
                    }
                });
    }

    public void getResults() {
        List<String> list = new ArrayList<>();
        for (MediaPicker i : selectionList) {
            list.add(i.getUrl());
            // Log.e("PickerActivity images", "img " + i.getUrl());
        }

        String mimeType = FilesManager.getMimeType(getContext(), Uri.parse(list.get(0)));
        if (FilesManager.isVideo(mimeType)) {
            Intent intent = new Intent(getContext(), VideoEditorActivity.class);
            intent.putExtra(AppConstants.MediaConstants.EXTRA_VIDEO_PATH, list.get(0));
            intent.putExtra(AppConstants.MediaConstants.EXTRA_FOR_STORY, forStory);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
        } else if (FilesManager.isImageType(mimeType)) {

            Intent intent = new Intent(getContext(), ImageEditActivity.class);
            intent.putExtra(AppConstants.MediaConstants.EXTRA_IMAGE_PATH, list.get(0));
            intent.putExtra(AppConstants.MediaConstants.EXTRA_FOR_STORY, forStory);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
        }

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        mView = inflater.inflate(R.layout.fragment_camera_gallery, null, false);
        ButterKnife.bind(this, mView);

        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


    @Override
    public void onPause() {
        super.onPause();

        AppHelper.LogCat("onPause ");
        if (cameraVisible) {
            cameraVisible = false;
            destroyCamera();
            exitFullScreen();
            AppHelper.LogCat("mCamera not isVisible");

        }
        try {

            if (customHandler != null)
                customHandler.removeCallbacksAndMessages(null);

            releaseMediaRecorder();       // if you are using MediaRecorder, release it first

            if (myOrientationEventListener != null)
                myOrientationEventListener.disable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        AppHelper.LogCat("onResume ");

        initialize();

        if (!cameraVisible) {

            cameraVisible = true;
            startPreview();
            setFullScreen();
            Permissions.with(getActivity())
                    .request(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                    .ifNecessary()
                    .withRationaleDialog(getActivity().getString(R.string.to_call_s_app_needs_access_to_your_microphone_and_camera, getActivity().getString(R.string.app_name)), R.drawable.ic_mic_white_24dp, R.drawable.ic_videocam_white_24dp)
                    .withPermanentDenialDialog(getActivity().getString(R.string.app_needs_the_microphone_and_camera_permissions_in_order_to_call_s, getActivity().getString(R.string.app_name)))
                    .onAllGranted(() -> {


                    }).onAnyDenied(() -> {
            }).execute();
            AppHelper.LogCat("mCamera  isVisible");
        }


        try {
            if (myOrientationEventListener != null)
                myOrientationEventListener.enable();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialize() {
        PickerHelper.getScreenSize(getActivity());

        try {
            SelectionCount = getArguments().getInt(SELECTION, 1);
            forStory = getArguments().getBoolean(AppConstants.MediaConstants.EXTRA_FOR_STORY, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        colorPrimaryDark = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getActivity().getTheme());

        capture_btn = mView.findViewById(R.id.capture_btn);

        flash = mView.findViewById(R.id.flash);
        front = mView.findViewById(R.id.front);
        topbar = mView.findViewById(R.id.topbar);
        initControls();

        identifyOrientationEvents();

        //create a folder to get image
        folder = new File(Environment.getExternalStorageDirectory(), "/DCIM/Camera");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        //capture image on callback
        captureImageCallback();
        //
        if (camera != null) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                flash.setVisibility(View.GONE);
            }
        }
        zoom = 0.0f;
        mCamera.setOnTouchListener((v, event) -> {
            if (event.getPointerCount() > 1) {

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        dist = PickerHelper.getFingerSpacing(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float maxZoom = 1;

                        Camera.Parameters parameters = camera.getParameters();
                        //  int maxZoom = parameters.getMaxZoom();


                        if (parameters.isZoomSupported()) {
                            if (zoom >= 0 && zoom < maxZoom) {
                                float newDist = PickerHelper.getFingerSpacing(event);
                                if (newDist > dist) {
                                    //zoom in
                                    if (zoom < maxZoom)
                                        zoom = zoom + 0.01f;
                                } else if (newDist < dist) {
                                    //zoom out
                                    if (zoom > 0)
                                        zoom = zoom - 0.01f;
                                }

                                dist = newDist;
                                AppHelper.LogCat("zoomed " + zoom);
                                //parameters.setZoom(Integer.parseInt(zoom));
                            } /*else {
                                // zoom parameter is incorrect
                            }*/
                        }
                        // camera.getParameters().setZoom(zoom);
                        break;
                }
            }
            return true;
        });
        selection_count = mView.findViewById(R.id.selection_count);
        selection_ok = mView.findViewById(R.id.selection_ok);
        selection_back = mView.findViewById(R.id.selection_back);
        selection_check = mView.findViewById(R.id.selection_check);
        selection_check.setVisibility((SelectionCount > 1) ? View.VISIBLE : View.GONE);
        sendButton = mView.findViewById(R.id.sendButton);
        img_count = mView.findViewById(R.id.img_count);
        mBubbleView = mView.findViewById(R.id.fastscroll_bubble);
        mHandleView = mView.findViewById(R.id.fastscroll_handle);
        mScrollbar = mView.findViewById(R.id.fastscroll_scrollbar);
        mScrollbar.setVisibility(View.GONE);
        mBubbleView.setVisibility(View.GONE);
        bottomButtons = mView.findViewById(R.id.bottomButtons);
        TOP_BAR_HEIGHT = PickerHelper.convertDpToPixel(56, getActivity());
        status_bar_bg = mView.findViewById(R.id.status_bar_bg);
        recentMediaRecyclerView = mView.findViewById(R.id.instantRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recentMediaRecyclerView.setLayoutManager(linearLayoutManager);
        recentMediaAdapter = new RecentMediaAdapter(getContext());
        recentMediaAdapter.addOnSelectionListener(onSelectionListener);
        recentMediaRecyclerView.setAdapter(recentMediaAdapter);
        mediaPickerRecyclerView = mView.findViewById(R.id.recyclerView);
        mediaPickerRecyclerView.addOnScrollListener(mScrollListener);
        mainFrameLayout = mView.findViewById(R.id.mainFrameLayout);
        int bottomBarHeight = PickerHelper.getSoftButtonsBarSizePort(getActivity());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(0, 0, 0, bottomBarHeight);
        mainFrameLayout.setLayoutParams(lp);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) sendButton.getLayoutParams();
        layoutParams.setMargins(0, 0, (int) (PickerHelper.convertDpToPixel(16, getContext())), (int) (PickerHelper.convertDpToPixel(174, getContext())));
        sendButton.setLayoutParams(layoutParams);
        mediaFilesAdapter = new MediaFilesAdapter(getContext());
        mLayoutManager = new GridLayoutManager(getContext(), MediaFilesAdapter.SPAN_COUNT);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mediaFilesAdapter.getItemViewType(position) == MediaFilesAdapter.HEADER) {
                    return MediaFilesAdapter.SPAN_COUNT;
                }
                return 1;
            }
        });
        mediaPickerRecyclerView.setLayoutManager(mLayoutManager);
        mediaFilesAdapter.addOnSelectionListener(onSelectionListener);
        mediaPickerRecyclerView.setAdapter(mediaFilesAdapter);
        mediaPickerRecyclerView.addItemDecoration(new HeaderItemDecoration(getContext(), mediaPickerRecyclerView, mediaFilesAdapter));
        mHandleView.setOnTouchListener(this);

        selection_ok.setOnClickListener(view -> getResults());
        sendButton.setOnClickListener(view -> getResults());
        selection_back.setOnClickListener(view -> mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));
        selection_check.setOnClickListener(view -> {
            topbar.setBackgroundColor(colorPrimaryDark);
            selection_count.setText(getResources().getString(R.string.tap_to_select));
            img_count.setText(String.valueOf(selectionList.size()));
            DrawableCompat.setTint(selection_back.getDrawable(), Color.parseColor("#ffffff"));
            LongSelection = true;
            selection_check.setVisibility(View.GONE);
        });
        flashChildAt = (AppCompatImageView) flash.getChildAt(0);


        flash.setOnClickListener(view -> flashToggle());


        front.setOnClickListener(view -> {
            //    final CameraConfiguration cameraConfiguration = new CameraConfiguration();
            if (camera == null) return;
            camera.stopPreview();
            camera.release();
            if (flag == 0) {
                flash.setVisibility(View.GONE);
                flag = 1;
            } else {
                flash.setVisibility(View.VISIBLE);
                flag = 0;
            }

            startPreview();
        });
        DrawableCompat.setTint(selection_back.getDrawable(), colorPrimaryDark);

        updateImages();


        ((MainActivity) getActivity()).setOnBackPressed(this::onBackPressed);
    }

    @SuppressLint("StaticFieldLeak")
    private void updateImages() {

        ArrayList<MediaPicker> INSTANTLIST = new ArrayList<>();
        mediaFilesAdapter.clearList();

        if (Permissions.hasAny(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {


            Cursor cursor = PickerHelper.getCursor(getActivity());

            String header = "";
            int limit = 100;
            if (cursor.getCount() < 100) {
                limit = cursor.getCount();
            }
            int date = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            int data = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            int contentUrl = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int MediaType = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
            Calendar calendar;
            for (int i = 0; i < limit; i++) {
                cursor.moveToNext();
                Uri path;
                String type;
                if (cursor.getInt(MediaType) == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    type = "image";
                    path = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + cursor.getInt(contentUrl));
                } else {
                    type = "video";
                    path = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + cursor.getInt(contentUrl));
                }
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(cursor.getLong(date));
                String dateDifference = PickerHelper.getDateDifference(getActivity(), calendar);
                if (!header.equalsIgnoreCase("" + dateDifference)) {
                    header = "" + dateDifference;
                    INSTANTLIST.add(new MediaPicker("" + dateDifference, "", "", "", type));
                }
                INSTANTLIST.add(new MediaPicker("" + header, "" + path, cursor.getString(data), "", type));
            }
            cursor.close();


            new MediaFetcher(getActivity()) {
                @Override
                protected void onPostExecute(ArrayList<MediaPicker> mediaPickers) {
                    super.onPostExecute(mediaPickers);
                    mediaFilesAdapter.addImageList(mediaPickers);
                }
            }.execute(PickerHelper.getCursor(getActivity()));

        }
        recentMediaAdapter.addImageList(INSTANTLIST);
        mediaFilesAdapter.addImageList(INSTANTLIST);
        setBottomSheetBehavior();

    }

    private void setBottomSheetBehavior() {
        View bottomSheet = mView.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight((int) (PickerHelper.convertDpToPixel(194, getActivity())));
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                PickerHelper.manipulateVisibility(slideOffset, recentMediaRecyclerView, mediaPickerRecyclerView, status_bar_bg, topbar, bottomButtons, sendButton, LongSelection);
                if (slideOffset == 1) {
                    PickerHelper.showScrollbar(mScrollbar, getActivity());
                    mediaFilesAdapter.notifyDataSetChanged();
                    mViewHeight = mScrollbar.getMeasuredHeight();
                    handler.post(() -> setViewPositions(getScrollProportion(mediaPickerRecyclerView)));
                    sendButton.setVisibility(View.GONE);


                } else if (slideOffset == 0) {

                    recentMediaAdapter.notifyDataSetChanged();
                    hideScrollbar();
                    img_count.setText(String.valueOf(selectionList.size()));

                    try {

                        if (camera != null)
                            camera.startPreview();
                    } catch (Exception e) {
                        AppHelper.LogCat("Exception " + e.getMessage());
                    }

                }
            }
        });
    }

    private float getScrollProportion(RecyclerView recyclerView) {
        final int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
        final int verticalScrollRange = recyclerView.computeVerticalScrollRange();
        final float rangeDiff = verticalScrollRange - mViewHeight;
        float proportion = (float) verticalScrollOffset / (rangeDiff > 0 ? rangeDiff : 1f);
        return mViewHeight * proportion;
    }

    private void setViewPositions(float y) {
        int handleY = PickerHelper.getValueInRange(0, (int) (mViewHeight - mHandleView.getHeight()), (int) (y - mHandleView.getHeight() / 2));
        mBubbleView.setY(handleY + PickerHelper.convertDpToPixel((56), getActivity()));
        mHandleView.setY(handleY);
    }

    private void setRecyclerViewPosition(float y) {
        if (mediaPickerRecyclerView != null && mediaPickerRecyclerView.getAdapter() != null) {
            int itemCount = mediaPickerRecyclerView.getAdapter().getItemCount();
            float proportion;

            if (mHandleView.getY() == 0) {
                proportion = 0f;
            } else if (mHandleView.getY() + mHandleView.getHeight() >= mViewHeight - sTrackSnapRange) {
                proportion = 1f;
            } else {
                proportion = y / mViewHeight;
            }

            int scrolledItemCount = Math.round(proportion * itemCount);
            int targetPos = PickerHelper.getValueInRange(0, itemCount - 1, scrolledItemCount);
            mediaPickerRecyclerView.getLayoutManager().scrollToPosition(targetPos);

            if (mediaFilesAdapter != null) {
                String text = mediaFilesAdapter.getSectionMonthYearText(targetPos);
                mBubbleView.setText(text);
                if (text.equalsIgnoreCase("")) {
                    mBubbleView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void showBubble() {
        if (!PickerHelper.isViewVisible(mBubbleView)) {
            mBubbleView.setVisibility(View.VISIBLE);
            mBubbleView.setAlpha(0f);
            mBubbleAnimator = mBubbleView.animate().alpha(1f)
                    .setDuration(sBubbleAnimDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        // adapter required for new alpha value to stick
                    });
            mBubbleAnimator.start();
        }
    }

    private void hideBubble() {
        if (PickerHelper.isViewVisible(mBubbleView)) {
            mBubbleAnimator = mBubbleView.animate().alpha(0f)
                    .setDuration(sBubbleAnimDuration)
                    .setListener(new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mBubbleView.setVisibility(View.GONE);
                            mBubbleAnimator = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            mBubbleView.setVisibility(View.GONE);
                            mBubbleAnimator = null;
                        }
                    });
            mBubbleAnimator.start();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < mHandleView.getX() - ViewCompat.getPaddingStart(mHandleView)) {
                    return false;
                }
                mHandleView.setSelected(true);
                handler.removeCallbacks(mScrollbarHider);
                PickerHelper.cancelAnimation(mScrollbarAnimator);
                PickerHelper.cancelAnimation(mBubbleAnimator);

                if (!PickerHelper.isViewVisible(mScrollbar) && (mediaPickerRecyclerView.computeVerticalScrollRange() - mViewHeight > 0)) {
                    mScrollbarAnimator = PickerHelper.showScrollbar(mScrollbar, getActivity());
                }

                if (mediaFilesAdapter != null) {
                    showBubble();
                }

                if (mFastScrollStateChangeListener != null) {
                    mFastScrollStateChangeListener.onFastScrollStart(getActivity());
                }
            case MotionEvent.ACTION_MOVE:
                final float y = event.getRawY();
                setViewPositions(y - TOP_BAR_HEIGHT);
                setRecyclerViewPosition(y);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mHandleView.setSelected(false);
                if (mHideScrollbar) {
                    handler.postDelayed(mScrollbarHider, sScrollbarHideDelay);
                }
                hideBubble();
                if (mFastScrollStateChangeListener != null) {
                    mFastScrollStateChangeListener.onFastScrollStop(getActivity());
                }
                return true;
        }
        return false;
    }


    public void onBackPressed() {

        if (selectionList.size() > 0) {
            for (MediaPicker mediaPicker : selectionList) {
                mediaFilesAdapter.getItemList().get(mediaPicker.getPosition()).setSelected(false);
                mediaFilesAdapter.notifyItemChanged(mediaPicker.getPosition());
                recentMediaAdapter.getItemList().get(mediaPicker.getPosition()).setSelected(false);
                recentMediaAdapter.notifyItemChanged(mediaPicker.getPosition());
            }
            LongSelection = false;
            if (SelectionCount > 1) {
                selection_check.setVisibility(View.VISIBLE);
            }
            DrawableCompat.setTint(selection_back.getDrawable(), colorPrimaryDark);
            topbar.setBackgroundColor(AppHelper.getColor(getActivity(), R.color.colorWhite));
            Animation anim = new ScaleAnimation(
                    1f, 0f, // Start and end values for the X axis scaling
                    1f, 0f, // Start and end values for the Y axis scaling
                    Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                    Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
            anim.setFillAfter(true); // Needed to keep the result of the animation
            anim.setDuration(300);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    sendButton.setVisibility(View.GONE);
                    sendButton.clearAnimation();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            sendButton.startAnimation(anim);
            selectionList.clear();
        } else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            cancelSavePicTaskIfNeed();
        }
    }


    private void cancelSavePicTaskIfNeed() {
        if (savePicTask != null && savePicTask.getStatus() == AsyncTask.Status.RUNNING) {
            savePicTask.cancel(true);
        }
    }

    private void cancelSaveVideoTaskIfNeed() {
        if (saveVideoTask != null && saveVideoTask.getStatus() == AsyncTask.Status.RUNNING) {
            saveVideoTask.cancel(true);
        }
    }

    private SavePicTask savePicTask;

    private class SavePicTask extends AsyncTask<Void, Void, String> {
        private byte[] data;
        private int rotation = 0;

        public SavePicTask(byte[] data, int rotation) {
            this.data = data;
            this.rotation = rotation;
        }

        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                return saveToSDCard(data, rotation);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            activeCameraCapture();

            tempFile = new File(result);

            new Handler().postDelayed(() -> {
                selectionList.clear();
                selectionList.add(new MediaPicker("", "", tempFile.getAbsolutePath(), "", "image"));
                getResults();

            }, 100);


        }
    }

    public String saveToSDCard(byte[] data, int rotation) throws IOException {
        String imagePath = "";
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);

            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int reqHeight = metrics.heightPixels;
            int reqWidth = metrics.widthPixels;

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if (rotation != 0) {
                Matrix mat = new Matrix();
                mat.postRotate(rotation);
                Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
                if (bitmap != bitmap1) {
                    bitmap.recycle();
                }
                imagePath = getSavePhotoLocal(bitmap1);
                if (bitmap1 != null) {
                    bitmap1.recycle();
                }
            } else {
                imagePath = getSavePhotoLocal(bitmap);
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagePath;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    private String getSavePhotoLocal(Bitmap bitmap) {
        String path = "";
        try {
            OutputStream output;
            File file = new File(folder.getAbsolutePath(), "IMG_" + System.currentTimeMillis() + ".jpg");
            try {
                output = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                output.flush();
                output.close();
                path = file.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    private void captureImageCallback() {

        surfaceHolder = mCamera.getHolder();

        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        jpegCallback = (data, camera) -> {

            refreshCamera();

            cancelSavePicTaskIfNeed();
            savePicTask = new SavePicTask(data, getPhotoRotation());
            savePicTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        };
    }

    @SuppressLint("StaticFieldLeak")
    private class SaveVideoTask extends AsyncTask<Void, Void, Void> {

        // File thumbFilename;

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Processing a video...");
            progressDialog.show();
            super.onPreExecute();
            capture_btn.setOnTouchListener(null);
            textCounter.setVisibility(View.GONE);
            front.setVisibility(View.VISIBLE);
            flash.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                try {
                    myOrientationEventListener.enable();

                    customHandler.removeCallbacksAndMessages(null);

                    mediaRecorder.stop();
                    releaseMediaRecorder();

                    tempFile = new File(folder.getAbsolutePath() + "/" + mediaFileName + ".mp4");


                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progressDialog != null) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
            if (tempFile != null)
                onVideoSendDialog(tempFile.getAbsolutePath());
        }
    }

    private int mPhotoAngle = 90;

    private void identifyOrientationEvents() {

        myOrientationEventListener = new OrientationEventListener(getContext(), SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int iAngle) {

                final int[] iLookup = {0, 0, 0, 90, 90, 90, 90, 90, 90, 180, 180, 180, 180, 180, 180, 270, 270, 270, 270, 270, 270, 0, 0, 0}; // 15-degree increments
                if (iAngle != ORIENTATION_UNKNOWN) {

                    int iNewOrientation = iLookup[iAngle / 15];
                    if (iOrientation != iNewOrientation) {
                        iOrientation = iNewOrientation;
                        if (iOrientation == 0) {
                            mOrientation = 90;
                        } else if (iOrientation == 270) {
                            mOrientation = 0;
                        } else if (iOrientation == 90) {
                            mOrientation = 180;
                        }

                    }
                    mPhotoAngle = normalize(iAngle);
                }
            }
        };

     /*   if (myOrientationEventListener != null)
            if (myOrientationEventListener.canDetectOrientation()) {
                myOrientationEventListener.enable();
            }*/

    }


    private void initControls() {

        mediaRecorder = new MediaRecorder();

        mCamera = mView.findViewById(R.id.camera_view);
        textCounter = mView.findViewById(R.id.textCounter);


        textCounter.setVisibility(View.GONE);

        hintTextView = mView.findViewById(R.id.hintTextView);

        activeCameraCapture();


    }

    private void flashToggle() {

        if (flashType == 1) {

            flashType = 2;
        } else if (flashType == 2) {

            flashType = 3;
        } else if (flashType == 3) {

            flashType = 1;
        }
        refreshCamera();
    }

    private void captureImage() {
        if (camera == null) return;
        try {

            camera.takePicture(null, null, jpegCallback);
            inActiveCameraCapture();
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
            AppHelper.LogCat(e);
        }
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = new MediaRecorder();
        }
    }


    public void refreshCamera() {

        if (surfaceHolder.getSurface() == null) {
            return;
        }
        try {
            if (camera == null) return;
            camera.stopPreview();
            Camera.Parameters param = camera.getParameters();

            if (flag == 0) {
                if (flashType == 1) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);


                    AppHelper.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            flashChildAt.setImageResource(R.drawable.ic_flash_auto_black_24dp);
                        }
                    });

                } else if (flashType == 2) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    Camera.Parameters params = null;
                    if (camera != null) {
                        params = camera.getParameters();

                        if (params != null) {
                            List<String> supportedFlashModes = params.getSupportedFlashModes();

                            if (supportedFlashModes != null) {
                                if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                                    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                } else if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                                    param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                                }
                            }
                        }
                    }


                    AppHelper.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            flashChildAt.setImageResource(R.drawable.ic_flash_on_black_24dp);
                        }
                    });
                } else if (flashType == 3) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

                    AppHelper.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            flashChildAt.setImageResource(R.drawable.ic_flash_off_black_24dp);
                        }
                    });

                }
            }


            refrechCameraPriview(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refrechCameraPriview(Camera.Parameters param) {
        try {
            camera.setParameters(param);
            setCameraDisplayOrientation(0);

            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCameraDisplayOrientation(int cameraId) {

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();

        if (Build.MODEL.equalsIgnoreCase("Nexus 6") && flag == 1) {
            rotation = Surface.ROTATION_180;
        }
        int degrees = 0;
        switch (rotation) {

            case Surface.ROTATION_0:

                degrees = 0;
                break;

            case Surface.ROTATION_90:

                degrees = 90;
                break;

            case Surface.ROTATION_180:

                degrees = 180;
                break;

            case Surface.ROTATION_270:

                degrees = 270;
                break;

        }

        int result;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror

        } else {
            result = (info.orientation - degrees + 360) % 360;

        }

        camera.setDisplayOrientation(result);

    }

    //------------------SURFACE CREATED FIRST TIME--------------------//

    int flashType = 1;

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        // The Surface has been created, now tell the camera where to draw the preview.
     /*   new Thread(() -> {
            startPreview();
        }).start();*/


    }


    private void startPreview() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                if (flag == 0) {
                    camera = Camera.open(0);
                } else {
                    camera = Camera.open(1);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                return;
            }

            try {
                Camera.Parameters param;
                param = camera.getParameters();
                List<Camera.Size> sizes = param.getSupportedPreviewSizes();
                //get diff to get perfact preview sizes
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int height = displaymetrics.heightPixels;
                int width = displaymetrics.widthPixels;
                long diff = (height * 1000 / width);
                long cdistance = Integer.MAX_VALUE;
                int idx = 0;
                for (int i = 0; i < sizes.size(); i++) {
                    long value = (long) (sizes.get(i).width * 1000) / sizes.get(i).height;
                    if (value > diff && value < cdistance) {
                        idx = i;
                        cdistance = value;
                    }
                }
                Camera.Size cs = sizes.get(idx);
                param.setPreviewSize(cs.width, cs.height);
                param.setPictureSize(cs.width, cs.height);
                camera.setParameters(param);
                setCameraDisplayOrientation(0);

                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();

                if (flashType == 1) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    AppHelper.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            flashChildAt.setImageResource(R.drawable.ic_flash_auto_black_24dp);
                        }
                    });


                } else if (flashType == 2) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    Camera.Parameters params = null;
                    if (camera != null) {
                        params = camera.getParameters();

                        if (params != null) {
                            List<String> supportedFlashModes = params.getSupportedFlashModes();

                            if (supportedFlashModes != null) {
                                if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                                    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                } else if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                                    param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                                }
                            }
                        }
                    }

                    AppHelper.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            flashChildAt.setImageResource(R.drawable.ic_flash_on_black_24dp);
                        }
                    });


                } else if (flashType == 3) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

                    AppHelper.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            flashChildAt.setImageResource(R.drawable.ic_flash_off_black_24dp);
                        }
                    });

                }


            } catch (Exception e) {
                e.printStackTrace();
            }


            //  mCamera.setZOrderOnTop(true);
            surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        });
    }

    public void destroyCamera() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                camera.stopPreview();
                camera.release();
                camera = null;

                surfaceHolder.setFormat(R.color.colorBlack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {


    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        refreshCamera();
    }

    //------------------SURFACE OVERRIDE METHIDS END--------------------//

    private long startTime = SystemClock.uptimeMillis();
    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            long timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            long timeSwapBuff = 0L;
            long updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            int hrs = mins / 60;

            secs = secs % 60;
            String counter = String.format(Locale.getDefault(), "%02d", mins) + ":" + String.format("%02d", secs);
            textCounter.setText(counter);
            customHandler.postDelayed(this, 0);

        }

    };

    private void scaleUpAnimation() {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(capture_btn, "scaleX", 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(capture_btn, "scaleY", 1f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);

        scaleDownX.addUpdateListener(valueAnimator -> {
            capture_btn.setBackground(AppHelper.getDrawable(getActivity(), R.drawable.bg_circle_red_ind));
            View p = (View) capture_btn.getParent();
            p.invalidate();
        });
        scaleDown.start();
    }

    private void scaleDownAnimation() {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(capture_btn, "scaleX", 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(capture_btn, "scaleY", 0.9f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);

        scaleDownX.addUpdateListener(valueAnimator -> {
            capture_btn.setBackground(AppHelper.getDrawable(getActivity(), R.drawable.bg_circle_ring));
            View p = (View) capture_btn.getParent();

            p.invalidate();
        });
        scaleDown.start();
    }


    private SaveVideoTask saveVideoTask = null;

    @SuppressLint("ClickableViewAccessibility")
    private void activeCameraCapture() {
        if (capture_btn != null) {
            capture_btn.setAlpha(1.0f);
            capture_btn.setOnLongClickListener(v -> {
                hintTextView.setVisibility(View.INVISIBLE);
                try {
                    if (prepareMediaRecorder()) {
                        myOrientationEventListener.disable();
                        mediaRecorder.start();
                        startTime = SystemClock.uptimeMillis();
                        customHandler.postDelayed(updateTimerThread, 0);
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                textCounter.setVisibility(View.VISIBLE);
                front.setVisibility(View.GONE);
                flash.setVisibility(View.GONE);
                scaleUpAnimation();
                capture_btn.setOnTouchListener((v1, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {

                        scaleDownAnimation();
                        hintTextView.setVisibility(View.VISIBLE);

                        cancelSaveVideoTaskIfNeed();
                        saveVideoTask = new SaveVideoTask();
                        saveVideoTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

                        return true;
                    }
                    return true;

                });
                return true;
            });
            capture_btn.setOnClickListener(v -> {

                if (isSpaceAvailable()) {
                    captureImage();
                } else {
                    Toast.makeText(getActivity(), "Memory is not available", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public void onVideoSendDialog(final String videopath) {

        getActivity().runOnUiThread(() -> {
            if (videopath != null) {
                File fileVideo = new File(videopath);
                long fileSizeInBytes = fileVideo.length();
                long fileSizeInKB = fileSizeInBytes / 1024;
                long fileSizeInMB = fileSizeInKB / 1024;
                if (fileSizeInMB > MAX_VIDEO_SIZE_UPLOAD) {
                    new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                            .setMessage(String.format(getString(R.string.file_limit_size_upload_format), "" + MAX_VIDEO_SIZE_UPLOAD))
                            .setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    AppHelper.LogCat("videopath " + videopath);

                    new Handler().postDelayed(() -> {
                        AppHelper.LogCat("videopath " + videopath);
                        selectionList.clear();
                        selectionList.add(new MediaPicker("", "", videopath, "", "video"));
                        getResults();

                    }, 100);

                }
            }
        });
    }

    private void inActiveCameraCapture() {
        if (capture_btn != null) {
            capture_btn.setAlpha(0.5f);
            capture_btn.setOnClickListener(null);
        }
    }

    //--------------------------CHECK FOR MEMORY -----------------------------//

    public int getFreeSpacePercantage() {
        int percantage = (int) (freeMemory() * 100 / totalMemory());
        int modValue = percantage % 5;
        return percantage - modValue;
    }

    public double totalMemory() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getBlockCount() * (double) stat.getBlockSize();
        return sdAvailSize / 1073741824;
    }

    public double freeMemory() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
        return sdAvailSize / 1073741824;
    }

    public boolean isSpaceAvailable() {
        return getFreeSpacePercantage() >= 1;
    }
    //-------------------END METHODS OF CHECK MEMORY--------------------------//


    private String mediaFileName = null;

    @SuppressLint("SimpleDateFormat")
    protected boolean prepareMediaRecorder() {

        mediaRecorder = new MediaRecorder(); // Works well
        camera.stopPreview();
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        if (flag == 1) {
            mediaRecorder.setProfile(CamcorderProfile.get(1, CamcorderProfile.QUALITY_HIGH));
        } else {
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        }
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

        mediaRecorder.setOrientationHint(mOrientation);

        if (Build.MODEL.equalsIgnoreCase("Nexus 6") && flag == 1) {

            if (mOrientation == 90) {
                mediaRecorder.setOrientationHint(mOrientation);
            } else if (mOrientation == 180) {
                mediaRecorder.setOrientationHint(0);
            } else {
                mediaRecorder.setOrientationHint(180);
            }

        } else if (mOrientation == 90 && flag == 1) {
            mediaRecorder.setOrientationHint(270);
        } else if (flag == 1) {
            mediaRecorder.setOrientationHint(mOrientation);
        }
        mediaFileName = "VID_" + System.currentTimeMillis();
        mediaRecorder.setOutputFile(folder.getAbsolutePath() + "/" + mediaFileName + ".mp4"); // Environment.getExternalStorageDirectory()

        mediaRecorder.setOnInfoListener((mr, what, extra) -> {

            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {

                long downTime = 0;
                long eventTime = 0;
                float x = 0.0f;
                float y = 0.0f;
                int metaState = 0;
                MotionEvent motionEvent = MotionEvent.obtain(
                        downTime,
                        eventTime,
                        MotionEvent.ACTION_UP,
                        0,
                        0,
                        metaState
                );

                capture_btn.dispatchTouchEvent(motionEvent);

                Toast.makeText(getActivity(), "You reached to Maximum(25MB) video size.", Toast.LENGTH_SHORT).show();
            }


        });

        mediaRecorder.setMaxFileSize(1000 * 25 * 1000);

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            releaseMediaRecorder();
            e.printStackTrace();
            return false;
        }
        return true;

    }


    public void generateVideoThmb(String srcFilePath, File destFile) {
        try {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(srcFilePath, MediaStore.Video.Thumbnails.MICRO_KIND);
            FileOutputStream out = new FileOutputStream(destFile);
            ThumbnailUtils.extractThumbnail(bitmap, 200, 200).compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int normalize(int degrees) {
        if (degrees > 315 || degrees <= 45) {
            return 0;
        }

        if (degrees > 45 && degrees <= 135) {
            return 90;
        }

        if (degrees > 135 && degrees <= 225) {
            return 180;
        }

        if (degrees > 225 && degrees <= 315) {
            return 270;
        }

        throw new RuntimeException("Error....");
    }

    private int getPhotoRotation() {
        int rotation;
        int orientation = mPhotoAngle;

        Camera.CameraInfo info = new Camera.CameraInfo();
        if (flag == 0) {
            Camera.getCameraInfo(0, info);
        } else {
            Camera.getCameraInfo(1, info);
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {
            rotation = (info.orientation + orientation) % 360;
        }
        return rotation;
    }


    private void setFullScreen() {
        if (!isFullScreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }

            isFullScreen = true;

        }
    }

    private void exitFullScreen() {

        if (isFullScreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
            isFullScreen = false;

        }
    }


}