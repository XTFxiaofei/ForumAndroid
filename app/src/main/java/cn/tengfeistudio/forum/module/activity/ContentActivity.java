package cn.tengfeistudio.forum.module.activity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


import butterknife.BindView;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.beans.ActivityBean;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.widget.CircleImageView;

public class ContentActivity extends BaseActivity {


    @BindView(R.id.ac_icon)
    CircleImageView acIcon;
    @BindView(R.id.ac_publisher)
    TextView acPublisher;
    @BindView(R.id.ac_place)
    TextView acPlace;
    @BindView(R.id.ac_time)
    TextView acTime;
    @BindView(R.id.ac_content)
    TextView acContent;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_content;
    }

    @Override
    protected void initData() {
        if (getIntent().getExtras().getBoolean("isNormalPost"))
            setIntentData(getIntent().getExtras().getString("ActivityJsonObject"));
        else
            getActivity();
    }

    @Override
    protected void initView() {
        initSlidr();

    }

    @SuppressLint("CheckResult")
    private void getActivity() {
        //活动id
        int id = getIntent().getIntExtra("activityId", 0);
        if (id == 0)
            return;
        RetrofitService.getActivity(id)
                .subscribe(responseBody -> {
                    String response = responseBody.string();
                    if (!response.contains("code")) {
                        ToastNetWorkError();
                        printLog("getPost onResponse !response.contains(\"code\")");
                        return;
                    }
                    JSONObject dataObj = JSON.parseObject(response);
                    if (dataObj.getInteger("code") != Constants.RETURN_CONTINUE) {
                        ToastShort("服务器出状况惹，稍等喔( • ̀ω•́ )✧");
                    } else {
                        setIntentData(dataObj.getString("data"));
                    }
                }, throwable -> {
                    ToastNetWorkError();
                    printLog("getPost onResponse !response.contains(\"code\")" + throwable.getMessage());
                });
    }


    /**
     * @param activityJsonObj
     */
    private void setIntentData(String activityJsonObj) {

        ActivityBean activityObj = JSON.parseObject(activityJsonObj, ActivityBean.class);
        int activityId = activityObj.getActivityId();
        int fromUserId = activityObj.getUserId();
        String target = activityObj.getTarget();

        acIcon.setImageURI(Uri.parse(activityObj.getLogoImage()));
        acPublisher.setText(activityObj.getSponsor());
        acPlace.setText(activityObj.getPlace());
        acTime.setText(activityObj.getActivityTime());
        acContent.setText(activityObj.getContent());

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
