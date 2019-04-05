package cn.tengfeistudio.forum.module.setting.collection;


import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.adapter.ActivityAdapter;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.api.beans.ActivityBean;
import cn.tengfeistudio.forum.listener.LoadMoreListener;
import cn.tengfeistudio.forum.module.activity.DetailActivity;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.NetConfig;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;

import static cn.tengfeistudio.forum.adapter.BaseAdapter.STATE_LOADING;
import static cn.tengfeistudio.forum.adapter.BaseAdapter.STATE_LOAD_NOTHING;


public class CollectionActivity extends BaseActivity implements LoadMoreListener.OnLoadMoreListener {

    @BindView(R.id.tv_hotnews_showlogin3)
    TextView tvHotnewsShowlogin;
    @BindView(R.id.rv_hotnews3)
    RecyclerView rv;
    @BindView(R.id.swiperefresh_hotnews3)
    SwipeRefreshLayout refreshLayout;

    /**
     * 初始化recylerView的一些属性
     */
    protected RecyclerView.LayoutManager mLayoutManager;
    protected LoadMoreListener loadMoreListener;
    private boolean isPullUpRefresh = false;
    /**
     * 活动列表
     */
    private List<ActivityBean> activityList = new ArrayList<>();

    //获取最大帖子页面
    int max_page_post = 0;
    //活动适配器
    private ActivityAdapter collectionAdapter;
    protected boolean new_loadnothing = false;
    private Observer<String> observer;
    /**
     * 下拉刷新样式
     * isRefresh
     * true 有刷新请求
     * false 无刷新请求
     */
    protected boolean isPullDownRefresh = false;


    @Override
    protected int getLayoutID() {
        return R.layout.collection_activity;
    }

    @Override
    protected void initData() {
        if (!App.ISLOGIN()) {
            tvHotnewsShowlogin.setText("登陆后就可以看了喔 ٩(๑❛ᴗ❛๑)۶");
            tvHotnewsShowlogin.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            return;
        }
        getData(false);
        tvHotnewsShowlogin.setText("刷新中...");
    }


    @Override
    protected void initView() {
        initToolBar(true, "我的收藏");
        initRefreshLayout();
        initRecyclerView();
    }

    private void initRefreshLayout() {
        refreshLayout.setColorSchemeResources(R.color.red_light, R.color.green_light, R.color.blue_light, R.color.orange_light);
        refreshLayout.setOnRefreshListener(() -> new Thread() {
            @Override
            public void run() {
                doRefresh();
            }
        }.start());
    }


    /**
     * 执行刷新操作
     */
    public void doRefresh() {
        if (!App.ISLOGIN()) {
            tvHotnewsShowlogin.setText("登陆后就可以看了喔 ٩(๑❛ᴗ❛๑)۶");
            tvHotnewsShowlogin.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            return;
        }
        isPullDownRefresh = true;
//        new Thread() {
//            @Override
//            public void run() {
//                getParent().runOnUiThread(() -> {
//                    tvHotnewsShowlogin.setText("刷新中2...");
//                    tvHotnewsShowlogin.setVisibility(View.VISIBLE);
//                });
//            }
//        }.start();
        getData(true);
    }

    public void getData(boolean isRefresh) {
        if (!isRefresh) {
            initObserver();
            getListData(Constants.DEFAULT_PAGE_NUMBER);
        } else {
            if (activityList == null)
                this.initData();
            getListData(Constants.DEFAULT_PAGE_NUMBER);
            max_page_post = Constants.DEFAULT_PAGE_NUMBER;
        }
    }

    private void initRecyclerView() {
        // 设置监听事件
        if (loadMoreListener == null) {
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            rv.setLayoutManager(mLayoutManager);
            loadMoreListener = new LoadMoreListener((LinearLayoutManager) mLayoutManager, this, 5);
        }
        rv.addOnScrollListener(loadMoreListener);

        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // 调整draw缓存,加速recyclerview加载
        rv.setItemViewCacheSize(20);
        rv.setDrawingCacheEnabled(true);
        rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

    }


