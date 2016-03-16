package com.roughike.bottombar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.TextView;

import com.roughike.bottombar.R;

/**
 * Created by Iiro Krankka (http://github.com/roughike)
 */
class MiscUtils {
    protected static int getColor(Context context, int color) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, tv, true);
        return tv.data;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.M)
    protected static void setTextAppearance(TextView textView, int textAppearance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextAppearance(textAppearance);
        } else {
            textView.setTextAppearance(textView.getContext(), textAppearance);
        }
    }

    /**
     * Converts dps to pixels nicely.
     * @param context the Context for getting the resources
     * @param dp dimension in dps
     * @return dimension in pixels
     */
    protected static int dpToPixel(Context context, float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / 160f));
    }

    /**
     * Returns screen width.
     *
     * @param context Context to get resources and device specific display metrics
     * @return screen width
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }
}
