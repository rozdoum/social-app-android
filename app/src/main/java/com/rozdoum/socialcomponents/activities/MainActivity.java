package com.rozdoum.socialcomponents.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.PostsAdapter;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.model.Post;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PostsAdapter postsAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

//        mAuth = FirebaseAuth.getInstance();
//        user = mAuth.getCurrentUser();
//        if (user == null) {
//            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//        } else {
            initContentView();
//        }
    }

    private void  initContentView() {
        if (recyclerView == null) {
            FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.addNewPostFab);

            if (floatingActionButton != null) {
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openCreatePostActivity();
                    }
                });
            }

            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            postsAdapter = new PostsAdapter(this);

            postsAdapter.setOnItemClickListener(new PostsAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(Post post) {
                    Intent intent = new Intent(MainActivity.this, PostDetailsActivity.class);
                    intent.putExtra(PostDetailsActivity.POST_EXTRA_KEY, post);
                    startActivity(intent);
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(postsAdapter);

            recyclerView.setAdapter(postsAdapter);

            OnDataChangedListener<Post> onPostsDataChangedListener = new OnDataChangedListener<Post>() {
                @Override
                public void onListChanged(List<Post> list) {
                    postsAdapter.setList(list);
                }
            };

            PostManager.getInstance(getApplicationContext()).getPosts(onPostsDataChangedListener);
        }
    }

    private void openCreatePostActivity() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivity(intent);
    }
}
