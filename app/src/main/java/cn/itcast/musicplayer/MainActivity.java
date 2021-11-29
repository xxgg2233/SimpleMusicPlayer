package cn.itcast.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //服务连接对象
    private MusicServiceConn conn;
    //服务绑定器对象
    private MusicService.MyBinder binder;

    //播放进度条
    private SeekBar sbProgress;
    //播放时间文本框
    private TextView tvPlayTime;
    //总时间文本框
    private TextView tvTotalTime;
    //定时器
    private Timer timer;
    //获取输入的音乐目录的相对路径
    String filepath = "";
    //标示状态
    int flag = 1;
    //标记是不是第一次播放
    int first = 0;

    private ImageButton btnPlayPause;
    private ImageButton btnMusicMod;
    //音乐id
    int musicId;
    //播放模式（默认顺序）
    int modSwitch = 2;
    //播放进度
    private int currPos;

    public ListView listView;
    private SimpleAdapter simpleAdapter;
    private List<Map<String,Object>> dataList;

    //获取外部储存上的音乐目录
    File musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    //创建文件对象
    File file = new File(musicPath, filepath);
    //获取文件的完整路径
    String path = file.getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //进度条
        sbProgress = findViewById(R.id.sb_progress);
        tvPlayTime = findViewById(R.id.tv_play_time);
        tvTotalTime = findViewById(R.id.tv_total_time);

        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnMusicMod = findViewById(R.id.btn_mod);

        //为切换模式添加事件响应
        findViewById(R.id.btn_mod).setOnClickListener(this);
        //为播放/暂停按钮添加事件响应
        findViewById(R.id.btn_play_pause).setOnClickListener(this);
        //为下一首按钮添加事件响应
        findViewById(R.id.btn_next).setOnClickListener(this);
        //创建服务连接对象
        conn = new MusicServiceConn();
        Intent intent = new Intent(this, MusicService.class);

        //以绑定方式启动并创建服务
        bindService(intent, conn, BIND_AUTO_CREATE);
        //对于涉及隐私的权限,6.0以上的要求既要静态声明权限（配置文件生成)，还要动态请求
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);




        listView = findViewById(R.id.music_list);
        dataList = new ArrayList<>();

        simpleAdapter = new SimpleAdapter(this, getData(), R.layout.item, new
                String[]{"imageId", "name"}, new int[]{R.id.music_pic, R.id.music_name}){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Boolean isFocus = (Boolean) dataList.get(position).get("infects");
                if(isFocus){
                    view.setBackgroundColor(Color.parseColor("#00ffff"));
                }else{
                    view.setBackgroundColor(Color.parseColor("#ffffff"));
                }
                return view;
            }
        };
        listView.setAdapter(simpleAdapter);

        //获取点击item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setItemBGColorWhite(musicId);
                setItemBGColorBlue(position);
                musicId = position;
                setPath(1,musicId);
                flag = 3;
                first = 0;
            }
        });

        //进度条获取
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currPos = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binder.setCurrpos(currPos);
            }
        });
    }

    // 过滤文件类型
    class MusicFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            //设置过滤格式
            return (name.endsWith(".mp3"));
        }
    }

    private List<Map<String,Object>> getData(){
        //取得指定位置的文件设置显示到播放列表
        File home = new File("/sdcard/Music/");
        if (home.listFiles(new MusicFilter()).length > 0) {
            //如果有文件就进行遍历
            for (File file : home.listFiles(new MusicFilter())) {
                Map map = new HashMap<String, Object>();
                map.put("imageId", R.drawable.music);
                map.put("name", file.getName());
                map.put("infects",false);
                dataList.add(map);
            }
        }
        return dataList;
    }


    //变蓝色
    public void setItemBGColorBlue(int position){
        dataList.get(position).put("infects", true);
        simpleAdapter.notifyDataSetChanged();
    }

    //变白色
    public void setItemBGColorWhite(int position){
        dataList.get(position).put("infects", false);
        simpleAdapter.notifyDataSetChanged();
    }

    private class MusicServiceConn implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder binder) {
            //将服务绑定成功后由服务返回的绑定器保存起来，以后可通过绑定器操控服务
            MainActivity.this.binder = (MusicService.MyBinder) binder;
        }

        public void onServiceDisconnected(ComponentName name) {

        }
    }

    public String setPath(int modSwitch,int i){
        switch (modSwitch){
            case 1://选中
                break;
            case 2://顺序
                if (i< dataList.size()-1){
                    musicId = i + 1;
                    i = musicId;
                }else {
                    Toast.makeText(this,"已经是最后一首",Toast.LENGTH_SHORT).show();
                }
                break;
            case 3://循环
                if (i>= dataList.size()-1){
                    i = 0;
                    musicId = i;
                }
                else {
                    musicId = i + 1;
                    i = musicId;
                }
                break;
            case 4://随机
                Random random = new Random();
                musicId = random.nextInt(dataList.size()-1);
                i = musicId;
                break;
            case 5://单曲
                if (i>= dataList.size()-1){
                    i = 0;
                    musicId = i;
                }
                else {
                    musicId = i + 1;
                    i = musicId;
                }
                break;
        }
        filepath= (String) dataList.get(i).get("name");
        file = new File(musicPath, filepath);
        path = file.getAbsolutePath();
        flag = 2;

        return path;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play_pause://播放
                //文件存在
                if (file.exists() && file.length() > 0 && filepath != "") {
                    //标识歌曲是否切换
                    flag = (flag == 3)?2:1;
                    binder.play(path,flag);
                    if (first == 1){//如果不是第一次播放
                        binder.paly_pause();
                    }
                    first = binder.state;
                    if (binder.iconNum() == 1){//设置播放图标
                        btnPlayPause.setImageResource(R.drawable.play);
                    }else if (binder.iconNum() == 2){
                        btnPlayPause.setImageResource(R.drawable.pause);
                    }
                    addTimer();
                } else {//未选择播放第一首
                    first = 1;
                    setPath(1,0);
                    setItemBGColorBlue(0);
                    binder.play(path,flag);
                    btnPlayPause.setImageResource(R.drawable.pause);
                    addTimer();
                }
                break;
            case R.id.btn_next:
                first = 1;//不再是第一次播放
                //设置暂停图标
                btnPlayPause.setImageResource(R.drawable.pause);
                //背景变白
                setItemBGColorWhite(musicId);
                //判断模式
                setPath(modSwitch,musicId);
                //传递歌名并播放
                binder.play(path,flag);
                //背景变蓝
                setItemBGColorBlue(musicId);
                //添加进度条
                addTimer();
                break;
            case R.id.btn_mod:
                switch (modSwitch){
                    case 2://循环
                        btnMusicMod.setImageResource(R.drawable.list_cycle);
                        modSwitch++;
                        break;
                    case 3://随机
                        btnMusicMod.setImageResource(R.drawable.random_play);
                        modSwitch++;
                        break;
                    case 4://单曲
                        btnMusicMod.setImageResource(R.drawable.single_cycle);
                        modSwitch++;
                        break;
                    case 5://顺序
                        btnMusicMod.setImageResource(R.drawable.list_order);
                        modSwitch++;
                        break;
                }
                if (modSwitch>5)
                    modSwitch = 2;
                break;
        }
    }

    @Override
    protected void onDestroy() {
        //解绑服务
        unbindService(conn);
        //避免内存泄露
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void addTimer() {//添加定时器
        if (timer == null) {
            timer = new Timer();//创建计时器对象
            TimerTask task = new TimerTask() {//定时任务线程
                @Override
                public void run() {//定时器线程代码
                    int currPos = binder.getCurrPos();//通过绑定器获取播放器进度（毫秒）
                    int duration = binder.getDuration();//通过绑定器获取总时长（毫秒）
                    Message msg = handler.obtainMessage();//创建消息对象
                    Bundle bundle = new Bundle();//将总时长和播放进度封装到消息对象中
                    bundle.putInt("duration", duration);
                    bundle.putInt("currPos", currPos);
                    msg.setData(bundle);
                    //将消息发送到主线程的消息队列
                    handler.sendMessage(msg);
                }
            };
            //开始计时任务后的5毫秒，第一次执行task任务，以后每500毫秒执行一次
            timer.schedule(task, 5, 500);
        }
    }

    private Handler handler = new Handler() {//创建消息处理器对象
        //在主线程中处理从子线程发过来的消息

        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();//获取消息数据
            int duration = bundle.getInt("duration");//总时长
            currPos = bundle.getInt("currPos");//当前进度
            sbProgress.setMax(duration);//设置进度条的最大值
            sbProgress.setProgress(currPos);//设置进度条的当前值
            tvPlayTime.setText(TimeUtil.fromatTime(currPos));//设置播放时间文本
            tvTotalTime.setText(TimeUtil.fromatTime(duration));//设置总时长文本
            super.handleMessage(msg);
            if (binder.musicCompletion){//播放完
                setItemBGColorWhite(musicId);
                if (modSwitch == 5)//单曲循环判断
                    modSwitch = 1;
                setPath(modSwitch,musicId);
                if (modSwitch == 1)//单曲判断
                    modSwitch = 5;
                binder.play(path,flag);
                setItemBGColorBlue(musicId);
            }
        }
    };
}
