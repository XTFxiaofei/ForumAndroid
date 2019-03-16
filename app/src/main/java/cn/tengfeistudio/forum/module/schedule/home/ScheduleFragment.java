package cn.tengfeistudio.forum.module.schedule.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import cn.tengfeistudio.forum.adapter.ScheduleGridAdapter;
import cn.tengfeistudio.forum.api.bean.MessageEvent;
import cn.tengfeistudio.forum.api.beans.Course;
import cn.tengfeistudio.forum.injector.components.DaggerScheduleFragComponent;
import cn.tengfeistudio.forum.local.DataBase.MyDB;
import cn.tengfeistudio.forum.module.edu.login.EduLoginActivity;
import cn.tengfeistudio.forum.module.home.HomeActivity;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.injector.modules.ScheduleFragModule;
import cn.tengfeistudio.forum.module.base.BaseFragment;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.utils.NetConfig;
import cn.tengfeistudio.forum.utils.StampToDate;
import cn.tengfeistudio.forum.utils.toast.ToastUtils;

import org.angmarch.views.NiceSpinner;
import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;

import static android.content.Context.MODE_PRIVATE;
import static cn.tengfeistudio.forum.utils.LogUtils.printLog;
import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastNetWorkError;
import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastShort;

public class ScheduleFragment extends BaseFragment
        implements ScheduleFragView, PopupMenu.OnMenuItemClickListener {
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
    private String[][] contents;
    private int nowWeek;
    private ScheduleGridAdapter adapter;
    List<Course> scheduleList = new ArrayList<>();

    @Inject
    protected ScheduleFragPresenter mPresenter;

    @Override
    public int getLayoutid() {
        return R.layout.fragment_schedule;
    }

    @Override
    protected void initData(Context content) {
        mPresenter.getData(false);
        //当前周数
        nowWeek = Integer.parseInt(App.getNowWeek());
        //课表内容
        contents = new String[6][7];
        //学号
        eduid = App.getEduid();
        //教务系统密码
        edupwd = App.getEduPwd();
        //用户姓名
        userName = App.getEduName();
        //cookie
        cookie = App.getCookie();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 删除旧的课表
     */
    private void clearOldSchedule() {
        MyDB db = new MyDB(getContext());
        db.clearSchedule();
    }

    /**
     * 初始化滑动事件
     */
    public void initSlidr() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)//滑动起始方向
                .edge(true)
                .edgeSize(0.18f)//距离左边界占屏幕大小的18%
                .build();
        Slidr.attach(getActivity(), config);
    }

    @Override
    protected void initView() {
        ivToolbarMenu.setOnClickListener(view -> {
            EventBus.getDefault().post(new MessageEvent("scheduleRefresh"));
            //finishActivity();
            if (App.getEduid().isEmpty() && App.getEduPwd().isEmpty()) {
                Intent intent = new Intent(mActivity, EduLoginActivity.class);
                startActivity(intent);
            } else {
                //创建弹出式菜单对象（最低版本11）
                PopupMenu popup = new PopupMenu(getContext(), getView());//第二个参数是绑定的那个view
                //获取菜单填充器
                MenuInflater inflater = popup.getMenuInflater();
                //填充菜单
                inflater.inflate(R.menu.menu_semester, popup.getMenu());
                //绑定菜单项的点击事件
                popup.setOnMenuItemClickListener(this::onMenuItemClick);
                //显示(这一行代码不要忘记了)
                popup.show();
                //ToastShort("已经登录了!");
            }

        });
        //初始化滑动事件
        initSlidr();
        //
        initSpinner();
        //删除旧的课表
        clearOldSchedule();
        //重教务系统获取课表
        getScheduleFromEdu();
    }

    public void doRefresh() {
        mPresenter.getData(true);
    }

    /**
     * 初始化spinner
     */
    private void initSpinner() {
        List<String> dataset = new LinkedList<>(Arrays.asList(getResources().getStringArray(R.array.scheduleWeek)));
        spinner.attachDataSource(dataset);
        spinner.setBackgroundResource(R.drawable.tv_round_border);
        spinner.setTextColor(getResources().getColor(R.color.white));
        spinner.setSelectedIndex(App.getScheduleNowWeek() - 1);
        spinner.setArrowTintColor(R.color.white);
        spinner.setArrowDrawable(getResources().getDrawable(R.drawable.ic_expand_more_black_24dp));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                App.setScheduleStartWeek(pos + 1);
                //选择的周数保存到本地数据库
                nowWeek = pos + 1;
                //选择的周数存到内存
                SharedPreferences sp = mActivity.getSharedPreferences(App.MY_SP_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(App.NOW_WEEK, String.valueOf(nowWeek));
                editor.apply();
                //刷新课表
                getScheduleFromEdu();
                //初始化,保存到数据库
                initScheduleDataFromDB();
                //
                loadSchedule();
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
        //printLog(App.getSchoolYear());
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            OkHttpUtils.get()
                    .url(NetConfig.BASE_SCHEDULE)
                    .addParams("card", eduid)  //学号
                    .addParams("cardpassword", edupwd) //教务系统密码
                    .addParams("week", String.valueOf(nowWeek))  //第几周
                    .addParams("schoolyear", App.getSchoolYear()) //学年
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onResponse(String response, int id) {
                            if (!response.contains("code")) {
                                ToastNetWorkError();
                            }
                            JSONObject dataObj = JSON.parseObject(response);
                            String JsonObjs = JSON.toJSONString(dataObj.get("data"));
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
        MyDB db = new MyDB(getContext());
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
        adapter = new ScheduleGridAdapter(getContext(), scheduleList);
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
        MyDB db = new MyDB(getContext());
        //删去旧课表
        db.clearSchedule();
        for (int i = 0; i < scheduleList.size(); i++) {
            Course course = scheduleList.get(i);
            db.handSingleReadSchedule(course);
        }

        initScheduleDataFromDB();
        //adapter.notifyDataSetChanged();
    }

    @Override
    public void ScrollToTop() {

    }

    @Override
    protected void initInjector() {
        DaggerScheduleFragComponent.builder()
                .applicationComponent(App.getAppComponent())
                .scheduleFragModule(new ScheduleFragModule(this))
                .build()
                .inject(this);
    }

    private HomeActivity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (HomeActivity) context;
    }


    //15251104218  App.getEduid()
    //2015-2016-1

    /**
     * 哪年入学，如 15251104218，是2015年
     *
     * @return
     */
    private int getBeginYear() {
        String year = App.getEduid().substring(0, 2);
        return Integer.parseInt(year);
    }

    //弹出式菜单的单击事件处理
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        String schoolyear = StampToDate.getCurrentSchoolYear();
        SharedPreferences sp = mActivity.getSharedPreferences(App.MY_SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        switch (item.getItemId()) {
            case R.id.one:
                //ToastShort("1");
                schoolyear = "20" + getBeginYear() + "-" + "20" + (getBeginYear() + 1) + "-" + 1;
                break;
            case R.id.two:
                //ToastShort("2");
                schoolyear = "20" + getBeginYear() + "-" + "20" + (getBeginYear() + 1) + "-" + 2;
                break;
            case R.id.three:
                //ToastShort("3");
                schoolyear = "20" + (getBeginYear() + 1) + "-" + "20" + (getBeginYear() + 2) + "-" + 1;
                break;
            case R.id.four:
                //ToastShort("4");
                schoolyear = "20" + (getBeginYear() + 1) + "-" + "20" + (getBeginYear() + 2) + "-" + 2;
                break;
            case R.id.five:
                //ToastShort("5");
                schoolyear = "20" + (getBeginYear() + 2) + "-" + "20" + (getBeginYear() + 3) + "-" + 1;
                break;
            case R.id.six:
                //ToastShort("6");
                schoolyear = "20" + (getBeginYear() + 2) + "-" + "20" + (getBeginYear() + 3) + "-" + 2;
                break;
            case R.id.seven:
                // ToastShort("7");
                schoolyear = "20" + (getBeginYear() + 3) + "-" + "20" + (getBeginYear() + 4) + "-" + 1;
                break;
            case R.id.eight:
                //ToastShort("8");
                schoolyear = "20" + (getBeginYear() + 3) + "-" + "20" + (getBeginYear() + 4) + "-" + 2;
                break;
            default:
                break;
        }
        editor.putString(App.SCHOOL_YEAR, schoolyear);
        editor.apply();

        //刷新课表
        getScheduleFromEdu();
        //初始化,保存到数据库
        initScheduleDataFromDB();
        return false;
    }

    // @OnClick(R.id.iv_toolbar_menu)
    public void onViewClicked() {
        //ToastUtils.ToastShort("由于技术升级，爬虫不可用，此功能暂时下线");
        if (App.getEduid().isEmpty() && App.getEduPwd().isEmpty()) {
            Intent intent = new Intent(mActivity, EduLoginActivity.class);
            startActivity(intent);
        } else {
            //创建弹出式菜单对象（最低版本11）
            PopupMenu popup = new PopupMenu(getContext(), getView());//第二个参数是绑定的那个view
            //获取菜单填充器
            MenuInflater inflater = popup.getMenuInflater();
            //填充菜单
            inflater.inflate(R.menu.menu_semester, popup.getMenu());
            //绑定菜单项的点击事件
            popup.setOnMenuItemClickListener(this::onMenuItemClick);
            //显示(这一行代码不要忘记了)
            popup.show();
            //ToastShort("已经登录了!");
        }
    }

    @Override
    public void loadSchedule() {
        courceDetail.setAdapter(mPresenter.getAdapter(mActivity));
    }
}
