package com.sourcecanyon.whatsClone.helpers.editorHelpers.filter;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.sourcecanyon.whatsClone.helpers.editorHelpers.TaskCallback;
import com.sourcecanyon.whatsClone.helpers.AppHelper;


public final class ProcessingImage extends AsyncTask<Void, Void, String> {
    private final Bitmap srcBitmap;
    private final TaskCallback<String> callback;
    private final String imagePath;

    public ProcessingImage(Bitmap srcBitmap, String imagePath, TaskCallback<String> taskCallback) {
        this.srcBitmap = srcBitmap;
        this.callback = taskCallback;
        this.imagePath = imagePath;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return AppHelper.saveBitmap(srcBitmap, imagePath);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (callback != null) {
            callback.onTaskDone(s);
        }
    }
}