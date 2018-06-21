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

package com.rozdoum.socialcomponents.main.base;

import android.content.DialogInterface;
import android.view.View;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.hannesdorfmann.mosby3.mvp.MvpFragment;

import java.util.Objects;

/**
 * Created by Alexey on 08.05.18.
 */

public abstract class BaseFragment<V extends BaseFragmentView, P extends MvpBasePresenter<V>> extends MvpFragment<V, P> implements BaseFragmentView {

    public void showProgress() {
        ((BaseActivity) getActivity()).showProgress();
    }

    @Override
    public void showProgress(int message) {
        ((BaseActivity) getActivity()).showProgress(message);
    }

    public void hideProgress() {
        ((BaseActivity) getActivity()).hideProgress();
    }

    @Override
    public void showSnackBar(String message) {
        ((BaseActivity) Objects.requireNonNull(getActivity())).showSnackBar(message);
    }

    @Override
    public void showSnackBar(int message) {
        ((BaseActivity) getActivity()).showSnackBar(message);
    }

    @Override
    public void showSnackBar(View view, int messageId) {
        ((BaseActivity) getActivity()).showSnackBar(view, messageId);
    }

    @Override
    public void showToast(int messageId) {
        ((BaseActivity) getActivity()).showToast(messageId);
    }

    @Override
    public void showToast(String message) {
        ((BaseActivity) getActivity()).showToast(message);
    }

    @Override
    public void showWarningDialog(int messageId) {
        ((BaseActivity) getActivity()).showWarningDialog(messageId);
    }

    @Override
    public void showWarningDialog(String message) {
        ((BaseActivity) getActivity()).showWarningDialog(message);
    }

    @Override
    public void showNotCancelableWarningDialog(String message) {
        ((BaseActivity) getActivity()).showNotCancelableWarningDialog(message);
    }

    @Override
    public void showWarningDialog(int messageId, DialogInterface.OnClickListener listener) {
        ((BaseActivity) getActivity()).showWarningDialog(messageId, listener);
    }

    @Override
    public void showWarningDialog(String message, DialogInterface.OnClickListener listener) {
        ((BaseActivity) getActivity()).showWarningDialog(message, listener);
    }

    @Override
    public boolean hasInternetConnection() {
        return ((BaseActivity) getActivity()).hasInternetConnection();
    }

    @Override
    public void startLoginActivity() {
        ((BaseActivity) getActivity()).startLoginActivity();
    }

    @Override
    public void hideKeyboard() {
        ((BaseActivity) getActivity()).hideKeyboard();
    }

    @Override
    public void finish() {
        ((BaseActivity) getActivity()).finish();
    }
}
