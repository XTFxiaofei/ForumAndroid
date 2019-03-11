package cn.tengfeistudio.forum.module.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.api.beans.ActivityBean;
import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.DisplayUtils;
import cn.tengfeistudio.forum.widget.DetailScrollView;
import cn.tengfeistudio.forum.widget.SVRootLinearLayout;

import static cn.tengfeistudio.forum.utils.LogUtils.printLog;
import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastNetWorkError;
import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastShort;


/**
 * Created by chenpengfei on 2016/11/23.
 */
public class DetailActivity extends BaseActivity {


    @BindView(R.id.scrollView)
    DetailScrollView mScrollView;
    //ScrollView 底部的布局LinearLayout
    @BindView(R.id.ll_sv_root)
    SVRootLinearLayout mSVRootLl;
    //中间内容布局，内容里的底部和顶部title布局
    @BindView(R.id.ll_content)
    LinearLayout mContentLl;
    //内容里的底部和顶部title布局
    @BindView(R.id.ll_bottom)
    LinearLayout mBottomLl;
    @BindView(R.id.ll_title)
    LinearLayout mTitleLl;
    //app图片
    @BindView(R.id.imageview_icon)
    ImageView mIconImageView;
    //app名字控件
    @BindView(R.id.textview_appname)
    TextView mAppNameTextView;
    //类型
    @BindView(R.id.a_type)
    TextView acType;
    //时间
    @BindView(R.id.a_time)
    TextView acTime;
    //内容
    @BindView(R.id.a_content)
    TextView acContent;
    //内容图片
    @BindView(R.id.content_img)
    ImageView contentImg;

    //根布局的背景色
    private ColorDrawable mRootCDrawable;

    private int mColorInitAlpha = 150;
    private int mContentTopOffsetNum;
    private int mContentBottomOffsetNum;
    private int mImageLeftOffsetNum;
    private int mImageTopOffsetNum;

    //接收过来的参数
    private int mViewMarginTop;
    // private int mImageId;
    // private String mAppName;

    private boolean initData;






    @Override
    protected int getLayoutID() {
        return R.layout.activity_detail;
    }

    @Override
    protected void initData() {
        DisplayUtils.hideActionBar(getWindow());

        if (getIntent().getExtras().getBoolean("isNormalPost")) {
            setIntentData(getIntent().getExtras().getString("ActivityJsonObject"));
        } else {
            getActivity();
        }
        mViewMarginTop = getIntent().getExtras().getInt("viewTop") + getResources().getDimensionPixelOffset(R.dimen.bar_view_height);

        initView();
        initAnimationData();
    }

