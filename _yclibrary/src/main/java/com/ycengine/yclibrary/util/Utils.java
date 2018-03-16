/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ycengine.yclibrary.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;

/**
 * Utility helper functions for time and date pickers.
 */
public class Utils {

    /**
     * Returns a tinted drawable from the given drawable resource, if {@code tintList != null}.
     * Otherwise, no tint will be applied.
     */
    public static Drawable getTintedDrawable(@NonNull Context context,
                                             @DrawableRes int drawableRes,
                                             @Nullable ColorStateList tintList) {
        Drawable d = DrawableCompat.wrap(ContextCompat.getDrawable(context, drawableRes).mutate());
        DrawableCompat.setTintList(d, tintList);
        return d;
    }

    /**
     * Returns a tinted drawable from the given drawable resource and color resource.
     */
    public static Drawable getTintedDrawable(@NonNull Context context,
                                             @DrawableRes int drawableRes,
                                             @ColorInt int colorInt) {
        Drawable d = DrawableCompat.wrap(ContextCompat.getDrawable(context, drawableRes).mutate());
        DrawableCompat.setTint(d, colorInt);
        return d;
    }

    /**
     * Sets the color on the {@code view}'s {@code selectableItemBackground} or the
     * borderless variant, whichever was set as the background.
     * @param view the view that should have its highlight color changed
     */
    public static void setColorControlHighlight(@NonNull View view, @ColorInt int color) {
        Drawable selectableItemBackground = view.getBackground();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && selectableItemBackground instanceof RippleDrawable) {
            ((RippleDrawable) selectableItemBackground).setColor(ColorStateList.valueOf(color));
        } else {
            // Draws the color (src) onto the background (dest) *in the same plane*.
            // That means the color is not overlapping (i.e. on a higher z-plane, covering)
            // the background. That would be done with SRC_OVER.
            // The DrawableCompat tinting APIs *could* be a viable alternative, if you
            // call setTintMode(). Previous attempts using those APIs failed without
            // the tint mode. However, those APIs have the overhead of mutating and wrapping
            // the drawable.
            selectableItemBackground.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    /**
     * Gets the required boolean value from the current context, if possible/available
     * @param context The context to use as reference for the boolean
     * @param attr Attribute id to resolve
     * @param fallback Default value to return if no value is specified in theme
     * @return the boolean value from current theme
     */
    private static boolean resolveBoolean(Context context, @AttrRes int attr, boolean fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getBoolean(0, fallback);
        } finally {
            a.recycle();
        }
    }
}
