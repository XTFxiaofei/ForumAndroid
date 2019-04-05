package cn.tengfeistudio.forum.module.user.login;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.java_websocket.WebSocket;

import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.listener.MyTextWatcher;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.module.home.HomeActivity;
import cn.tengfeistudio.forum.module.home.LaunchActivity;
import cn.tengfeistudio.forum.module.post.postcontent.fullscreen.PostActivity;
import cn.tengfeistudio.forum.module.post.postcontent.fullscreen.ReplyActivity;
import cn.tengfeistudio.forum.module.post.postlist.PostsActivity;
import cn.tengfeistudio.forum.module.user.sign.SignActivity;
import cn.tengfeistudio.forum.module.user.findpwd.FindpwdActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.NetConfig;
import cn.tengfeistudio.forum.utils.NotifictionUtils;
import cn.tengfeistudio.forum.utils.toast.MyToast;
import cn.tengfeistudio.forum.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

import static android.content.ContentValues.TAG;
import static cn.tengfeistudio.forum.utils.LogUtils.printLog;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.myToolBar)
    FrameLayout myToolBar;
    @BindView(R.id.et_login_name)
    TextInputEditText etLoginName;
    @BindView(R.id.et_login_pas)
    TextInputEditText etLoginPas;
    @BindView(R.id.cb_rem_user)
    CheckBox cbRemUser;
    @BindView(R.id.btn_login)
    Button btnLogin;
    public static final int requestCode = 64;
    private StompClient mStompClient;

    private MyTextWatcher textWatcher = new MyTextWatcher() {
        @Override
        public void afterMyTextChanged(Editable editable) {
            btnLoginSetEnabled();
        }
    };

    @Override
    protected int getLayoutID() {
        return R.layout.activity_login;
    }

    @Override
    protected void initData() {
    }

    @SuppressLint("CheckResult")
    @Override
    protected void initView() {
        initToolBar(true,"登陆账号");
        initSlidr();
        initText();

        btnLoginSetEnabled();
        Observable.timer(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> showSoftInput());
    }

    private void initText() {
        if (App.isRemeberPwdUser()) {
            etLoginName.setText(App.getEmail());
            etLoginPas.setText(App.getPwd());
            cbRemUser.setChecked(true);
        } else{
            etLoginName.setText(App.getEmail());
            cbRemUser.setChecked(false);
        }
        etLoginName.setSelection(etLoginName.getText().length());
        etLoginPas.setSelection(etLoginPas.getText().length());

        etLoginName.addTextChangedListener(textWatcher);

        etLoginPas.addTextChangedListener(textWatcher);
    }

    /**
     * 设置判断按钮是否可以点击
     */
    private void btnLoginSetEnabled() {
        if (!TextUtils.isEmpty(etLoginName.getText().toString().trim())
                && !TextUtils.isEmpty(etLoginPas.getText().toString().trim()))
            btnLogin.setEnabled(true);
        else
            btnLogin.setEnabled(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.btn_login, R.id.tv_register, R.id.tv_forgetPwd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                String email = etLoginName.getText().toString().trim();
                String pwd = etLoginPas.getText().toString().trim();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd))
                    doLogin(email, pwd);
                break;
            case R.id.tv_register:
                gotoActivity(SignActivity.class);
                break;
            case R.id.tv_forgetPwd:
                gotoActivity(FindpwdActivity.class);
                break;
        }
    }

    @SuppressLint("CheckResult")
    private void doLogin(String email, String pwd) {
        RetrofitService.doLogin(email, pwd)
                .subscribe(responseBody -> {
                    JSONObject obj = null;
                    try {
                        obj = JSON.parseObject(responseBody.string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int statusCode = obj.getInteger("code");
                    //String token = obj.getString("result");
                    //if (statusCode == 20000){
                    if(statusCode== Constants.RETURN_CONTINUE){
                        String token=obj.getJSONObject("data").getString("token");
                        printLog("code:" + statusCode + " token:" + token);
                        afterLoginSuccess(email, pwd, token);
                    }else{
                        afterLoginFail();
                    }
                });
    }

    private void afterLoginFail() {
        MyToast.showText(getApplicationContext(),"账号或密码错误", Toast.LENGTH_SHORT,false);
    }

    private void afterLoginSuccess(String email, String pwd, String token) {
        SharedPreferences sp = getSharedPreferences(App.MY_SP_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(App.USER_EMAIL_KEY, email);
        editor.putString(App.USER_PWD_KEY, pwd);
        editor.putString(App.USER_TOKEN_KEY, token);
        editor.putBoolean(App.IS_REMEBER_PWD_USER,cbRemUser.isChecked());
        editor.putBoolean(App.IS_LOGIN, true);
        editor.apply();
        MyToast.showText(getApplicationContext(),"登录成功", Toast.LENGTH_SHORT,true);
        printLog("登录成功");
        getUserInfo();
        getCollectActivity();
    }


    /** 获取玩家已经收藏的活动 */
    private void getCollectActivity(){
        if(Store.getInstance().getToken().length()>0){
            RetrofitService.getCollectionActivity()
                    .subscribe(responseBody -> {
                        JSONObject jsonObject = JSON.parseObject(responseBody.string());
                        if (jsonObject.getInteger("code") != Constants.RETURN_CONTINUE){
                        }else{
                            //收藏的活动id集合
                            LaunchActivity.collectActivityIds=JSON.parseArray(jsonObject.getString("data"), Integer.class);
                        }
                    });
        }
    }

    /**
     * 登陆成功后获取用户信息
     */
    @SuppressLint("CheckResult")
    private void getUserInfo() {
        RetrofitService.getUserDetails()
                .subscribe(responseBody -> {
                    JSONObject jsonObject = JSON.parseObject(responseBody.string());
                    if (jsonObject.getInteger("code") != Constants.RETURN_CONTINUE){
                        ToastShort("服务器出状况惹，再试试( • ̀ω•́ )✧");
                    }else{
                        //afterGetUserInfo(jsonObject.getString("result"));
                        afterGetUserInfo(jsonObject.getString("data"));
                    }
                });
    }

    private void afterGetUserInfo(String jsonObj){
        JSONObject obj = JSON.parseObject(jsonObj);
        SharedPreferences sp = getSharedPreferences(App.MY_SP_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(App.USER_UID_KEY, obj.getInteger("userId"));
        editor.putString(App.USER_NAME_KEY, obj.getString("nickname"));
        editor.putString(App.USER_ROLE_KEY, obj.getString("role"));
        editor.apply();
        setResult(RESULT_OK);



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
        finishActivity();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        mStompClient.topic("/user/"+App.getUid()+"/message").subscribe(new Action1<StompMessage>() {
            @Override
            public void call(StompMessage stompMessage) {
                Log.e(TAG, App.getUid()+" login_call: " +stompMessage.getPayload() );
                String message=stompMessage.getPayload();
                //回复内容
                String content=message.substring(message.indexOf(":")+1).trim();
                int topicId=Integer.parseInt(stompMessage.findHeader("topicId"));
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
