package com.advance.advancesdkdemo;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alimm.tanx.core.image.ILoader;
import com.alimm.tanx.core.image.util.GifConfig;
import com.alimm.tanx.core.image.util.ImageConfig;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

/**
 * Glide自定义实现tanx 广告图片加载
 */
public class MyImageLoader implements ILoader {
    @Override
    public void load(ImageConfig config, final ImageConfig.ImageBitmapCallback callback) {

        Glide.with(config.getContext())
                .asBitmap()
                .load(config.getUrl())
                .into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        if (callback != null) {
                            callback.onFailure("图片加载失败");
                        }
                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (callback != null) {
                            callback.onSuccess(resource);
                        }
                    }

                });
    }

    @Override
    public void loadGif(GifConfig gifConfig, ImageConfig.GifCallback gifCallback) {
        String error = "";
        if (gifConfig != null && gifConfig.getGifView() != null) {
            if (!TextUtils.isEmpty(gifConfig.getGifUrl())) {

                Glide.with(gifConfig.getGifView().getContext())
                        .load(gifConfig.getGifUrl())
                        .into(gifConfig.getGifView());
                if (gifCallback != null) {
                    gifCallback.onSuccess();
                }
                return;
            }
            if (gifConfig.getGifRes() != -1) {
                Glide.with(gifConfig.getGifView().getContext())
                        .load(gifConfig.getGifRes())
                        .into(gifConfig.getGifView());
                if (gifCallback != null) {
                    gifCallback.onSuccess();
                }
                return;
            }
        } else {
            error = "imageView对象为空";
        }
        if (gifCallback != null) {
            gifCallback.onFailure(error);
        }
    }
}