#include <jni.h>
#include <string>
#include <android/log.h>

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "android/bitmap.h"
#include <fstream>

#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgcodecs/imgcodecs.hpp> // Cargar y manipular imágenes en distintos formatos gráficos
#include <opencv2/imgproc/imgproc.hpp> // Operaciones de procesamiento sobre imágenes
#include <opencv2/video/video.hpp> // Manejo de vídeo
#include <opencv2/videoio/videoio.hpp> // Lectura y escritura de vídeo

#include <opencv2/objdetect/objdetect.hpp>

using namespace std;
using namespace cv;

extern "C" JNIEXPORT jstring

JNICALL
Java_ups_logic_robbyapp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello robby 2.0 holandaaaa";
    return env->NewStringUTF(hello.c_str());
}

void bitmapToMat(JNIEnv * env, jobject bitmap, cv::Mat &dst, jboolean needUnPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void* pixels = 0;
    try {
        // Verificar información del bitmap
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 || info.format == ANDROID_BITMAP_FORMAT_RGB_565);

        // Bloquear píxeles
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);

        dst.create(info.height, info.width, CV_8UC4); // Crear matriz de destino

        // Procesar según formato
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (needUnPremultiplyAlpha) cvtColor(tmp, dst, cv::COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else { // RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, cv::COLOR_BGR5652RGBA);
        }

        // Liberar los píxeles
        AndroidBitmap_unlockPixels(env, bitmap);
    } catch (const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
    }
}

void matToBitmap(JNIEnv * env, cv::Mat src, jobject bitmap, jboolean needPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void*
            pixels = 0;
    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( src.dims == 2 && info.height == (uint32_t)src.rows && info.width ==
                                                                         (uint32_t)src.cols );
        CV_Assert( src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, cv::COLOR_GRAY2RGBA);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, cv::COLOR_RGB2RGBA);
            } else if(src.type() == CV_8UC4){
                if(needPremultiplyAlpha) cvtColor(src, tmp, cv::COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
// info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, cv::COLOR_GRAY2BGR565);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, cv::COLOR_RGB2BGR565);
            } else if(src.type() == CV_8UC4){
                cvtColor(src, tmp, cv::COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
//jclass je = env->FindClass("org/opencv/core/CvException");
        jclass je = env->FindClass("java/lang/Exception");
//if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

CascadeClassifier face_cascade;
extern "C"{
    JNICALL jstring Java_ups_logic_robbyapp_Juego5_procesarImagen(JNIEnv * env, jobject /**/, jobject bitmapIn, jobject bitmapOut){
        cv::Mat frame;

        bitmapToMat(env, bitmapIn, frame, false);  // bitmapToMat es una función personalizada

        cv::flip(frame, frame, 1);

        vector<Rect> body;
        face_cascade.detectMultiScale(frame, body);

        int mind = frame.cols/2;
        const char* lados = "No detecta";
        for(int i = 0; i < body.size(); i++){
            Rect face = body[i];
            rectangle(frame, body[i], cv::Scalar(255, 0, 0), 2);
            if(face.x + face.width/2 < mind){

                lados = "Izquierda";
            }else{
                lados = "Derecha";
            }
        }

        matToBitmap(env, frame, bitmapOut, false);
        return env->NewStringUTF(lados);
    }

    JNICALL void Java_ups_logic_robbyapp_Juego5_cargarxml(JNIEnv * env, jobject /**/, jstring ruta){
        const char * direccion = env->GetStringUTFChars(ruta, nullptr);

        face_cascade = CascadeClassifier(direccion);
    }

}

