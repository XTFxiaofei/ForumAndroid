package cn.tengfeistudio.forum.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.listener.ListItemClickListener;

import static cn.tengfeistudio.forum.adapter.BaseAdapter.STATE_LOADING;
import static cn.tengfeistudio.forum.adapter.BaseAdapter.STATE_LOAD_FAIL;
import static cn.tengfeistudio.forum.adapter.BaseAdapter.STATE_LOAD_NOTHING;
import static cn.tengfeistudio.forum.adapter.BaseAdapter.STATE_LOAD_OK;
import static cn.tengfeistudio.forum.adapter.BaseAdapter.STATE_NEED_LOGIN;
import static cn.tengfeistudio.forum.adapter.BaseAdapter.TYPE_LOADMORE;
import static cn.tengfeistudio.forum.adapter.BaseAdapter.TYPE_NO_DATA;
import static cn.tengfeistudio.forum.utils.LogUtils.printLog;


/**
 * 简单封装
 */

public abstract class BaseAdapter2 extends RecyclerView.Adapter<BaseAdapter2.BaseViewHolder>{
    private ListItemClickListener itemListener;

//    static final int TYPE_LOADMORE = 1001;
//    static final int TYPE_NO_DATA = 1002;
//    static final int TYPE_NORMAL =1003;


//    public static final int STATE_LOADING = 1;
//    public static final int STATE_LOAD_FAIL = 2;
//    public static final int STATE_LOAD_NOTHING = 3;
//    public static final int STATE_LOAD_OK = 4;
//    public static final int STATE_NEED_LOGIN = 5;

    int loadState = STATE_LOADING;

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_LOADMORE:
            case TYPE_NO_DATA:
                return new LoadMoreViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_load_more2, parent, false));
            default:
                return getItemViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && getDataCount() == 0) {
            return TYPE_NO_DATA;
        }
        if (position == getItemCount() - 1) {
            return TYPE_LOADMORE;
        }
        return getItemType(position);
    }

    @Override
    public int getItemCount() {
        int count = getDataCount();
        if (count == 0) {
            return 1;
        }else {
            return count;
        }
    }

    protected abstract int getDataCount();

    protected abstract int getItemType(int pos);

    protected abstract BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType);

    // 改变状态
    public void changeLoadMoreState(int state) {
        this.loadState = state;
        int pos = getItemCount() - 1;
        if (pos >= 0 && getItemViewType(pos) == TYPE_LOADMORE) {
            notifyItemChanged(pos);
        }
    }

    public void setOnItemClickListener(ListItemClickListener listener) {
        this.itemListener = listener;
    }

    // 加载更多ViewHolder
    class LoadMoreViewHolder extends BaseViewHolder {
        @BindView(R.id.main_container2)
        LinearLayout container;
        @BindView(R.id.load_more_progress2)
        ProgressBar progressBar;


        LoadMoreViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        void setData(int pos) {
            // 不是第一次加载
            container.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            switch (loadState) {
                case STATE_LOAD_FAIL:
                    progressBar.setVisibility(View.GONE);
                    break;
                case STATE_NEED_LOGIN:
                    progressBar.setVisibility(View.GONE);
                    // 没有数据填充第一次加载
                    if (getItemCount() == 1){
                        container.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    break;
                case STATE_LOAD_OK:
                    progressBar.setVisibility(View.GONE);
                    break;
                case STATE_LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case STATE_LOAD_NOTHING:
                    // 没有数据填充无数据
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        }
    }

    abstract class BaseViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        BaseViewHolder(View itemView) {
            super(itemView);
            if (itemListener != null)
                itemView.setOnClickListener(this);
        }

        void setData(int pos) {

        }

        @Override
        public void onClick(View view) {

            if (itemListener != null) {
                try {
                    itemListener.onListItemClick(view, this.getAdapterPosition());
                } catch (Exception e) {
                    printLog("点击 暂无更多 出现异常");
                    //e.printStackTrace();
                    return ;
                }
            }
        }
    }
}
