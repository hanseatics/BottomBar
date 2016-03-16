package com.roughike.bottombar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

/**
 * Created by Iiro Krankka (http://github.com/roughike)
 */
public class BottomBarTab {
    private int iconResource;
    private Drawable icon;
    private int titleResource;
    private String title;

    public BottomBarTab(@DrawableRes int iconResource, @NonNull String title) {
        this.iconResource = iconResource;
        this.title = title;
    }

    public BottomBarTab(Drawable icon, @NonNull String title) {
        this.icon = icon;
        this.title = title;
    }

    public BottomBarTab(Drawable icon, @StringRes int titleResource) {
        this.icon = icon;
        this.titleResource = titleResource;
    }

    public BottomBarTab(@DrawableRes int iconResource, @StringRes int titleResource) {
        this.iconResource = iconResource;
        this.titleResource = titleResource;
    }

    protected Drawable getIcon(Context context) {
        if (iconResource != 0) {
            return ContextCompat.getDrawable(context, iconResource);
        } else {
            return icon;
        }
    }

    protected String getTitle(Context context) {
        if (titleResource != 0) {
            return context.getString(titleResource);
        } else {
            return title;
        }
    }
}
