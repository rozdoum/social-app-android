package com.rozdoum.socialcomponents.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.ImageUtil;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String POST_EXTRA_KEY = "PostDetailsActivity.POST_EXTRA_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Post post = (Post) getIntent().getSerializableExtra(POST_EXTRA_KEY);

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        TextView descriptionEditText = (TextView) findViewById(R.id.descriptionEditText);
        ImageView postImageView = (ImageView) findViewById(R.id.postImageView);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        titleTextView.setText(post.getTitle());
        descriptionEditText.setText(post.getDescription());

        String imageUrl = post.getImagePath();

        ImageUtil imageUtil = ImageUtil.getInstance(this);
        imageUtil.getImage(imageUrl, postImageView, progressBar, R.drawable.ic_stub, R.drawable.ic_stub);

    }
}
