package cn.tengfeistudio.forum.module.post.postcontent.main;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.tengfeistudio.forum.adapter.CommentReplyAdapter;
import cn.tengfeistudio.forum.adapter.MyRecyclerViewAdapter;
import cn.tengfeistudio.forum.api.beans.Comment;
import cn.tengfeistudio.forum.api.beans.CommentBean;
import cn.tengfeistudio.forum.api.beans.TopicBean;
import cn.tengfeistudio.forum.module.post.postlist.PostsActivity;
import cn.tengfeistudio.forum.module.user.userdetail.UserDetailActivity;
import cn.tengfeistudio.forum.adapter.CommentAdapter;
import cn.tengfeistudio.forum.api.RetrofitService;
import cn.tengfeistudio.forum.App;

import cn.tengfeistudio.forum.module.base.BaseActivity;
import cn.tengfeistudio.forum.module.base.BaseFragment;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.SensitiveWordUtil;
import cn.tengfeistudio.forum.utils.toast.GlobalDialog;
import cn.tengfeistudio.forum.widget.CircleImageView;
import cn.tengfeistudio.forum.utils.IntentUtils;
import cn.tengfeistudio.forum.utils.StringUtils;

import com.luck.picture.lib.tools.Constant;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.squareup.picasso.Picasso;
import com.zzhoujay.richtext.RichText;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import butterknife.BindView;
import butterknife.OnClick;

import static cn.tengfeistudio.forum.utils.LogUtils.printLog;
import static cn.tengfeistudio.forum.utils.StampToDate.getStringDate;
import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastNetWorkError;
import static cn.tengfeistudio.forum.utils.toast.ToastUtils.ToastShort;


