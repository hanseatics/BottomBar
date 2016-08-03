package com.roughike.bottombar;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

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
public class BottomBarTab extends LinearLayout {
    public static final String TAG_ACTIVE = "TAB_ACTIVE";
    public static final String TAG_INACTIVE = "TAB_INACTIVE";

    private static final long ANIMATION_DURATION = 150;
    private static final float INACTIVE_ALPHA = 0.6f;

    private final int sixDps;
    private final int eightDps;
    private final int sixteenDps;

    private Type type = Type.FIXED;
    private int iconResId;
    private String title;

    private int inActiveColor;
    private int activeColor;

    private AppCompatImageView iconView;
    private TextView titleView;
    private boolean isSelected;

    public enum Type {
        FIXED, SHIFTING, TABLET
    }

    public BottomBarTab(Context context) {
        super(context);

        sixDps = MiscUtils.dpToPixel(context, 6);
        eightDps = MiscUtils.dpToPixel(context, 8);
        sixteenDps = MiscUtils.dpToPixel(context, 16);

        inActiveColor = ContextCompat.getColor(context, R.color.bb_inActiveBottomBarItemColor);
        activeColor = MiscUtils.getColor(context, R.attr.colorPrimary);
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

    public int getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(int activeIconColor) {
        this.activeColor = activeIconColor;
    }

    public void prepareLayout() {
        int layoutResource;

        switch (type) {
            case FIXED:
                layoutResource = R.layout.bb_bottom_bar_item_fixed;
                break;
            case SHIFTING:
                layoutResource = R.layout.bb_bottom_bar_item_shifting;
                break;
            case TABLET:
                layoutResource = R.layout.bb_bottom_bar_item_fixed_tablet;
                break;
            default:
                // should never happen
                throw new RuntimeException("Unknown BottomBarTab type.");
        }

        inflate(getContext(), layoutResource, this);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        iconView = (AppCompatImageView) findViewById(R.id.bb_bottom_bar_icon);
        titleView = (TextView) findViewById(R.id.bb_bottom_bar_title);

        iconView.setImageResource(iconResId);
        titleView.setText(title);
    }

    public void setIconTint(int tint) {
        iconView.setColorFilter(tint);
    }

    @SuppressWarnings("deprecation")
    public void setTitleTextAppearance(int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            titleView.setTextAppearance(resId);
        } else {
            titleView.setTextAppearance(getContext(), resId);
        }
    }

    public void setTitleTypeface(Typeface typeface) {
        titleView.setTypeface(typeface);
    }

    public void select(boolean animate) {
        setTag(TAG_ACTIVE);
        isSelected = true;

        if (type != Type.SHIFTING) {
            iconView.setColorFilter(activeColor);
            titleView.setTextColor(activeColor);
        }

        if (animate) {
            ViewPropertyAnimatorCompat titleAnimator = ViewCompat.animate(titleView)
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(1)
                    .scaleY(1);

            if (type == Type.SHIFTING) {
                titleAnimator.alpha(1.0f);
            }

            titleAnimator.start();

            // We only want to animate the icon to avoid moving the title
            // Shifting or fixed the padding above icon is always 6dp
            MiscUtils.resizePaddingTop(iconView, iconView.getPaddingTop(), sixDps, ANIMATION_DURATION);

            if (type == Type.SHIFTING) {
                ViewCompat.animate(iconView)
                        .setDuration(ANIMATION_DURATION)
                        .alpha(1.0f)
                        .start();
            }

            // TODO: handleBackgroundColorChange(tabPosition, tab);
        } else {
            ViewCompat.setScaleX(titleView, 1);
            ViewCompat.setScaleY(titleView, 1);
            iconView.setPadding(
                    iconView.getPaddingLeft(),
                    sixDps,
                    iconView.getPaddingRight(),
                    iconView.getPaddingBottom()
            );

            if (type == Type.SHIFTING) {
                ViewCompat.setAlpha(iconView, 1.0f);
                ViewCompat.setAlpha(titleView, 1.0f);
            }
        }
    }

    public void deselect(boolean animate) {
        isSelected = false;
        setTag(TAG_INACTIVE);

        if (type != Type.SHIFTING) {
            iconView.setColorFilter(inActiveColor);

            if (title != null) {
                titleView.setTextColor(inActiveColor);
            }
        }

        float scale = type == Type.SHIFTING ? 0 : 0.86f;
        int iconPaddingTop = type == Type.SHIFTING ? sixteenDps : eightDps;

        if (animate) {
            ViewPropertyAnimatorCompat titleAnimator = ViewCompat.animate(titleView)
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(scale)
                    .scaleY(scale);

            if (type == Type.SHIFTING) {
                titleAnimator.alpha(0);
            }

            titleAnimator.start();

            MiscUtils.resizePaddingTop(iconView, iconView.getPaddingTop(), iconPaddingTop, ANIMATION_DURATION);

            if (type == Type.SHIFTING) {
                ViewCompat.animate(iconView)
                        .setDuration(ANIMATION_DURATION)
                        .alpha(INACTIVE_ALPHA)
                        .start();
            }
        } else {
            ViewCompat.setScaleX(titleView, scale);
            ViewCompat.setScaleY(titleView, scale);

            iconView.setPadding(
                    iconView.getPaddingLeft(),
                    iconPaddingTop,
                    iconView.getPaddingRight(),
                    iconView.getPaddingBottom()
            );

            if (type == Type.SHIFTING) {
                ViewCompat.setAlpha(iconView, INACTIVE_ALPHA);
                ViewCompat.setAlpha(titleView, 0);
            }
        }
    }
}
