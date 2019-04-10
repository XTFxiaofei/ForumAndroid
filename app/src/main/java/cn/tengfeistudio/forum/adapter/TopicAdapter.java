package cn.tengfeistudio.forum.adapter;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSONArray;
import com.squareup.picasso.Picasso;
import com.zzhoujay.richtext.RichText;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.beans.TopicBean;
import cn.tengfeistudio.forum.model.NineGridTestModel;
import cn.tengfeistudio.forum.module.post.postcontent.fullscreen.PostActivity;
import cn.tengfeistudio.forum.module.user.userdetail.UserDetailActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.IntentUtils;
import cn.tengfeistudio.forum.utils.NetConfig;
import cn.tengfeistudio.forum.utils.StampToDate;
import cn.tengfeistudio.forum.utils.toast.GlobalDialog;
import cn.tengfeistudio.forum.widget.CircleImageView;
import cn.tengfeistudio.forum.widget.NineGridTestLayout;

import static cn.tengfeistudio.forum.utils.LogUtils.printLog;
import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastNetWorkError;
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
        Button replyCount;
        @BindView(R.id.view_count)
        Button viewCount;
        @BindView(R.id.praise_count)
        Button praiseCount;
        @BindView(R.id.rb_grade)
        RatingBar level;
        @BindView(R.id.article_content)
        TextView content;
        @BindView(R.id.btn_more2)
        ImageView moreImage;
        @BindView(R.id.layout_nine_grid)
        NineGridTestLayout nineGridTestLayout;

        /** 9宫图model */
        private List<NineGridTestModel> mList = new ArrayList<>();
        private List<String> praiseUseridList=new ArrayList<>();


        NormalViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

       /** 初始化9图model数据 */
        private void initListData(List<String> list) {
            NineGridTestModel model = new NineGridTestModel();
            int len=list.size();
            for (int i = 0; i < len; i++) {
                model.urlList.add(list.get(i));
            }
            mList.add(model);
        }

        @Override
        void setData(int pos) {

            List<String> imgUrls = new ArrayList<>();

            TopicBean object = topicList.get(pos);
            imgUrls= JSONArray.parseArray(object.getContentPictureJson(),String.class);
            if(imgUrls!=null && imgUrls.size()>0){
                initListData(imgUrls);
                imgUrls.clear();
                nineGridTestLayout.setIsShowAll(mList.get(0).isShowAll);
                nineGridTestLayout.setUrlList(mList.get(0).urlList);
            }
            String praiseUsers=object.getPraiseAccountJson();
            //点赞的用户id数字
            String[] userids=praiseUsers.split(Constants.COMMA);
            if(userids.length>0){
                //点赞用户集合
                praiseUseridList= new ArrayList<>(Arrays.asList(userids));
                praiseUseridList.remove("");
            }
            //点赞数量
            int praiseNumber=praiseUseridList.size();
            if(object.getTitle().isEmpty()){
                articleTitle.setVisibility(View.GONE);
            }else {
                articleTitle.setText("# "+object.getTitle()+" #");
            }
            authorName.setText(" " + object.getUserByUserId().getNickname());
            postTime.setText(" " + StampToDate.getStringDate(object.getCreateTime()));
            //评论数量
            replyCount.setText("" + object.getCommentNumber());
            //阅读量,现在先用点赞计算  object.getViewNumber(),改成转发
           // viewCount.setText("" + object.getCommentNumber());
            viewCount.setText("转发");

            //点赞数量
            praiseCount.setText(""+praiseNumber);
            //已经点赞
            if(praiseUseridList.contains(String.valueOf(App.getUid()))){
                // 使用代码设置drawableleft
                Drawable drawable = context.getDrawable(R.drawable.praised);
                // / 这一步必须要做,否则不会显示.
                drawable.setBounds(0, 0,drawable.getMinimumWidth() , drawable.getMinimumHeight());
                praiseCount.setCompoundDrawables(drawable,null, null, null);
            }
            level.setRating(object.getUserByUserId().getLevel());
            if(object.getContent().isEmpty()){
                content.setVisibility(View.GONE);
            }else{
                RichText.fromMarkdown(object.getContent()).into(content);
            }
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
            //点赞
            praiseCount.setOnClickListener(view->{
                if(praiseUseridList.contains(String.valueOf(App.getUid()))){
                   // ToastShort("你已点赞");
                    return ;
                }else{
                    praiseCount.setText(""+(praiseNumber+1));
                    praiseUseridList.add(String.valueOf(App.getUid()));
                    // 使用代码设置drawableleft
                    Drawable drawable = context.getDrawable(R.drawable.praised);
                    // / 这一步必须要做,否则不会显示.
                    drawable.setBounds(0, 0,drawable.getMinimumWidth() , drawable.getMinimumHeight());
                    praiseCount.setCompoundDrawables(drawable,null, null, null);
                    RetrofitService.praiseTopic(object.getTopicId())
                            .subscribe(responseBody -> {
                                String response=responseBody.string();
                                if(!response.contains("code")){
                                    ToastNetWorkError();
                                }else {
                                    //ToastShort("点赞成功");
                                }
                            }, throwable -> {
                                printLog("TopicAdapter:" + throwable.getMessage());
                                ToastNetWorkError();
                            });
                }

            });
            //回复
            replyCount.setOnClickListener(view->{
                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra("topicId", object.getTopicId());
                intent.putExtra("isNormalPost", false);
                context.startActivity(intent);
            });
            //浏览量,改成转发了
            viewCount.setOnClickListener(view->{
//                Intent intent = new Intent(context, PostActivity.class);
//                intent.putExtra("topicId", object.getTopicId());
//                intent.putExtra("isNormalPost", false);
//                context.startActivity(intent);
                String data = "这篇文章不错，分享给你们 【" +" \n链接地址："+ NetConfig.SHARE_TOPIC + object.getTopicId() + "】\n来自广财校园吧";
                IntentUtils.sharePost(context, data);

            });
            //点击更多图
            moreImage.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(context, view);
                popup.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        //编辑
                        case R.id.tv_edit:
                            RetrofitService.deleteTopic(object.getTopicId(), Constants.REPORT_FLAG)
                                    .subscribe(responseBody -> {
                                        String response=responseBody.string();
                                        if(!response.contains("code")){
                                            ToastNetWorkError();
                                        }else {
                                            ToastShort("OK已举报!");
                                        }
                                    }, throwable -> {
                                        printLog("TopicAdapter:" + throwable.getMessage());
                                        ToastNetWorkError();
                                    });
                            break;
                        //复制
                        case R.id.tv_copy:
                            String user = object.getUserByUserId().getNickname();
                            String s = content.getText().toString();
                            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cm != null) {
                                cm.setPrimaryClip(ClipData.newPlainText(null, s));
                                ToastShort("已复制" + user + "的内容");
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
                                    //Toast.makeText(context, "取消", Toast.LENGTH_SHORT).show();
                                    delDialog.dismiss();
                                }
                            });
                            delDialog.setRightOnclick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    RetrofitService.deleteTopic(object.getTopicId(), Constants.DELETE_FLAG)
                                                    .subscribe(responseBody -> {
                                                        String response=responseBody.string();
                                                        if(!response.contains("code")){
                                                            ToastNetWorkError();
                                                        }else {
                                                            Toast.makeText(context, "已删除,要刷新(⊙o⊙)", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, throwable -> {
                                                        printLog("TopicAdapter:" + throwable.getMessage());
                                                        ToastNetWorkError();
                                                    });

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
                if(object.getUserByUserId().getUserId()== App.getUid() || App.getRole().equals("admin")){
                    // 如果有管理权限,则显示删除
                    // popup.getMenu().removeGroup(R.id.menu_manege);
                }else{
                    popup.getMenu().removeGroup(R.id.menu_manege);
                }
                // popup.getMenu().removeItem(R.id.tv_edit);
                popup.show();
            });
        }



    }
}
