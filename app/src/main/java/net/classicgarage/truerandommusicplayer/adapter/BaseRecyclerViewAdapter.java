package net.classicgarage.truerandommusicplayer.adapter;

/**
 * Created by eaton on 27/10/2017.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.listener.OnItemClickListener;
import net.classicgarage.truerandommusicplayer.listener.OnLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerViewAdapter<T, VH extends SongAdapter.BaseViewHolder>
        extends RecyclerView.Adapter<SongAdapter.BaseViewHolder>
        implements OnLoadMoreListener {

    protected static final String TAG = "BaseRecyclerViewAdapter";

    protected static final int EMPTY_VIEW = 1 << 5;
    protected static final int LOADING_VIEW = 1 << 6;
    protected static final int FOOTER_VIEW = 1 << 7;
    protected static final int HEADER_VIEW = 1 << 8;

    private final ArrayList<AdapterView.OnItemClickListener> mOnItemClickListeners =
            new ArrayList<>();

    private boolean mLoading = false;
    private boolean mEmptyEnable;

    private View mHeaderView;
    private View mFooterView;
    private View mEmptyView;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<T> mItems;

    public BaseRecyclerViewAdapter(Context context){ this(context, null); }

    /**
     * initialization
     *
     * @param context
     * @param items
     */
    public BaseRecyclerViewAdapter(Context context, List<T> items) {
        this.mItems = items == null ? new ArrayList<T>() : new ArrayList<T>(items);
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    public List<T> getItems() {
        return mItems;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private int getHeaderViewsCount() {
        return mHeaderView == null ? 0 : 1;
    }

    private int getFooterViewsCount() {
        return mFooterView == null ? 0 : 1;
    }

    private int getEmptyViewCount() {
        return mEmptyView == null ? 0 : 1;
    }

    protected boolean isEmpty() {
        return getHeaderViewsCount() + getFooterViewsCount() + getItems().size() == 0;
    }

    @Override
    public int getItemCount() {
        int count;
        if (mLoading) { //if loading ignore footer view
            count = mItems.size() + 1 + getHeaderViewsCount();
        } else {
            count = mItems.size() + getHeaderViewsCount() + getFooterViewsCount();
        }
        mEmptyEnable = false;
        if (count == 0) {
            mEmptyEnable = true;
            count += getEmptyViewCount();
        }
        return count;
    }

    @Override
    public final int getItemViewType(int position) {
        if (mHeaderView != null && position == 0) {
            return HEADER_VIEW;
        } else if (mEmptyView != null && getItemCount() == 1 && mEmptyEnable) {
            return EMPTY_VIEW;
        } else if (position == mItems.size() + getHeaderViewsCount()) {
            if (mLoading) {
                return LOADING_VIEW;
            } else if (mFooterView != null) {
                return FOOTER_VIEW;
            }
        }
        return getDefItemViewType(position);
    }

    protected int getDefItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public SongAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SongAdapter.BaseViewHolder vh;
        switch (viewType) {
            case LOADING_VIEW:
                vh = onCreateLoadingViewHolder(parent);
                if (vh == null) {
                    vh = createBaseViewHolder(parent, R.layout.footer_item_default_loading);
                }
                break;
            case EMPTY_VIEW:
                vh = new SongAdapter.BaseViewHolder(mEmptyView);
                break;
            case FOOTER_VIEW:
                vh = new SongAdapter.BaseViewHolder(mFooterView);
                break;
            case HEADER_VIEW:
                vh = new SongAdapter.BaseViewHolder(mHeaderView);
                break;
            default:
                vh = onCreateDifViewHolder(parent, viewType);
                dispatchItemClickListener(vh);
                break;
        }
        return vh;
    }

    /**
     * custom Loading Footer
     * @param parent
     * @return
     */
    private VH onCreateLoadingViewHolder(ViewGroup parent) {
        return null;
    }

    /**
     * create different view holder
     * @param parent
     * @param viewType
     * @return
     */
    abstract protected VH onCreateDifViewHolder(ViewGroup parent, int viewType);

    @NonNull
    private SongAdapter.BaseViewHolder createBaseViewHolder(ViewGroup parent, int layoutResId) {
        return new SongAdapter.BaseViewHolder(inflateItemView(layoutResId, parent));
    }

    /**
     * @param layoutResId
     * @param parent
     * @return
     */
    View inflateItemView(int layoutResId, ViewGroup parent) {
        return mLayoutInflater.inflate(layoutResId, parent, false);
    }

    @Override
    public void onBindViewHolder(SongAdapter.BaseViewHolder holder, int position) {
        switch (holder.getItemViewType()) {

            case LOADING_VIEW:
                break;
            case HEADER_VIEW:
                break;
            case EMPTY_VIEW:
                break;
            case FOOTER_VIEW:
                break;
            default:
                convert((VH) holder, mItems.get(holder.getLayoutPosition() - getHeaderViewsCount()));
                break;
        }
    }

    /**
     * Implement this method and use the holder to adapt the view to the given item.
     *
     * @param holder A fully initialized holder.
     * @param item   The item that needs to be displayed.
     */
    abstract protected void convert(VH holder, T item);

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    BaseRecyclerViewAdapter adapter = (BaseRecyclerViewAdapter) recyclerView.getAdapter();
                    if (isFullSpanType(adapter.getItemViewType(position))) {
                        return gridLayoutManager.getSpanCount();
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(SongAdapter.BaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        int type = getItemViewType(position);
        if (isFullSpanType(type)) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                lp.setFullSpan(true);
            }
        }
    }

    private boolean isFullSpanType(int type) {
        return type == HEADER_VIEW ||
                type == FOOTER_VIEW ||
                type == LOADING_VIEW ||
                type == EMPTY_VIEW;
    }

    @Override
    public void onLoadingMore() {
        if (!mLoading) {
            mLoading = true;
            notifyDataSetChanged();
        }
    }

    private void dispatchItemClickListener(final SongAdapter.BaseViewHolder viewHolder) {

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListeners != null && mOnItemClickListeners.size() > 0) {
                    for (int i = 0; i < mOnItemClickListeners.size(); i++) {
                        final OnItemClickListener listener = (OnItemClickListener) mOnItemClickListeners.get(i);
                        listener.onItemClick(viewHolder, viewHolder.getLayoutPosition() - getHeaderViewsCount());
                    }
                }
            }
        });

        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemClickListeners != null && mOnItemClickListeners.size() > 0) {
                    for (int i = 0; i < mOnItemClickListeners.size(); i++) {
                        final OnItemClickListener listener = (OnItemClickListener) mOnItemClickListeners.get(i);
                        listener.onItemLongClick(viewHolder, viewHolder.getLayoutPosition() - getHeaderViewsCount());
                    }
                    return true;
                }
                return false;
            }
        });
    }
}

