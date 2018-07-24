

#ifndef VOICERECOGNIZER_PACKET_HANDLER_H
#define VOICERECOGNIZER_PACKET_HANDLER_H

#include "_kiss_fft_guts.h"
#include "kiss_fftr.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct packetHandler {
    float sampleHz;
    int nFFT;
    //kiss_fft_cpx *sigIn;
    kiss_fft_cpx *sigOut;
    kiss_fft_scalar *sigIn;
    kiss_fftr_cfg  kissFFTState;
}*packetHandler_t;


packetHandler_t packetHandlerInit(int nFFT, int hz);
void packetHandlerCalculate(packetHandler_t dFFT);
void packetHandlerClose(packetHandler_t dFFT);

#ifdef __cplusplus
}
#endif


#endif //VOICERECOGNIZER_PACKET_HANDLER_H

