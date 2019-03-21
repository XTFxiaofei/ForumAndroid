package cn.tengfeistudio.forum.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.tengfeistudio.forum.R;
import cn.tengfeistudio.forum.utils.LogUtils;

/**
 * Created by Administrator on 2018/4/28 0028.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private List<String> images = new ArrayList<String>();//Image资源，内容为图片的网络地址
    private Context mContext;
    private DisplayImageOptions options;//UniversalImageLoad
    private GridLayoutManager glm;
    private OnItemClickListener mOnItemClickListener;

    public MyRecyclerViewAdapter(List<String> images, Context mContext, DisplayImageOptions options, GridLayoutManager glm) {
        this.images = images;
        this.mContext = mContext;
        this.options = options;
        this.glm = glm;
        /** 解决每次加载问题 */
        initImageLoader();
        // getSimpleOptions();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.rv_item_layout, null);//加载item布局
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }


    private void initImageLoader() {
        File cacheDir = StorageUtils.getOwnCacheDirectory(mContext, Environment.getExternalStorageDirectory().getPath());
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(mContext);
        builder.threadPoolSize(9);
        builder.threadPriority(Thread.NORM_PRIORITY - 4);//
        builder.diskCacheExtraOptions(600, 328, null);//
        builder.memoryCacheExtraOptions(600, 328);//
        builder.memoryCache(new UsingFreqLimitedMemoryCache(1024 * 1024 * 10));
        builder.diskCacheSize(200 * 1024 * 1024); // 200Mb sd卡(本地)缓存的最大值
        builder.diskCacheFileCount(100);
        //builder.diskCache(new UnlimitedDiscCache(new File(Environment.getExternalStorageDirectory()+File.separator+"ForumAndroid"+File.separator+"Image_cache")));
        builder.diskCache(new UnlimitedDiscCache(cacheDir));
        builder.imageDownloader(new BaseImageDownloader(mContext));
        builder.writeDebugLogs();//打印日记
        builder.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        ImageLoaderConfiguration imageLoaderConfiguration = builder.build();
        ImageLoader.getInstance().init(imageLoaderConfiguration);


    }
//    private DisplayImageOptions getSimpleOptions() {
//        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.mipmap.ic_launcher) //设置图片在下载期间显示的图片
//                .showImageForEmptyUri(R.mipmap.ic_launcher)//设置图片Uri为空或是错误的时候显示的图片
//                .showImageOnFail(R.mipmap.ic_launcher)  //设置图片加载/解码过程中错误时候显示的图片
//                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
//                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
//                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
//                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型
//                .displayer(new RoundedBitmapDisplayer(10))
//                .build();//构建完成
//        return options;
//    }

    @Override
    public void onBindViewHolder(final MyViewHolder myViewHolder, final int i) {
        myViewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);//设置图片充满ImageView并自动裁剪居中显示
        ViewGroup.LayoutParams parm = myViewHolder.imageView.getLayoutParams();
        parm.height = glm.getWidth() / glm.getSpanCount()
                - 2 * myViewHolder.imageView.getPaddingLeft() - 2 * ((ViewGroup.MarginLayoutParams) parm).leftMargin;//设置imageView宽高相同


        LogUtils.printLog(images.get(i));
        ImageLoader.getInstance().displayImage(images.get(i), myViewHolder.imageView, options);//网络加载原图
        if (mOnItemClickListener != null)//传递监听事件
        {
            myViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(myViewHolder.imageView, i);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.iv_item);
        }
    }

    /**
     * 对外暴露子项点击事件监听器
     *
     * @param mOnItemClickListener
     */
    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    /**
     * 子项点击接口
     */
    public interface OnItemClickListener {
        public void onClick(View view, int position);
    }
}
