package com.rozdoum.socialcomponents.utils;

import android.view.View;
import android.view.ViewPropertyAnimator;

@SuppressWarnings("UnnecessaryLocalVariable")
public class AnimationUtils {

    public final static int DEFAULT_DELAY = 0;
    public final static int SHORT_DURATION = 200;

    /**
     * Reduces the X & Y
     *
     * @param v the view to be scaled
     *
     * @return the ViewPropertyAnimation to manage the animation
     */
    public static ViewPropertyAnimator hideViewByScale (View v) {

        ViewPropertyAnimator propertyAnimator = v.animate().setStartDelay(DEFAULT_DELAY).setDuration(SHORT_DURATION)
          .scaleX(0).scaleY(0);

        return propertyAnimator;
    }

    /**
     * Shows a view by scaling
     *
     * @param v the view to be scaled
     *
     * @return the ViewPropertyAnimation to manage the animation
     */
    public static ViewPropertyAnimator showViewByScale (View v) {

        ViewPropertyAnimator propertyAnimator = v.animate().setStartDelay(DEFAULT_DELAY)
            .scaleX(1).scaleY(1);

        return propertyAnimator;
    }
}
