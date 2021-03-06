package cn.tengfeistudio.forum.module.home.fullscreen;

import android.annotation.SuppressLint;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.bean.Forum;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.api.bean.Weather;
import cn.tengfeistudio.forum.local.DataBase.MyDB;
import cn.tengfeistudio.forum.module.base.BasePresenter;
import cn.tengfeistudio.forum.utils.NetConfig;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HomeFragPresenter implements BasePresenter {
    private final String cityCode;
    private final HomeFragView mView;
    private List<Forum> forumList;


    public HomeFragPresenter(String cityCode, HomeFragView view) {
        this.cityCode = cityCode;
        this.mView = view;
    }

    @Override
    public void getData(boolean isRefresh) {
        getForumListData();
        getWeatherData();
        if (App.ISLOGIN()){
            MyDB db = new MyDB(App.getContext());
            if (db.isUserExist(App.getUid())) {
                mView.loadAvatar(db.getUserAvatarPath(App.getUid()));
            } else {
                getUserAvator();
            }
        }
    }

    @Override
    public void getMoreData() {

    }

    @SuppressLint("CheckResult")
    private void getWeatherData() {
        RetrofitService.getWeather(cityCode)
                .subscribe(new Observer<Weather>() {
                    @Override
                    public void onSubscribe(Disposable d) {}
                    @Override
                    public void onNext(Weather weather) {
                        mView.loadWeather(true, weather.getWeatherinfo().getWeather());
                    }
                    @Override
                    public void onError(Throwable e) {
                        mView.loadWeather(false, null);
                    }
                    @Override
                    public void onComplete() {}
                });
    }

    @SuppressLint("CheckResult")
    private synchronized void getUserAvator() {
        Observable<String> observable = RetrofitService.getNewToken();

        DisposableObserver<String> observer = new DisposableObserver<String>() {
            @Override
            public void onNext(String s) {
                Observable.create((ObservableOnSubscribe<String>) emitter -> {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                           // .url(NetConfig.BASE_USERDETAIL_PLUS)
                            .url(NetConfig.BASE_GET_USER_DETAILS)
                            .header("authorization",  Store.getInstance().getToken())
                            .get()
                            .build();
                    String response = client.newCall(request).execute().body().string();
                    emitter.onNext(response);
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(responseString -> {
                            if (!responseString.contains("code")){
                                Log.e("print","HomeFragment_getAvatar_subscribe:获取用户信息出错 需要处理");
                                return;
                            }
                            JSONObject jsonObject = JSON.parseObject(responseString);
                            String path = "";
                            JSONObject obj = JSON.parseObject(jsonObject.getString("data"));
                            path = obj.getString("icon");
                            mView.loadAvatar(path);
                        }, throwable -> Log.e("print","HomeFragment_getAvatar_subscribe_onError:" + throwable.getMessage()));

            }

            @Override
            public void onError(Throwable e) {
                Log.e("print", "HomeFragment_getUserAvator_onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {}
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void getForumListData() {
        forumList = new ArrayList<>();
        forumList.add(new Forum("灌水专区", R.drawable.ic_01, 0, "daily"));
        forumList.add(new Forum("技术交流", R.drawable.ic_02, 0, "code"));
        forumList.add(new Forum("问答专区", R.drawable.ic_03, 1, "qa"));
        forumList.add(new Forum("发展建议", R.drawable.ic_08, 1, "suggests"));
        forumList.add(new Forum("论坛反馈", R.drawable.ic_05, 2, "feedback"));
        forumList.add(new Forum("校园交易", R.drawable.ic_06, 2, "transaction"));
        forumList.add(new Forum("公告活动", R.drawable.ic_07, 2, "activity"));
        mView.setForumList(forumList);
    }
}
