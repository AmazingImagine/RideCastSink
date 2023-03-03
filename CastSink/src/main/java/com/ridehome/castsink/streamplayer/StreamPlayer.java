package com.ridehome.castsink.streamplayer;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCUtil;

import java.util.ArrayList;

public class StreamPlayer {
    private MediaPlayer mMediaPlayer = null;
    private LibVLC mLibVLC = null;
    private ArrayList<String> options = null;
    private Context mContext = null;

    public StreamPlayer(Context context){
        mContext = context;
        System.loadLibrary("streamPlayer");
    }

    public void Play(String strMRL){
        if(null == mLibVLC){
            InitPlayer();
            mLibVLC = new LibVLC(mContext, options);
        }

        if(null == mMediaPlayer){
            mMediaPlayer = new MediaPlayer(mLibVLC);
        }

        // 设置视频回调
        setVideoCallBack(mMediaPlayer);

        boolean ret = loadMedia(strMRL);
        if(ret)
        {
            mMediaPlayer.play();
        }
    }

    public void Stop(){

    }

    private void InitPlayer(){
        if(null == options)
             options = new ArrayList<String>();
        else
             options.clear();

        //正式参数配置
        //值越大，缓存越大，延迟越大。这三项是延迟设置
        options.add("--network-caching=100");//网络缓存
        options.add(":clock-jitter=0");
        options.add(":clock-synchro=0");

        options.add("--rtsp-caching=100");//
        options.add("--tcp-caching=100");//TCP输入缓存值 (毫秒)
        options.add("--realrtsp-caching=100");//RTSP缓存值 (毫秒)
        options.add(":file-caching=100");//文件缓存
        options.add(":live-cacheing=100");//直播缓存
        options.add("--file-caching");//文件缓存
        options.add("--sout-mux-caching=100");//输出缓存
        options.add("--no-drop-late-frames");//关闭丢弃晚的帧 (默认打开)
        options.add("--no-skip-frames");//关闭跳过帧 (默认打开)
        options.add(":rtsp-frame-buffer-size=1000"); //RTSP帧缓冲大小，默认大小为100000
        options.add("--rtsp-tcp");
        options.add("--http-reconnect");    //: 重连
        options.add("--deinterlace");    //: 交错
        options.add("" + getDeblocking(-1));//这里太大了消耗性能   太小了会花屏
        options.add("--deinterlace-mode={discard,blend,mean,bob,linear,x}");// 视频译码器 解除交错模式
        options.add("--network-synchronisation");// 网络同步化 (默认关闭)
    }

    private int getDeblocking(int deblocking) {
        int ret = deblocking;
        if (deblocking < 0) {
            /**
             * Set some reasonable sDeblocking defaults:
             *
             * Skip all (4) for armv6 and MIPS by default
             * Skip non-ref (1) for all armv7 more than 1.2 Ghz and more than 2 cores
             * Skip non-key (3) for all devices that don't meet anything above
             */
            VLCUtil.MachineSpecs m = VLCUtil.getMachineSpecs();
            if (m == null)
                return ret;
            if ((m.hasArmV6 && !(m.hasArmV7)) || m.hasMips)
                ret = 4;
            else if (m.frequency >= 1200 && m.processors > 2)
                ret = 1;
            else if (m.bogoMIPS >= 1200 && m.processors > 2) {
                ret = 1;
                Log.d("TAG", "Used bogoMIPS due to lack of frequency info");
            } else
                ret = 3;
        } else if (deblocking > 4) { // sanity check
            ret = 3;
        }
        return ret;
    }

    private boolean loadMedia(String strMRL) {
        if (TextUtils.isEmpty(strMRL)) {
            return false;
        }
        Media media;
        if (strMRL.contains("://")) {
            media = new Media(mLibVLC, Uri.parse(strMRL));
        } else {
            media = new Media(mLibVLC, strMRL);
        }

        media.setHWDecoderEnabled(true, false);
        mMediaPlayer.setMedia(media);
        media.release();
        return true;
    }





    public native  void  setVideoCallBack(MediaPlayer mediaPlayer);
}
