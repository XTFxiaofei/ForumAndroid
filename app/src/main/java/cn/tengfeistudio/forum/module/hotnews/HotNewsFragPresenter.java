package cn.tengfeistudio.forum.module.hotnews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import cn.tengfeistudio.forum.adapter.MoreAdapter;
import cn.tengfeistudio.forum.adapter.MyPostAdapter;
import cn.tengfeistudio.forum.adapter.ReplyAdapter;
import cn.tengfeistudio.forum.adapter.TopicAdapter;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.api.bean.Weather;
import cn.tengfeistudio.forum.api.beans.Comment;
import cn.tengfeistudio.forum.api.beans.TopicBean;
import cn.tengfeistudio.forum.module.post.postcontent.fullscreen.PostActivity;
import cn.tengfeistudio.forum.module.post.postcontent.fullscreen.ReplyActivity;
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


public class HotNewsFragPresenter {
    private HotNewsFragment mView;
    /**
      * 天气
     */
    private String weatherString;
    /**
     * 帖子列表[新帖]
     */
    private List<TopicBean> topicList;
    /**
     * 回复列表
     */
    private List<Comment> replyList;
    /**
     * 我的列表
     */
    private List<Comment> myList;
    /**
     * 获取最大帖子页面
     */
    int max_page_post = 0;
    int max_page_reply = 0;
    int max_page_my = 0;

    private TopicAdapter topicAdapter;
    private ReplyAdapter replyAdapter;
    private MyPostAdapter myPostAdapter;
    private MoreAdapter moreAdapter;

    private Observer<String> observer;

    private static final int TYPE_NEW = 101;
    private static final int TYPE_REPLY = 102;
    private static final int TYPE_MY = 103;
    private static final int TYPE_MORE=104;

