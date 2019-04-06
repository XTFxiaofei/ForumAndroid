package cn.tengfeistudio.forum.module.setting.about;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.R;
import com.zzhoujay.richtext.RichText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AboutActivity extends BaseActivity {
    @BindView(R.id.btn_back)
    ImageView btnBack;
    @BindView(R.id.version)
    TextView version;
    @BindView(R.id.server_version)
    TextView serverVersion;
    @BindView(R.id.tv_about)
    TextView tvAbout;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_about;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        initSlidr();
        String text = "###开发组介绍：  \n" +
                "Android开发：[@xiaofei](https://github.com/XTFxiaofei)  \n" +
                "Web开发&后台开发：源码暂不开放  \n" +
                "###Bug反馈  \n" +
                "功能不断完善中，还请多多反馈......  \n" +
                "1.加入QQ交流：2323533690  \n" +
                "2.Github提交 [点击这儿](https://github.com/XTFxiaofei/ForumAndroid)  \n" +
                "3.在 **论坛反馈** 模块进行反馈" ;
        RichText.fromMarkdown(text).into(tvAbout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.btn_back, R.id.server_version})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finishActivity();
                break;
            case R.id.server_version:
                // 检测更新
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
