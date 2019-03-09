package cn.tengfeistudio.forum.module.user.sign;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.listener.MyTextWatcher;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.toast.MyToast;
import cn.tengfeistudio.forum.utils.NetConfig;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.utils.StringUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import okhttp3.Call;

public class SignActivity extends BaseActivity {
    @BindView(R.id.myToolBar)
    FrameLayout myToolBar;
    @BindView(R.id.et_sign_email)
    TextInputEditText etSignEmail;
    @BindView(R.id.et_sign_name)
    TextInputEditText etSignName;
    @BindView(R.id.et_sign_stuid)
    TextInputEditText etSignStuid;
    @BindView(R.id.et_sign_course)
    TextInputEditText etSignCourse;
    @BindView(R.id.et_sign_phone)
    TextInputEditText etSignPhone;
    @BindView(R.id.et_sign_pwd)
    TextInputEditText etSignPwd;
    @BindView(R.id.et_sign_repwd)
    TextInputEditText etSignRepwd;
    @BindView(R.id.iv_toolbar_menu)
    ImageView ivToolbarMenu;
    @BindView(R.id.til_sign_pwd)
    TextInputLayout tilSignPwd;
    @BindView(R.id.til_sign_repwd)
    TextInputLayout tilSignRepwd;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_sign;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        initToolBar(true, "用户注册");
        addToolbarMenu(R.drawable.ic_check_black_24dp).setOnClickListener(view -> submit());
        setSubmitBtn(false);
        addTextWatcher();
        initSlidr();
    }

    private void addTextWatcher() {
        etSignEmail.addTextChangedListener(new MyTextWatcher() {
            @Override
            public void afterMyTextChanged(Editable editable) {
                EmailCheck();
            }
        });
        MyTextWatcher textWatcher = new MyTextWatcher() {
            @Override
            public void afterMyTextChanged(Editable editable) {
                checkInput();
            }
        };
        etSignName.addTextChangedListener(textWatcher);
        etSignStuid.addTextChangedListener(textWatcher);
        etSignCourse.addTextChangedListener(textWatcher);
        etSignPhone.addTextChangedListener(textWatcher);
        etSignPwd.addTextChangedListener(textWatcher);
        etSignRepwd.addTextChangedListener(textWatcher);

    }

    private void setSubmitBtn(boolean setEnable) {
        if (setEnable) {
            ivToolbarMenu.setClickable(true);
            ivToolbarMenu.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_black_24dp));
        } else {
            ivToolbarMenu.setClickable(false);
            ivToolbarMenu.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_gray_24dp));
        }
    }

    private void checkInput() {
        String name = etSignName.getText().toString();
        String stuid = etSignStuid.getText().toString();
        String course = etSignCourse.getText().toString();
        String phone = etSignPhone.getText().toString();
        String newPwd = etSignPwd.getText().toString();
        String newPwd2 = etSignRepwd.getText().toString();
        if (TextUtils.isEmpty(name.trim())) {
            etSignName.setError("姓名不能为空");
            setSubmitBtn(false);
            return;
        }
        if (TextUtils.isEmpty(stuid.trim())) {
            etSignStuid.setError("学号不能为空");
            setSubmitBtn(false);
            return;
        }
        if (TextUtils.isEmpty(course.trim())) {
            etSignCourse.setError("班级不能为空");
            setSubmitBtn(false);
            return;
        }
        if (TextUtils.isEmpty(phone.trim())) {
            etSignPhone.setError("联系方式不能为空");
            setSubmitBtn(false);
            return;
        }
        if (TextUtils.isEmpty(newPwd.trim())) {
            tilSignPwd.setError("密码不能为空");
            setSubmitBtn(false);
            return;
        }

        if (newPwd.length() < 6) {
            tilSignPwd.setError("密码太短");
            setSubmitBtn(false);
        } else if (!StringUtils.checkSecurity(newPwd)) {
            tilSignPwd.setError("密码中必须包含数字、字母");
            setSubmitBtn(false);
        } else if (!Objects.equals(newPwd, newPwd2)) {
            tilSignPwd.setError(null);
            tilSignRepwd.setError("两次输入的密码不一致");
            setSubmitBtn(false);
        } else {
            tilSignRepwd.setError(null);
            setSubmitBtn(true);
        }
    }

    private void EmailCheck() {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(etSignEmail.getText().toString());
            flag = matcher.matches();
        } catch (Exception e) {
            etSignEmail.setError("请输入正确的邮箱地址");
        }
        if (!flag)
            etSignEmail.setError("请输入正确的邮箱地址");
    }

    /**
     * 提交注册申请
     */
    private void submit() {
        OkHttpUtils.post()
                //.url(NetConfig.BASE_REGISTER_PLUS)
                .url(NetConfig.BASE_USER_REGISTER)
                .addParams("email",etSignEmail.getText().toString().trim())
                .addParams("nickName",etSignName.getText().toString().trim())
                .addParams("studentCard",etSignStuid.getText().toString().trim())
                //.addParams("grades",etSignCourse.getText().toString().trim())
                .addParams("phone",etSignPhone.getText().toString().trim())
                .addParams("password",etSignPwd.getText().toString().trim())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        printLog("submit onError");
                        ToastShort("注册失败，邮箱已注册");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        JSONObject obj = JSON.parseObject(response);
                        if (!response.contains("code")){
                            ToastNetWorkError();
                        }else if (obj.getInteger("code") == Constants.RETURN_CONTINUE){
                            //Store.getInstance().setToken(obj.getJSONObject("data").getString("token"));
                            MyToast.showText(getApplicationContext(),"注册成功",true);
                            hideKeyBoard();
                            finishActivity();
                        }
                    }
                });
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
