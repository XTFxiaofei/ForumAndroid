package cn.tengfeistudio.forum.module.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.local.DataBase.MyDB;
import cn.tengfeistudio.forum.module.post.postlist.PostsActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.NetConfig;
import cn.tengfeistudio.forum.utils.NotifictionUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

import static android.content.ContentValues.TAG;
import static cn.tengfeistudio.forum.utils.LogUtils.printLog;

public class LaunchActivity extends Activity {
    private static final int WAIT_TIME = 2;
    //玩家收藏活动的Id集合
    public static List<Integer> collectActivityIds = new ArrayList<Integer>();


    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launch);
        App app = (App) getApplication();
        app.regReciever();
        setCopyRight();
        doPreWrok();
        Observable.timer(WAIT_TIME, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> enterHome());
    }

    /**
     * 做一些启动前的工作
     */
    private void doPreWrok() {
        App.setCookie("");


        if (Store.getInstance().getToken().length() > 0) {
            /** 获取玩家已经收藏的活动 */
            RetrofitService.getCollectionActivity()
                    .subscribe(responseBody -> {
                        JSONObject jsonObject = JSON.parseObject(responseBody.string());
                        if (jsonObject.getInteger("code") != Constants.RETURN_CONTINUE) {
                        } else {
                            //收藏的活动id集合
                            collectActivityIds = JSON.parseArray(jsonObject.getString("data"), Integer.class);
                            //连接网络则清楚之前的素拓活动
                            MyDB db = new MyDB(getBaseContext());
                            //清理旧素拓活动
                            db.clearActivityList();
                        }
                    });
        }
    }


    private void enterHome() {
        startActivity(new Intent(LaunchActivity.this, HomeActivity.class));
        finish();
    }

    //自动续命copyright
    private void setCopyRight() {
        int year = 2019;
        int yearNow = Calendar.getInstance().get(Calendar.YEAR);

        if (year < yearNow) {
            year = yearNow;
        }
        ((TextView) findViewById(R.id.copyright))
                .setText("©2019-" + year + " tengfeistudio.cn");
    }

    /**
     * startActivity屏蔽物理返回按钮
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish() {
        super.finish();
        // 去掉自带的转场动画
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
    }


}
