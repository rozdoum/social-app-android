package com.rozdoum.socialcomponents.adapters;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.activities.MainActivity;
import com.rozdoum.socialcomponents.adapters.holders.LoadViewHolder;
import com.rozdoum.socialcomponents.adapters.holders.PostViewHolder;
import com.rozdoum.socialcomponents.controllers.LikeController;
import com.rozdoum.socialcomponents.enums.ItemType;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.model.Post;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kristina on 10/31/16.
 */

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = PostsAdapter.class.getSimpleName();

    private List<Post> postList = new LinkedList<>();
    private Callback callback;
    private MainActivity activity;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;
    private SwipeRefreshLayout swipeContainer;
    private int selectedPostPosition = -1;

    public PostsAdapter(final MainActivity activity, SwipeRefreshLayout swipeContainer) {
        this.activity = activity;
        this.swipeContainer = swipeContainer;
        initRefreshLayout();
    }

    private void initRefreshLayout() {
        if (swipeContainer != null) {
            this.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefreshAction();
                }
            });
        }
    }

    private void onRefreshAction() {
        if (activity.hasInternetConnection()) {
            loadFirstPage();
            cleanSelectedPostInformation();
        } else {
            swipeContainer.setRefreshing(false);
            activity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
        }
    }

    private void cleanSelectedPostInformation() {
        selectedPostPosition = -1;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ItemType.ITEM.getTypeCode()) {
            return new PostViewHolder(inflater.inflate(R.layout.post_item_list_view, parent, false),
                    createOnItemClickListener());
        } else {
            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
        }
    }

    private PostViewHolder.OnClickListener createOnItemClickListener() {
        return new PostViewHolder.OnClickListener() {
            @Override
            public void onItemClick(int position) {
                if (callback != null) {
                    selectedPostPosition = position;
                    callback.onItemClick(getItemByPosition(position));
                }
            }

            @Override
            public void onLikeClick(LikeController likeController, int position) {
                Post post = getItemByPosition(position);
                likeController.handleLikeClickAction(activity, post);
            }

            @Override
            public void onAuthorClick(int position) {
                if (callback != null) {
                    callback.onAuthorClick(getItemByPosition(position).getAuthorId());
                }
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading) {
            long lastItemCreatedDate = postList.get(postList.size() - 1).getCreatedDate();
            final long nextItemCreatedDate = lastItemCreatedDate - 1;

            android.os.Handler mHandler = activity.getWindow().getDecorView().getHandler();
            mHandler.post(new Runnable() {
                public void run() {
                    //change adapter contents
                    if (activity.hasInternetConnection()) {
                        isLoading = true;
                        postList.add(new Post(ItemType.LOAD));
                        notifyItemInserted(postList.size());
                        loadNext(nextItemCreatedDate);
                    } else {
                        activity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
                    }
                }
            });


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

    public void loadFirstPage() {
        loadNext(0);
    }

    private void loadNext(final long nextItemCreatedDate) {

        if (!activity.hasInternetConnection()) {
            activity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
            hideProgress();
            callback.onListLoadingFinished();
            return;
        }

        OnDataChangedListener<Post> onPostsDataChangedListener = new OnDataChangedListener<Post>() {
            @Override
            public void onListChanged(List<Post> list) {

                if (nextItemCreatedDate == 0) {
                    postList.clear();
                    notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
                }

                hideProgress();

                if (!list.isEmpty()) {
                    addList(list);
                    isMoreDataAvailable = true;
                } else {
                    isMoreDataAvailable = false;
                }

                callback.onListLoadingFinished();
            }
        };

        PostManager.getInstance(activity).getPostsList(onPostsDataChangedListener, nextItemCreatedDate);
    }

    private void hideProgress() {
        if (!postList.isEmpty() && getItemViewType(postList.size() - 1) == ItemType.LOAD.getTypeCode()) {
            postList.remove(postList.size() - 1);
            notifyItemRemoved(postList.size() - 1);
        }
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
        if (selectedPostPosition != -1) {
            Post selectedPost = getItemByPosition(selectedPostPosition);
            PostManager.getInstance(activity).getSinglePostValue(selectedPost.getId(), createOnPostChangeListener(selectedPostPosition));
        }
    }

    public void removeSelectedPost() {
        postList.remove(selectedPostPosition);
        notifyItemRemoved(selectedPostPosition);
    }

    public interface Callback {
        void onItemClick(Post post);

        void onListLoadingFinished();

        void onAuthorClick(String authorId);
    }
}
