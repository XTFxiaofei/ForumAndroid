package cn.tengfeistudio.forum.module.schedule.edu.set;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSON;
import cn.tengfeistudio.forum.api.beans.Course;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ScheduleDetailSetActivity extends BaseActivity {
    @BindView(R.id.myToolBar)
    FrameLayout myToolBar;
    @BindView(R.id.et_setinfo)
    EditText etSetinfo;

    private Course object;

    public static final int requestCode = 256;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_schedule_detail_set;
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        String JsonObj = intent.getStringExtra("JsonObj");
        object = JSON.parseObject(JsonObj,Course.class);
        switch (intent.getStringExtra("set")){
            case "courseName":
                initToolBar(true,"修改课名");
                etSetinfo.setText(object.getCourseName());
                break;
            case "class":
                initToolBar(true,"修改教室");
                etSetinfo.setText(object.getPlace());
                break;
            case "weeks":
                initToolBar(true,"修改周数");
                etSetinfo.setText(object.getWeekNumber());

                break;
            case "sumSection":
                initToolBar(true,"修改连上节数");
                etSetinfo.setText(String.valueOf(object.getSumSection()));
                break;
            case "js":
                initToolBar(true,"修改节数");
                etSetinfo.setText(object.getSectionNumber().toString());
                break;
            case "teacher":
                initToolBar(true,"修改老师");
                etSetinfo.setText(object.getTeacher());
                break;
        }
    }

    @Override
    protected void initView() {
        initSlidr();
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