public class PostFragment extends BaseFragment {
    @BindView(R.id.article_title)
    TextView articleTitle;
    @BindView(R.id.main_window)
    RelativeLayout mainWindow;
    @BindView(R.id.article_user_image)
    CircleImageView articleUserImage;
    @BindView(R.id.article_username)
    TextView articleUsername;
    @BindView(R.id.bt_lable_lz)
    TextView btLableLz;
    @BindView(R.id.article_post_time)
    TextView articlePostTime;
    @BindView(R.id.btn_more)
    ImageView btnMore;
    @BindView(R.id.divider)
    View divider;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.rv_comment)
    RecyclerView rvComment;
    @BindView(R.id.tv_comment_suggest)
    TextView tvCommentSuggest;
    @BindView(R.id.load_bottom)
    LinearLayout loadBottom;
    @BindView(R.id.close_panel)
    ImageView closePanel;
    @BindView(R.id.rb_grade)
    RatingBar userLevel;
    @BindView(R.id.rv)
    RecyclerView rv;

    private List<CommentBean> beanList;
    private List<Comment> comments;
    //private Post postObj;
    private TopicBean topicObj;
    // private long postID;
    //帖子id
    private int topicId;
    //用户等级
    private int level;
    private int targetId;
    //给谁通知
    private int toUserId;
    // 输入框
    private View mInputBarView;
    private CommentAdapter adapter = null;
    //评论回复适配器
    private CommentReplyAdapter commentReplyAdapter = null;
    private String from = "";
    /**
     * -----------  九图 ---------------
     */
    private List<String> images = new ArrayList<String>();//图片地址
    private Context mContext;
    private DisplayImageOptions options;
    private MyRecyclerViewAdapter adapter2;
    private HashMap<Integer, float[]> xyMap = new HashMap<Integer, float[]>();//所有子项的坐标
    private int screenWidth;//屏幕宽度
    private int screenHeight;//屏幕高度

    @Override
    public int getLayoutid() {
        return R.layout.fragment_post;
    }

    @Override
    protected void initData(Context content) {
        getPostObj();
        getCommentListData();

        mContext = getContext();
        onResume();
        initView();
        initData();
        setEvent();
    }

    public void onResume() {
        super.onResume();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
    }


    /**
     * 初始化底部输入框
     */
    private EditText et;

    public void initMyInputBar() {
        if (!isInputBarShow) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM;
            mInputBarView = LayoutInflater.from(getContext()).inflate(R.layout.my_input_bar, null);
            et = mInputBarView.findViewById(R.id.ed_comment);
            mInputBarView.findViewById(R.id.btn_send).setOnClickListener(view -> {
                if (!App.ISLOGIN()) {
                    ToastShort("是不是忘了登录？(ฅ′ω`ฅ)");
                    return;
                }
                if (!TextUtils.isEmpty(et.getText().toString())) {
                    //评论的内容
                    String commentString=et.getText().toString();
                    /** 敏感词汇过滤 */
                    SensitiveWordUtil filterEngine = SensitiveWordUtil.getInstance();
                    Vector<Integer> levelSet = new Vector<Integer>();
                    try {
                        commentString=filterEngine.parse(new String(commentString.getBytes(), "UTF-8"), levelSet);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    postComments(commentString);
                    et.setText("");
                } else
                    ToastShort("回复内容不能为空喔(ฅ′ω`ฅ)");
            });
            getActivity().addContentView(mInputBarView, lp);
            doShowAnimation();
            isInputBarShow = true;
        }
    }

    @SuppressLint("CheckResult")
    private void postComments(String comment) {
        //topicId帖子id,toUserId给谁通知，
        RetrofitService.addComment(targetId, comment + StringUtils.getTextTail(), topicId)
                .subscribe(responseBody -> {
                    String response = responseBody.string();
                    if (!response.contains("code")) {
                        ToastNetWorkError();
                        return;
                    }
                    JSONObject jsonObject = JSON.parseObject(response);
                    if (jsonObject.getInteger("code") == Constants.TOKEN_OVERDUE) {
                        getNewToken(comment);
                    } else {
                        ToastShort("发布成功");
                        ((BaseActivity) super.mContent).hideKeyBoard();
                        getCommentListData();
                    }
                }, throwable -> ToastNetWorkError());
    }

    /**
     * 获取新的Token
     */
    @SuppressLint("CheckResult")
    private void getNewToken(String comment) {
        RetrofitService.getNewToken()
                .subscribe(s -> postComments(comment));
    }

    /**
     * 移除输入框
     */
    private boolean isInputBarShow = false;

    public void removeMyInputBar() {
        if (mInputBarView != null && isInputBarShow) {
            doHideAnimation();
            isInputBarShow = false;
        }
    }

    /**
     * 从activity 获取帖子对象
     */
    private void getPostObj() {
        String PostJsonString = getArguments().getString("PostJsonObject");
        from = getArguments().getString("from");
        topicObj = JSON.parseObject(PostJsonString, TopicBean.class);
        //帖子id
        topicId = topicObj.getTopicId();
        //给谁通知
        toUserId = topicObj.getUserByUserId().getUserId();
        //帖子id
        targetId = topicObj.getTopicId();
        //等级
        level = topicObj.getUserByUserId().getLevel();
    }

    /**
     * 根据activity获取的POSTID从服务器获取[回复对象含User对象]详情
     */
    @SuppressLint("CheckResult")
    private void getCommentListData() {
        // RetrofitService.getCommentListData(postID)
        RetrofitService.getCommentListData(topicId)
                .subscribe(responseBody -> {
                    String response = responseBody.string();
                    JSONObject obj = JSON.parseObject(response);
                    if (obj.getInteger("code") != Constants.RETURN_CONTINUE) {
                        ToastShort("获取评论失败惹，再试试( • ̀ω•́ )✧");
                        return;
                    }
                    //initCommentListData(obj.getString("data"));
                    initCommentListData(obj);
                    if (adapter == null)
                        initCommentList();
                    else
                        adapter.notifyDataSetChanged();
                }, throwable -> ToastNetWorkError());
    }


    private void initCommentListData(JSONObject CommentJsonObj) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        List<Comment> commentTempList = JSON.parseArray(CommentJsonObj.getString("data"), Comment.class);
        //对回复进行排序
        Collections.sort(commentTempList);
        if (comments.size() != commentTempList.size()) {
            for (int i = comments.size(); i < commentTempList.size(); i++)
                comments.add(commentTempList.get(i));
        }
        //没有评论
        if (comments.size() == 0) {
            tvCommentSuggest.setVisibility(View.VISIBLE);
            loadBottom.setVisibility(View.GONE);
        } else {
            rvComment.setVisibility(View.VISIBLE);
            tvCommentSuggest.setVisibility(View.GONE);
            loadBottom.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 初始化标题等等信息
     */
    private void initHead() {
        List<String> imgUrls = new ArrayList<>();
        imgUrls = JSONArray.parseArray(topicObj.getContentPictureJson(), String.class);
        if (imgUrls != null) {
            images.clear();
            images.addAll(imgUrls);
        }

        if (from.equals("PostActivity")) {
            closePanel.setVisibility(View.GONE);
            initMyInputBar();
        }

        articleTitle.setText(topicObj.getTitle());
        articleUsername.setText(topicObj.getUserByUserId().getNickname());
        articlePostTime.setText(getStringDate(topicObj.getCreateTime()));
        userLevel.setRating(topicObj.getUserByUserId().getLevel());
        if (topicObj.getContent().isEmpty()) {
            content.setVisibility(View.GONE);
        } else {
            RichText.fromMarkdown(topicObj.getContent()).into(content);
        }

        Picasso.get()
                .load(topicObj.getUserByUserId().getIcon())
                .placeholder(R.drawable.image_placeholder)
                .into(articleUserImage);
    }

    private void initCommentList() {
        commentReplyAdapter = new CommentReplyAdapter(getContext(), comments, topicObj.getUserByUserId().getUserId());
        commentReplyAdapter.setOnItemClickListener(listener);
        rvComment.setAdapter(commentReplyAdapter);

        rvComment.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        // 解决数据加载不全的问题
        rvComment.setNestedScrollingEnabled(false);
        rvComment.setHasFixedSize(true);
        //解决数据加载完成后，没有停留在顶部的问题
        rvComment.setFocusable(false);
        rvComment.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        // 调整draw缓存,加速recyclerview加载
        rvComment.setItemViewCacheSize(20);
        rvComment.setDrawingCacheEnabled(true);
        rvComment.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }

    Animator showAnimator, hideAnimator;

    private void doShowAnimation() {
        showAnimator = new ObjectAnimator().ofFloat(mInputBarView, "alpha", 0f, 1f);
        showAnimator.start();
    }

    private void doHideAnimation() {
        hideAnimator = new ObjectAnimator().ofFloat(mInputBarView, "alpha", 1f, 0f);
        hideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mInputBarView.setVisibility(View.GONE);
                ((ViewGroup) mInputBarView.getParent()).removeView(mInputBarView);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        hideAnimator.start();
    }

    /**
     * 评论的
     */
    private CommentReplyAdapter.OnItemClickListener listener = (view, pos) -> {
        switch (view.getId()) {
            //回复
            case R.id.btn_reply_cz:
                if (et != null) {
                    //et.setText("***回复@" + commentList.get(pos).getUser().getName() + "：***\n");
                    et.setText("回复@" + comments.get(pos).getUserByFromId().getNickname() + ":\n");
                    et.setSelection(et.getText().length() - 1);
                    //设置回复的目标id
                    targetId = comments.get(pos).getCommentId();
                }
                break;
            //更多
            case R.id.btn_more:
                PopupMenu popup = new PopupMenu(getActivity(), view);
                popup.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.tv_edit:
                            RetrofitService.deleteComment(comments.get(pos).getCommentId(),Constants.REPORT_FLAG)
                                    .subscribe(responseBody -> {
                                        String response=responseBody.string();
                                        if(!response.contains("code")){
                                            ToastNetWorkError();
                                        }else {
                                            ToastShort("OK已举报!");
                                        }
                                    }, throwable -> {
                                        printLog("PostFragement Comment:" + throwable.getMessage());
                                        ToastNetWorkError();
                                    });
                            break;
                        case R.id.tv_copy:
                            //String user = commentList.get(pos).getUser().getName();
                            String user = comments.get(pos).getUserByFromId().getNickname();
                            //String content = commentList.get(pos).getBody();
                            String content = comments.get(pos).getCommentContent();
                            ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cm != null) {
                                cm.setPrimaryClip(ClipData.newPlainText(null, content));
                                ToastShort("已复制" + user + "的评论");
                            }
                            break;
                        case R.id.tv_remove:
                            final GlobalDialog delDialog = new GlobalDialog(getContext());
                            delDialog.setCanceledOnTouchOutside(true);
                            delDialog.getTitle().setText("提示");
                            delDialog.getContent().setText("确定删除吗?");
                            delDialog.setLeftBtnText("取消");
                            delDialog.setRightBtnText("确定");
                            delDialog.setLeftOnclick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                   // Toast.makeText(getContext(), "取消", Toast.LENGTH_SHORT).show();
                                    delDialog.dismiss();
                                }
                            });
                            delDialog.setRightOnclick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    RetrofitService.deleteComment(comments.get(pos).getCommentId(),Constants.DELETE_FLAG)
                                            .subscribe(responseBody -> {
                                                String response=responseBody.string();
                                                if(!response.contains("code")){
                                                    ToastNetWorkError();
                                                }else {
                                                    Toast.makeText(getContext(), "已删除,要刷新(⊙o⊙)", Toast.LENGTH_SHORT).show();
                                                }
                                            }, throwable -> {
                                                printLog("PostFragement Comment:" + throwable.getMessage());
                                                ToastNetWorkError();
                                            });
                                    delDialog.dismiss();
                                }
                            });
                            delDialog.show();
                            break;
                    }
                    return true;
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_post_more, popup.getMenu());
                //popup.getMenu().removeItem(R.id.tv_edit);
                // 判断是不是本人
                if(comments.get(pos).getFromId()==App.getUid() || App.getRole().equals("admin")){
                    // 如果有管理权限,则显示删除
                    //popup.getMenu().removeGroup(R.id.menu_manege);
                }else{
                    popup.getMenu().removeGroup(R.id.menu_manege);
                }
                popup.show();
                break;
        }
    };

    @Override
    public void ScrollToTop() {
        rvComment.scrollToPosition(0);
    }

    @Override
    protected void initInjector() {
    }

    @Override
    public void onDestroyView() {
        removeMyInputBar();
        super.onDestroyView();
    }

    /**
     * 帖子内容
     * @param view
     */
    @OnClick({R.id.share_panel, R.id.close_panel, R.id.btn_more, R.id.article_user_image, R.id.article_username})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //分享
            case R.id.share_panel:
                String data = "这篇文章不错，分享给你们 【" + articleTitle.getText() + " \n链接地址：http://118.24.0.78/#/forum/" + topicId + "】\n来自PlusClub客户端";
                IntentUtils.sharePost(getActivity(), data);
                break;
            //隐藏
            case R.id.close_panel:
                PostsActivity postsActivity = (PostsActivity) getActivity();
                postsActivity.hidePanel();
                break;
            //更多
            case R.id.btn_more:
                PopupMenu popup = new PopupMenu(getActivity(), view);
                popup.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.tv_edit:
                            RetrofitService.deleteTopic(topicObj.getTopicId(), Constants.REPORT_FLAG)
                                    .subscribe(responseBody -> {
                                        String response=responseBody.string();
                                        if(!response.contains("code")){
                                            ToastNetWorkError();
                                        }else {
                                            ToastShort("ok已举报!");
                                        }
                                    }, throwable -> {
                                        printLog("PostFragment Content:" + throwable.getMessage());
                                        ToastNetWorkError();
                                    });

                            break;
                        case R.id.tv_copy:
                            String user = articleUsername.getText().toString();
                            String s = content.getText().toString();
                            ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cm != null) {
                                cm.setPrimaryClip(ClipData.newPlainText(null, s));
                                ToastShort("已复制" + user + "的内容");
                            }
                            break;
                        case R.id.tv_remove:
                            final GlobalDialog delDialog = new GlobalDialog(getContext());
                            delDialog.setCanceledOnTouchOutside(true);
                            delDialog.getTitle().setText("提示");
                            delDialog.getContent().setText("确定删除吗?");
                            delDialog.setLeftBtnText("取消");
                            delDialog.setRightBtnText("确定");
                            delDialog.setLeftOnclick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //Toast.makeText(getContext(), "取消", Toast.LENGTH_SHORT).show();
                                    delDialog.dismiss();
                                }
                            });
                            delDialog.setRightOnclick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    RetrofitService.deleteTopic(topicObj.getTopicId(), Constants.DELETE_FLAG)
                                            .subscribe(responseBody -> {
                                                String response=responseBody.string();
                                                if(!response.contains("code")){
                                                    ToastNetWorkError();
                                                }else {
                                                    Toast.makeText(getContext(), "已删除,要刷新(⊙o⊙)", Toast.LENGTH_SHORT).show();
                                                }
                                            }, throwable -> {
                                                printLog("PostFragment Content:" + throwable.getMessage());
                                                ToastNetWorkError();
                                            });
                                    delDialog.dismiss();
                                }
                            });
                            delDialog.show();
                            break;
                    }
                    return true;
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_post_more, popup.getMenu());
                // 判断是不是本人
                //popup.getMenu().removeItem(R.id.tv_edit);
                if(topicObj.getUserByUserId().getUserId()==App.getUid() || App.getRole().equals("admin")){
                    // 如果有管理权限,则显示删除
                    //popup.getMenu().removeGroup(R.id.menu_manege);
                }else{
                    popup.getMenu().removeGroup(R.id.menu_manege);
                }
                popup.show();
                break;
            case R.id.article_user_image:
            case R.id.article_username:
                Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                //intent.putExtra("userid", postObj.getUser().getId());
                intent.putExtra("userid", topicObj.getUserByUserId().getUserId());
                startActivity(intent);
                break;
        }
    }


    /** -------------- 九图 -------------------- */
    /**
     * recyclerView item点击事件
     */
    private void setEvent() {
        adapter2.setmOnItemClickListener(new MyRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(mContext, SecondActivity.class);
                intent.putStringArrayListExtra("urls", (ArrayList<String>) images);
                intent.putExtra("position", position);
                xyMap.clear();//每一次点击前子项坐标都不一样，所以清空子项坐标

                //子项前置判断，是否在屏幕内，不在的话获取屏幕边缘坐标
                View view0 = rv.getChildAt(0);
                int position0 = rv.getChildPosition(view0);
                if (position0 > 0) {
                    for (int j = 0; j < position0; j++) {
                        float[] xyf = new float[]{(1 / 6.0f + (j % 3) * (1 / 3.0f)) * screenWidth, 0};//每行3张图，每张图的中心点横坐标自然是屏幕宽度的1/6,3/6,5/6
                        xyMap.put(j, xyf);
                    }
                }

                //其余子项判断
                for (int i = position0; i < rv.getAdapter().getItemCount(); i++) {
                    View view1 = rv.getChildAt(i - position0);
                    if (rv.getChildPosition(view1) == -1)//子项末尾不在屏幕部分同样赋值屏幕底部边缘
                    {
                        float[] xyf = new float[]{(1 / 6.0f + (i % 3) * (1 / 3.0f)) * screenWidth, screenHeight};
                        xyMap.put(i, xyf);
                    } else {
                        int[] xy = new int[2];
                        view1.getLocationOnScreen(xy);
                        float[] xyf = new float[]{xy[0] * 1.0f + view1.getWidth() / 2, xy[1] * 1.0f + view1.getHeight() / 2};
                        xyMap.put(i, xyf);
                    }
                }
                intent.putExtra("xyMap", xyMap);
                mContext.startActivity(intent);
            }
        });
    }

    protected void initView() {
        GridLayoutManager glm = new GridLayoutManager(mContext, 3);//定义3列的网格布局
        rv.setLayoutManager(glm);
        rv.addItemDecoration(new RecyclerViewItemDecoration(20, 3));//初始化子项距离和列数
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.mipmap.ic_launcher)
                .showImageOnLoading(R.mipmap.ic_launcher)
                .showImageOnFail(R.mipmap.ic_launcher)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(new FadeInBitmapDisplayer(5))
                .build();
        adapter2 = new MyRecyclerViewAdapter(images, mContext, options, glm);
        rv.setAdapter(adapter2);

        initHead();
    }

    /**
     * 初始化网络图片地址，来自百度图片
     */
    private void initData() {
        adapter2.notifyDataSetChanged();
    }

    public class RecyclerViewItemDecoration extends RecyclerView.ItemDecoration {
        private int itemSpace;//定义子项间距
        private int itemColumnNum;//定义子项的列数

        public RecyclerViewItemDecoration(int itemSpace, int itemColumnNum) {
            this.itemSpace = itemSpace;
            this.itemColumnNum = itemColumnNum;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.bottom = itemSpace;//底部留出间距
            if (parent.getChildPosition(view) % itemColumnNum == 0)//每行第一项左边不留间距，其他留出间距
            {
                outRect.left = 0;
            } else {
                outRect.left = itemSpace;
            }

        }
    }



}
