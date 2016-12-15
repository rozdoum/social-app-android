package com.rozdoum.socialcomponents.adapters;

import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.enums.ItemType;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.ImageUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kristina on 10/31/16.
 */

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = PostsAdapter.class.getSimpleName();

    private List<Post> postList = new LinkedList<>();
    private ImageUtil imageUtil;
    private OnItemClickListener onItemClickListener;
    private Activity activity;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;
    private SwipeRefreshLayout swipeContainer;

    public PostsAdapter(Activity activity, SwipeRefreshLayout swipeContainer) {
        this.activity = activity;
        imageUtil = ImageUtil.getInstance(activity.getApplicationContext());
        this.swipeContainer = swipeContainer;

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFirstPage();
            }
        });
    }

    private class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;
        TextView titleTextView;
        TextView detailsTextView;
        ImageLoader.ImageContainer imageRequest;

        PostViewHolder(View view) {
            super(view);

            postImageView = (ImageView) view.findViewById(R.id.postImageView);
            titleTextView = (TextView) view.findViewById(R.id.titleTextView);
            detailsTextView = (TextView) view.findViewById(R.id.detailsTextView);

            if (onItemClickListener != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onItemClick(getItemByPosition(getAdapterPosition()));
                    }
                });
            }
        }

        void bindData(Post post) {
            titleTextView.setText(post.getTitle());
            detailsTextView.setText(post.getDescription());


            if (imageRequest != null) {
                imageRequest.cancelRequest();
            }

            String imageUrl = post.getImagePath();
            imageRequest = imageUtil.getImageThumb(imageUrl, postImageView, R.drawable.ic_stub, R.drawable.ic_stub);
        }
    }

    private class LoadViewHolder extends RecyclerView.ViewHolder {
        LoadViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ItemType.ITEM.getTypeCode()) {
            return new PostViewHolder(inflater.inflate(R.layout.post_item_list_view, parent, false));
        } else {
            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading) {
            isLoading = true;
            long lastItemCreatedDate = postList.get(postList.size() - 1).getCreatedDate();
            long nextItemCreatedDate = lastItemCreatedDate - 1;

            android.os.Handler mHandler = activity.getWindow().getDecorView().getHandler();
            mHandler.post(new Runnable() {
                public void run() {
                    //change adapter contents
                    postList.add(new Post(ItemType.LOAD));
                    notifyItemInserted(postList.size());
                }
            });

            loadMore(nextItemCreatedDate);
        }

        if (getItemViewType(position) != ItemType.LOAD.getTypeCode()) {
            ((PostViewHolder) holder).bindData(postList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // TODO: 09.12.16 remove after clearing DB
        if (postList.get(position).getItemType() == null) {
            return ItemType.ITEM.getTypeCode();
        }

        return postList.get(position).getItemType().getTypeCode();
    }

    private Post getItemByPosition(int position) {
        return postList.get(position);
    }

    private void addList(List<Post> list) {
        this.postList.addAll(list);
        notifyDataSetChanged();
        isLoading = false;
    }

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    public void loadFirstPage() {
        loadMore(0);
    }

    private void loadMore(final long nextItemCreatedDate) {
        OnDataChangedListener<Post> onPostsDataChangedListener = new OnDataChangedListener<Post>() {
            @Override
            public void onListChanged(List<Post> list) {

                if (nextItemCreatedDate == 0) {
                    postList.clear();
                    swipeContainer.setRefreshing(false);
                }

                //remove loading view
                if (!postList.isEmpty() && getItemViewType(postList.size() - 1) == ItemType.LOAD.getTypeCode()) {
                    postList.remove(postList.size() - 1);
                    notifyItemRemoved(postList.size() - 1);
                }

                if (!list.isEmpty()) {
                    addList(list);
                    isMoreDataAvailable = true;
                } else {
                    isMoreDataAvailable = false;
                }


            }
        };

        PostManager.getInstance(activity).getPosts(onPostsDataChangedListener, nextItemCreatedDate);
    }
}
