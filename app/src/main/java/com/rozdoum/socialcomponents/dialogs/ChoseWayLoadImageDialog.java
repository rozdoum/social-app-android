package com.rozdoum.socialcomponents.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.rozdoum.socialcomponents.enums.TakePictureMenu;

/**
 * Created by alexey on 01.11.16.
 */

public class ChoseWayLoadImageDialog extends DialogFragment {

    private OnChooseWayLoadImageListener onChooseWayLoadImageListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnChooseWayLoadImageListener) {
            onChooseWayLoadImageListener = (OnChooseWayLoadImageListener) activity;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setItems(TakePictureMenu.getTitles(getActivity()), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TakePictureMenu choice = TakePictureMenu.values()[i];
                        if (onChooseWayLoadImageListener != null) {
                            onChooseWayLoadImageListener.onChooseWayLoadImage(choice);
                        }
                    }
                })
                .create();
    }

    public interface OnChooseWayLoadImageListener {
        void onChooseWayLoadImage(TakePictureMenu choice);
    }
}