package com.roughike.bottombar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

/*
 * BottomBar library for Android
 * Copyright (c) 2016 Iiro Krankka (http://github.com/roughike).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class BottomBarTab {
    private int iconResource;
    private Drawable icon;
    private int titleResource;
    private String title;

    /**
     * Creates a new Tab for the BottomBar.
     * @param iconResource a resource for the Tab icon.
     * @param title title for the Tab.
     */
    public BottomBarTab(@DrawableRes int iconResource, @NonNull String title) {
        this.iconResource = iconResource;
        this.title = title;
    }

    /**
     * Creates a new Tab for the BottomBar.
     * @param icon an icon for the Tab.
     * @param title title for the Tab.
     */
    public BottomBarTab(Drawable icon, @NonNull String title) {
        this.icon = icon;
        this.title = title;
    }

    /**
     * Creates a new Tab for the BottomBar.
     * @param icon an icon for the Tab.
     * @param titleResource resource for the title.
     */
    public BottomBarTab(Drawable icon, @StringRes int titleResource) {
        this.icon = icon;
        this.titleResource = titleResource;
    }

    /**
     * Creates a new Tab for the BottomBar.
     * @param iconResource a resource for the Tab icon.
     * @param titleResource resource for the title.
     */
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
