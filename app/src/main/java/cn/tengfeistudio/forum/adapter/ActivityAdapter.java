package cn.tengfeistudio.forum.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.beans.ActivityBean;
import cn.tengfeistudio.forum.widget.CircleImageView;


/**
 * 帖子列表adapter
 */
public class ActivityAdapter extends BaseAdapter {
    private List<ActivityBean> activityList;
    private Context context;

    public ActivityAdapter(Context context, List<ActivityBean> activityList) {
        this.context = context;
        this.activityList = activityList;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == activityList.size()) {
            return TYPE_LOADMORE;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return activityList.size() + 1;
    }

    @Override
    protected int getDataCount() {
        return activityList.size() > 0 ? 1 : activityList.size();
    }

    @Override
    protected int getItemType(int pos) {
        if (pos == activityList.size()) {
            return TYPE_LOADMORE;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
        NormalViewHolder viewHolder = new NormalViewHolder(view);
        return viewHolder;
    }

    //改变状态
    public void changeLoadMoreState(int i) {
        loadState = i;
        notifyItemChanged(0);
    }

    class NormalViewHolder extends BaseViewHolder {
        @BindView(R.id.article_title2)
        TextView articleTitle2;
        @BindView(R.id.author_img2)
        CircleImageView authorImg2;
        @BindView(R.id.author_name2)
        TextView authorName2;
        @BindView(R.id.post_time2)
        TextView postTime2;
        @BindView(R.id.reply_count2)
        TextView replyCount2;
        @BindView(R.id.view_count2)
        TextView viewCount2;

        NormalViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        void setData(int pos) {
            ActivityBean object = activityList.get(pos);
            articleTitle2.setText(" " + object.getActivityName());
            authorName2.setText(" " + object.getPlace());
            postTime2.setText(" " + object.getActivityTime());
            //replyCount.setText(" " + object.getCommentNumber());
            //viewCount.setText(" " + object.getViewNumber());
            Picasso.get().load(object.getLogoImage())
                    .placeholder(R.drawable.image_placeholder)
                    .into(authorImg2);
            //点击图片今日具体信息
//            authorImg2.setOnClickListener(view -> {
//                Intent intent = new Intent(context, UserDetailActivity.class);
//                intent.putExtra("userid",object.getActivityId());
//                context.startActivity(intent);
//            });
        }
    }
}
