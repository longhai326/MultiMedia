#include <jni.h>
#include <stdio.h>
#include <zlib.h>

#ifdef __cplusplus
extern "C" {
#endif
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavfilter/avfilter.h>
#ifdef __cplusplus
}
#endif

extern "C" {
JNIEXPORT jstring JNICALL
Java_com_lh_mediatest_FFmpegActivity_stringFromJNI(JNIEnv *env, jobject instance) {
    return env->NewStringUTF("Hello from C++ ");
}

JNIEXPORT jstring JNICALL
Java_com_lh_mediatest_FFmpegActivity_getConfigurationInfo(JNIEnv *env, jobject instance) {
    char info[10000] = {0};
    av_register_all();
    sprintf(info, "%s\n", avcodec_configuration());
    return env->NewStringUTF(info);
}

//JNIEXPORT jstring JNICALL
//Java_com_lh_mediatest_FFmpegActivity_getUrlProtocolInfo(JNIEnv *env, jobject instance) {
//    char info[40000] = {0};
//    av_register_all();
//
//    struct URLProtocol *pup = NULL;
//    //Input
//    struct URLProtocol **p_temp = &pup;
//    avio_enum_protocols((void **) p_temp, 0);
//    while ((*p_temp) != NULL) {
//        sprintf(info, "%s[In ][%10s]\n", info, avio_enum_protocols((void **) p_temp, 0));
//    }
//    pup = NULL;
//    //Output
//    avio_enum_protocols((void **) p_temp, 1);
//    while ((*p_temp) != NULL) {
//        sprintf(info, "%s[Out][%10s]\n", info, avio_enum_protocols((void **) p_temp, 1));
//    }
//    return env->NewStringUTF(info);
//}
//
//JNIEXPORT jstring JNICALL
//Java_com_lh_mediatest_FFmpegActivity_getAvformatInfo(JNIEnv *env, jobject instance) {
//    char info[40000] = {0};
//    av_register_all();
//    AVInputFormat *if_temp = av_iformat_next(NULL);
//    AVOutputFormat *of_temp = av_oformat_next(NULL);
//    //Input
//    while (if_temp != NULL) {
//        sprintf(info, "%s[In ] %10s\n", info, if_temp->name);
//        if_temp = if_temp->next;
//    }
//    //Output
//    while (of_temp != NULL) {
//        sprintf(info, "%s[Out] %10s\n", info, of_temp->name);
//        of_temp = of_temp->next;
//    }
//    return env->NewStringUTF(info);
//}
//
//JNIEXPORT jstring JNICALL
//Java_com_lh_mediatest_FFmpegActivity_getAvcodecInfo(JNIEnv *env, jobject instance) {
//    char info[40000] = {0};
//    av_register_all();
//    AVCodec *c_temp = av_codec_next(NULL);
//    while (c_temp != NULL) {
//        if (c_temp->decode != NULL) {
//            sprintf(info, "%s[Dec]", info);
//        } else {
//            sprintf(info, "%s[Enc]", info);
//        }
//        switch (c_temp->type) {
//            case AVMEDIA_TYPE_VIDEO:
//                sprintf(info, "%s[Video]", info);
//                break;
//            case AVMEDIA_TYPE_AUDIO:
//                sprintf(info, "%s[Audio]", info);
//                break;
//            default:
//                sprintf(info, "%s[Other]", info);
//                break;
//        }
//        sprintf(info, "%s %10s\n", info, c_temp->name);
//        c_temp = c_temp->next;
//    }
//    return env->NewStringUTF(info);
//}
//
//JNIEXPORT jstring JNICALL
//Java_com_lh_mediatest_FFmpegActivity_getAvfilterInfo(JNIEnv *env, jobject instance) {
//    char info[40000] = {0};
//    av_register_all();
//    AVFilter *f_temp = (AVFilter *) avfilter_next(NULL);
//    while (f_temp != NULL) {
//        sprintf(info, "%s[%10s]\n", info, f_temp->name);
//    }
//    return env->NewStringUTF(info);
//}

}