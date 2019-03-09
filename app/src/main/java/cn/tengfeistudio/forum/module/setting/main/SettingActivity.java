package cn.tengfeistudio.forum.module.setting.main;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.FrameLayout;

import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.module.setting.fullscreen.SettingFragment;
import cn.tengfeistudio.forum.R;

import butterknife.BindView;

public class SettingActivity extends BaseActivity {
    @BindView(R.id.myToolBar)
    FrameLayout myToolBar;
    @BindView(R.id.container)
    FrameLayout container;

    public static final int requestCode = 256;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        initSlidr();
        initToolBar(true, "设置");
        Fragment to = new SettingFragment();
        getFragmentManager().beginTransaction().replace(R.id.container, to).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void afterLogout(){
        setResult(RESULT_OK);
        finishActivity();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
