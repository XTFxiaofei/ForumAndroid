package cn.tengfeistudio.forum.module.user.userdetail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import butterknife.BindView;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.beans.Course;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.utils.Constants;

public class UserDetailSetActivity extends BaseActivity {
    @BindView(R.id.myToolBar)
    FrameLayout myToolBar;
    @BindView(R.id.et_setinfo2)
    EditText etSetinfo;
    public static final int requestCode = 256;
    private String infoType="";


    @Override
    protected int getLayoutID() {
        return R.layout.activity_user_detail_set;
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        String content = intent.getStringExtra("content");
        infoType=intent.getStringExtra("set");
        switch (infoType){
            case "email":
                initToolBar(true,"修改邮箱");
                etSetinfo.setText(content);
                break;
            case "account":
                initToolBar(true,"修改账号");
                etSetinfo.setText(content);
                break;
            case "nickname":  //可以修改
                initToolBar(true,"修改昵称");
                etSetinfo.setText(content);
                break;
            case "level":
                initToolBar(true,"修改等级");
                etSetinfo.setText(content);
                break;
            case "studentCard": //可以修改
                initToolBar(true,"修改学号");
                etSetinfo.setText(content);
                break;
            case "phone":  //可以修改
                initToolBar(true,"修改手机号");
                etSetinfo.setText(content);
                break;
            case "role":
                initToolBar(true,"修改身份");
                etSetinfo.setText(content);
                break;
        }
    }
    // 判断一个字符串是否都为数字
    private boolean isDigit(String strNum) {
        return strNum.matches("[0-9]{1,}");
    }
    @Override
    protected void initView() {
        initSlidr();
        addToolbarMenu(R.drawable.ic_check_black_24dp).setOnClickListener(view -> {
            String info=etSetinfo.getText().toString();
            if(infoType.equals("phone") && !isDigit(info)){
                ToastShort("号码非法,只能输入数字");
                return;
            }else{
                RetrofitService.modifyUserInfo(infoType,info)
                        .subscribe(responseBody -> {
                            JSONObject jsonObject = JSON.parseObject(responseBody.string());
                            if (jsonObject.getInteger("code") != Constants.RETURN_CONTINUE){
                                ToastShort("服务器出状况惹，再试试( • ̀ω•́ )✧");
                            }else{
                                Intent intent = new Intent(this, UserDetailActivity.class);
                                intent.putExtra("userid", App.getUid());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                this.startActivity(intent);

                            }
                        });
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
