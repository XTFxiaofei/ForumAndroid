package cn.tengfeistudio.forum.module.user.userdetail;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.tengfeistudio.forum.adapter.FullyGridLayoutManager;
import cn.tengfeistudio.forum.adapter.GridImageAdapter;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.module.mine.MineFragment;
import cn.tengfeistudio.forum.module.post.edit.EditAcitivity;
import cn.tengfeistudio.forum.module.schedule.edu.set.ScheduleDetailSetActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.StampToDate;
import cn.tengfeistudio.forum.utils.UploadUtil;
import cn.tengfeistudio.forum.utils.toast.MyToast;
import cn.tengfeistudio.forum.utils.NetConfig;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.widget.CircleImageView;
import cn.tengfeistudio.forum.widget.GradeProgressView;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.compress.Luban;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.squareup.picasso.Picasso;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

import static com.luck.picture.lib.config.PictureConfig.LUBAN_COMPRESS_MODE;

public class UserDetailActivity extends BaseActivity implements AdapterView.OnItemClickListener{
    @BindView(R.id.user_detail_img_avatar)
    CircleImageView userDetailImgAvatar;
    @BindView(R.id.grade_progress)
    GradeProgressView gradeProgress;
    @BindView(R.id.progress_text)
    TextView progressText;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.main_window)
    CoordinatorLayout mainWindow;
    @BindView(R.id.btn_logout)
    Button btnLogout;
    @BindView(R.id.listView)
    ListView listView;

    private final List<String>keys = new ArrayList<>();
    private final List<String>values = new ArrayList<>();
    public static final int requestCode = 128;
    private int userid;
    private String username = null;
    private String imageUrl = null;
    // 对象是否为登陆用户
    private boolean isLoginUser = false;
    //选择图片
    private PopupWindow pop;
    private List<LocalMedia> selectList = new ArrayList<>();
    private ArrayList<String> imagesPath = new ArrayList<>();

    @Override
    protected int getLayoutID() {
        return R.layout.activity_user_detail;
    }

    @Override
    protected void initData() {
        getUserInfo();
    }

    @Override
    protected void initView() {
        initSlidr();
        toolbarLayout.setTitle(username);
    }

    private void getUserInfo() {
        Intent intent = getIntent();
        userid = intent.getExtras().getInt("userid");
        if (TextUtils.equals(String.valueOf(userid), String.valueOf(App.getUid())))
            isLoginUser = true;
        getInfo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.btn_logout)
    public void onViewClicked() {
        onLogout();
    }

    /**
     * 点击头像
     * @param view
     */
    @OnClick(R.id.user_detail_img_avatar)
    public void onClick(View view){
        if(userid==App.getUid()){
            showPop();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<LocalMedia> images;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    images = PictureSelector.obtainMultipleResult(data);
                    selectList.addAll(images);
                    for (LocalMedia i : images) {
                        if (i.isCompressed() && i != null){
                            imagesPath.clear();
                            imagesPath.add(i.getCompressPath());
                        }
                        else if(i!=null) {
                            imagesPath.clear();
                            //获取原本路径
                            imagesPath.add(i.getPath());
                        }
                    }

//                    selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                   userDetailImgAvatar.setImageURI(Uri.fromFile(new File(imagesPath.get(0))));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            UploadUtil.uploadIcon(Store.getInstance().getToken(),imagesPath,NetConfig.BASE_USER_MODIFY_ICON);
                        }
                    }).start();
                    break;
            }
        }
    }

    /**
     * 底部弹出菜单选择拍照
     */
    private void showPop() {
        View bottomView = View.inflate(UserDetailActivity.this, R.layout.layout_bottom_dialog, null);
        TextView mAlbum = (TextView) bottomView.findViewById(R.id.tv_album);
        TextView mCamera = (TextView) bottomView.findViewById(R.id.tv_camera);
        TextView mCancel = (TextView) bottomView.findViewById(R.id.tv_cancel);

        pop = new PopupWindow(bottomView, -1, -2);
        //设置背景透明
        pop.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        pop.setOutsideTouchable(true);
        pop.setFocusable(true);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        pop.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        pop.setAnimationStyle(R.style.main_menu_photo_anim);
        pop.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_album:
                        //相册
                        PictureSelector.create(UserDetailActivity.this)
                                .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                                .maxSelectNum(1)// 最大图片选择数量
                                .minSelectNum(1)// 最小选择数量
                                .imageSpanCount(4)// 每行显示个数
                                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选PictureConfig.MULTIPLE : PictureConfig.SINGLE
                                .previewImage(true)// 是否可预览图片
                                .compressGrade(Luban.CUSTOM_GEAR)// luban压缩档次，默认3档 Luban.FIRST_GEAR、Luban.CUSTOM_GEAR,Luban.THIRD_GEAR
                                .isCamera(false)// 是否显示拍照按钮
                                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                                //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
                                .enableCrop(true)// 是否裁剪
                                .compress(true)// 是否压缩
                                .compressMode(LUBAN_COMPRESS_MODE)//系统自带 or 鲁班压缩 PictureConfig.SYSTEM_COMPRESS_MODE or LUBAN_COMPRESS_MODE
                                //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                                .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                                .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                                .selectionMedia(selectList)// 是否传入已选图片
                                //.previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                                //.cropCompressQuality(90)// 裁剪压缩质量 默认100
                                .compressMaxKB(5000)//压缩最大值kb compressGrade()为Luban.CUSTOM_GEAR有效
                                //.compressWH() // 压缩宽高比 compressGrade()为Luban.CUSTOM_GEAR有效
                                //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                                .rotateEnabled(true) // 裁剪是否可旋转图片
                                .scaleEnabled(true)// 裁剪是否可放大缩小图片
                                //.recordVideoSecond()//录制视频秒数 默认60s
                                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
                        break;
                    case R.id.tv_camera:
                        //拍照
                        PictureSelector.create(UserDetailActivity.this)
                                .openCamera(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                                .maxSelectNum(1)// 最大图片选择数量
                                .minSelectNum(1)// 最小选择数量
                                .imageSpanCount(1)// 每行显示个数
                                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选PictureConfig.MULTIPLE : PictureConfig.SINGLE
                                .previewImage(true)// 是否可预览图片
                                .compressGrade(Luban.CUSTOM_GEAR)// luban压缩档次，默认3档 Luban.FIRST_GEAR、Luban.CUSTOM_GEAR,Luban.THIRD_GEAR
                                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                                //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
                                .enableCrop(true)// 是否裁剪
                                .compress(true)// 是否压缩
                                .compressMode(LUBAN_COMPRESS_MODE)//系统自带 or 鲁班压缩 PictureConfig.SYSTEM_COMPRESS_MODE or LUBAN_COMPRESS_MODE
                                //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                                .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                                .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                                .selectionMedia(selectList)// 是否传入已选图片
                                //.previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                                //.cropCompressQuality(90)// 裁剪压缩质量 默认100
                                .compressMaxKB(5000)//压缩最大值kb compressGrade()为Luban.CUSTOM_GEAR有效
                                //.compressWH() // 压缩宽高比 compressGrade()为Luban.CUSTOM_GEAR有效
                                //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                                .rotateEnabled(true) // 裁剪是否可旋转图片
                                .scaleEnabled(true)// 裁剪是否可放大缩小图片
                                //.recordVideoSecond()//录制视频秒数 默认60s
                                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
                        break;
                    case R.id.tv_cancel:
                        //取消
                        //closePopupWindow();
                        break;
                }
                closePopupWindow();
            }
        };

        mAlbum.setOnClickListener(clickListener);
        mCamera.setOnClickListener(clickListener);
        mCancel.setOnClickListener(clickListener);
    }

    public void closePopupWindow() {
        if (pop != null && pop.isShowing()) {
            pop.dismiss();
            pop = null;
        }
    }
    /**
     * 返回MineFragment
     */
    @Override
    public void onBackPressed(){
        Intent intent = new Intent("android.intent.action.CART_BROADCAST");
        intent.putExtra("data","refresh");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        sendBroadcast(intent);
        super.onBackPressed();
    }



    /**
     * 点击退出登录按钮后
     */
    private void onLogout() {
        App.setIsLogout();
        setResult(RESULT_OK);
        MyToast.showText(getApplicationContext(), "退出登录成功", Toast.LENGTH_SHORT, true);
        finishActivity();
    }

    public void getInfo() {
        if (isLoginUser) {
            // 获取用户个人信息
            OkHttpUtils.get()
                    //.url(NetConfig.BASE_USERDETAIL_PLUS)
                    //.addHeader("Authorization", "Bearer " + Store.getInstance().getToken())
                    .url(NetConfig.BASE_GET_USER_DETAILS)
                    .addHeader(Constants.AUTHORIZATION,Store.getInstance().getToken())
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            printLog("UserDetailActivity getInfo getUserDetail onError" + e.getMessage());
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            if (!response.contains("code")) {
                                ToastNetWorkError();
                                return;
                            }
                            JSONObject jsonObject = JSON.parseObject(response);
                            //如果code==50011表示
                            if (jsonObject.getInteger("code") == Constants.TOKEN_OVERDUE){
                                getNewToken();
                            }else if (jsonObject.getInteger("code") != Constants.RETURN_CONTINUE) {
                                ToastShort("查无此用户信息");
                            } else {
                                Message msg = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putString("resultObject", jsonObject.getString("data"));
                                msg.setData(bundle);
                                msg.what = GET_INFO;
                                handler.sendMessage(msg);
                            }
                        }
                    });
        } else {

            // 获取他人信息
            OkHttpUtils.get()
                    .url(NetConfig.BASE_GET_OTHER_DETAILS + userid)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            printLog("UserDetailActivity getInfo getUserDetail2 onError " + e.getMessage());
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            if (!response.contains("code")) {
                                ToastNetWorkError();
                                return;
                            }
                            JSONObject jsonObject = JSON.parseObject(response);
                            if (jsonObject.getInteger("code") != Constants.RETURN_CONTINUE) {
                                ToastShort("查无此用户信息");
                            } else {
                                Message msg = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putString("resultObject", jsonObject.getString("data"));
                                msg.setData(bundle);
                                msg.what = GET_INFO;
                                handler.sendMessage(msg);
                            }

                        }
                    });
        }
    }

    /**
     * 获取新的Token
     */
    private void getNewToken() {
        OkHttpUtils.post()
                .url(NetConfig.BASE_REFRESH_TOKEN)
                .addHeader(Constants.AUTHORIZATION,Store.getInstance().getToken())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        printLog("HomeFragment getNewToken onError");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        JSONObject obj = JSON.parseObject(response);
                        if (obj.getInteger("code") != Constants.RETURN_CONTINUE){
                            printLog("HomeFragment getNewToken() onResponse获取Token失败,重新登陆");
                        }else{
                            //Store.getInstance().setToken(obj.getString("result"));
                            Store.getInstance().setToken(obj.getJSONObject("data").getString("token"));
                            getInfo();
                        }
                    }
                });
    }

    private static final int GET_INFO = 2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_INFO:
                    setInfo(msg.getData().getString("resultObject"));
                    break;
            }
        }
    };

    private void setInfo(String result) {
        JSONObject obj = JSON.parseObject(result);
        Picasso.get()
                .load(obj.getString("icon"))
                .placeholder(R.drawable.image_placeholder)
                .into(userDetailImgAvatar);
        toolbarLayout.setTitle(obj.getString("nickname"));
        gradeProgress.setProgress(Float.parseFloat(obj.getString("level"))/5);
        progressText.setText(obj.getString("level")+"/5");


        //如果是本人则显示全部
        if (isLoginUser) {
            keys.add("邮箱");
            values.add(obj.getString("email"));
            keys.add("账号");
            values.add(obj.getString("account").trim().isEmpty() ? "null" : obj.getString("account"));
            keys.add("昵称(可修改)");
            values.add(obj.getString("nickname").trim().isEmpty() ? "null" : obj.getString("nickname"));
            keys.add("等级");
            values.add(obj.getString("level").trim().isEmpty()?"0":obj.getString("level"));
            keys.add("学号(可修改)");
            values.add(obj.getString("studentCard").trim().isEmpty() ? "null" : obj.getString("studentCard"));
            keys.add("手机号(可修改)");
            values.add(obj.getString("phone").trim().isEmpty() ? "null" : obj.getString("phone"));
            if (obj.containsKey("role")){
                keys.add("身份");
                values.add(obj.getString("role").equals("user") ? "普通用户" : obj.getString("role"));
            }
            keys.add("注册时间");
            values.add(obj.getString("createTime").trim().isEmpty() ?"null":StampToDate.stampToDate(obj.getString("createTime")));
        }else{
            //不是本人
            keys.add("邮箱");
            values.add(obj.getString("email"));
            keys.add("账号");
            values.add(obj.getString("account").trim().isEmpty() ? "null" : obj.getString("account"));
            keys.add("昵称");
            values.add(obj.getString("nickname").trim().isEmpty() ? "null" : obj.getString("nickname"));
            keys.add("等级");
            values.add(obj.getString("level").trim().isEmpty()?"0":obj.getString("level"));
            keys.add("学号");
            values.add(obj.getString("studentCard").trim().isEmpty() ? "null" : obj.getString("studentCard"));
            keys.add("手机号");
            values.add(obj.getString("phone").trim().isEmpty() ? "null" : obj.getString("phone"));

        }


        List<Map<String, String>> list = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            Map<String, String> ob = new HashMap<>();
            ob.put("key", keys.get(i));
            ob.put("value", values.get(i));
            list.add(ob);
        }
        listView.setAdapter(new SimpleAdapter(this, list, R.layout.item_sim_list, new String[]{"key", "value"}, new int[]{R.id.key, R.id.value}));
        listView.setOnItemClickListener(this::onItemClick);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //通过view获取其内部的组件，进而进行操作
         String text = (String) ((TextView)view.findViewById(R.id.value)).getText();
        //大多数情况下，position和id相同，并且都从0开始
//        String showText = "点击第" + position + "项，文本内容为：" + text + "，ID为：" + id;
//        Toast.makeText(this, showText, Toast.LENGTH_LONG).show();
        if(!isLoginUser || position==0 || position==1 ||position==3 || position==6 ||position==7){
            return;
        }
        Intent intent = new Intent(this,UserDetailSetActivity.class);
        intent.putExtra("content",text);
        switch (position) {
            case 0: //点击第1个
                intent.putExtra("set","email");
                break;
            case 1://点击第2个
                intent.putExtra("set","account");
                break;
            case 2://点击第3个，可以修改
                intent.putExtra("set","nickname");
                break;
            case 3://点击第4个
               intent.putExtra("set","level");
                break;
            case 4://点击第5个，可以修改
                intent.putExtra("set","studentCard");
                break;
            case 5://点击第6个，可以修改
                intent.putExtra("set","phone");
                break;
            case 6://点击第7个
                intent.putExtra("set","role");
                break;
        }
        startActivityForResult(intent, UserDetailSetActivity.requestCode);
    }

}
