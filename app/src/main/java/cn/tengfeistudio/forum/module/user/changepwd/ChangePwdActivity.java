package cn.tengfeistudio.forum.module.user.changepwd;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.toast.MyToast;
import cn.tengfeistudio.forum.utils.NetConfig;
import cn.tengfeistudio.forum.listener.MyTextWatcher;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.utils.StringUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Objects;

import butterknife.BindView;
import okhttp3.Call;

public class ChangePwdActivity extends BaseActivity {
    @BindView(R.id.myToolBar)
    FrameLayout myToolBar;
    @BindView(R.id.old_pass)
    EditText oldPass;
    @BindView(R.id.new_pass)
    EditText newPass;
    @BindView(R.id.new_pass2)
    EditText newPass2;
    @BindView(R.id.old_pass_c)
    TextInputLayout oldPassC;
    @BindView(R.id.new_pass_c)
    TextInputLayout newPassC;
    @BindView(R.id.new_pass_c2)
    TextInputLayout newPassC2;
    @BindView(R.id.iv_toolbar_menu)
    ImageView ivToolbarMenu;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_changepwd;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        initToolBar(true, "修改密码");
        addToolbarMenu(R.drawable.ic_check_black_24dp).setOnClickListener(view -> submit());
        initSlidr();
        initEditText();
    }

    private void initEditText() {
        MyTextWatcher myTextWatcher = new MyTextWatcher() {
            @Override
            public void afterMyTextChanged(Editable editable) {
                checkInput();
            }
        };
        newPass.addTextChangedListener(myTextWatcher);
        newPass2.addTextChangedListener(myTextWatcher);
    }

    /**
     * 提交操作
     */
    private synchronized void submit() {
        OkHttpUtils.post()
                .url(NetConfig.BASE_RESET_PASSWORD)
                .addHeader(Constants.AUTHORIZATION, Store.getInstance().getToken())
                .addParams("oldPassword", oldPass.getText().toString())
                .addParams("newPassword", newPass2.getText().toString())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastNetWorkError();
                        printLog(e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (!response.contains("code")) {
                            ToastShort("请检查网络设置ヽ(#`Д´)ﾉ");
                            return;
                        }
                        JSONObject jsonObject = JSON.parseObject(response);
                        printLog(jsonObject.getInteger("code") + "");
                        if (jsonObject.getInteger("code") == Constants.TOKEN_OVERDUE) {
                            getNewToken();
                        } else if (jsonObject.getInteger("code") == 50002) {
                            ToastShort("密码输错了，检查一下吧");
                        } else {
                            MyToast.showText(getApplicationContext(), jsonObject.getString("message"), true);
                            finishActivity();
                        }
                    }
                });

    }

    /**
     * 获取新的Token
     */
    @SuppressLint("CheckResult")
    private void getNewToken() {
        RetrofitService.getNewToken()
                .subscribe(s -> submit());
    }

    private void checkInput() {
        ivToolbarMenu.setClickable(false);
        String newPwd = newPass.getText().toString();
        String newPwd2 = newPass2.getText().toString();
        if (TextUtils.isEmpty(newPwd)) {
            newPassC2.setError("新密码不能为空");
            return;
        }

        if (newPwd.length() < 6) {
            newPassC.setError("密码太短");
        } else if (!StringUtils.checkSecurity(newPwd)) {
            newPassC.setError("密码中必须包含数字、字母");
        } else if (!Objects.equals(newPwd, newPwd2)) {
            newPassC.setError(null);
            newPassC2.setError("两次输入的密码不一致");
        } else {
            newPassC2.setError(null);
            ivToolbarMenu.setClickable(true);
        }
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
