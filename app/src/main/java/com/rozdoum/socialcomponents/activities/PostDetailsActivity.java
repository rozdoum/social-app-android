package com.rozdoum.socialcomponents.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rozdoum.socialcomponents.ApplicationHelper;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.CommentsAdapter;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.model.Comment;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.ImageUtil;

import java.util.List;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String POST_EXTRA_KEY = "PostDetailsActivity.POST_EXTRA_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final Post post = (Post) getIntent().getSerializableExtra(POST_EXTRA_KEY);

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        TextView descriptionEditText = (TextView) findViewById(R.id.descriptionEditText);
        ImageView postImageView = (ImageView) findViewById(R.id.postImageView);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        LinearLayout commentsContainer = (LinearLayout) findViewById(R.id.commentsContainer);
        final TextView commentsLabel = (TextView) findViewById(R.id.commentsLabel);

        final EditText commentEditText = (EditText) findViewById(R.id.commentEditText);
        Button sendButton = (Button) findViewById(R.id.sendButton);

        titleTextView.setText(post.getTitle());
        descriptionEditText.setText(post.getDescription());

        String imageUrl = post.getImagePath();

        ImageUtil imageUtil = ImageUtil.getInstance(this);
        imageUtil.getImage(imageUrl, postImageView, progressBar, R.drawable.ic_stub, R.drawable.ic_stub);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = commentEditText.getText().toString();
                ApplicationHelper.getDatabaseHelper().createOrUpdateComment(commentText, post.getId());
                commentEditText.setText(null);
                commentEditText.clearFocus();
            }
        });

        final CommentsAdapter commentsAdapter = new CommentsAdapter(commentsContainer);
        OnDataChangedListener<Comment> onPostsDataChangedListener = new OnDataChangedListener<Comment>() {
            @Override
            public void onListChanged(List<Comment> list) {
                commentsAdapter.setList(list);
                if (list.size() > 0) {
                    commentsLabel.setVisibility(View.VISIBLE);
                    commentsLabel.setText(String.format(getString(R.string.label_comments), list.size()));
                } else {
                    commentsLabel.setVisibility(View.GONE);
                }
            }
        };

        ApplicationHelper.getDatabaseHelper().getCommentsList(post.getId(), onPostsDataChangedListener);

    }
}