    protected void initView() {
        //设置root节点view的背景透明度
        LinearLayout rootLl = (LinearLayout) findViewById(R.id.ll_root);
        Drawable rootBgDrawable = rootLl.getBackground();
        mRootCDrawable = (ColorDrawable) rootBgDrawable;
        mRootCDrawable.setAlpha(mColorInitAlpha);

        // mScrollView = (DetailScrollView) findViewById(R.id.scrollView);
        //mSVRootLl = (SVRootLinearLayout) findViewById(R.id.ll_sv_root);
        // mIconImageView = (ImageView) findViewById(R.id.imageview_icon);
        // mIconImageView.setImageResource(mImageId);

        // mContentLl = (LinearLayout) findViewById(R.id.ll_content);
        //mBottomLl = (LinearLayout) findViewById(R.id.ll_bottom);
        //mAppNameTextView = (TextView) findViewById(R.id.textview_appname);
        //  mAppNameTextView.setText(mAppName);
        //mTitleLl = (LinearLayout) findViewById(R.id.ll_title);

        mImageTopOffsetNum = getResources().getDimensionPixelOffset(R.dimen.title_view_height);

        //设置初始化的位置
        mSVRootLl.setContentInitMarginTop(mViewMarginTop);
        mContentTopOffsetNum = mViewMarginTop - getResources().getDimensionPixelOffset(R.dimen.view_height);

        /**
         *  activity 关闭回调
         */
        mSVRootLl.setOnCloseListener(new SVRootLinearLayout.OnCloseListener() {
            @Override
            public void onClose() {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        /**
         *  下拉拖动时候回调修改root背景色的透明度
         */
        mSVRootLl.setOnUpdateBgColorListener(new SVRootLinearLayout.OnUpdateBgColorListener() {
            @Override
            public void onUpdate(float ratio) {
                mRootCDrawable.setAlpha((int) (mColorInitAlpha - mColorInitAlpha * ratio));
            }
        });
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

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what){
                case 0x11:
                    mIconImageView.setImageBitmap((Bitmap)msg.obj);
                    break;
                case 0x12:
                    contentImg.setImageBitmap((Bitmap)msg.obj);
                    break;
            }
        }
    };
    /**
     * @param activityJsonObj
     */
    private void setIntentData(String activityJsonObj) {

        ActivityBean activityObj = JSON.parseObject(activityJsonObj, ActivityBean.class);


        new Thread(){
            @Override
            public void run() {
                Bitmap result=getURLimage(activityObj.getLogoImage());
                Message msg=Message.obtain();
                msg.what=0x11;
                msg.obj=result;
                handler.sendMessage(msg);
            }
        }.start();
        mAppNameTextView.setText(activityObj.getSponsor());
        acType.setText(activityObj.getType());
        acTime.setText(activityObj.getActivityTime());
        acContent.setText(activityObj.getContent());
        new Thread(){
            @Override
            public void run() {
                Bitmap result=getURLimage(activityObj.getContentPicture());
                Message msg=Message.obtain();
                msg.what=0x12;
                msg.obj=result;
                handler.sendMessage(msg);
            }
        }.start();
    }

    public Bitmap getURLimage(String UrlPath) {
        Bitmap bm = null;
        // 1、确定网址
        // http://pic39.nipic.com/20140226/18071023_164300608000_2.jpg
        String urlpath = UrlPath;
        // 2、获取Uri
        try {
            URL uri = new URL(urlpath);
            // 3、获取连接对象、此时还没有建立连接
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            // 4、初始化连接对象
            // 设置请求的方法，注意大写
            connection.setRequestMethod("GET");
            // 读取超时
            connection.setReadTimeout(5000);
            // 设置连接超时
            connection.setConnectTimeout(5000);
            // 5、建立连接
            connection.connect();
            // 6、获取成功判断,获取响应码
            if (connection.getResponseCode() == 200) {
                // 7、拿到服务器返回的流，客户端请求的数据，就保存在流当中
                InputStream is = connection.getInputStream();
                // 8、从流中读取数据，构造一个图片对象GoogleAPI
                bm = BitmapFactory.decodeStream(is);
                // 9、把图片设置到UI主线程
                // ImageView中,获取网络资源是耗时操作需放在子线程中进行,通过创建消息发送消息给主线程刷新控件；
            } else {
                bm = null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }

    private void initAnimationData() {
        mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!initData) {
                    mContentBottomOffsetNum = mScrollView.getMeasuredHeight() - mContentLl.getBottom();
                    mSVRootLl.setInitBottom(mContentLl.getBottom());
                    mSVRootLl.setAnimationStatus(true);
                    mSVRootLl.setLayoutImageView(true);
                    mImageLeftOffsetNum = (DisplayUtils.getScreenWidth(DetailActivity.this) - mIconImageView.getWidth()) / 2 - getResources().getDimensionPixelOffset(R.dimen.icon_margin);
                    initData = true;
                    startAnimation();
                }
            }
        });
    }

    private void startAnimation() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(400);
        valueAnimator.setStartDelay(100);
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float ratio = (float) animation.getAnimatedValue();
                //内容布局顶部偏移量
                int contentTopOffset = (int) (ratio * mContentTopOffsetNum);
                //内容布局底部偏移量
                int contentBottomOffset = (int) (ratio * mContentBottomOffsetNum);
                //图片左边偏移量
                int imageLeftOffset = (int) (ratio * mImageLeftOffsetNum);
                //图片上边偏移量
                int imageTopOffset = (int) (ratio * mImageTopOffsetNum);
                mSVRootLl.setAllViewOffset(mViewMarginTop - contentTopOffset, contentBottomOffset, imageLeftOffset, imageTopOffset);
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mSVRootLl.setAnimationStatus(false);
                mBottomLl.setVisibility(View.VISIBLE);
                mTitleLl.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
            if (mSVRootLl != null)
                mSVRootLl.startAnimation(mSVRootLl.getCenterVisibleViewHeight(), false, 0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
