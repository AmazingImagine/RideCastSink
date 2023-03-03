#include <jni.h>
#include <vlc/vlc.h>
#include "libvlcjni-vlcobject.h"
#include "yyl_log.h"

void *buffer = NULL;
const int g_target_screen_width = 720;
const int g_target_screen_height = 1280;

typedef void (*MiracastVideoDataCallBack)(unsigned char *p, int bufLen, int width, int height);
static MiracastVideoDataCallBack _mcVideoDataCallBack = NULL;

/////////////////////////////// For Unity
//C++的回调函数
void SetMiracastVideoDataCallBack(void (*func)(unsigned char *p, int nBufLen, int width, int height))
{
    //设置回调函数指针
    _mcVideoDataCallBack = func;
}
/////////////////////////////// For Unity

static void lock(void *opaque, void **planes)
{
    *planes = buffer;
    LOGII("display:-------------------lock\r\n");
}

// add by shenyt
static void display(void *opaque, void *picture)
{
    /* do not display the video */
    LOGII("display:-------------------getthedata\r\n");
    if(NULL!=_mcVideoDataCallBack)
        _mcVideoDataCallBack(picture, g_target_screen_width*g_target_screen_height*3, g_target_screen_width, g_target_screen_height);
}


JNIEXPORT void JNICALL
Java_com_ridehome_castsink_streamplayer_StreamPlayer_setVideoCallBack(JNIEnv *env, jobject thiz,
                                                                      jobject media_player) {
    // TODO : 加锁
    buffer = (void*)malloc(g_target_screen_height*g_target_screen_width*3);
    vlcjni_object *vlcjniObject = VLCJniObject_getInstance(env, media_player);
    libvlc_media_player_t *player = vlcjniObject->u.p_mp;
    libvlc_video_set_callbacks(player, lock, NULL, display,NULL);
    libvlc_video_set_format(player, "RV24", g_target_screen_width, g_target_screen_height,g_target_screen_width*3);
}