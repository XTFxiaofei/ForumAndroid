package cn.tengfeistudio.forum.module.edu.main;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import butterknife.BindView;
import butterknife.OnClick;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.module.schedule.edu.main.ScheduleActivity;
import cn.tengfeistudio.forum.utils.NetConfig;
import cn.tengfeistudio.forum.utils.toast.MyToast;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.Call;
import okhttp3.Response;

public class EduActivity extends BaseActivity {

    @BindView(R.id.btn_main_exit)
    Button btnMainExit;
    @BindView(R.id.tv_main_name)
    TextView tvMainName;
    @BindView(R.id.btn_main_jump)
    Button btnMainJump;

    private String Cookie;
    private int id;
    private String stuName;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        printLog("butterKnife bind");
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_edumain;
    }

    @Override
    protected void initData() {
        sp = getSharedPreferences(App.MY_SP_NAME, MODE_PRIVATE);
        Cookie = sp.getString(App.COOKIE, "");
        id = sp.getInt(App.USER_UID_KEY, 0);
       stuName = sp.getString(App.USER_EDUNAME_KEY, "");
    }


    @Override
    protected void initView() {
        initSlidr();
        initToolBar(true, "教务系统选项");
        if (!stuName.isEmpty() && stuName != null)
            tvMainName.setText("你好，" + stuName + "同学");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void doLogout() {
        printLog("退出登陆");
        OkHttpUtils.post()
                .url(NetConfig.BASE_EDU_HOST_ME + id)
                .addHeader("Cookie", Cookie)
                //无__VIEWSTATE参数不能够正常post
                //.addParams("__VIEWSTATE", App.get__VIEWSTATE())
                .build()
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
                        String responseHTML = new String(response.body().bytes(), "UTF-8");
//                        writeData("/sdcard/Test/LogoutHTML.txt", responseHTML);
                        getLogout();
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        printLog("doLogout onError");
                    }

                    @Override
                    public void onResponse(Object response, int id) {
                        printLog("doLogout onResponse");
                    }
                });
    }

    /**
     * 注册二次退出
     */
    private void getLogout() {
        OkHttpUtils.get()
                .url("http://jwxt.gdufe.edu.cn/jsxsd/")
                .addHeader("Cookie", Cookie)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        printLog("getLogout onError");
                    }

                    @Override
                    public void onResponse(String response, int id) {
//                        printLog(response);
//                        writeData("/sdcard/Test/LogoutGetHTML.txt", response);
                        checkLogoutSuccess(response);
                    }
                });
    }

    /**
     * 完成退出登录操作
     */
    private void afterSuccessLogout() {
        SharedPreferences.Editor editor = sp.edit();
        String Cookie = sp.getString("Cookie", "");
        editor.clear();
        editor.putString("Cookie", Cookie);
        editor.apply();
        printLog("退出登录成功");
        MyToast.showText(getApplicationContext(), "退出登录成功", Toast.LENGTH_SHORT, true);
        finishActivity();
    }

    /**
     * 检查是否退出登录成功
     */
    @SuppressLint("CheckResult")
    private void checkLogoutSuccess(String logoutresult) {
        Document doc=Jsoup.parse(logoutresult);
        Elements elements=doc.select("form");
        String str=elements.attr("action");
            //获取所要查询的URL,这里相应地址button的名字叫成绩查询
            if (str.equals("/jsxsd/xk/LoginToXkLdap")) {
                Observable.just(true)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aBoolean -> afterSuccessLogout());
                return;
            }

    }

    @OnClick({R.id.btn_main_exit, R.id.btn_main_jump, R.id.btn_main_ecard})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_main_exit:
                doLogout();
                break;
            case R.id.btn_main_jump:
                gotoActivity(ScheduleActivity.class);
                break;
            case R.id.btn_main_ecard:
//                gotoActivity(ECardActivity.class);
                ToastShort("据程序员说下个版本一定会开发出来的(flag");
                break;
        }
    }

}