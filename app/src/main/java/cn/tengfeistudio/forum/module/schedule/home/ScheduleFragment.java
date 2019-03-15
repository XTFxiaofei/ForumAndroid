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
import android.widget.PopupMenu;

import cn.tengfeistudio.forum.injector.components.DaggerScheduleFragComponent;
import cn.tengfeistudio.forum.module.edu.login.EduLoginActivity;
import cn.tengfeistudio.forum.module.home.HomeActivity;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.injector.modules.ScheduleFragModule;
import cn.tengfeistudio.forum.module.base.BaseFragment;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.utils.StampToDate;
import cn.tengfeistudio.forum.utils.toast.ToastUtils;

import org.angmarch.views.NiceSpinner;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static android.content.Context.MODE_PRIVATE;
import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastShort;

public class ScheduleFragment extends BaseFragment
        implements ScheduleFragView, PopupMenu.OnMenuItemClickListener {
    @BindView(R.id.courceDetail)
    GridView courceDetail;
    @BindView(R.id.switchWeek)
    NiceSpinner spinner;

    @Inject
    protected ScheduleFragPresenter mPresenter;

    @Override
    public int getLayoutid() {
        return R.layout.fragment_schedule;
    }

    @Override
    protected void initData(Context content) {
        mPresenter.getData(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void initView() {
        initSpinner();
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
                loadSchedule();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
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
     * @return
     */
    private int getBeginYear(){
        String year=App.getEduid().substring(0,2);
        return Integer.parseInt(year);
    }

    //弹出式菜单的单击事件处理
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        String schoolyear= StampToDate.getCurrentSchoolYear();
        SharedPreferences sp = mActivity.getSharedPreferences(App.MY_SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        switch (item.getItemId()) {
            case R.id.one:
                //ToastShort("1");
                schoolyear="20"+getBeginYear()+"-"+"20"+(getBeginYear()+1)+"-"+1;
                break;
            case R.id.two:
                //ToastShort("2");
                schoolyear="20"+getBeginYear()+"-"+"20"+(getBeginYear()+1)+"-"+2;
                break;
            case R.id.three:
                //ToastShort("3");
                schoolyear="20"+(getBeginYear()+1)+"-"+"20"+(getBeginYear()+2)+"-"+1;
                break;
            case R.id.four:
                //ToastShort("4");
                schoolyear="20"+(getBeginYear()+1)+"-"+"20"+(getBeginYear()+2)+"-"+2;
                break;
            case R.id.five:
                //ToastShort("5");
                schoolyear="20"+(getBeginYear()+2)+"-"+"20"+(getBeginYear()+3)+"-"+1;
                break;
            case R.id.six:
                //ToastShort("6");
                schoolyear="20"+(getBeginYear()+2)+"-"+"20"+(getBeginYear()+3)+"-"+2;
                break;
            case R.id.seven:
               // ToastShort("7");
                schoolyear="20"+(getBeginYear()+3)+"-"+"20"+(getBeginYear()+4)+"-"+1;
                break;
            case R.id.eight:
                //ToastShort("8");
                schoolyear="20"+(getBeginYear()+3)+"-"+"20"+(getBeginYear()+4)+"-"+2;
                break;
            default:
                break;
        }
        editor.putString(App.SCHOOL_YEAR, schoolyear);
        editor.apply();
        return false;
    }

    @OnClick(R.id.iv_toolbar_menu)
    public void onViewClicked() {
        //ToastUtils.ToastShort("由于技术升级，爬虫不可用，此功能暂时下线");
        if (App.getEduid() != null && App.getEduPwd() != null) {
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
        } else {
            Intent intent = new Intent(mActivity, EduLoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void loadSchedule() {
        courceDetail.setAdapter(mPresenter.getAdapter(mActivity));
    }
}
