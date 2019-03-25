package cn.tengfeistudio.forum.module.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.adapter.MainPageAdapter;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.bean.MessageEvent;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.module.activity.ActivityFragment;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.module.base.BaseFragment;
import cn.tengfeistudio.forum.module.hotnews.HotNewsFragment;
import cn.tengfeistudio.forum.module.mine.MineFragment;
import cn.tengfeistudio.forum.module.post.postcontent.fullscreen.PostActivity;
import cn.tengfeistudio.forum.module.post.postlist.PostsActivity;
import cn.tengfeistudio.forum.module.schedule.edu.main.ScheduleActivity;
import cn.tengfeistudio.forum.module.schedule.home.ScheduleFragment;
import cn.tengfeistudio.forum.module.setting.main.SettingActivity;
import cn.tengfeistudio.forum.module.setting.theme.ThemeActivity;
import cn.tengfeistudio.forum.module.user.login.LoginActivity;
import cn.tengfeistudio.forum.module.user.userdetail.UserDetailActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.NetConfig;
import cn.tengfeistudio.forum.utils.NotifictionUtils;
import cn.tengfeistudio.forum.widget.MyBottomTab;
import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

import static android.content.ContentValues.TAG;
import static cn.tengfeistudio.forum.utils.LogUtils.printLog;

