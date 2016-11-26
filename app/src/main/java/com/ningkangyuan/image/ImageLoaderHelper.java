package com.ningkangyuan.image;

import android.widget.ImageView;

import com.ningkangyuan.MyApplication;
import com.ningkangyuan.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 * Created by xuchun on 2016/8/22.
 */
public class ImageLoaderHelper {

    private static final String TAG = "ImageLoaderHelper";

    private static ImageLoaderHelper mImageLoaderHelper;

    public static synchronized ImageLoaderHelper getInstance() {
        if (mImageLoaderHelper == null) {
            mImageLoaderHelper = new ImageLoaderHelper();

            ImageLoader.getInstance().init(makeConfiguration());
        }
        return mImageLoaderHelper;
    }

    public void loader(String uri,ImageView imageView,DisplayImageOptions imageOptions) {
        ImageLoader.getInstance().displayImage(uri,imageView,imageOptions);
    }

    private static ImageLoaderConfiguration makeConfiguration() {
        File cacheDir = StorageUtils.getCacheDirectory(MyApplication.mContext);
        return new ImageLoaderConfiguration.Builder(MyApplication.mContext)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheSize(100)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13)
                .writeDebugLogs()
                .build();
    }

    public DisplayImageOptions getCircleImageOptions() {
        return new DisplayImageOptions.Builder()
                .displayer(new CircleBitmapDisplayer())
                .showImageOnLoading(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .build();
    }
}