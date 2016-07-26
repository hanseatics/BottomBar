package com.roughike.bottombar.view;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.roughike.bottombar.R;

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
public class BottomBarTab extends FrameLayout {
    private Type type = Type.FIXED;
    private int iconResId;
    private String title;
    private int activeIconColor;

    public enum Type {
        FIXED, SHIFTING, TABLET
    }

    public BottomBarTab(Context context) {
        super(context);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getActiveIconColor() {
        return activeIconColor;
    }

    public void setActiveIconColor(int activeIconColor) {
        this.activeIconColor = activeIconColor;
    }

    public void addToRoot(ViewGroup root) {
        int layoutResource;

        switch (type) {
            case FIXED:
                layoutResource = R.layout.bb_bottom_bar_item_fixed;
                break;
            case SHIFTING:
                layoutResource = R.layout.bb_bottom_bar_item_shifting;
                break;
            case TABLET:
                layoutResource = R.layout.bb_bottom_bar_item_fixed;
                break;
            default:
                // should never happen
                throw new RuntimeException("Unknown BottomBarTab type.");
        }

        inflate(getContext(), layoutResource, this);
        root.addView(this);
    }
}
