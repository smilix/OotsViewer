package de.smilix.ootsviewer;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import de.smilix.ootsviewer.R;
import de.smilix.ootsviewer.logger.Log;
import de.smilix.ootsviewer.logger.LogWrapper;
import de.smilix.ootsviewer.util.ImageCache;
import de.smilix.ootsviewer.util.ImageFetcher;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {

    private static final String TAG = MainActivity.class.toString();
    private static final String CURRENT_STRIP = "currentStrip";
    private static final String PREFS_NAME = "settings";

    private static final String IMAGE_CACHE_DIR = "images";

    private ImageFetcher imageFetcher;
    private ViewPager pager;

    private int currentStrip = 1;
    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadStripNumber();

        Log.setLogNode(new LogWrapper());

        setContentView(R.layout.activity_main);

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        this.imageFetcher = new ImageFetcher(this, 1000);
        this.imageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
        this.imageFetcher.setImageFadeIn(true);

        this.pager = (ViewPager) findViewById(R.id.viewpager);
        // he adapter feeds the image pages
        StripPagerAdapter adapter = new StripPagerAdapter(getSupportFragmentManager());
        this.pager.setAdapter(adapter);
        // adds a listener to update the counter
        this.pager.addOnPageChangeListener(this);

        updateTextField();
        updateImage();
    }

    private int getMaxComicNumber() {
        // this is the most current comic
        // TODO: fetch this info from the network
        return 1011;
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
        this.pager.setCurrentItem(this.currentStrip - 1, false);
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
                this.currentStrip--;
                updateTextField();
                updateImage();
                break;

            case R.id.next:
                if (this.currentStrip == getMaxComicNumber()) {
                    Log.d(TAG, "Max comic number reached.");
                    return;
                }
                this.currentStrip++;
                updateTextField();
                updateImage();
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
            fragment.loadImage(MainActivity.this.imageFetcher);
            return fragment;
        }

        @Override
        public int getCount() {
            return MainActivity.this.getMaxComicNumber();
        }
    }
}
