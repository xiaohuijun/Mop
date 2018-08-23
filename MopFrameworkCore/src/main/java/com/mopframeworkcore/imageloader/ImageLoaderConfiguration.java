package com.mopframeworkcore.imageloader;


import android.support.annotation.IdRes;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mopframeworkcore.R;


/**
 * Created by soulrelay on 2016/10/11 13:44.
 * Class Note:
 * encapsulation of ImageView,Build Pattern used
 */
public class ImageLoaderConfiguration {
    private int placeHolder; //设置资源加载过程中的占位
    private int error; //设置load失败时显示
    private boolean isShowPlace;
    private boolean isBlur;
    /**
     * 图片缩放比例,默认“1”。
     */
    private int blurScale = 1;
    /**
     * 设置模糊度(在0.0到25.0之间)，默认”25";
     */
    private int blurProgress = 25;

    private int resizeWidth = -1;
    private int resizeHeight = -1;

    public static final int SACLE_CENTER_CROP = 1001;
    public static final int SACLE_FIT_CENTER = 1002;
    private int sacleType = SACLE_CENTER_CROP;//缩放模式

    private DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.DATA;

    private ImageLoaderConfiguration(Builder builder) {
        this.placeHolder = builder.placeHolder;
        this.error = builder.error;
        this.isShowPlace = builder.isShowPlace;
        this.isBlur = builder.isBlur;
        this.blurScale = builder.blurScale;
        this.blurProgress = builder.blurProgress;
        this.sacleType = builder.sacleType;
        this.diskCacheStrategy = builder.diskCacheStrategy;
    }

    @Override
    public String toString() {
        return "ImageLoaderConfiguration{" +
                "placeHolder=" + placeHolder +
                ", error=" + error +
                ", isShowPlace=" + isShowPlace +
                ", isBlur=" + isBlur +
                ", blurScale=" + blurScale +
                ", blurProgress=" + blurProgress +
                ", resizeWidth=" + resizeWidth +
                ", resizeHeight=" + resizeHeight +
                ", sacleType=" + sacleType +
                '}';
    }

    public int getPlaceHolder() {
        return placeHolder;
    }

    public ImageLoaderConfiguration setPlaceHolder(int placeHolder) {
        this.placeHolder = placeHolder;
        return this;
    }

    public int getError() {
        return error;
    }

    public ImageLoaderConfiguration setError(int error) {
        this.error = error;
        return this;
    }

    public boolean isShowPlace() {
        return isShowPlace;
    }

    public ImageLoaderConfiguration setShowPlace(boolean showPlace) {
        isShowPlace = showPlace;
        return this;
    }

    public boolean isBlur() {
        return isBlur;
    }

    public ImageLoaderConfiguration setBlur(boolean blur) {
        isBlur = blur;
        return this;
    }

    public int getBlurScale() {
        return blurScale;
    }

    public ImageLoaderConfiguration setBlurScale(int blurScale) {
        this.blurScale = blurScale;
        return this;
    }

    public int getBlurProgress() {
        return blurProgress;
    }

    public ImageLoaderConfiguration setBlurProgress(int blurProgress) {
        this.blurProgress = blurProgress;
        return this;
    }

    public DiskCacheStrategy getDiskCacheStrategy() {
        return diskCacheStrategy;
    }


    public int getResizeWidth() {
        return resizeWidth;
    }

    public int getResizeHeight() {
        return resizeHeight;
    }

    public int getSacleType() {
        return sacleType;
    }

    public static class Builder {
        private int placeHolder;
        private int error;
        private boolean isShowPlace;

        private boolean isBlur;
        private int blurScale = 1;
        private int blurProgress;

        private int sacleType = SACLE_CENTER_CROP;

        private int resizeWidth = -1;
        private int resizeHeight = -1;

        private DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.DATA;

        public Builder() {
            this.placeHolder = R.drawable.ic_default_pic_bg;
            this.error = R.drawable.ic_default_pic_bg;
        }


        public Builder placeHolder(@IdRes int placeHolder) {
            this.placeHolder = placeHolder;
            return this;
        }

        public Builder setError(@IdRes int error) {
            this.error = error;
            return this;
        }

        public Builder setShowPlace(boolean showPlace) {
            isShowPlace = showPlace;
            return this;
        }

        public Builder setBlur(boolean blur) {
            isBlur = blur;
            return this;
        }

        public Builder setBlurScale(int blurScale) {
            this.blurScale = blurScale;
            return this;
        }


        public Builder setSacleType(int sacleType) {
            this.sacleType = sacleType;
            return this;
        }

        public Builder setBlurProgress(int blurProgress) {
            this.blurProgress = blurProgress;
            return this;
        }

        public Builder setResizeWidth(int resizeWidth) {
            this.resizeWidth = resizeWidth;
            return this;
        }

        public Builder setResizeHeight(int resizeHeight) {
            this.resizeHeight = resizeHeight;
            return this;
        }

        public void setDiskCacheStrategy(DiskCacheStrategy diskCacheStrategy) {
            this.diskCacheStrategy = diskCacheStrategy;
        }

        public ImageLoaderConfiguration build() {
            return new ImageLoaderConfiguration(this);
        }

    }
}