public class HomeActivity extends BaseActivity
        implements ViewPager.OnPageChangeListener {
    @BindView(R.id.bottom_bar)
    MyBottomTab bottomBar;
    private ViewPager viewPager;
    private List<BaseFragment> fragments = new ArrayList<>();

    private long mExitTime;
    private String version_name;
    private StompClient mStompClient;
    @Override
    protected int getLayoutID() {
        return R.layout.activity_home;
    }

    @Override
    protected void initData() {
        initViewpager();
        //更新版本
        // discoverVersion();
    }

    @SuppressLint("CheckResult")
    private void discoverVersion() {
        PackageManager manager = getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        version_name = "1.0";
        if (info != null) {
            version_name = info.versionName;
        }
        RetrofitService.getRelease()
                .subscribe(responseBody -> {
                    String response = responseBody.string();
                    if (!response.contains("url"))
                        return;
                    afterGetVersion(response);
                }, throwable -> printLog("HomeActivity_discoverVersion_onError:" + throwable.getMessage()));
    }

    @Override
    protected void initView() {

        //登录教务系统后，跳转传过来的参数
        Intent intent = getIntent();
        int schedulePosition = intent.getIntExtra("schedule", 0);
        if (schedulePosition == 1) {
            //跳转到ScheduleFragment
            viewPager.setCurrentItem(1);
        }
        bottomBar.setOnTabChangeListener((v, position, isChange) -> setSelect(position, isChange));

        //已经登录
        if(Store.getInstance().getToken().length()>0){
                //websocket连接服务端
                createStompClient();
                //接受回复
                receiveReply();
                //新活动
                receiveActivity();
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep(10*1000);//休眠10秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //拉取未读消息
                    getAllUnreadReply();
                }
            }.start();

        }
    }

    private void setSelect(int position, boolean isChange) {
        if (isChange)
            viewPager.setCurrentItem(position, false);
        else{
            fragments.get(position).ScrollToTop();
        }

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        printLog("HomeActivity onActivityResult");
        if (resultCode == RESULT_OK) {
            printLog("resultCode == RESULT_OK");
            switch (requestCode) {
                case ThemeActivity.requestCode://32
                    recreate();
                    printLog("onActivityResult ThemeActivity");
                    break;
                case LoginActivity.requestCode://64
                    doRefresh();
                    printLog("onActivityResult LoginActivity");
                    break;
                case UserDetailActivity.requestCode://128
                    doRefresh();
                    printLog("onActivityResult UserDetailActivity");
                    break;
                case SettingActivity.requestCode://256
                    doRefresh();
                    printLog("onActivityResult SettingActivity");
                    break;
                case ScheduleActivity.requestCode://512
                    doRefresh();
                    printLog("onActivityResult SettingActivity");
                    break;
            }
        } else
            printLog("resultCode != RESULT_OK");
        hideKeyBoard();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideKeyBoard();
    }

    public static void doRefresh() {
//        if (homeFragment != null)
//            homeFragment.doRefresh();
        if (activityFragment != null)
            activityFragment.doRefresh();
        if (hotNewsFragment != null)
            hotNewsFragment.doRefresh();
        if (scheduleFragment != null)
            scheduleFragment.doRefresh();
        if (mineFragment != null)
            mineFragment.doRefresh();
    }

    //private static HomeFragment homeFragment;
    private static ActivityFragment activityFragment;
    private static HotNewsFragment hotNewsFragment;
    private static ScheduleFragment scheduleFragment;
    private static MineFragment mineFragment;

    private void initViewpager() {
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(4);
        viewPager.addOnPageChangeListener(this);
        //homeFragment = new HomeFragment();
        activityFragment = new ActivityFragment();
        hotNewsFragment = new HotNewsFragment();
        scheduleFragment = new ScheduleFragment();
        mineFragment = new MineFragment();
        //fragments.add(homeFragment);

        fragments.add(activityFragment);
        fragments.add(scheduleFragment);
        fragments.add(hotNewsFragment);
        fragments.add(mineFragment);
        MainPageAdapter adapter = new MainPageAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
    }

    /**
     * 解析更新版本信息
     *
     * @param jsonObj
     */
    private void afterGetVersion(String jsonObj) {
        JSONObject jsonObject = JSON.parseObject(jsonObj);
        String tag_name = jsonObject.getString("tag_name"); // eg:1.4.0
        String name = jsonObject.getString("name"); // eg:正式推送版本
        String body = jsonObject.getString("body"); // eg:不要改需求了！
        JSONArray assets = JSONArray.parseArray(jsonObject.getString("assets"));
        JSONObject assetObj = assets.getJSONObject(0);
        String updated_at = assetObj.getString("updated_at"); // eg:2018-10-09T07:06:09Z
        String browser_download_url = assetObj.getString("browser_download_url");
        // eg:https://github.com/WithLei/DistanceMeasure/releases/download/1.4.0/DistanceMeasure.apk

        if (tag_name.equals(version_name)) {
//            MyToast.showText(this,"已经是最新版本");
            printLog("HomeActivity_afterGetVersion:已经是最新版本");
        } else {
            printLog("HomeActivity_afterGetVersion:检测到新版本");
            new AlertDialog.Builder(this)
                    .setTitle("检测到新版本")
                    .setMessage("版本名：" + name + "\n" +
                            "版本号：" + tag_name + "\n" +
                            "更新内容：\n" + body + "\n\n" +
                            "更新时间：" + updated_at)
                    .setCancelable(!body.contains("重要更新"))
                    .setPositiveButton("下载", (dialogInterface, i) -> {
                        Intent intent = new Intent();
                        Uri uri = Uri.parse(browser_download_url);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .create()
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            if ((System.currentTimeMillis() - mExitTime) > 1500) {
                Toast.makeText(this, "再按一次退出广财校园吧客户端(｡･ω･｡)~~", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessageEvent(MessageEvent messageEvent) {
        // 使用EventBus接受消息，当更新schedule后返回主页刷新课程表
        if (scheduleFragment != null)
            scheduleFragment.doRefresh();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideKeyBoard();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        bottomBar.setSelect(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    /**
     * 连接服务端
     */
    private void createStompClient() {
        Map<String,String> map=new HashMap<>();
        map.put(Constants.AUTHORIZATION, Store.getInstance().getToken());
        mStompClient = Stomp.over(WebSocket.class, NetConfig.CONNECT_WEBSOCKET_URL,map);
        mStompClient.connect();
        // Toast.makeText(LoginActivity.this,"开始连接 192.168.191.1:8080",Toast.LENGTH_SHORT).show();
        mStompClient.lifecycle().subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d(TAG, "Stomp connection opened");
                        break;
                    case ERROR:
                        Log.e(TAG, "Stomp Error", lifecycleEvent.getException());
                        break;
                    case CLOSED:
                        Log.d(TAG, "Stomp connection closed");
                        break;
                }
            }
        });
    }


    /**
     * 接受服务端返回数据(评论回复)
     */
    private void receiveReply() {
        mStompClient.topic("/user/"+ App.getUid()+"/message").subscribe(new Action1<StompMessage>() {
            @Override
            public void call(StompMessage stompMessage) {
                Log.e(TAG, App.getUid()+" launch_call: " +stompMessage.getPayload() );
                String message=stompMessage.getPayload();
                //回复内容
                String content=message.substring(message.indexOf(":")+1).trim();
                int topicId=Integer.parseInt(stompMessage.findHeader("topicId"));;
                //通知栏
                NotifictionUtils.showNotifictionIcon(getApplicationContext(), PostActivity.class,"新回复",content,topicId);
            }
        });
    }


    /**
     * 暂未使用
     * 接受服务端返回数据(新活动)
     */
    private void receiveActivity() {
        mStompClient.topic("/user/"+App.getUid()+"/activity").subscribe(new Action1<StompMessage>() {
            @Override
            public void call(StompMessage stompMessage) {
                Log.e(TAG, "call: " +stompMessage.getPayload() );
                int activityId=1;
                //通知栏
                NotifictionUtils.showNotifictionIcon(getApplicationContext(), HomeActivity.class,"新活动","活动内容",activityId);
            }
        });
    }

    /**
     * 拉取未读消息
     */
    private void getAllUnreadReply(){
        RetrofitService.getUnreadReply()
                .subscribe(responseBody -> {
                    String response = responseBody.string();
                    if (!response.contains("code"))
                        return;
                }, throwable -> printLog("获取未读消息:" + throwable.getMessage()));
    }
}
