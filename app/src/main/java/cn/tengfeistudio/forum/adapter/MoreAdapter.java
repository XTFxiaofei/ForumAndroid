package cn.tengfeistudio.forum.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.bean.Forum;
import cn.tengfeistudio.forum.module.home.HomeActivity;
import cn.tengfeistudio.forum.module.home.fullscreen.HomeFragPresenter;
import cn.tengfeistudio.forum.module.post.edit.EditAcitivity;
import cn.tengfeistudio.forum.module.post.postlist.PostsActivity;
import cn.tengfeistudio.forum.utils.DateUtils;

/**
 * Reply列表adapter
 */
public class MoreAdapter extends BaseAdapter {
    private Context context;
    private String weatherString;

    public MoreAdapter(Context context, String weatherString) {
        this.context = context;
        this.weatherString = weatherString;
    }

    @Override
    protected int getDataCount() {
        return TYPE_NORMAL;
    }

    @Override
    protected int getItemType(int pos) {
        return TYPE_NORMAL;
    }

    //改变状态
    public void changeLoadMoreState(int i) {
        this.loadState = i;
        notifyItemChanged(0);
    }

    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_home2, parent, false);
        NormalViewHolder viewHolder = new NormalViewHolder(view);
        return viewHolder;
    }

    // implements HomeFragView
    class NormalViewHolder extends BaseViewHolder {
        @BindView(R.id.list2)
        RecyclerView list;
        @BindView(R.id.tv_weather2)
        TextView tvWeather;
        @BindView(R.id.iv_weather2)
        ImageView ivWeather;
        @BindView(R.id.scrollView2)
        ScrollView scrollView;
        @BindView(R.id.ll_logintip2)
        RelativeLayout llLogintip;
        @BindView(R.id.refresh_layout2)
        SmartRefreshLayout refreshLayout;
        @BindView(R.id.ll_edittip2)
        RelativeLayout llEdittip;

        NormalViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }


        @Override
        void setData(int pos) {
            //super.setData(pos);
            initData(context);
        }

        private List<Forum> forumList;
        @Inject
        protected HomeFragPresenter mPresenter;

        protected void initData(Context content) {
            //mPresenter.getData(false);
            if (App.ISLOGIN()) {
                llLogintip.setVisibility(View.GONE);
                llEdittip.setVisibility(View.VISIBLE);
            } else {
                llLogintip.setVisibility(View.VISIBLE);
                llEdittip.setVisibility(View.GONE);
            }
            initView(content);
        }

        protected void initView(Context content) {
            loadWeather(weatherString);
            initForumList2();
            initForumList(content);
            initRefreshLayout();
        }

        private void initForumList(Context content) {
            ForumAdapter adapter = new ForumAdapter(mActivity, forumList);
            adapter.setOnItemClickListener(pos -> {
                Intent intent = new Intent(mActivity, PostsActivity.class);
                intent.putExtra("Title", forumList.get(pos).getTitle());
                intent.putExtra("category", forumList.get(pos).getCategory());
                content.startActivity(intent);
            });
            GridLayoutManager layoutManager = new GridLayoutManager(mActivity, 4);
            list.setClipToPadding(false);
            list.setLayoutManager(layoutManager);
            list.setAdapter(adapter);
        }

        private void initRefreshLayout() {
            refreshLayout.setOnRefreshListener(refreshLayout -> {
                doRefresh();
                refreshLayout.finishRefresh(2000);
            });
            refreshLayout.setOnLoadMoreListener(refreshLayout -> refreshLayout.finishLoadMore(2000));

        }

        public void doRefresh() {
            if (App.ISLOGIN()) {
                llLogintip.setVisibility(View.GONE);
                llEdittip.setVisibility(View.VISIBLE);
            } else {
                llLogintip.setVisibility(View.VISIBLE);
                llEdittip.setVisibility(View.GONE);
            }
            mPresenter.getData(true);
        }

        private HomeActivity mActivity = (HomeActivity) context;

        @OnClick({R.id.tip_edit2})
        public void onViewClicked(View view) {
            switch (view.getId()) {
                case R.id.tip_edit2:
                    Intent in = new Intent(mActivity, EditAcitivity.class);
                    in.putExtra("category", "E-M-P-T-Y");
                    mActivity.startActivity(in);
            }
        }


        /**
         * 天气
         *
         * @param weather
         */
        private void loadWeather(String weather) {
            ivWeather.setImageResource(R.drawable.ic_sun);
            int currentHour = DateUtils.getHourTimeOfDay();
            if (currentHour <= 5 || currentHour >= 19) {
                tvWeather.setText("晚上好！");
                ivWeather.setImageResource(R.drawable.ic_moon);
            } else if (currentHour <= 10) {
                tvWeather.setText("早上好！");
            } else if (currentHour <= 13) {
                tvWeather.setText("中午好！");
            } else {
                tvWeather.setText("下午好！");
            }


            if(weather!=null){
                if (weather.contains("晴"))
                    return;
                else if (weather.contains("雨"))
                    ivWeather.setImageResource(R.drawable.ic_rain);
                else if (weather.contains("风"))
                    ivWeather.setImageResource(R.drawable.ic_cloudy);
                else if (weather.contains("雪"))
                    ivWeather.setImageResource(R.drawable.ic_snow);
            }
        }

        private void initForumList2() {
            forumList = new ArrayList<>();
            forumList.add(new Forum("灌水专区", R.drawable.ic_01, 0, "daily"));
            forumList.add(new Forum("技术交流", R.drawable.ic_02, 0, "code"));
            forumList.add(new Forum("问答专区", R.drawable.ic_03, 1, "qa"));
            forumList.add(new Forum("发展建议", R.drawable.ic_08, 1, "suggests"));
            forumList.add(new Forum("论坛反馈", R.drawable.ic_05, 2, "feedback"));
            forumList.add(new Forum("校园交易", R.drawable.ic_06, 2, "transaction"));
            forumList.add(new Forum("公告活动", R.drawable.ic_07, 2, "activity"));
        }
    }
}
