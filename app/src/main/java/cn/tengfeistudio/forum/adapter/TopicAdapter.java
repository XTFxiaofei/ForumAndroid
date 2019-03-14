package cn.tengfeistudio.forum.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import com.alibaba.fastjson.JSONArray;
import com.jaeger.ninegridimageview.ItemImageClickListener;
import com.jaeger.ninegridimageview.ItemImageLongClickListener;
import com.jaeger.ninegridimageview.NineGridImageView;
import com.jaeger.ninegridimageview.NineGridImageViewAdapter;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.Arrays;
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

import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastShort;

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
        @BindView(R.id.btn_more2)
        ImageView moreImage;
        @BindView(R.id.ngl_images)
        NineGridImageView<String> mNglContent;

        //九格图适配器
        private NineGridImageViewAdapter<String> mAdapter = new NineGridImageViewAdapter<String>() {
            @Override
            protected void onDisplayImage(Context context, ImageView imageView, String s) {
                Picasso.get().load(s).placeholder(R.drawable.image_placeholder).into(imageView);
            }

            @Override
            protected ImageView generateImageView(Context context) {
                return super.generateImageView(context);
            }

            @Override
            protected void onItemImageClick(Context context, ImageView imageView, int index, List<String> list) {
                Toast.makeText(context, "image position is " + index, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected boolean onItemImageLongClick(Context context, ImageView imageView, int index, List<String> list) {
                Toast.makeText(context, "image long click position is " + index, Toast.LENGTH_SHORT).show();
                return true;
            }
        };


        NormalViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            //九格图
            mNglContent.setAdapter(mAdapter);
            mNglContent.setItemImageClickListener(new ItemImageClickListener<String>() {
                @Override
                public void onItemImageClick(Context context, ImageView imageView, int index, List<String> list) {
                    Log.d("onItemImageClick", list.get(index));
                }
            });
            mNglContent.setItemImageLongClickListener(new ItemImageLongClickListener<String>() {
                @Override
                public boolean onItemImageLongClick(Context context, ImageView imageView, int index, List<String> list) {
                    Log.d("onItemImageLongClick", list.get(index));
                    return true;
                }
            });
        }
        private String[] IMG_URL_LIST = {
                "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=935292084,2640874667&fm=27&gp=0.jpg", "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=873265023,1618187578&fm=27&gp=0.jpg",
                "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=935292084,2640874667&fm=27&gp=0.jpg", "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=873265023,1618187578&fm=27&gp=0.jpg",
                "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=935292084,2640874667&fm=27&gp=0.jpg", "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=873265023,1618187578&fm=27&gp=0.jpg",
                "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=935292084,2640874667&fm=27&gp=0.jpg", "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=873265023,1618187578&fm=27&gp=0.jpg",
                "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=935292084,2640874667&fm=27&gp=0.jpg", "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=873265023,1618187578&fm=27&gp=0.jpg",
        };

        @Override
        void setData(int pos) {
            //["http://10.0.2.2:8080/upload/imgs/1552591792843/1552591792843_933.JPEG","http://10.0.2.2:8080/upload/imgs/1552591792869/1552591792869_462.jpg","http://10.0.2.2:8080/upload/imgs/1552591792886/1552591792886_953.jpg"]
            List<String> imgUrls = new ArrayList<>();
            //imgUrls.addAll(Arrays.asList(IMG_URL_LIST));


            TopicBean object = topicList.get(pos);
            imgUrls= JSONArray.parseArray(object.getContentPictureJson(),String.class);
            mNglContent.setImagesData(imgUrls, NineGridImageView.STYLE_GRID);

            articleTitle.setText(object.getTitle());
            authorName.setText(" " + object.getUserByUserId().getNickname());
            postTime.setText(" " + StampToDate.getStringDate(object.getCreateTime()));
            replyCount.setText(" " + object.getCommentNumber());
            viewCount.setText(" " + object.getViewNumber());
            level.setRating(object.getUserByUserId().getLevel());
            content.setText("" + object.getContent());
            Picasso.get()
                    .load(object.getUserByUserId().getIcon())
                    .placeholder(R.drawable.image_placeholder)
                    .into(authorImg);
            //点击头像
            authorImg.setOnClickListener(view -> {
                Intent intent = new Intent(context, UserDetailActivity.class);
                intent.putExtra("userid", object.getUserByUserId().getUserId());
                context.startActivity(intent);
            });
            //点击更多图
            moreImage.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(context, view);
                popup.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        //编辑
                        case R.id.tv_edit:
                            break;
                        //复制
                        case R.id.tv_copy:
                            String user = object.getUserByUserId().getNickname();
                            String s = content.getText().toString();
                            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cm != null) {
                                cm.setPrimaryClip(ClipData.newPlainText(null, s));
                                ToastShort("已复制" + user + "的评论");
                            }
                            break;
                        //删除
                        case R.id.tv_remove:
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
                            break;
                    }
                    return true;
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_post_more, popup.getMenu());
                // 判断是不是本人
                // popup.getMenu().removeItem(R.id.tv_edit);
                // 如果有管理权限,则显示删除
                // popup.getMenu().removeGroup(R.id.menu_manege);
                popup.show();
            });
        }

    }
}
