package cn.tengfeistudio.forum.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.bean.Post;
import cn.tengfeistudio.forum.module.user.userdetail.UserDetailActivity;
import cn.tengfeistudio.forum.widget.CircleImageView;

/**
 * 帖子列表adapter
 */
public class PostAdapter extends BaseAdapter {
    private List<Post> postList;
    private Context context;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == postList.size()) {
            return TYPE_LOADMORE;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return postList.size() + 1;
    }

    @Override
    protected int getDataCount() {
        return postList.size() > 0 ? 1 : postList.size();
    }

    @Override
    protected int getItemType(int pos) {
        if (pos == postList.size()) {
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

        NormalViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        void setData(int pos) {
            Post object = postList.get(pos);
            articleTitle.setText(object.getTitle());
            authorName.setText(" " + object.getUser().getName());
            postTime.setText(" " + object.getCreated_at());
            replyCount.setText(" " + object.getComments_total());
            viewCount.setText(" " + object.getPageViewsCount());
            Picasso.get()
                    .load(object.getUser().getAvatar())
                    .placeholder(R.drawable.image_placeholder)
                    .into(authorImg);
            authorImg.setOnClickListener(view -> {
                Intent intent = new Intent(context, UserDetailActivity.class);
                intent.putExtra("userid",object.getUser().getId());
                context.startActivity(intent);
            });
        }
    }
}
