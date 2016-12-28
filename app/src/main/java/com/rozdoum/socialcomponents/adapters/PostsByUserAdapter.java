package com.rozdoum.socialcomponents.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.activities.BaseActivity;
import com.rozdoum.socialcomponents.adapters.holders.PostViewHolder;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.model.Post;

import java.util.LinkedList;
import java.util.List;


public class PostsByUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = PostsByUserAdapter.class.getSimpleName();

    private List<Post> postList = new LinkedList<>();
    private CallBack callBack;
    private BaseActivity activity;
    private String userId;
    private OnObjectChangedListener<Post> onSelectedPostChangeListener;
    private int selectedPostPosition = -1;

    public PostsByUserAdapter(final BaseActivity activity, String userId) {
        this.activity = activity;
        this.userId = userId;
    }

    public void setOnItemClickListener(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.post_item_list_view, parent, false);

        return new PostViewHolder(view, createOnItemClickListener(), false);
    }

    private PostViewHolder.OnItemClickListener createOnItemClickListener() {
        return new PostViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (callBack != null) {
                    selectedPostPosition = position;
                    onSelectedPostChangeListener = createOnPostChangeListener(selectedPostPosition);
                    callBack.onItemClick(getItemByPosition(position));
                }
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((PostViewHolder) holder).bindData(postList.get(position));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    private Post getItemByPosition(int position) {
        return postList.get(position);
    }

    private void addList(List<Post> list) {
        this.postList.addAll(list);
        notifyDataSetChanged();
    }

    public void loadPosts() {
        if (!activity.hasInternetConnection()) {
            activity.showSnackBar(R.string.internet_connection_failed);
            return;
        }

        OnDataChangedListener<Post> onPostsDataChangedListener = new OnDataChangedListener<Post>() {
            @Override
            public void onListChanged(List<Post> list) {
                addList(list);
                callBack.onPostsLoaded(list.size());
            }
        };

        PostManager.getInstance(activity).getPostsListByUser(onPostsDataChangedListener, userId);
    }

    public interface CallBack {
        void onItemClick(Post post);

        void onPostsLoaded(int postsCount);
    }

    private OnObjectChangedListener<Post> createOnPostChangeListener(final int postPosition) {
        return new OnObjectChangedListener<Post>() {
            @Override
            public void onObjectChanged(Post obj) {
                postList.set(postPosition, obj);
                notifyItemChanged(postPosition);
            }
        };
    }

    public void updateSelectedPost() {
        if (onSelectedPostChangeListener != null && selectedPostPosition != -1) {
            Post selectedPost = getItemByPosition(selectedPostPosition);
            PostManager.getInstance(activity).getSinglePostValue(selectedPost.getId(), createOnPostChangeListener(selectedPostPosition));
        }
    }
}
