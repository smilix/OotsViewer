package de.smilix.ootsviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import de.smilix.ootsviewer.util.ImageFetcher;

/**
 * In addition to ImageFetcher, it ensures that the resized image is never exceeds the given width/height.
 *
 * Created by holger on 17.04.16.
 */
public class ImageFetcherMaxSize extends ImageFetcher {
    private static final String TAG = ImageFetcher.class.getSimpleName();

    public ImageFetcherMaxSize(Context context, int imageSize) {
        super(context, imageSize);
    }

    public ImageFetcherMaxSize(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        final Bitmap bitmap = super.processBitmap(data);
        if (bitmap == null) {
            return null;
        }

        return ensureMaxSize(bitmap);
    }

    private Bitmap ensureMaxSize(Bitmap bitmap) {
        if (bitmap.getWidth() <= mImageWidth && bitmap.getHeight() <= mImageHeight) {
            // nothing to do
            return bitmap;
        }

        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        int resultWidth, resultHeight;

        float ratioW = (float) imageWidth / mImageWidth;
        float ratioH = (float) imageHeight / mImageHeight;

        if (ratioW > ratioH) {
            resultWidth = mImageWidth;
            resultHeight = (int) (imageHeight / ratioW);
        } else {
            resultHeight = mImageHeight;
            resultWidth = (int) (imageWidth / ratioH);
        }

        Log.d(TAG, "Rescale image to " + resultWidth + "/" + resultHeight);
        return Bitmap.createScaledBitmap(bitmap, resultWidth, resultHeight, false);
    }
}
