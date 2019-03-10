package cn.tengfeistudio.forum.module.schedule.edu.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;

import cn.tengfeistudio.forum.adapter.ScheduleGridAdapter;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.api.beans.Course;
import cn.tengfeistudio.forum.api.bean.MessageEvent;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.local.DataBase.MyDB;
import cn.tengfeistudio.forum.R;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.angmarch.views.NiceSpinner;
import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.tengfeistudio.forum.utils.NetConfig;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Response;

import static cn.tengfeistudio.forum.adapter.BaseAdapter.STATE_LOAD_NOTHING;
import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastNetWorkError;

public class ScheduleActivity extends BaseActivity {
    @BindView(R.id.courceDetail)
    GridView courceDetail;
    @BindView(R.id.switchWeek)
    NiceSpinner spinner;
    @BindView(R.id.iv_toolbar_menu)
    ImageView ivToolbarMenu;

    private String eduid;
    private String edupwd;
    private String userName;
    private String cookie;

    private int nowWeek = 1;
    public static final int requestCode = 512;

    private String[][] contents;
    private ScheduleGridAdapter adapter;
    List<Course> scheduleList = new ArrayList<>();

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_schedule;
    }

    @Override
    protected void initData() {
        contents = new String[6][7];
        //学号
        eduid = App.getEduid();
        //教务系统密码
        edupwd=App.getEduPwd();
        //用户姓名
        userName = App.getEduName();
        cookie = App.getCookie();
    }

    @Override
    protected void initView() {

        ivToolbarMenu.setImageResource(R.drawable.ic_check_black_24dp);
        ivToolbarMenu.setOnClickListener(view -> {
                EventBus.getDefault().post(new MessageEvent("scheduleRefresh"));
                finishActivity();

        });
        initSlidr();
        initSpinner();
        clearOldSchedule();
        getScheduleFromEdu();
    }

    private void clearOldSchedule() {
        MyDB db = new MyDB(this);
        db.clearSchedule();
    }

    /**
     * 初始化spinner
     */
    private void initSpinner() {
        List<String> dataset = new LinkedList<>(Arrays.asList(getResources().getStringArray(R.array.scheduleWeek)));
        spinner.attachDataSource(dataset);
        spinner.setBackgroundResource(R.drawable.tv_round_border);
        spinner.setTextColor(getResources().getColor(R.color.white));
        spinner.setSelectedIndex(nowWeek - 1);
        spinner.setArrowDrawable(getResources().getDrawable(R.drawable.ic_expand_more_black_24dp));
        spinner.setArrowTintColor(R.color.white);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                nowWeek = pos + 1;
                initScheduleDataFromDB();
                //刷新课表
                getScheduleFromEdu();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    /**
     * 从教务系统抓取课程表
     */
    private void getScheduleFromEdu() {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            OkHttpUtils.get()
                    .url(NetConfig.BASE_SCHEDULE)
                    .addParams("card", eduid)  //学号
                    .addParams("cardpassword",edupwd) //教务系统密码
                    .addParams("week",String.valueOf(nowWeek))  //第几周
                    .addParams("schoolyear","2016-2017-2")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onResponse(String response, int id){
                            if (!response.contains("code")) {
                                ToastNetWorkError();
                            }
                            JSONObject dataObj = JSON.parseObject(response);
                            String JsonObjs=JSON.toJSONString(dataObj.get("data"));
                            emitter.onNext(JsonObjs);
                        }
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            printLog("ScheduleActivity getSchedule onError");
                            printLog(e.getMessage());
                        }
                    });
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                    @Override
                    public void onNext(String s) {
                        initScheduleDataFromEdu(s);
                        ToastShort("更新课表完成");
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
     * 初始化从DataBase的schedule数据
     */
    private void initScheduleDataFromDB() {
        MyDB db = new MyDB(this);
        scheduleList = db.getSchedule();
        contents = new String[6][7];
        for (int x = 0; x < 6; x++)
            for (int y = 0; y < 7; y++)
                contents[x][y] = "";
        for (int i = 0; i < scheduleList.size(); i++) {
            Course course = scheduleList.get(i);
            printLog(course.toString());
            contents[(course.getStartSection() - 1) / 2][course.getWeekDay() - 1] = course.getCourseName() + "\n" + course.getPlace();
        }
        adapter = new ScheduleGridAdapter(this, scheduleList);
        adapter.setContent(contents, 6, 7);
        courceDetail.setAdapter(adapter);
    }

    /**
     * 初始化从教务系统爬来的Schedule数据 存到 DB中
     *
     * @param JsonObjs
     */
    private void initScheduleDataFromEdu(String JsonObjs) {
        scheduleList = JSON.parseArray(JsonObjs, Course.class);
        MyDB db = new MyDB(this);
        for (int i = 0; i < scheduleList.size(); i++) {
            Course course = scheduleList.get(i);
            db.handSingleReadSchedule(course);
        }
        initScheduleDataFromDB();
        //adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
