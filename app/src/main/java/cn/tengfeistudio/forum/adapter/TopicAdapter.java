package cn.tengfeistudio.forum.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.beans.TopicBean;
import cn.tengfeistudio.forum.module.user.userdetail.UserDetailActivity;
import cn.tengfeistudio.forum.utils.StampToDate;
import cn.tengfeistudio.forum.utils.toast.GlobalDialog;
import cn.tengfeistudio.forum.widget.CircleImageView;

/**
 * 帖子列表adapter
 */
public class TopicAdapter extends BaseAdapter {
    private List<TopicBean> topicList;
    private Context context;

    public TopicAdapter(Context context, List<TopicBean> topicList) {
        this.context = context;
        this.topicList = topicList;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == topicList.size()) {
            return TYPE_LOADMORE;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return topicList.size() + 1;
    }

    @Override
    protected int getDataCount() {
        return topicList.size() > 0 ? 1 : topicList.size();
    }

    @Override
    protected int getItemType(int pos) {
        if (pos == topicList.size()) {
            return TYPE_LOADMORE;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        NormalViewHolder viewHolder = new NormalViewHolder(view);
        return viewHolder;
    }

    //改变状态
    public void changeLoadMoreState(int i) {
        loadState = i;
        notifyItemChanged(0);
    }

    class NormalViewHolder extends BaseViewHolder {
        @BindView(R.id.article_title)
        TextView articleTitle;
        @BindView(R.id.author_img)
        CircleImageView authorImg;
        @BindView(R.id.author_name)
        TextView authorName;
        @BindView(R.id.post_time)
        TextView postTime;
        @BindView(R.id.reply_count)
        TextView replyCount;
        @BindView(R.id.view_count)
        TextView viewCount;
        @BindView(R.id.rb_grade)
        RatingBar level;
        @BindView(R.id.article_content)
        TextView content;


        NormalViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        void setData(int pos) {
            TopicBean object = topicList.get(pos);
            articleTitle.setText(object.getTitle());
            authorName.setText(" " + object.getUserByUserId().getNickname());
            postTime.setText(" " + StampToDate.getStringDate(object.getCreateTime()));
            replyCount.setText(" " + object.getCommentNumber());
            viewCount.setText(" " + object.getViewNumber());
            level.setRating(object.getUserByUserId().getLevel());
            content.setText(""+object.getContent());
            Picasso.get()
                    .load(object.getUserByUserId().getIcon())
                    .placeholder(R.drawable.image_placeholder)
                    .into(authorImg);
            authorImg.setOnClickListener(view -> {
                Intent intent = new Intent(context, UserDetailActivity.class);
                intent.putExtra("userid", object.getUserByUserId().getUserId());
                context.startActivity(intent);
            });
        }




        @OnClick(R.id.delete)
        public void delete(View view) {
            final GlobalDialog delDialog = new GlobalDialog(context);
            delDialog.setCanceledOnTouchOutside(true);
            delDialog.getTitle().setText("提示");
            delDialog.getContent().setText("确定删除吗?");
            delDialog.setLeftBtnText("取消");
            delDialog.setRightBtnText("确定");
            delDialog.setLeftOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "取消", Toast.LENGTH_SHORT).show();
                    delDialog.dismiss();
                }
            });
            delDialog.setRightOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "确定", Toast.LENGTH_SHORT).show();
                    delDialog.dismiss();
                }
            });
            delDialog.show();
        }
    }
}
