package cn.tengfeistudio.forum.module.activity;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;

import com.zxl.library.DropDownMenu;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import butterknife.BindView;
import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.api.beans.ActivityBean;
import cn.tengfeistudio.forum.injector.components.DaggerActivityFragComponent;
import cn.tengfeistudio.forum.injector.modules.ActivityFragModule;
import cn.tengfeistudio.forum.listener.LoadMoreListener;
import cn.tengfeistudio.forum.module.base.BaseFragment;
import cn.tengfeistudio.forum.module.home.HomeActivity;

import static cn.tengfeistudio.forum.utils.LogUtils.printLog;


public class ActivityFragment extends BaseFragment
        implements LoadMoreListener.OnLoadMoreListener {

    @BindView(R.id.dropDownMenu)
    DropDownMenu mDropDownMenu;
    @BindView(R.id.tv_hotnews_showlogin2)
    TextView tvHotnewsShowlogin;
    @BindView(R.id.rv_hotnews2)
    RecyclerView rv;
    @BindView(R.id.swiperefresh_hotnews2)
    SwipeRefreshLayout refreshLayout;

    //活动地点
    private String acPlace="";
    //活动类型
    private String acType="";



    //private String headers[] = {"城市", "年龄", "性别", "星座"};
    private String headers[] = {"城市", "素拓类型",};
    //private int[] types = new int[]{DropDownMenu.TYPE_LIST_CITY, DropDownMenu.TYPE_LIST_SIMPLE, DropDownMenu.TYPE_CUSTOM, DropDownMenu.TYPE_GRID};
    private int[] types = new int[]{DropDownMenu.TYPE_LIST_CITY, DropDownMenu.TYPE_LIST_SIMPLE};
    private String citys[] = {"不限", "本部", "佛山"};
    private String actypes[] = {"不限", "身心", "文化", "创新创业", "思想品德", "技能"};
    //private String constellations[] = {"不限", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};


    @Inject
    protected ActivityFragPresenter mPresenter;
    private MyHandler handler = new MyHandler(getActivity());

    class MyHandler extends Handler {
        private WeakReference<Activity> weakReference;

        public MyHandler(Activity mActivity) {
            weakReference = new WeakReference<>(mActivity);
            printLog("hotnewsFragment myHandler()");
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Activity mActivity = weakReference.get();
            printLog("handleMessage01");
            if (mActivity != null) {
                printLog("handleMessage02 ");
                switch (msg.what) {
                }
            }
        }
    }


    @Override
    public int getLayoutid() {
        return R.layout.fragment_activity;
    }

    @Override
    protected void initData(Context content) {
        //getActivityObj();
        if (!App.ISLOGIN()) {
            tvHotnewsShowlogin.setText("登陆后就可以看了喔 ٩(๑❛ᴗ❛๑)۶");
            tvHotnewsShowlogin.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            return;
        }
        tvHotnewsShowlogin.setText("刷新中...");
        mPresenter.getData(false, mContent,acPlace,acType);
        //不用放到initView()否则每次跳回都刷新添加到标题
        initNewLayout();
        initView();
    }

    @Override
    protected void initView() {
        initRefreshLayout();
        initRecyclerView();
    }
    private void initNewLayout() {

       ((ViewGroup)rv.getParent()).removeAllViews();
        mDropDownMenu.setDropDownMenu(Arrays.asList(headers), initViewData(), rv);

        //该监听回调只监听默认类型，如果是自定义view请自行设置，参照demo
        mDropDownMenu.addMenuSelectListener(new DropDownMenu.OnDefultMenuSelectListener() {
            @Override
            public void onSelectDefaultMenu(int index, int pos, String clickstr) {
                //index:点击的tab索引，pos：单项菜单中点击的位置索引，clickstr：点击位置的字符串
                printLog("index:"+index+"  pos:"+pos+" clickstr:"+clickstr);
                Toast.makeText(getContext(), clickstr, Toast.LENGTH_SHORT).show();
                if(0==index && pos!=0){
                    acPlace=clickstr;
                }else if(0==index && 0==pos){
                    acPlace="";
                }
                if(1==index && pos!=0){
                    acType=clickstr;
                }else if(1==index && 0==pos){
                    acType="";
                }
                doRefresh();
            }
        });
    }

    /**
     * 设置类型和数据源：
     * DropDownMenu.KEY对应类型（DropDownMenu中的常量，参考上述核心源码）
     * DropDownMenu.VALUE对应数据源：key不是TYPE_CUSTOM则传递string[],key是TYPE_CUSTOM类型则传递对应view
     */
    private List<HashMap<String, Object>> initViewData() {

        List<HashMap<String, Object>> viewDatas = new ArrayList<>();
        HashMap<String, Object> map;
        for (int i = 0; i < headers.length; i++) {
            map = new HashMap<String, Object>();
            map.put(DropDownMenu.KEY, types[i]);
            switch (types[i]) {
                case DropDownMenu.TYPE_LIST_CITY:
                    map.put(DropDownMenu.VALUE, citys);
                   // map.put(DropDownMenu.SELECT_POSITION,0);
                    break;
                case DropDownMenu.TYPE_LIST_SIMPLE:
                    map.put(DropDownMenu.VALUE, actypes);
                    //map.put(DropDownMenu.SELECT_POSITION,0);
                    break;
//                case DropDownMenu.TYPE_GRID:
//                    map.put(DropDownMenu.VALUE, constellations);
//                    break;
//                default:
//                    map.put(DropDownMenu.VALUE, getCustomView());
//                    break;
            }
            viewDatas.add(map);
        }
        return viewDatas;
    }

    /**
     * 自定义标题栏，先不用
     * @return
     */
    private View getCustomView() {
        View v = getLayoutInflater().inflate(R.layout.custom, null);
        TextView btn = (TextView) v.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDropDownMenu.setTabText(2,"自定义");//设置tab标签文字
                mDropDownMenu.closeMenu();//关闭menu
            }
        });
        return v;
    }



    /**
     * 初始化recylerView的一些属性
     */
    protected RecyclerView.LayoutManager mLayoutManager;
    protected LoadMoreListener loadMoreListener;

    private void initRecyclerView() {
        // 设置监听事件
        if (loadMoreListener == null) {
            mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            rv.setLayoutManager(mLayoutManager);
            loadMoreListener = new LoadMoreListener((LinearLayoutManager) mLayoutManager, this, 5);
        }
        rv.addOnScrollListener(loadMoreListener);

        rv.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        // 调整draw缓存,加速recyclerview加载
        rv.setItemViewCacheSize(20);
        rv.setDrawingCacheEnabled(true);
        rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

    }

    /**
     * 执行刷新操作
     */
    public void doRefresh() {
        if (!App.ISLOGIN()) {
            tvHotnewsShowlogin.setText("登陆后就可以看了喔 ٩(๑❛ᴗ❛๑)۶");
            tvHotnewsShowlogin.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            return;
        }
        isPullDownRefresh = true;
        new Thread() {
            @Override
            public void run() {
                getActivity().runOnUiThread(() -> {
                    tvHotnewsShowlogin.setText("刷新中2...");
                    tvHotnewsShowlogin.setVisibility(View.VISIBLE);
                });
            }
        }.start();
        mPresenter.getData(true, mContent,acPlace,acType);
    }

    /**
     * 下拉刷新样式
     * isRefresh
     * true 有刷新请求
     * false 无刷新请求
     */
    protected boolean isPullDownRefresh = false;

    private void initRefreshLayout() {
        refreshLayout.setColorSchemeResources(R.color.red_light, R.color.green_light, R.color.blue_light, R.color.orange_light);
        refreshLayout.setOnRefreshListener(() -> new Thread() {
            @Override
            public void run() {
                doRefresh();
            }
        }.start());
    }
    protected boolean new_loadnothing = false;
    protected void afterGetDataSuccess(String data) {
        mPresenter.afterGetDataSuccess(data, mContent);
        isPullDownRefresh = false;
        isPullUpRefresh = false;
        tvHotnewsShowlogin.setVisibility(View.GONE);
        rv.setVisibility(View.VISIBLE);
        if (refreshLayout.isRefreshing())
            refreshLayout.setRefreshing(false);
    }

    protected void setLoadMoreListener() {
        loadMoreListener = new LoadMoreListener((LinearLayoutManager) mLayoutManager, this, 5);
        rv.addOnScrollListener(loadMoreListener);
    }

    @Override
    public void ScrollToTop() {
    }

    private HomeActivity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (HomeActivity) context;
    }

    @Override
    protected void initInjector() {
        DaggerActivityFragComponent.builder()
                .applicationComponent(App.getAppComponent())
                .activityFragModule(new ActivityFragModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    private boolean isPullUpRefresh = false;



    @Override
    public void onLoadMore() {
        if (isPullDownRefresh || isPullUpRefresh)
            return;
        isPullUpRefresh = true;
        mPresenter.getListData(mPresenter.max_page_post + 1,acPlace,acType);
    }


}

