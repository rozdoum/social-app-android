/*
 * Copyright 2018 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.rozdoum.socialcomponents.adapters.viewPager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.rozdoum.socialcomponents.main.search.Searchable;

import java.util.ArrayList;

public class TabsPagerAdapter extends SmartFragmentStatePagerAdapter {

    private final ArrayList<TabInfo> mTabs = new ArrayList<>();
    private final FragmentActivity mActivity;

    public TabsPagerAdapter(FragmentActivity activity, FragmentManager fragmentManager) {
        super(fragmentManager);
        mActivity = activity;
    }

    @Override
    public Fragment getItem(int position) {
        return mTabs.get(position).getFragment();
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    public void addTab(Class<? extends Searchable> clss, @Nullable Bundle args, String title) {
        mTabs.add(new TabInfo(title, Fragment.instantiate(mActivity, clss.getName(), args)));
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs.get(position).getTitle();
    }

    static final class TabInfo {
        String title;
        Fragment fragment;

        public TabInfo(String title, Fragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }

        public String getTitle() {
            return title;
        }

        public Fragment getFragment() {
            return fragment;
        }
    }
}
