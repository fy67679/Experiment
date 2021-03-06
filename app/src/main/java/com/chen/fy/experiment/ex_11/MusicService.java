package com.chen.fy.experiment.ex_11;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.chen.fy.experiment.R;

import java.io.IOException;

/**
 * 音乐播放后台服务类
 */
public class MusicService extends Service {

    private static final String CHANNEL_ID = "Music channel";
    private static final int NOTIFICATION_ID = 101;   //Notification的ID,一遍后续可以通过该ID更新Notification
    private MediaPlayer mMediaPlayer;

    private NotificationManager mNotificationManager;

    private final IBinder mBinder = new MusicServiceBinder();

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
    }

    /**
     * 响应startService()方法的回调，可以多次调用
     *
     * @param intent  开启服务时传来的上下文
     * @param startId 表示特定的一次startService的调用
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 点击歌曲Item后执行，对歌曲进行播放并显示通知
     *
     * @param data      歌曲地址
     * @param title     歌曲名
     * @param artist    歌手名
     */
    public void prepareMusic(String data, String title, String artist) {
        //1 解析歌曲地址，并准备歌曲进行播放
        Uri dataUri = Uri.parse(data);
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(getApplicationContext(), dataUri);
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                //发送广播，开启子线程对进度条进行更新
                Intent intent1 = new Intent(MusicActivity.ACTION_MUSIC_START);
                sendBroadcast(intent1);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //2 创建通知管道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID, "Music Channel", NotificationManager.IMPORTANCE_HIGH);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }

        //3 创建延迟意图，点击通知后跳转列表界面
        Intent notificationIntent = new Intent(getApplicationContext(), MusicActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, notificationIntent, 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);

        //4 设置通知信息
        Notification notification = builder
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent).build();

        //5 开启前台服务
        startForeground(NOTIFICATION_ID, notification);

    }

    /**
     * 暴露接口，让活动可以进行调用，实现歌曲的暂停播放等等操作
     */
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void play() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    public int getDuration() {
        int duration = 0;
        if (mMediaPlayer != null) {
            duration = mMediaPlayer.getDuration();
        }
        return duration;
    }

    public int getCurrentPosition() {
        int currentPosintion = 0;
        if (mMediaPlayer != null) {
            currentPosintion = mMediaPlayer.getCurrentPosition();
        }
        return currentPosintion;
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }


    /**
     * 服务内部的Binder类，绑定服务后此Binder类对象就可以获取到当前服务的对象
     */
    class MusicServiceBinder extends Binder {

        /**
         * 暴露接口，获取Service实例
         */
        MusicService getService() {
            return MusicService.this;
        }

    }
}
