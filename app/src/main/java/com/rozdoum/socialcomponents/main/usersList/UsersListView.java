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

package com.rozdoum.socialcomponents.main.usersList;

import android.support.annotation.StringRes;

import com.rozdoum.socialcomponents.main.base.BaseView;

import java.util.List;

/**
 * Created by Alexey on 03.05.18.
 */

public interface UsersListView extends BaseView {

    void onProfilesIdsListLoaded(List<String> list);

    void showLocalProgress();

    void hideLocalProgress();

    void setTitle(@StringRes int title);

    void showEmptyListMessage(String message);

    void hideEmptyListMessage();

    void updateSelectedItem();
}
