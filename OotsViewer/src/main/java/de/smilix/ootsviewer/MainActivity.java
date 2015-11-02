package de.smilix.ootsviewer;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import de.smilix.ootsviewer.R;
import de.smilix.ootsviewer.logger.Log;
import de.smilix.ootsviewer.util.ImageCache;
import de.smilix.ootsviewer.util.ImageFetcher;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "ActionBarActivity";
    private static final String IMAGE_CACHE_DIR = "images";
    private static final String CURRENT_STRIP = "currentStrip";
    private static final String PREFS_NAME = "settings";

    private ImageFetcher imageFetcher;
    private ImageView imageView;
    private PhotoViewAttacher mAttacher;

    private int currentStrip = 1;
    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadStripNumber();

        setContentView(R.layout.activity_main);

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        this.imageFetcher = new ImageFetcher(this, 1000);
        this.imageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
        this.imageFetcher.setImageFadeIn(true);

        this.imageView = (ImageView) findViewById(R.id.imageView);

        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        this.mAttacher = new PhotoViewAttacher(this.imageView);

        updateTextField();
        updateImage();
    }

    private void saveStripNumber() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(CURRENT_STRIP, this.currentStrip);
        editor.apply();
    }

    private void loadStripNumber() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        this.currentStrip = settings.getInt(CURRENT_STRIP, 1);
        System.out.println("loaded strip: " + this.currentStrip);
    }

    private void updateImage() {
        String url = String.format("http://www.giantitp.com/comics/images/oots%04d.gif", this.currentStrip);
        Log.d(TAG, "Loading image: " + url);
        this.imageFetcher.loadImage(url, this.imageView, this.mAttacher);
    }

    private void updateTextField() {
        ((EditText) findViewById(R.id.editText)).setText(String.valueOf(this.currentStrip));
    }

    @Override
    public void onResume() {
        super.onResume();
        this.imageFetcher.setExitTasksEarly(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.imageFetcher.setExitTasksEarly(true);
        this.imageFetcher.flushCache();
        saveStripNumber();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.imageFetcher.closeCache();
    }

    private EditText getStripNumberText() {
        return ((EditText) findViewById(R.id.editText));
    }

    private void disableEditMode() {
        findViewById(R.id.loadStrip).setVisibility(View.INVISIBLE);
        findViewById(R.id.loadStripCancel).setVisibility(View.INVISIBLE);
        getStripNumberText().setFocusable(false);
        this.editMode = false;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editText:
                if (this.editMode) {
                    break;
                }
                this.editMode = true;
                getStripNumberText().setFocusable(true);
                getStripNumberText().setFocusableInTouchMode(true);
                getStripNumberText().requestFocus();

                findViewById(R.id.loadStrip).setVisibility(View.VISIBLE);
                findViewById(R.id.loadStripCancel).setVisibility(View.VISIBLE);
                break;

            case R.id.loadStrip:
                String stripText = getStripNumberText().getText().toString();
                this.currentStrip = Integer.parseInt(stripText);
                updateImage();

                disableEditMode();
                break;

            case R.id.loadStripCancel:
                disableEditMode();
                break;

            case R.id.prev:
                this.currentStrip--;
                updateTextField();
                updateImage();
                break;

            case R.id.next:
                this.currentStrip++;
                updateTextField();
                updateImage();
                break;
        }


    }
}
