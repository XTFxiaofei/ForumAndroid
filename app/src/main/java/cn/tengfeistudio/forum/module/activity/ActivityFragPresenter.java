package cn.tengfeistudio.forum.module.activity;


import android.content.Context;
import android.content.Intent;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import java.util.ArrayList;
import java.util.List;

import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.adapter.ActivityAdapter;
import cn.tengfeistudio.forum.api.beans.ActivityBean;
import cn.tengfeistudio.forum.local.DataBase.MyDB;
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
import static cn.tengfeistudio.forum.utils.LogUtils.printLog;
import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastNetWorkError;
import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastShort;


public class ActivityFragPresenter {
    private ActivityFragment mView;
    /**
     * 活动列表
     */
    private List<ActivityBean> activityList;
    /**
     * 获取最大帖子页面
     */
    int max_page_post = 0;
    //活动适配器
    private ActivityAdapter activityAdapter;

    private Observer<String> observer;

    //是否从本地获取数据，如果连接网络则重网络端获取
    private static boolean getDataFromMydb=true;

    private Context context;

    public ActivityFragPresenter(ActivityFragment mView) {
        this.mView = mView;
    }

    public void getData(boolean isRefresh, Context context,String acPlace,String acType) {
        this.context=context;
        if (!isRefresh) {
            initList();
            initObserver();
            getListData(Constants.DEFAULT_PAGE_NUMBER,acPlace,acType);
        } else {
            if (activityList == null)
                mView.initData(context);
            getListData(Constants.DEFAULT_PAGE_NUMBER,acPlace,acType);
            max_page_post = Constants.DEFAULT_PAGE_NUMBER;
        }

    }

    // 第一次加载的行为
    private void initAdapter(Context context) {
        activityAdapter = new ActivityAdapter(context, activityList);
        mView.rv.setAdapter(activityAdapter);
        activityAdapter.setOnItemClickListener((v, position) -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("viewTop",v.getTop());
            intent.putExtra("ActivityJsonObject", JSON.toJSONString(activityList.get(position)));
            intent.putExtra("isNormalPost", true);
            context.startActivity(intent);
        });
        if (mView.new_loadnothing) {
            activityAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
            mView.new_loadnothing = false;
        }
        mView.setLoadMoreListener();
    }

    void afterGetDataSuccess(String data, Context context) {
        initListData(data);
        // 处理第一次刷新和后续刷新
        if (activityAdapter == null)
            initAdapter(context);
        else
            activityAdapter.notifyDataSetChanged();
    }

    /**
     * 初始化帖子列表数据
     */
    private void initListData(String JsonDataArray) {
        if (mView.isPullDownRefresh) {
            // 处理下拉刷新的请求
            activityList.clear();
            if (activityAdapter != null)
                activityAdapter.changeLoadMoreState(STATE_LOADING);
        }
        //判断是否从本地获取数据
        if(!getDataFromMydb){
            JSONObject jsonObject = JSON.parseObject(JsonDataArray);
            // 尾页处理
            printLog("currentPage:" + jsonObject.getInteger("number") + " last: " + jsonObject.getBoolean("last") + " numberOfElements:" + jsonObject.getInteger("numberOfElements") + " size:" + jsonObject.getInteger("size"));
            if (jsonObject.getInteger("number") >= jsonObject.getInteger("totalPages") || jsonObject.getInteger("totalPages") == 1) {

                if (activityAdapter != null) {
                    activityAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                    return;
                } else {
                    mView.new_loadnothing = true;
                }
            }
            MyDB db = new MyDB(context);
            //不是最后一页的处理
            JSONArray array = JSON.parseArray(jsonObject.getString("content"));
            for (int i = 0; i < array.size(); i++){
                ActivityBean bean=JSON.parseObject(array.getString(i), ActivityBean.class);
                activityList.add(bean);
                //活动存到数据库
                db.handSingleReadActivity(bean);
            }
        }else{
            //从本地获取数据
            MyDB db=new MyDB(context);
            activityList=db.getActivityBean();
            for(ActivityBean activityBean:activityList){
                printLog(activityBean.getActivityName());
            }

        }


    }

    public void getListData(int page,String acPlace,String acType) {
        if (page == Constants.DEFAULT_PAGE_NUMBER)
            activityAdapter = null;
        getActivityListData(page,acPlace,acType);
    }

    private void initObserver() {
        observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }
            @Override
            public void onNext(String s) {
                JSONObject obj = JSON.parseObject(s);
                mView.afterGetDataSuccess(obj.getString("data"));
            }
            @Override
            public void onError(Throwable e) {

            }
            @Override
            public void onComplete() {
            }
        };
    }

    private void initList() {
        activityList = new ArrayList<>();
    }


    /**
     * 从服务器获取活动
     */
    private void getActivityListData(int page,String place,String type) {

        Observable.create((ObservableOnSubscribe<String>) emitter -> OkHttpUtils.get()
                .url(NetConfig.BASE_ACTIVITY + "get_all_activity_byPlaceAndType")
                .addParams("page", page + "")
                .addParams("place",place+"")
                .addParams("type",type+"")
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
                            if (activityAdapter != null)
                                activityAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                            return;
                        }

                        JSONObject dataObj = JSON.parseObject(response);
                        if (dataObj.getInteger("code") != Constants.RETURN_CONTINUE) {
                            ToastShort("服务器出状况惹，稍等喔( • ̀ω•́ )✧");
                        } else {
                            //能够正常从服务端获取数据
                            getDataFromMydb=false;
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
}
