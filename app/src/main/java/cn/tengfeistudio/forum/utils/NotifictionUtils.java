package cn.tengfeistudio.forum.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import cn.tengfeistudio.forum.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by xietufei on 2019/3/25.
 */

public class NotifictionUtils {

    /**
     * 通知
     * @param context
     * @param cls  当前activity.class
     * @param title  标题
     * @param content 回复内容
     * @param topicId 帖子Id
     */
    public static void showNotifictionIcon(Context context, Class<?> cls,String title,String content,int topicId) {

//        String state="未读";  //状态文字
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"default");
//        Intent intent = new Intent(context, cls);//将要跳转的界面
//        //Intent intent = new Intent();//只显示通知，无页面跳转
//        builder.setAutoCancel(true);//点击后消失
//        builder.setSmallIcon(R.drawable.message);//设置通知栏消息标题的头像
//        builder.setDefaults(NotificationCompat.DEFAULT_SOUND);//设置通知铃声
//        builder.setTicker(state);
//        builder.setContentTitle(title);
//        builder.setContentText(content);
//        builder.setWhen(System.currentTimeMillis());
//        //利用PendingIntent来包装我们的intent对象,使其延迟跳转
//        PendingIntent intentPend = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        builder.setContentIntent(intentPend);
//        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
//        manager.notify(1, builder.build());

        NotificationManager manager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        //8.0 以后需要加上channelId 才能正常显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelId = "default";
            String channelName = "默认通知";
            manager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH));
        }
        Intent intent = new Intent(context, cls);
        intent.putExtra("topicId",topicId);
        //设置TaskStackBuilder
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(cls);
        stackBuilder.addNextIntent(intent);

        //PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.message)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .build();

        manager.notify(0, notification);

    }
}
