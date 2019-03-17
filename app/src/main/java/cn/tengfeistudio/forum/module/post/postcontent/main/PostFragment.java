package cn.tengfeistudio.forum.module.post.postcontent.main;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
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
import cn.tengfeistudio.forum.utils.StampToDate;
import cn.tengfeistudio.forum.utils.toast.GlobalDialog;
import cn.tengfeistudio.forum.widget.CircleImageView;
import cn.tengfeistudio.forum.utils.IntentUtils;
import cn.tengfeistudio.forum.utils.StringUtils;

import com.jaeger.ninegridimageview.ItemImageClickListener;
import com.jaeger.ninegridimageview.ItemImageLongClickListener;
import com.jaeger.ninegridimageview.NineGridImageView;
import com.jaeger.ninegridimageview.NineGridImageViewAdapter;
import com.squareup.picasso.Picasso;
import com.zzhoujay.richtext.RichText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

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
    @BindView(R.id.ngl_images)
    NineGridImageView<String> mNglContent;

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
    private CommentReplyAdapter commentReplyAdapter=null;

    private String from = "";

    @Override
    public int getLayoutid() {
        return R.layout.fragment_post;
    }

    @Override
    protected void initData(Context content) {
        getPostObj();
        getCommentListData();
        initView();
    }

    @Override
    protected void initView() {
        //九格图
        mNglContent.setAdapter(mAdapter);
        mNglContent.setItemImageClickListener(new ItemImageClickListener<String>() {
            @Override
            public void onItemImageClick(Context context, ImageView imageView, int index, List<String> list) {
                Log.d("onItemImageClick", list.get(index));
            }
        });
        mNglContent.setItemImageLongClickListener(new ItemImageLongClickListener<String>() {
            @Override
            public boolean onItemImageLongClick(Context context, ImageView imageView, int index, List<String> list) {
                Log.d("onItemImageLongClick", list.get(index));
                return true;
            }
        });

        initHead();
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
                    postComments(et.getText().toString());
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
        level=topicObj.getUserByUserId().getLevel();
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
        if(comments==null){
            comments=new ArrayList<>();
        }
        List<Comment> commentTempList=JSON.parseArray(CommentJsonObj.getString("data"), Comment.class);
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

    //九格图适配器
    private NineGridImageViewAdapter<String> mAdapter = new NineGridImageViewAdapter<String>() {
        @Override
        protected void onDisplayImage(Context context, ImageView imageView, String s) {
            Picasso.get().load(s).placeholder(R.drawable.image_placeholder).into(imageView);
        }

        @Override
        protected ImageView generateImageView(Context context) {
            return super.generateImageView(context);
        }

        @Override
        protected void onItemImageClick(Context context, ImageView imageView, int index, List<String> list) {
            Toast.makeText(context, "image position is " + index, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected boolean onItemImageLongClick(Context context, ImageView imageView, int index, List<String> list) {
            Toast.makeText(context, "image long click position is " + index, Toast.LENGTH_SHORT).show();
            return true;
        }
    };
    private String[] IMG_URL_LIST = {
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=935292084,2640874667&fm=27&gp=0.jpg", "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=873265023,1618187578&fm=27&gp=0.jpg",
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=935292084,2640874667&fm=27&gp=0.jpg", "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=873265023,1618187578&fm=27&gp=0.jpg",
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=935292084,2640874667&fm=27&gp=0.jpg", "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=873265023,1618187578&fm=27&gp=0.jpg",
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=935292084,2640874667&fm=27&gp=0.jpg", "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=873265023,1618187578&fm=27&gp=0.jpg",
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=935292084,2640874667&fm=27&gp=0.jpg", "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=873265023,1618187578&fm=27&gp=0.jpg",
    };

    /**
     * 初始化标题等等信息
     */
    private void initHead() {
        //样例数据
        List<String> imgUrls = new ArrayList<>();
        //imgUrls.addAll(Arrays.asList(IMG_URL_LIST));
        imgUrls= JSONArray.parseArray(topicObj.getContentPictureJson(),String.class);

        if (from.equals("PostActivity")) {
            closePanel.setVisibility(View.GONE);
            initMyInputBar();
        }
        mNglContent.setImagesData(imgUrls, NineGridImageView.STYLE_GRID);
        articleTitle.setText(topicObj.getTitle());
        articleUsername.setText(topicObj.getUserByUserId().getNickname());
        articlePostTime.setText(getStringDate(topicObj.getCreateTime()));
        userLevel.setRating(topicObj.getUserByUserId().getLevel());
//        content.setText(postObj.getBody());
        if(topicObj.getContent().isEmpty()){
            content.setVisibility(View.GONE);
        }else{
            RichText.fromMarkdown(topicObj.getContent()).into(content);
        }

        Picasso.get()
                .load(topicObj.getUserByUserId().getIcon())
                .placeholder(R.drawable.image_placeholder)
                .into(articleUserImage);
    }

    private void initCommentList() {
//        adapter = new CommentAdapter(getContext(), commentList, topicObj.getUserByUserId().getUserId());
//        adapter.setOnItemClickListener(listener);
//        rvComment.setAdapter(adapter);
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
                                    Toast.makeText(getContext(), "取消", Toast.LENGTH_SHORT).show();
                                    delDialog.dismiss();
                                }
                            });
                            delDialog.setRightOnclick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getContext(), "确定", Toast.LENGTH_SHORT).show();
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
                popup.getMenu().removeItem(R.id.tv_edit);
                // 如果有管理权限,则显示删除
                //popup.getMenu().removeGroup(R.id.menu_manege);
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
                            break;
                        case R.id.tv_copy:
                            String user = articleUsername.getText().toString();
                            String s = content.getText().toString();
                            ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cm != null) {
                                cm.setPrimaryClip(ClipData.newPlainText(null, s));
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
                                    Toast.makeText(getContext(), "取消", Toast.LENGTH_SHORT).show();
                                    delDialog.dismiss();
                                }
                            });
                            delDialog.setRightOnclick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getContext(), "确定", Toast.LENGTH_SHORT).show();
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
                // 如果有管理权限,则显示删除
                //popup.getMenu().removeGroup(R.id.menu_manege);
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
}
