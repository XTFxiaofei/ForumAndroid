package cn.tengfeistudio.forum.module.post.edit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.compress.Luban;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;


import org.angmarch.views.NiceSpinner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.adapter.FullyGridLayoutManager;
import cn.tengfeistudio.forum.adapter.GridImageAdapter;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.NetConfig;
import cn.tengfeistudio.forum.utils.UploadUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.luck.picture.lib.config.PictureConfig.LUBAN_COMPRESS_MODE;

public class EditAcitivity extends BaseActivity {
    @BindView(R.id.editor)
    EditText editor;
    @BindView(R.id.et_post_title)
    EditText etPostTitle;
    @BindView(R.id.iv_toolbar_back)
    ImageView ivToolbarBack;
    @BindView(R.id.spinner)
    NiceSpinner spinner;
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;


    private ArrayList<String> imagesPath = new ArrayList<>();
    private int maxSelectNum = 9;
    private List<LocalMedia> selectList = new ArrayList<>();
    private GridImageAdapter adapter;
    private PopupWindow pop;
    private String[] categories = new String[]{
            "daily", "code", "qa", "suggests", "feedback", "transaction", "activity",
    };
    String currentCategory = categories[0];

    public static final int requestCode = 110;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_edit;
    }

    @Override
    protected void initData() {
        initSpinner();
        initWidget();
    }

    private void initSpinner() {
        List<String> dataset = new LinkedList<>(Arrays.asList(getResources().getStringArray(R.array.post_categories)));
        spinner.attachDataSource(dataset);
        spinner.setBackgroundColor(getResources().getColor(R.color.bg_secondary));
        spinner.setTextColor(getResources().getColor(R.color.text_color_sec));
        spinner.setArrowDrawable(getResources().getDrawable(R.drawable.ic_expand_more_black_24dp));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                currentCategory = categories[pos];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Intent intent = getIntent();
        String temp = intent.getExtras().getString("category");
        if (temp.equals("E-M-P-T-Y"))
            return;
        for (int pos = 0; pos < categories.length; pos++)
            if (temp.equals(categories[pos])) {
                currentCategory = temp;
                spinner.setSelectedIndex(pos);
                break;
            }
    }

    private void initWidget() {
        FullyGridLayoutManager manager = new FullyGridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        adapter = new GridImageAdapter(this, onAddPicClickListener);
        adapter.setList(selectList);
        adapter.setSelectMax(maxSelectNum);
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (selectList.size() > 0) {
                    LocalMedia media = selectList.get(position);
                    String pictureType = media.getPictureType();
                    int mediaType = PictureMimeType.pictureToVideo(pictureType);
                    switch (mediaType) {
                        case 1:
                            // 预览图片 可自定长按保存路径
                            //PictureSelector.create(MainActivity.this).externalPicturePreview(position, "/custom_file", selectList);
                            PictureSelector.create(EditAcitivity.this).externalPicturePreview(position, selectList);
                            break;
                        case 2:
                            // 预览视频
                            PictureSelector.create(EditAcitivity.this).externalPictureVideo(media.getPath());
                            break;
                        case 3:
                            // 预览音频
                            PictureSelector.create(EditAcitivity.this).externalPictureAudio(media.getPath());
                            break;
                    }
                }
            }
        });
    }


    @SuppressLint("CheckResult")
    @Override
    protected void initView() {
        initToolBar(true, "发帖子");
        ivToolbarBack.setOnClickListener(view -> {
            if (!etPostTitle.getText().toString().isEmpty() || !editor.getText().toString().isEmpty())
                new AlertDialog.Builder(EditAcitivity.this)
                        .setMessage("程序猿还没开发出保存的功能喔，确定返回吗|ω・）")
                        .setCancelable(true)
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            setResult(RESULT_OK);
                            finishActivity();
                        })
                        .setNegativeButton("取消", (dialogInterface, i) -> {
                        })
                        .create()
                        .show();
            else {
                setResult(RESULT_OK);
                finishActivity();
            }
        });
        addToolbarMenu(R.drawable.ic_check_black_24dp).setOnClickListener(view -> {
            if (checkInput())
                sendTopic(etPostTitle.getText().toString(), editor.getText().toString());
        });
        initEditor();
        initTextWatcher();
        Observable.timer(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> showSoftInput());
    }

    private void initTextWatcher() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
