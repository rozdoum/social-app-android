package com.rozdoum.socialcomponents.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.model.Comment;
import com.rozdoum.socialcomponents.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexey on 15.11.16.
 */

public class CommentsAdapter {
    private static final String TAG = CommentsAdapter.class.getName();

    private ViewGroup parent;
    private List<Comment> commentList = new ArrayList<>();
    private ImageUtil imageUtil;
    private LayoutInflater inflater;


    public CommentsAdapter(ViewGroup parent) {
        this.parent = parent;
        imageUtil = ImageUtil.getInstance(parent.getContext().getApplicationContext());
        inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void initListView() {
        parent.removeAllViews();
        for (int i = 0; i < commentList.size(); i++) {
            inflater.inflate(R.layout.custom_divider, parent, true);
            Comment comment = commentList.get(i);
            parent.addView(getView(comment));
        }
    }

    private View getView(Comment comment) {
        final View convertView = inflater.inflate(R.layout.comment_list_item, parent, false);

        TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        ImageView avatarImageView = (ImageView) convertView.findViewById(R.id.avatarImageView);
        TextView commentTextView = (TextView) convertView.findViewById(R.id.commentTextView);
        TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);

        commentTextView.setText(comment.getText());

        long now = System.currentTimeMillis();
        CharSequence date = DateUtils.getRelativeTimeSpanString(comment.getCreatedDate(), now, DateUtils.MINUTE_IN_MILLIS);
        dateTextView.setText(date);

        return convertView;
    }

    public void setList(List<Comment> commentList) {
        this.commentList = commentList;
        initListView();
    }

}
