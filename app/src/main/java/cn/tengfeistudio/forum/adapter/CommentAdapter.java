package cn.tengfeistudio.forum.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zzhoujay.richtext.RichText;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.beans.CommentBean;
import cn.tengfeistudio.forum.module.user.userdetail.UserDetailActivity;
import cn.tengfeistudio.forum.utils.DateUtils;
import cn.tengfeistudio.forum.utils.StampToDate;
import cn.tengfeistudio.forum.utils.toast.ToastUtils;
import cn.tengfeistudio.forum.widget.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<CommentBean> commentList;
    private Context context;
    private OnItemClickListener mItemClickListener = null;
    // 楼主ID
    private long lzid;

    public CommentAdapter(Context context, List<CommentBean> commentList, long lzid) {
        this.context = context;
        this.commentList = commentList;
        this.lzid = lzid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, mItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CommentBean object = commentList.get(position);
        if (object.getComment().getFromId()== lzid)
            holder.btLableLz.setVisibility(View.VISIBLE);
        else
            holder.btLableLz.setVisibility(View.GONE);
        //评论人的昵称
        holder.replayAuthor.setText(object.getComment().getUserByFromId().getNickname());
        //第几楼
        holder.replayIndex.setText(position + 1 + "#");
        //评论时间
        if (object.getComment().getCreateTime().equals(null))
            holder.replayTime.setText(DateUtils.getFromNowOnTime(object.getComment().getCreateTime()));
        else
            holder.replayTime.setText(StampToDate.stampToDate(String.valueOf(object.getComment().getCreateTime())));
        RichText.fromMarkdown(object.getComment().getCommentContent()).into(holder.markdownText);
        Picasso.get()
                .load(object.getComment().getUserByFromId().getIcon())
                .placeholder(R.drawable.image_placeholder)
                .into(holder.articleUserImage);

        //复制评论内容
        holder.markdownText.setOnLongClickListener(view -> {
            String user = object.getComment().getUserByFromId().getNickname();
            String content = object.getComment().getCommentContent();
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText(null, content));
                ToastUtils.ToastShort("已复制" + user + "的评论");
            }
            return true;
        });
        //评论人头像
        holder.articleUserImage.setOnClickListener(view -> {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra("userid", object.getComment().getUserByFromId().getUserId());
            context.startActivity(intent);
        });
        holder.btnReplyCz.setOnClickListener(holder);
        holder.btnMore.setOnClickListener(holder);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        @BindView(R.id.article_user_image)
        CircleImageView articleUserImage;
        @BindView(R.id.replay_author)
        TextView replayAuthor;
        @BindView(R.id.bt_lable_lz)
        TextView btLableLz;
        @BindView(R.id.btn_reply_cz)
        ImageView btnReplyCz;
        @BindView(R.id.btn_more)
        ImageView btnMore;
        @BindView(R.id.replay_index)
        TextView replayIndex;
        @BindView(R.id.replay_time)
        TextView replayTime;
        @BindView(R.id.markdown_text)
        TextView markdownText;
        @BindView(R.id.main_window)
        ConstraintLayout mainWindow;

        OnItemClickListener mItemClickListener;

        ViewHolder(View view, OnItemClickListener listener) {
            super(view);
            ButterKnife.bind(this, view);
            this.mItemClickListener = listener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

}
