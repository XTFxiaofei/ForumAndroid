package cn.tengfeistudio.forum.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.beans.ActivityBean;
import cn.tengfeistudio.forum.module.home.LaunchActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.toast.ScaleAnimatorUtils;
import cn.tengfeistudio.forum.utils.toast.ToastUtils;
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
        ImageView authorImg2;
        //CircleImageView authorImg2;
        @BindView(R.id.author_name2)
        TextView authorName2;
        @BindView(R.id.post_time2)
        TextView postTime2;
        @BindView(R.id.iv_collect)
        ImageView ivCollect;

        private int collectionId;

        NormalViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }

        @Override
        void setData(int pos) {
            ActivityBean object = activityList.get(pos);
            collectionId = object.getActivityId();

            articleTitle2.setText("【" +object.getType()+"】" +object.getActivityName());
            authorName2.setText(" " + object.getPlace());
            StringBuilder sb = new StringBuilder(object.getActivityTime());//构造一个StringBuilder对象
            sb.insert(4, "/");//在指定的位置1，插入指定的字符串
            sb.insert(7,"/");
            if(sb.length()>10){
                sb.insert(15,"/");
                sb.insert(18,"/");
            }
            postTime2.setText(" " + sb.toString());
            if(LaunchActivity.collectActivityIds.contains(collectionId)){
                ivCollect.setImageResource(R.drawable.collect_yes);
                ivCollect.setSelected(true);
            }

            Picasso.get()
                    .load(object.getLogoImage())
                    .placeholder(R.drawable.image_placeholder)
                    .into(authorImg2);

            //点击图片今日具体信息
//            authorImg2.setOnClickListener(view -> {
//                Intent intent = new Intent(context, ContentActivity.class);
//                intent.putExtra("userid",object.getActivityId());
//                context.startActivity(intent);
//            });

            //点击收藏按钮
            ivCollect.setOnClickListener(view -> {
                switch (view.getId()) {
                    case R.id.iv_collect:
                       // if (ivCollect.isSelected() == false) {
                        //已经收藏
                        if(!LaunchActivity.collectActivityIds.contains(collectionId)){
                            ivCollect.setImageResource(R.drawable.collect_yes);
                            ivCollect.setSelected(true);
                            ScaleAnimatorUtils.setScalse(ivCollect);
                            //ToastUtils.ToastShort("收藏成功" + collectionId);
                            //本地收藏活动
                            LaunchActivity.collectActivityIds.add(collectionId);
                            //收藏活动发到服务端
                            collectActivityId(collectionId);
                        } else {
                            ivCollect.setImageResource(R.drawable.collect_no);
                            ivCollect.setSelected(false);
                            ScaleAnimatorUtils.setScalse(ivCollect);
                            //ToastUtils.ToastShort("取消收藏" + collectionId);
                            //本地取消收藏
                            LaunchActivity.collectActivityIds.remove((Integer)collectionId);
                            //取消收藏发送服务端
                            cancelCollectActivityId(collectionId);
                        }
                        break;
                }
            });
        }


        /**
         * 收藏活动
         * @param activityId
         */
        @SuppressLint("CheckResult")
        private void collectActivityId(int activityId) {
            RetrofitService.collectActivity(activityId)
                    .subscribe(responseBody -> {
                        JSONObject obj = null;
                        try {
                            obj = JSON.parseObject(responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int statusCode = obj.getInteger("code");
                        if(statusCode== Constants.RETURN_CONTINUE){
                            ToastUtils.ToastShort("收藏成功");
                        }
                    });
        }

        /**
         * 取消收藏
         * @param activityId
         */
        @SuppressLint("CheckResult")
        private void cancelCollectActivityId(int activityId) {
            RetrofitService.cancelCollectActivity(activityId)
                    .subscribe(responseBody -> {
                        JSONObject obj = null;
                        try {
                            obj = JSON.parseObject(responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int statusCode = obj.getInteger("code");
                        if(statusCode== Constants.RETURN_CONTINUE){
                            ToastUtils.ToastShort("取消收藏");
                        }
                    });
        }


    }
}
