package com.rozdoum.socialcomponents.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.PostsAdapter;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.model.Post;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView postsListView;
    private PostsAdapter postsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.addNewPostFab);

        if (floatingActionButton != null) {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCreatePostActivity();
                }
            });
        }

        postsListView = (ListView)findViewById(R.id.postsListView);
        postsAdapter = new PostsAdapter();
        postsListView.setAdapter(postsAdapter);

        OnDataChangedListener<Post> onPostsDataChangedListener = new OnDataChangedListener<Post>() {
            @Override
            public void onListChanged(List<Post> list) {
                postsAdapter.setList(list);
            }
        };

        PostManager.getInstance(getApplicationContext()).getPosts(onPostsDataChangedListener);

        postsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PostDetailsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void openCreatePostActivity() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivity(intent);
    }
}
