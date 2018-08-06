/*
 *  Copyright 2018 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.rozdoum.socialcomponents.views;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.enums.FollowState;
import com.rozdoum.socialcomponents.utils.LogUtil;

public class FollowButton extends android.support.v7.widget.AppCompatButton {
    public static final String TAG = FollowButton.class.getSimpleName();

    public static final int FOLLOW_STATE = 1;
    public static final int FOLLOW_BACK_STATE = 2;
    public static final int FOLLOWING_STATE = 3;
    public static final int INVISIBLE_STATE = -1;

    private int state;


    public FollowButton(Context context) {
        super(context);
        init();
    }

    public FollowButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FollowButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setState(FollowState followState) {
        switch (followState) {
            case I_FOLLOW_USER:
            case FOLLOW_EACH_OTHER:
                state = FOLLOWING_STATE;
                break;
            case USER_FOLLOW_ME:
                state = FOLLOW_BACK_STATE;
                break;
            case NO_ONE_FOLLOW:
                state = FOLLOW_STATE;
                break;
            case MY_PROFILE:
                state = INVISIBLE_STATE;

        }

        updateButtonState();
        LogUtil.logDebug(TAG, "new state code: " + state);
    }

    private void init() {
        state = INVISIBLE_STATE;
        updateButtonState();
    }

    public int getState() {
        return state;
    }

    public void updateButtonState() {
        setClickable(true);

        switch (state) {
            case FOLLOW_STATE: {
                setVisibility(VISIBLE);
                setText(R.string.button_follow_title);
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.follow_button_dark_bg));
                setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                break;
            }

            case FOLLOW_BACK_STATE: {
                setVisibility(VISIBLE);
                setText(R.string.button_follow_back_title);
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.follow_button_dark_bg));
                setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                break;
            }

            case FOLLOWING_STATE: {
                setVisibility(VISIBLE);
                setText(R.string.button_following);
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.follow_button_light_bg));
                setTextColor(ContextCompat.getColor(getContext(), R.color.primary_dark_text));
                break;
            }

            case INVISIBLE_STATE: {
                setVisibility(INVISIBLE);
                setClickable(false);
                break;
            }
        }
    }
}
