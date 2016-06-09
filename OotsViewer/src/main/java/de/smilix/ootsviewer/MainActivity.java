package de.smilix.ootsviewer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import de.smilix.ootsviewer.ui.DeactivatableViewPager;
import de.smilix.ootsviewer.ui.FullscreenFragmentActivity;
import de.smilix.ootsviewer.util.ImageCache;
import de.smilix.ootsviewer.util.ImageFetcher;

public class MainActivity extends FullscreenFragmentActivity implements ViewPager.OnPageChangeListener {

    private static final String TAG = MainActivity.class.toString();
    private static final String CURRENT_STRIP = "currentStrip";
    private static final String PREFS_NAME = "settings";

    private static final String IMAGE_CACHE_DIR = "images";

    private ImageFetcher imageFetcher;
    private DeactivatableViewPager pager;

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
        // 04-17 13:17:01.906 7299-7365/de.smilix.ootsviewer W/OpenGLRenderer: Bitmap too large to be uploaded into a texture (766x2733, max=2048x2048)
        this.imageFetcher = new ImageFetcherMaxSize(this, 2048);
        this.imageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
        this.imageFetcher.setImageFadeIn(true);

        this.pager = (DeactivatableViewPager) findViewById(R.id.viewpager);
        // he adapter feeds the image pages
        StripPagerAdapter adapter = new StripPagerAdapter(getSupportFragmentManager());
        this.pager.setAdapter(adapter);
        // adds a listener to update the counter
        this.pager.addOnPageChangeListener(this);

        // TODO: make an option from this
        this.pager.setEnabled(false);

        updateTextField();
        updateImage();
    }

    public ImageFetcher getImageFetcher() {
        return this.imageFetcher;
    }


    private int getMaxComicNumber() {
        // this is the most current comic
        // TODO: fetch this info from the network
        return 1011;
    }

    private int convertComicNumberToIndex(int comicNumber) {
        return comicNumber - 1;
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
        Log.d(TAG, "loaded strip settings: " + this.currentStrip);
    }

    private void updateImage() {
        this.pager.setCurrentItem(convertComicNumberToIndex(this.currentStrip), false);
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
                if (this.currentStrip == 1) {
                    Log.d(TAG, "Already at first comic.");
                    return;
                }
                this.pager.setCurrentItem(convertComicNumberToIndex(this.currentStrip - 1), true);
                break;

            case R.id.next:
                if (this.currentStrip == getMaxComicNumber()) {
                    Log.d(TAG, "Max comic number reached.");
                    return;
                }
                this.pager.setCurrentItem(convertComicNumberToIndex(this.currentStrip + 1), true);
                break;
        }
    }

    /*
     * OnPageChangeListener functions
     */

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        this.currentStrip = position + 1;
        MainActivity.this.updateTextField();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private class StripPagerAdapter extends FragmentStatePagerAdapter {

        public StripPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "Get item for position: " + position);
            int comicNumber = position + 1;
            ComicStripFragment fragment = ComicStripFragment.create(comicNumber);
            return fragment;
        }

        @Override
        public int getCount() {
            return MainActivity.this.getMaxComicNumber();
        }
    }
}
