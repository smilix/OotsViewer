package de.smilix.ootsviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.smilix.ootsviewer.util.ImageFetcher;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Shows a comic strip with a specific number.
 */
public class ComicStripFragment extends Fragment {

    private static final String TAG = ComicStripFragment.class.toString();

    private static final String URL_TEMPLATE = "http://www.giantitp.com/comics/images/oots%04d.gif";
    private static final String ARG_COMIC_NUMBER = "comicNumber";

    private ImageView imageView;
    private PhotoViewAttacher attacher;
    private ImageFetcher imageFetcher;


    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ComicStripFragment create(int comicNumber) {
        ComicStripFragment fragment = new ComicStripFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COMIC_NUMBER, comicNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.comic_page, container, false);

        this.imageView = (ImageView) rootView.findViewById(R.id.imageView);
        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        this.attacher = new PhotoViewAttacher(this.imageView);

        loadImageIfPossible();

        return rootView;
    }


    public void loadImage(ImageFetcher imageFetcher) {
        this.imageFetcher = imageFetcher;
        loadImageIfPossible();
    }

    private void loadImageIfPossible() {
        if (this.imageFetcher == null) {
            return;
        }
        if (this.imageView == null) {
            return;
        }
        int comicNumber = getArguments().getInt(ARG_COMIC_NUMBER);
        String url = String.format(URL_TEMPLATE, comicNumber);
        Log.i(TAG, "Loading image: " + url);
        this.imageFetcher.loadImage(url, this.imageView, this.attacher);
    }
}