    public HotNewsFragPresenter(HotNewsFragment mView) {
        this.mView = mView;
    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            switch (requestCode) {
//                case EditAcitivity.requestCode:
//                    doRefresh();
//                    hideKeyBoard();
//                    break;
//            }
//        }
//    }

    public void getData(boolean isRefresh, Context context, int currentType) {
        if (!isRefresh) {
            initList();
            initObserver();
            getListData(Constants.DEFAULT_PAGE_NUMBER, currentType);
        } else {
            if (topicList == null)
                mView.initData(context);
            getListData(Constants.DEFAULT_PAGE_NUMBER, mView.currentType);
            switch (currentType) {
                case TYPE_NEW:
                    max_page_post = Constants.DEFAULT_PAGE_NUMBER;
                    break;
                case TYPE_REPLY:
                    max_page_reply = Constants.DEFAULT_PAGE_NUMBER;
                    break;
                case TYPE_MY:
                    max_page_my = Constants.DEFAULT_PAGE_NUMBER;
                    break;
                case TYPE_MORE:
                    //initAdapter(currentType,context);
                    break;
            }
        }
    }

    // 第一次加载的行为
    private void initAdapter(int type, Context context) {
        switch (type) {
            case TYPE_NEW:
                topicAdapter = new TopicAdapter(context, topicList);
                mView.rv.setAdapter(topicAdapter);
                topicAdapter.setOnItemClickListener((v, position) -> {
                    Intent intent = new Intent(context, PostActivity.class);
                    intent.putExtra("PostJsonObject", JSON.toJSONString(topicList.get(position)));
                    intent.putExtra("isNormalPost", true);
                    context.startActivity(intent);
                });
                if (mView.new_loadnothing) {
                    topicAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                    mView.new_loadnothing = false;
                }
                break;
            case TYPE_REPLY:
                replyAdapter = new ReplyAdapter(context, replyList);
                mView.rv.setAdapter(replyAdapter);
                replyAdapter.setOnItemClickListener((view, pos) -> {
                    //这里传帖子id，然后回到帖子内容那，现在不是帖子id
                    Intent intent = new Intent(context, ReplyActivity.class);
                    intent.putExtra("commentId", replyList.get(pos).getTargetId());
                    intent.putExtra("isNormalPost", false);
                    context.startActivity(intent);
                });
                // 初次加载
                if (mView.reply_loadnothing) {
                    replyAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                    mView.reply_loadnothing = false;
                }
                break;
            case TYPE_MY:
                myPostAdapter = new MyPostAdapter(context, myList);
                mView.rv.setAdapter(myPostAdapter);
                myPostAdapter.setOnItemClickListener((view, pos) -> {
                    Intent intent = new Intent(context, PostActivity.class);
                    intent.putExtra("topicId", myList.get(pos).getTargetId());
                    intent.putExtra("isNormalPost", false);
                    context.startActivity(intent);
                });
                if (mView.my_loadnothing) {
                    myPostAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                    mView.my_loadnothing = false;
                }
                break;
            case TYPE_MORE:
                printLog("点击了更多");
                moreAdapter=new MoreAdapter(context,getWeatherString());
                mView.rv.setAdapter(moreAdapter);
                moreAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                break;
        }
        mView.setLoadMoreListener(type);
    }

    void afterGetDataSuccess(String data, int type, Context context) {
        initListData(data, type);
        // 处理第一次刷新和后续刷新
        switch (type) {
            case TYPE_NEW:
                if (topicAdapter == null)
                    initAdapter(type, context);
                else
                    topicAdapter.notifyDataSetChanged();
                break;
            case TYPE_REPLY:
                if (replyAdapter == null)
                    initAdapter(type, context);
                else
                    replyAdapter.notifyDataSetChanged();
                break;
            case TYPE_MY:
                if (myPostAdapter == null)
                    initAdapter(type, context);
                else
                    myPostAdapter.notifyDataSetChanged();
                break;
            case TYPE_MORE:
                printLog("第4个");
                if(moreAdapter==null)
                    initAdapter(type,context);
                else
                    moreAdapter.notifyDataSetChanged();
                break;
        }
    }

    /**
     * 初始化帖子列表数据
     */
    private void initListData(String JsonDataArray, int type) {
        if (mView.isPullDownRefresh) {
            // 处理下拉刷新的请求
            switch (type) {
                case TYPE_NEW:
                    topicList.clear();
                    if (topicAdapter != null)
                        topicAdapter.changeLoadMoreState(STATE_LOADING);
                    break;
                case TYPE_REPLY:
                    replyList.clear();
                    if (replyAdapter != null)
                        replyAdapter.changeLoadMoreState(STATE_LOADING);
                    break;
                case TYPE_MY:
                    myList.clear();
                    if (myPostAdapter != null)
                        myPostAdapter.changeLoadMoreState(STATE_LOADING);
                    break;
                case TYPE_MORE:
                    if(moreAdapter!=null){
                        moreAdapter.changeLoadMoreState(STATE_LOADING);
                    }
                    break;
            }
        }
        JSONObject jsonObject = JSON.parseObject(JsonDataArray);
        // 尾页处理
        printLog("currentPage:" + jsonObject.getInteger("number") + " last: " + jsonObject.getBoolean("last")+" numberOfElements:"+jsonObject.getInteger("numberOfElements")+" size:"+jsonObject.getInteger("size"));
       // if (jsonObject.getBoolean("last")) {
        if(jsonObject.getInteger("number")>=jsonObject.getInteger("totalPages") || jsonObject.getInteger("totalPages")==1){
            switch (type) {
                case TYPE_NEW:
                    if (topicAdapter != null) {
                        topicAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                        return;
                    } else {
                        mView.new_loadnothing = true;
                    }
                    break;
                case TYPE_REPLY:
                    if (replyAdapter != null) {
                        replyAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                        return;
                    } else {
                        mView.reply_loadnothing = true;
                    }
                    break;
                case TYPE_MY:
                    if (myPostAdapter != null) {
                        myPostAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                        return;
                    } else {
                        mView.my_loadnothing = true;
                    }
                    break;
                case TYPE_MORE:
                    if(moreAdapter!=null){
                        moreAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                    }//else{
//                        mView.more_loadnothing=true;
//                    }
                    break;
            }
        }
        //不是最后一页的处理
        JSONArray array = JSON.parseArray(jsonObject.getString("content"));
        switch (type) {
            case TYPE_NEW:
                for (int i = 0; i < array.size(); i++)
                    topicList.add(JSON.parseObject(array.getString(i), TopicBean.class));
                break;
            case TYPE_REPLY:
                for (int i = 0; i < array.size(); i++)
                    replyList.add(JSON.parseObject(array.getString(i), Comment.class));
                break;
            case TYPE_MY:
                for (int i = 0; i < array.size(); i++)
                    myList.add(JSON.parseObject(array.getString(i), Comment.class));
                break;
        }

    }

    void getListData(int page, int currentType) {
        switch (currentType) {
            case TYPE_NEW:
                if (page == Constants.DEFAULT_PAGE_NUMBER)
                    topicAdapter = null;
                getPostListData(page);
                break;
            case TYPE_REPLY:
                if (page == Constants.DEFAULT_PAGE_NUMBER)
                    replyAdapter = null;
                getReplyListData(page);
                break;
            case TYPE_MY:
                if (page == Constants.DEFAULT_PAGE_NUMBER)
                    myPostAdapter = null;
                getMyListData(page);
                break;
            case TYPE_MORE:
                if(page==Constants.DEFAULT_PAGE_NUMBER)
                    moreAdapter=null;
                getMoreListData(page);
                getWeatherData();
                break;
        }
    }

    private void initObserver() {
        observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(String s) {
                JSONObject obj = JSON.parseObject(s);
                mView.afterGetDataSuccess(obj.getInteger("type"), obj.getString("data"));
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
        topicList = new ArrayList<>();
        replyList = new ArrayList<>();
        myList = new ArrayList<>();
    }

    /**
     * 从服务器获取  回复我的
     */
    @SuppressLint("CheckResult")
    private void getReplyListData(int page) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> OkHttpUtils.get()
                //App.getUid()直接获取用户id
                .url(NetConfig.BASE_COMMENT + "get_comment_byToUserId")
                .addParams("page", page + "")
                .addHeader(Constants.AUTHORIZATION, Store.getInstance().getToken())
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
                            if (replyAdapter != null)
                                replyAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                            return;
                        }
                        JSONObject dataObj = JSON.parseObject(response);
                        if (dataObj.getInteger("code") == Constants.TOKEN_OVERDUE) {
                            getNewToken(page, TYPE_REPLY);
                        } else if (dataObj.getInteger("code") != Constants.RETURN_CONTINUE) {
                            emitter.onError(new Throwable("100 "));
                            ToastShort("服务器出状况惹，稍等喔( • ̀ω•́ )✧");
                        } else {
                            max_page_reply = max_page_reply >= page ? max_page_reply : page;
                            JSONObject obj = new JSONObject();
                            obj.put("type", TYPE_REPLY);
                            obj.put("data", dataObj.getString("data"));
                            emitter.onNext(obj.toJSONString());
                        }
                    }
                }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 从服务器获取我的回复
     */
    private void getMyListData(int page) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> OkHttpUtils.get()
                //.url(NetConfig.BASE_USER_PLUS + App.getUid() + "/discussions")
                //.addHeader("Authorization", "Bearer " + Store.getInstance().getToken())
                .url(NetConfig.BASE_COMMENT + "get_comment_byUserId")
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
                            if (myPostAdapter != null)
                                myPostAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                            return;
                        }

                        JSONObject dataObj = JSON.parseObject(response);
                        if (dataObj.getInteger("code") == Constants.TOKEN_OVERDUE) {
                            getNewToken(page, TYPE_MY);
                        } else if (dataObj.getInteger("code") != Constants.RETURN_CONTINUE) {
                            ToastShort("服务器出状况惹，稍等喔( • ̀ω•́ )✧");
                        } else {
                            max_page_my = max_page_my >= page ? max_page_my : page;
                            JSONObject obj = new JSONObject();
                            obj.put("type", TYPE_MY);
                            obj.put("data", dataObj.getString("data"));
                            emitter.onNext(obj.toJSONString());
                        }
                    }
                }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }

    /**
     * 从服务器获取帖子
     */
    private void getPostListData(int page) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> OkHttpUtils.get()
                //.url(NetConfig.BASE_POST_PLUS)
                .url(NetConfig.BASE_ALL_TOPIC)
                .addParams("page", page + "")
                .addParams("theme", "")   //查找所有帖子，所以theme为空
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
                            if (topicAdapter != null)
                                topicAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                            return;
                        }

                        JSONObject dataObj = JSON.parseObject(response);
                        if (dataObj.getInteger("code") != Constants.RETURN_CONTINUE) {
                            ToastShort("服务器出状况惹，稍等喔( • ̀ω•́ )✧");
                        } else {
                            max_page_post = max_page_post >= page ? max_page_post : page;
                            JSONObject obj = new JSONObject();
                            obj.put("type", TYPE_NEW);
                            obj.put("data", dataObj.getString("data"));
                            emitter.onNext(obj.toJSONString());
                        }
                    }
                }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }


    private void getMoreListData(int page) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> OkHttpUtils.get()
                //.url(NetConfig.BASE_POST_PLUS)
                .url(NetConfig.BASE_ALL_TOPIC)
                .addParams("page", page + "")
                .addParams("theme", "")   //查找所有帖子，所以theme为空
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
                            if (moreAdapter != null)
                                moreAdapter.changeLoadMoreState(STATE_LOAD_NOTHING);
                            return;
                        }

                        JSONObject dataObj = JSON.parseObject(response);
                        if (dataObj.getInteger("code") != Constants.RETURN_CONTINUE) {
                            ToastShort("服务器出状况惹，稍等喔( • ̀ω•́ )✧");
                        } else {
                            max_page_post = max_page_post >= page ? max_page_post : page;
                            JSONObject obj = new JSONObject();
                            obj.put("type", TYPE_MORE);
                            obj.put("data", dataObj.getString("data"));
                            emitter.onNext(obj.toJSONString());
                        }
                    }
                }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }


    /**
     * 获取天气
     */
    private void getWeatherData() {
        RetrofitService.getWeather("101210801")
                .subscribe(new Observer<Weather>() {
                    @Override
                    public void onSubscribe(Disposable d) {}
                    @Override
                    public void onNext(Weather weather) {
                       // moreAdapter.loadWeather(true, weather.getWeatherinfo().getWeather());
                        setWeatherString(weather.getWeatherinfo().getWeather());
                    }
                    @Override
                    public void onError(Throwable e) {
                        //moreAdapter.loadWeather(false, null);
                        setWeatherString(null);
                    }
                    @Override
                    public void onComplete() {}
                });
    }


    /**
     * 获取新的Token
     */
    @SuppressLint("CheckResult")
    private void getNewToken(int page, int currentType) {
        RetrofitService.getNewToken()
                .subscribe(s -> getListData(page, currentType));
    }

    public String getWeatherString() {
        return weatherString;
    }

    public void setWeatherString(String weatherString) {
        this.weatherString = weatherString;
    }
}
