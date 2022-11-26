package com.sourcecanyon.whatsClone.fragments;


import android.os.Bundle;
import androidx.loader.content.Loader;


import com.sourcecanyon.whatsClone.helpers.giph.model.GiphyImage;
import com.sourcecanyon.whatsClone.helpers.giph.net.GiphyStickerLoader;

import java.util.List;

public class GiphyStickerFragment extends GiphyFragment {
    @Override
    public Loader<List<GiphyImage>> onCreateLoader(int id, Bundle args) {
        return new GiphyStickerLoader(getActivity(), searchString);
    }
}
