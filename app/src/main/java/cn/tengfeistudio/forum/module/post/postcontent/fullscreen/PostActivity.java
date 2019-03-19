package cn.tengfeistudio.forum.module.post.postcontent.fullscreen;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ScrollView;
import android.widget.Toast;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.module.post.postcontent.main.PostFragment;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.utils.Constants;

import butterknife.BindView;
import cn.tengfeistudio.forum.utils.toast.ToastUtils;


public class PostActivity extends BaseActivity {
    @BindView(R.id.fragment)
    ScrollView fragment;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_post;
    }

    @Override
    protected void initData() {
        if (getIntent().getExtras().getBoolean("isNormalPost"))
            setIntentData(getIntent().getExtras().getString("PostJsonObject"));
        else
            getPost();
    }

    @Override
    protected void initView() {
        initSlidr();

    }

    @SuppressLint("CheckResult")
    private void getPost() {
        //long id = getIntent().getLongExtra("id",0);
        //帖子id
        int id = getIntent().getIntExtra("topicId", 0);
        if (id == 0)
            return;
        RetrofitService.getPost(id)
                .subscribe(responseBody -> {
                    String response = responseBody.string();
                    if (!response.contains("code")) {
                        Looper.prepare();
                        ToastNetWorkError();
                        Looper.loop();// 进入loop中的循环，查看消息队列
                        printLog("getPost onResponse !response.contains(\"code\")");
                        return;
                    }
                    JSONObject dataObj = JSON.parseObject(response);
                    if (dataObj.getInteger("code") != Constants.RETURN_CONTINUE) {
                        ToastShort("服务器出状况惹，稍等喔( • ̀ω•́ )✧");
                    } else if(dataObj.getString("data")!=null){
                        setIntentData(dataObj.getString("data"));
                    }else{
                        Looper.prepare();
                        ToastShort("帖子已删了( • ̀ω•́ )✧");
                        Looper.loop();// 进入loop中的循环，查看消息队列
                    }
                }, throwable -> {
                    ToastNetWorkError();
                    printLog("getPost onResponse !response.contains(\"code\")" + throwable.getMessage());
                });
    }


    /**
     * @param postJsonObj
     */
    private void setIntentData(String postJsonObj) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        PostFragment postFragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putString("PostJsonObject", postJsonObj);
        bundle.putString("from", "PostActivity");
        postFragment.setArguments(bundle);
        transaction.add(R.id.fragment, postFragment);
        transaction.commit();
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
