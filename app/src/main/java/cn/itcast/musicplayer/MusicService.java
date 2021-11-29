package cn.itcast.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class MusicService extends Service {
    //新建mediaPlayer
    private MediaPlayer mediaPlayer = new MediaPlayer();
    //图标id，1是播放，2是暂停
    public int iconNumber;

    //服务的绑定器，服务启动者可以通过该绑定器对服务进行操控
    class MyBinder extends Binder {
        public boolean musicCompletion = false;
        //播放器是否有资源
        int state = 0;

        //播放
        public void play(String filepath, final int flag) {
            try {
                musicCompletion = false;
                if (state == 1 && flag == 2) {
                    //state播放器状态，flag路径是否改变
                    mediaPlayer.reset();
                    state = 0;
                }
                //指定播放类型
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                //指定文件路径
                mediaPlayer.setDataSource(filepath);
                //准备播放
                mediaPlayer.prepare();
                //播放
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        musicCompletion = true;
                    }
                });
                state = 1;
                iconNumber = 2;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //播放暂停
        public void paly_pause() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                iconNumber = 1;
            } else {//如果暂停，则继续播放
                mediaPlayer.start();
                iconNumber = 2;
            }
        }

        //跳到播放处
        public void setCurrpos(int i) {
            mediaPlayer.seekTo(i);
        }

        //获取当前播放进度
        public int getCurrPos() {
            if (mediaPlayer != null) {
                //获取当前位置
                return mediaPlayer.getCurrentPosition();
            }
            return 0;
        }

        //获取总时长
        public int getDuration() {
            if (mediaPlayer != null) {
                return mediaPlayer.getDuration();
            }
            return 0;
        }

        //更改图标标识
        public int iconNum() {
            return iconNumber;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //服务被绑定时，返回绑定器对象,服务启动者就可以通过绑定器操控服务
        return new MyBinder();
    }
}
