package cn.tengfeistudio.forum.module.edu.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.Callback;
import com.zzhoujay.richtext.RichText;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import butterknife.BindView;
import butterknife.OnClick;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.listener.MyTextWatcher;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.module.home.HomeActivity;
import cn.tengfeistudio.forum.utils.toast.MyToast;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Response;

public class EduLoginActivity extends BaseActivity {

    @BindView(R.id.iv_toolbar_back)
    ImageView ivToolbarBack;
    @BindView(R.id.tv_toolbar_title)
    TextView title;
    @BindView(R.id.et_login_name)
    TextInputEditText etMobile;
    @BindView(R.id.et_login_pas)
    TextInputEditText etPassword;
    @BindView(R.id.et_login_check)
    TextInputEditText etCheck;
    @BindView(R.id.iv_check)
    ImageView ivCheck;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.tv_edulogin_tip)
    TextView tvEduloginTip;

    private String user_eduid = "";
    private String user_edupwd = "";
    private String cookie = "";
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_edu_login;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        GetCookies();
        isLogin();
    }

    @Override
    protected void initView() {
        initSlidr();
        initToolBar(true,"教务系统登陆");
        String text = "**更新课表需要重新登陆**\n此模块为爬虫模拟登陆教务系统，如想了解更多可查看[此篇博客](https://blog.csdn.net/qq_42895379/article/details/83098443)。\n如遇到系统错误，请尝试重新登陆。如多次登陆失败，请联系我们：个人 - 关于本程序 - bug反馈";
        RichText.fromMarkdown(text).into(tvEduloginTip);
        GetVerifation();
        btnLoginSetEnabled();
        etMobile.setText(App.getEduid());
        etMobile.setSelection(etMobile.getText().length());
        MyTextWatcher myTextWatcher = new MyTextWatcher() {
            @Override
            public void afterMyTextChanged(Editable editable) {
                btnLoginSetEnabled();
            }
        };
        etMobile.addTextChangedListener(myTextWatcher);
        etPassword.addTextChangedListener(myTextWatcher);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.btn_login, R.id.iv_check})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                String id = etMobile.getText().toString().trim();
                String pwd = etPassword.getText().toString().trim();
                String checkid = etCheck.getText().toString();
                doLogin(id, pwd, checkid);
                break;
            case R.id.iv_check:
                GetVerifation();
                break;
        }
    }

    /**
     * 设置判断按钮是否可以点击
     */
    private void btnLoginSetEnabled() {
        if (!TextUtils.isEmpty(etMobile.getText().toString().trim())
                && !TextUtils.isEmpty(etPassword.getText().toString().trim()))
            btnLogin.setEnabled(true);
        else
            btnLogin.setEnabled(false);
    }

    /**
     * 请求新的Cookie
     */
    private void GetCookies() {
        cookie = "";
        Observable.create((ObservableOnSubscribe<String>) emitter -> OkHttpUtils.get()
                .url("http://jwxt.gdufe.edu.cn/")
                .build()
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
                        Headers headers = response.headers();
                        for (int i = 0; i < headers.size(); i++) {
                            if (headers.name(i).equals("Set-Cookie"))
                                if (headers.value(i).endsWith(" Path=/"))
                                    cookie += headers.value(i).substring(0, headers.value(i).length() - 7);
                                else
                                    cookie += headers.value(i);
                        }
                        emitter.onNext(response.body().string());
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        emitter.onError(new Throwable("GetCookies onError" + call.toString()));
                    }

                    @Override
                    public void onResponse(Object response, int id) {
                        Log.e("print", "GetCookies onResponse");
                    }
                }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        //刷新验证码
                        GetVerifation();
                    }

                    @Override
                    public void onError(Throwable e) {
                        printLog(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    /**
     * 判断是否登录
     */
    private void isLogin() {
        if (sp.getString("id", "").isEmpty())
            return;
        Observable.create((ObservableOnSubscribe<String>) emitter -> OkHttpUtils.post()
                .url("http://jwxt.gdufe.edu.cn/jsxsd/xk/LoginToXkLdap")
                .addHeader("Cookie", cookie)
                .build()
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
                        String responseHTML = new String(response.body().bytes(), "utf-8");
                        emitter.onNext(responseHTML);
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        printLog("isLogin() onError");
                    }

                    @Override
                    public void onResponse(Object response, int id) {
                        printLog("isLogin() onResponse");
                    }
                }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(String s) {
                        checkLoginSuccess(s);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }



    /**
     * 获取验证码图片
     */
    @SuppressLint("CheckResult")
    private void GetVerifation() {
        Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> OkHttpUtils.post()
                .url("http://jwxt.gdufe.edu.cn/verifycode.servlet")
                .addHeader("Cookie", cookie)
                .build()
                .execute(new BitmapCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        emitter.onError(new Throwable("EduLoginActivity GetVerifation onError"));
                    }

                    @Override
                    public void onResponse(Bitmap response, int id) {
                        printLog("GetVerifation onResponse");
                        emitter.onNext(response);
                    }
                }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    ivCheck.setImageBitmap(bitmap);
                    etCheck.setText("");
                }, throwable -> printLog("EduLoginActivity_GetVerifation_onError:" + throwable.getMessage()));

    }

    /**
     * 登录操作
     *
     * @param eduid
     * @param pwd
     * @param checkid
     */
    @SuppressLint("CheckResult")
    private void doLogin(String eduid, String pwd, String checkid) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> OkHttpUtils.post()
                .url("http://jwxt.gdufe.edu.cn/jsxsd/xk/LoginToXkLdap")
                .addParams("USERNAME",eduid)
                .addParams("PASSWORD",pwd)
                .addHeader("Cookie", cookie)
                .build()
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
                        String responseHTML = new String(response.body().bytes(), "UTF-8");
                        user_eduid = eduid;
                        user_edupwd = pwd;
                        emitter.onNext(responseHTML);
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        emitter.onError(new Throwable("doLogin() onError " + call.toString() + " " + e.getMessage()));
                    }

                    @Override
                    public void onResponse(Object response, int id) {
                        Log.e("print", "doLogin() onResponse");
                    }
                }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> checkLoginSuccess(s), throwable -> printLog("EduLoginActivity_doLogin_onError:" + throwable.getMessage()));

    }

    /**
     *    提取页面action操作，用于判断是否以及登录成功
     * @param htmlText
     * @return
     */
    private static String getLoginPageInformation(String htmlText){
        Document doc=Jsoup.parse(htmlText);
        Elements elements=doc.select("form");
        String str=elements.attr("action");
        return str;
    }

    /**
     * 学生姓名
     * @param htmlText
     * @return
     */
    private static String getStudentName(String htmlText){
        Document doc=Jsoup.parse(htmlText);
        Element element=doc.getElementById("Top1_divLoginName");
        return element.text();

    }
    /**
     * 检查是否登录成功
     */
    private void checkLoginSuccess(String loginresult) {
        if(getLoginPageInformation(loginresult).equals("/jsxsd/xk/LoginToXkLdap")){
            ToastShort("学号或密码错误");
        }else{
            AfterSuccessLogin(getStudentName(loginresult));
            return ;
        }
    }

    /**
     * 成功登陆后的操作
     */
    private void AfterSuccessLogin(String studentName) {
        MyToast.showText(getApplicationContext(), "登陆成功", Toast.LENGTH_SHORT, true);
        SharedPreferences sp = getSharedPreferences(App.MY_SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(App.COOKIE, cookie);
        editor.putString(App.USER_EDUID_KEY, user_eduid);
        editor.putString(App.USER_EDUPWD_KEY, user_edupwd);
        editor.putString(App.USER_EDUNAME_KEY,studentName);
        editor.apply();
       // gotoActivity(EduActivity.class);
        Intent intent=new Intent(EduLoginActivity.this,HomeActivity.class);
        intent.putExtra("schedule",1);
        startActivity(intent);
        finishActivity();
    }



}