    private void initObserver() {
        observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(String s) {
                JSONObject obj = JSON.parseObject(s);
                afterGetDataSuccess1(obj.getString("data"));
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
    }

    // 第一次加载的行为
    private void initAdapter(Context context) {
        collectionAdapter = new ActivityAdapter(context, activityList);
        this.rv.setAdapter(collectionAdapter);
        collectionAdapter.setOnItemClickListener((v, position) -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("viewTop", v.getTop());
            intent.putExtra("ActivityJsonObject", JSON.toJSONString(activityList.get(position)));
            intent.putExtra("isNormalPost", true);
            context.startActivity(intent);
        });
        if (this.new_loadnothing) {
            collectionAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
            this.new_loadnothing = false;
        }
        this.setLoadMoreListener();
    }

    protected void setLoadMoreListener() {
        loadMoreListener = new LoadMoreListener((LinearLayoutManager) mLayoutManager, this, 5);
        rv.addOnScrollListener(loadMoreListener);
    }

    void afterGetDataSuccess1(String data) {
        initListData(data);
        afterGetDataSuccess2(data, getBaseContext());
        isPullDownRefresh = false;
        isPullUpRefresh = false;
        tvHotnewsShowlogin.setVisibility(View.GONE);
        rv.setVisibility(View.VISIBLE);
        if (refreshLayout.isRefreshing())
            refreshLayout.setRefreshing(false);
    }

    void afterGetDataSuccess2(String data, Context context) {
        initListData(data);
        // 处理第一次刷新和后续刷新
        if (collectionAdapter == null)
            initAdapter(context);
        else
            collectionAdapter.notifyDataSetChanged();
    }

    /**
     * 初始化帖子列表数据
     */
    private void initListData(String JsonDataArray) {
        if (this.isPullDownRefresh) {
            // 处理下拉刷新的请求
            activityList.clear();
            if (collectionAdapter != null)
                collectionAdapter.changeLoadMoreState(STATE_LOADING);
        }
        JSONArray array = JSON.parseArray(JsonDataArray);
        //最后一页
        if (array.size() < Constants.DEFAULT_EACH_PAGE_SIZE) {
            if (collectionAdapter!= null) {
                collectionAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                return;
            } else {
                new_loadnothing = true;
            }
        }
        //不是最后一页的处理
        for (int i = 0; i < array.size(); i++)
            activityList.add(JSON.parseObject(array.getString(i), ActivityBean.class));
//        List<ActivityBean> list = JSONObject.parseArray(JsonDataArray, ActivityBean.class);
//        activityList.clear();
//        activityList.addAll(list);
    }


    void getListData(int page) {
        if (page == Constants.DEFAULT_PAGE_NUMBER)
            collectionAdapter = null;
        getActivityListData(page);

    }


    /**
     * 从服务器获取活动
     */
    private void getActivityListData(int page) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> OkHttpUtils.post()
                .url(NetConfig.BASE_COLLECTION + "get_collect_activitylist")
                .addHeader(Constants.AUTHORIZATION, Store.getInstance().getToken())
                .addParams("page", page + "")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastNetWorkError();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (!response.contains("code")) {
                            ToastNetWorkError();
                            if (collectionAdapter != null)
                                collectionAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                            return;
                        }
                        JSONObject dataObj = JSON.parseObject(response);
                        if (dataObj.getInteger("code") != Constants.RETURN_CONTINUE) {
                            ToastShort("服务器出状况惹，稍等喔( • ̀ω•́ )✧");
                        } else {
                            max_page_post = max_page_post >= page ? max_page_post : page;
                            JSONObject obj = new JSONObject();
                            obj.put("data", dataObj.getString("data"));
                            emitter.onNext(obj.toJSONString());
                        }
                    }
                }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }


    @Override
    public void onLoadMore() {
        if (isPullDownRefresh || isPullUpRefresh)
            return;
        isPullUpRefresh = true;
        this.getListData(max_page_post + 1);
    }
}
