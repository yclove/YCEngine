package com.ycengine.rainsound.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ycengine.rainsound.R;

public class ScalePageTransformer implements ViewPager.PageTransformer{
    private static final float MIN_SCALE = 0.5f;
    private static final float MIN_ALPHA = 0.3f;

    public void transformPage(View view, float position) {
        TextView tvCoreTitle = (TextView)view.findViewById(R.id.tvCoreTitle);
        ImageView ivCoreCircle = (ImageView)view.findViewById(R.id.ivCoreCircle);

        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            //view.setAlpha(0);

        } else if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));

            /*float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                tvCoreTitle.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                tvCoreTitle.setTranslationX(-horzMargin + vertMargin / 2);
            }*/

            // Scale the page down (between MIN_SCALE and 1)
            tvCoreTitle.setScaleX(scaleFactor);
            tvCoreTitle.setScaleY(scaleFactor);
            ivCoreCircle.setScaleX(scaleFactor);
            ivCoreCircle.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            tvCoreTitle.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            ivCoreCircle.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            //view.setAlpha(0);
        }
    }
}