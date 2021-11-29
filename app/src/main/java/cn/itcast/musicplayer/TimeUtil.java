package cn.itcast.musicplayer;

public class TimeUtil {
    public static String fromatTime(int millli){
        int minute = millli / 1000 / 60;
        int second = millli / 1000 % 60;
        return String.format("%02d:%02d",minute,second);
    }
}
