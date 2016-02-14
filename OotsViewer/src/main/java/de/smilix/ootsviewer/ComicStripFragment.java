package de.smilix.ootsviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.smilix.ootsviewer.ui.RecyclingImageView;
import de.smilix.ootsviewer.util.ImageFetcher;
import de.smilix.ootsviewer.util.ImageWorker;
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

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ComicStripFragment create(int comicNumber) {
        Log.d(TAG, "new ComicStripFragment for comic " + comicNumber);
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

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated " + getArguments().getInt(ARG_COMIC_NUMBER));

        if (MainActivity.class.isInstance(getActivity())) {
            ImageFetcher imageFetcher = ((MainActivity) getActivity()).getImageFetcher();
            int comicNumber = getArguments().getInt(ARG_COMIC_NUMBER);
            String url = String.format(URL_TEMPLATE, comicNumber);
            Log.i(TAG, "Loading image: " + url);

            // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
            this.attacher = new PhotoViewAttacher(this.imageView);
            imageFetcher.loadImage(url, this.imageView, this.attacher);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // avoids the warning "PhotoViewAttacher: ImageView no longer exists. You should not use this PhotoViewAttacher any more."
        this.attacher.cleanup();
        this.attacher = null;
        Log.d(TAG, "Destroy comic view " + getArguments().getInt(ARG_COMIC_NUMBER));
        if (this.imageView != null) {
            ImageWorker.cancelWork(this.imageView);
            this.imageView.setImageDrawable(null);
        }
    }
}
