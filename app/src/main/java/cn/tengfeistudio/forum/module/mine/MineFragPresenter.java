package cn.tengfeistudio.forum.module.mine;

import android.annotation.SuppressLint;
import android.provider.SyncStateContract;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.module.base.BasePresenter;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.NetConfig;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MineFragPresenter implements BasePresenter {
    private final MineFragment mView;
    private static final int[] icons = new int[]{
            R.drawable.ic_favorite_white_12dp,
            R.drawable.ic_palette_black_24dp,
            R.drawable.ic_settings_24dp,
            R.drawable.ic_menu_share_24dp,
            R.drawable.ic_info_24dp,
            R.drawable.ic_autorenew_black_24dp,
            R.drawable.ic_lab_24dp,
    };



    private static final String[] titles = new String[]{
            "我的收藏",
            "主题设置",
            "设置",
            "分享客户端",
            "关于本程序",
            "热爱开源，感谢分享",
            "教务系统登录",
    };

    public MineFragPresenter(MineFragment mView){
        this.mView = mView;
    }

    @Override
    public void getData(boolean isRefresh) {
        if (App.ISLOGIN()){
            getUserAvator();
        }
    }

    List<Map<String, Object>> getMenuList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < icons.length; i++) {
            Map<String, Object> ob = new HashMap<>();
            ob.put("icon", icons[i]);
            ob.put("title", titles[i]);
            list.add(ob);
        }
        return list;
    }

    @Override
    public void getMoreData() {

    }


    @SuppressLint("CheckResult")
    protected synchronized void getUserAvator() {
        Observable<String> observable = RetrofitService.getNewToken();

        DisposableObserver<String> observer = new DisposableObserver<String>() {
            @Override
            public void onNext(String s) {
                Observable.create((ObservableOnSubscribe<String>) emitter -> {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(NetConfig.BASE_GET_USER_DETAILS)
                           // .header("Authorization", "Bearer " + Store.getInstance().getToken())
                            .header(Constants.AUTHORIZATION, Store.getInstance().getToken())
                            .get()
                            .build();
                    String response = client.newCall(request).execute().body().string();
                    emitter.onNext(response);
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(responseString -> {
                            if (!responseString.contains("data")) {
                                Log.e("print","MineFragment_getAvatar_subscribe:获取用户信息出错 需要处理");
                                return;
                            }
                            JSONObject jsonObject = JSON.parseObject(responseString);
                            String avatarSrc = "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3269115595,4045078490&fm=27&gp=0.jpg", name = "xxx";
                            JSONObject obj = JSON.parseObject(jsonObject.getString("data"));
                            if(obj.getString("icon").trim().length()>0){
                                avatarSrc = obj.getString("icon");
                            }
                            name = obj.getString("nickname");
                            mView.loadInfo(avatarSrc, name);
                        }, throwable -> Log.e("print","MineFragment_getAvatar_subscribe_onError:" + throwable.getMessage()));
            }

            @Override
            public void onError(Throwable e) {
                Log.e("print", "MineFragment_getUserAvator_onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