//                if (TextUtils.isEmpty(etPostTitle.getText().toString())) {
//                    etPostTitle.setError("标题不能为空");
//                }
            }
        };
        editor.addTextChangedListener(watcher);
    }

    /**
     * 检查输入是否正确
     */
    private boolean checkInput() {
//        if (TextUtils.isEmpty(etPostTitle.getText().toString())) {
//            etPostTitle.setError("标题不能为空");
//            return false;
//        }
//        if (TextUtils.isEmpty(editor.getText().toString())) {
//            ToastShort("帖子内容不能为空");
//            return false;
//        }
        if(TextUtils.isEmpty(etPostTitle.getText().toString()) && TextUtils.isEmpty(editor.getText().toString()) &&imagesPath.size()<=0 ){
            ToastShort("最少要发图片哦 |ω・）");
            return false;
        }
        return true;
    }

    /**
     * 发送帖子
     */
    @SuppressLint("CheckResult")
    private void sendTopic(String title, String content) {
        if (currentCategory.equals(categories[6]) && !App.getRole().equals("admin")) {
            ToastShort("不好意思,管理员才能发公告!");
            return;
        }
        //上传图片时
        if (imagesPath.size() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UploadUtil.uploadImage(Store.getInstance().getToken(), title, content, currentCategory, imagesPath, NetConfig.BASE_TOPIC_INCLUDE_IMAGES);
//                  for(String str:imagesPath){
//                      try {
//                     String s=UploadUtil.picCOS(new File(str)).toString();
//                     printLog(s);
//                      } catch (Exception e) {
//                          e.printStackTrace();
//                      }
//                  }
                }
            }).start();
            setResult(RESULT_OK);
            finishActivity();
            ToastShort("发布成功");
        } else {
            //没有上传图片
            RetrofitService.sendTopic(title, content, currentCategory)
                    .subscribe(responseBody -> {
                        String response = responseBody.string();
                        if (!response.contains("code")) {
                            ToastNetWorkError();
                            return;
                        }
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (jsonObject.getInteger("code") == Constants.TOKEN_OVERDUE) {
                            getNewToken(title, content);
                        } else if (jsonObject.getInteger("code") != Constants.RETURN_CONTINUE) {
                            ToastShort("服务器出状况惹，再试试( • ̀ω•́ )✧");
                            printLog("getInfoError" + response);
                        } else {
                            setResult(RESULT_OK);
                            ToastShort("发布成功");
                            finishActivity();
                        }
                    }, throwable -> {
                        printLog("EditActivity doPost onError:" + throwable.getMessage());
                        ToastNetWorkError();
                    });
        }


    }

    /**
     * 获取新的Token
     */
    @SuppressLint("CheckResult")
    private void getNewToken(String title, String content) {
        RetrofitService.getNewToken()
                .subscribe(s -> sendTopic(title, content));
    }

    /**
     * 初始化输入框
     */
    private void initEditor() {
        editor.setSelection(editor.getText().length());
        //设置EditText的显示方式为多行文本输入
        editor.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //文本显示的位置在EditText的最上方
        editor.setGravity(Gravity.TOP);
        //改变默认的单行模式
        editor.setSingleLine(false);
        //水平滚动设置为False
        editor.setHorizontallyScrolling(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (etPostTitle.getText().toString().isEmpty() && editor.getText().toString().isEmpty()) {
            setResult(RESULT_OK);
            super.onBackPressed();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("程序猿还没开发保存的功能喔，确定返回吗|ω・）")
                    .setCancelable(true)
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        setResult(RESULT_OK);
                        finishActivity();
                    })
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                    })
                    .create()
                    .show();
        }
    }


    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {

        @Override
        public void onAddPicClick() {

            //第一种方式，弹出选择和拍照的dialog
            //showPop();

            //第二种方式，直接进入相册，但是 是有拍照得按钮的
            //参数很多，根据需要添加

            PictureSelector.create(EditAcitivity.this)
                    .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                    .maxSelectNum(maxSelectNum)// 最大图片选择数量
                    .minSelectNum(1)// 最小选择数量
                    .imageSpanCount(4)// 每行显示个数
                    .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选PictureConfig.MULTIPLE : PictureConfig.SINGLE
                    .previewImage(true)// 是否可预览图片
                    .compressGrade(Luban.CUSTOM_GEAR)// luban压缩档次，默认3档 Luban.FIRST_GEAR、Luban.CUSTOM_GEAR,Luban.THIRD_GEAR
                    .isCamera(true)// 是否显示拍照按钮
                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                    //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
                    //.enableCrop(true)// 是否裁剪
                    .compress(true)// 是否压缩
                    .compressMode(LUBAN_COMPRESS_MODE)//系统自带 or 鲁班压缩 PictureConfig.SYSTEM_COMPRESS_MODE or LUBAN_COMPRESS_MODE
                    //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                    .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                    .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .selectionMedia(selectList)// 是否传入已选图片
                    //.previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                    .cropCompressQuality(80)// 裁剪压缩质量 默认100
                    .compressMaxKB(5000)//压缩最大值kb compressGrade()为Luban.CUSTOM_GEAR有效
                    //.compressWH() // 压缩宽高比 compressGrade()为Luban.CUSTOM_GEAR有效
                    //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                    .rotateEnabled(false) // 裁剪是否可旋转图片
                    //.scaleEnabled()// 裁剪是否可放大缩小图片
                    //.recordVideoSecond()//录制视频秒数 默认60s
                    .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
        }
    };


    /**
     * 底部弹出菜单选择拍照
     */
    private void showPop() {
        View bottomView = View.inflate(EditAcitivity.this, R.layout.layout_bottom_dialog, null);
        TextView mAlbum = (TextView) bottomView.findViewById(R.id.tv_album);
        TextView mCamera = (TextView) bottomView.findViewById(R.id.tv_camera);
        TextView mCancel = (TextView) bottomView.findViewById(R.id.tv_cancel);

        pop = new PopupWindow(bottomView, -1, -2);
        //设置背景透明
        pop.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
                        PictureSelector.create(EditAcitivity.this)
                                .openGallery(PictureMimeType.ofImage())
                                .maxSelectNum(maxSelectNum)
                                .minSelectNum(1)
                                .imageSpanCount(4)
                                .selectionMode(PictureConfig.MULTIPLE)
                                .forResult(PictureConfig.CHOOSE_REQUEST);
                        break;
                    case R.id.tv_camera:
                        //拍照
                        PictureSelector.create(EditAcitivity.this)
                                .openCamera(PictureMimeType.ofImage())
                                .forResult(PictureConfig.CHOOSE_REQUEST);
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
                        if (i.isCompressed() && i != null)
                            imagesPath.add(i.getCompressPath());
                        else if(i!=null)  //获取原本路径
                            imagesPath.add(i.getPath());
                    }

//                    selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    adapter.setList(selectList);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }
}
