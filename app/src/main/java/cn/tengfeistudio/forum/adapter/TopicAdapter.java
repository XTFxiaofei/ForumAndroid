package cn.tengfeistudio.forum.adapter;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSONArray;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.squareup.picasso.Picasso;
import com.zzhoujay.richtext.RichText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.beans.TopicBean;
import cn.tengfeistudio.forum.module.post.postcontent.main.SecondActivity;
import cn.tengfeistudio.forum.module.user.userdetail.UserDetailActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.StampToDate;
import cn.tengfeistudio.forum.utils.toast.GlobalDialog;
import cn.tengfeistudio.forum.utils.toast.ToastUtils;
import cn.tengfeistudio.forum.widget.CircleImageView;

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
        @BindView(R.id.rv)
        RecyclerView rv;

        private List<String> images=new ArrayList<String>();//图片地址
        private DisplayImageOptions options;
        private MyRecyclerViewAdapter adapter;
        private HashMap<Integer, float[]> xyMap=new HashMap<Integer, float[]>();//所有子项的坐标
        private int screenWidth;//屏幕宽度
        private int screenHeight;//屏幕高度





        NormalViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            onResume();
        }

        protected void onResume() {
           // super.onResume();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            screenWidth = wm.getDefaultDisplay().getWidth();
            screenHeight = wm.getDefaultDisplay().getHeight();
        }

        /**
         * recyclerView item点击事件
         */
        private void setEvent() {
            adapter.setmOnItemClickListener(new MyRecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    Intent intent=new Intent(context,SecondActivity.class);
                    intent.putStringArrayListExtra("urls", (ArrayList<String>) images);
                    intent.putExtra("position", position);
                    xyMap.clear();//每一次点击前子项坐标都不一样，所以清空子项坐标

                    //子项前置判断，是否在屏幕内，不在的话获取屏幕边缘坐标
                    View view0=rv.getChildAt(0);
                    int position0=rv.getChildPosition(view0);
                    if(position0>0)
                    {
                        for(int j=0;j<position0;j++)
                        {
                            float[] xyf=new float[]{(1/6.0f+(j%3)*(1/3.0f))*screenWidth,0};//每行3张图，每张图的中心点横坐标自然是屏幕宽度的1/6,3/6,5/6
                            xyMap.put(j, xyf);
                        }
                    }

                    //其余子项判断
                    for(int i=position0;i<rv.getAdapter().getItemCount();i++)
                    {
                        View view1=rv.getChildAt(i-position0);
                        if(rv.getChildPosition(view1)==-1)//子项末尾不在屏幕部分同样赋值屏幕底部边缘
                        {
                            float[] xyf=new float[]{(1/6.0f+(i%3)*(1/3.0f))*screenWidth,screenHeight};
                            xyMap.put(i, xyf);
                        }
                        else
                        {
                            int[] xy = new int[2];
                            view1.getLocationOnScreen(xy);
                            float[] xyf=new float[]{xy[0]*1.0f+view1.getWidth()/2,xy[1]*1.0f+view1.getHeight()/2};
                            xyMap.put(i, xyf);
                        }
                    }
                    intent.putExtra("xyMap",xyMap);
                    context.startActivity(intent);
                }
            });
        }

        private void initView()
        {
            GridLayoutManager glm=new GridLayoutManager(context,3);//定义3列的网格布局
            rv.setLayoutManager(glm);
            rv.addItemDecoration(new RecyclerViewItemDecoration(20,3));//初始化子项距离和列数
            options=new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.mipmap.ic_launcher)
                    .showImageOnLoading(R.mipmap.ic_launcher)
                    .showImageOnFail(R.mipmap.ic_launcher)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .displayer(new FadeInBitmapDisplayer(5))
                    .build();

//               options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.mipmap.ic_launcher) //设置图片在下载期间显示的图片
//                .showImageForEmptyUri(R.mipmap.ic_launcher)//设置图片Uri为空或是错误的时候显示的图片
//                .showImageOnFail(R.mipmap.ic_launcher)  //设置图片加载/解码过程中错误时候显示的图片
//                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
//                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
//                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
//                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型
//                .displayer(new RoundedBitmapDisplayer(10))
//                .build();//构建完成
            adapter=new MyRecyclerViewAdapter(images,context,options,glm);
            rv.setAdapter(adapter);
        }

        /**
         * 初始化网络图片地址，来自百度图片
         */
        private void initData()
        {
           adapter.notifyDataSetChanged();
        }
        public class RecyclerViewItemDecoration extends RecyclerView.ItemDecoration
        {
            private int itemSpace;//定义子项间距
            private int itemColumnNum;//定义子项的列数

            public RecyclerViewItemDecoration(int itemSpace, int itemColumnNum) {
                this.itemSpace = itemSpace;
                this.itemColumnNum = itemColumnNum;
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.bottom=itemSpace;//底部留出间距
                if(parent.getChildPosition(view)%itemColumnNum==0)//每行第一项左边不留间距，其他留出间距
                {
                    outRect.left=0;
                }
                else
                {
                    outRect.left=itemSpace;
                }

            }
        }


        @Override
        void setData(int pos) {

            List<String> imgUrls = new ArrayList<>();

            TopicBean object = topicList.get(pos);
            imgUrls= JSONArray.parseArray(object.getContentPictureJson(),String.class);
            if(imgUrls!=null){
                images.clear();
                images.addAll(imgUrls);
            }
            //images=JSONArray.parseArray(object.getContentPictureJson(),String.class);

            if(object.getTitle().isEmpty()){
                articleTitle.setVisibility(View.GONE);
            }else {
                articleTitle.setText("# "+object.getTitle()+" #");
            }
            authorName.setText(" " + object.getUserByUserId().getNickname());
            postTime.setText(" " + StampToDate.getStringDate(object.getCreateTime()));
            replyCount.setText("" + object.getCommentNumber());
            viewCount.setText("" + object.getViewNumber());
            praiseCount.setText(""+0);
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
            initView();
            initData();
            setEvent();
            //点击头像
            authorImg.setOnClickListener(view -> {
                Intent intent = new Intent(context, UserDetailActivity.class);
                intent.putExtra("userid", object.getUserByUserId().getUserId());
                context.startActivity(intent);
            });
            //点赞
            praiseCount.setOnClickListener(view->{
                ToastUtils.ToastShort("点赞成功");
            });
            //回复
            replyCount.setOnClickListener(view->{

            });
            //评论
            viewCount.setOnClickListener(view->{

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
