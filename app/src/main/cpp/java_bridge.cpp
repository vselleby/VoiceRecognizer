#include <jni.h>
#include <android/log.h>

#include "packet_handler.h"

struct mapper {
    float value;
    int index;
};

static int compare(const void *a, const void *b) {
    struct mapper *a1 = (struct mapper *)a;
    struct mapper *a2 = (struct mapper*)b;
    if((*a1).value>(*a2).value) {
        return -1;
    }
    else if((*a1).value<(*a2).value) {
        return 1;
    }
    else {
        return 0;
    }
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_selleby_voicerecognizer_AudioHandler_bufferRelay(JNIEnv
                                                          *env,
                                                          jobject instance,
                                                          jfloatArray buffer_,
                                                          jint numberSamples,
                                                          jint samplingRate)
{
    jfloat *buffer = (env)->GetFloatArrayElements(buffer_, NULL);

    __android_log_print(ANDROID_LOG_DEBUG, "BUFFER_RELAY", "Entering C-code numberSamples: %d", numberSamples);

    int samplesPerFFT = 4096;
    int handledSamples = 0;
    float *tempBuffer = (float *)malloc((sizeof(float)) * samplesPerFFT / 2);
    struct mapper *indexMapper = (struct mapper*)malloc((sizeof(mapper)) * samplesPerFFT / 2);
    int topBins[10];
    packetHandler_t packetHandler = NULL;
    packetHandler = packetHandlerInit(samplesPerFFT, samplingRate);
    __android_log_print(ANDROID_LOG_DEBUG, "BUFFER_RELAY", "After packetHandlerInit");

    int divisor = numberSamples / samplesPerFFT /*+ (numberSamples % samplesPerFFT != 0)*/; //Ceil of division
    if(divisor == 0) {
        divisor = 1;
    }
    while(handledSamples < samplesPerFFT * divisor) {
        for(int i = 0; i < samplesPerFFT; i++) {
            if(handledSamples + i < numberSamples) {
                packetHandler->sigIn[i] = buffer[handledSamples + i];
            }
            else {
                packetHandler->sigIn[i] = 0;
            }
        }
        handledSamples += samplesPerFFT;
        packetHandlerCalculate(packetHandler);
        for(int i = 0; i < samplesPerFFT / 2; i++) {
            tempBuffer[i] += packetHandler->sigOut[i].r;
        }
    }

    for(int i = 0; i < samplesPerFFT / 2; i++) {
        tempBuffer[i] /= divisor;
        indexMapper[i].index = i;
        indexMapper[i].value = tempBuffer[i];
    }
    qsort(indexMapper, (size_t) samplesPerFFT / 2, sizeof(indexMapper[0]), compare);
    for(int i = 0; i < 10; i++) {
        topBins[i] = indexMapper[i].index;
        __android_log_print(ANDROID_LOG_DEBUG, "BUFFER_RELAY", "Bin: %d", topBins[i]);
    }

    jintArray jArray;
    jArray = (env)->NewIntArray(10);
    (env)->SetIntArrayRegion(jArray, 0, 10, topBins);
    (env)->ReleaseFloatArrayElements(buffer_, buffer, 0);
    packetHandlerClose(packetHandler);
    free(indexMapper);
    free(tempBuffer);
    return jArray;
}