package cn.tengfeistudio.forum.module.post.postcontent.fullscreen;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ScrollView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.module.post.postcontent.main.PostFragment;
import cn.tengfeistudio.forum.utils.Constants;

import butterknife.BindView;

public class ReplyActivity extends BaseActivity {
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
        //因为现在传过来的是评论id,所以要根据评论Id查找帖子id
        int id=getIntent().getIntExtra("commentId",0);
        if (id == 0)
            return;
        RetrofitService.getTopicByCommentId(id)
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
