
#include "packet_handler.h"

static void complexAbs(kiss_fft_cpx *sigOut, int n) {
    while (n-- > 0) {
        sigOut->r = sqrt(sigOut->r * sigOut->r + sigOut->i * sigOut->i);
        sigOut->i = 0;
        sigOut++;
    }
}


packetHandler_t packetHandlerInit(int nFFT, int hz) {
    packetHandler_t dFFT = NULL;
    dFFT = (packetHandler_t)malloc(sizeof(struct packetHandler));
    dFFT->sampleHz = hz;
    dFFT->nFFT = nFFT;
    dFFT->sigIn = (kiss_fft_scalar *)malloc(sizeof(kiss_fft_scalar) * dFFT->nFFT);
    dFFT->sigOut = (kiss_fft_cpx *)malloc(sizeof(kiss_fft_cpx) * dFFT->nFFT);
    dFFT->kissFFTState = kiss_fftr_alloc(nFFT,0,0,0);

    return dFFT;
}

void packetHandlerCalculate(packetHandler_t dFFT) {
    kiss_fftr(dFFT->kissFFTState, dFFT->sigIn, dFFT->sigOut);
    complexAbs(dFFT->sigOut, dFFT->nFFT);
}

void packetHandlerClose(packetHandler_t dFFT) {
    free(dFFT->sigIn);
    free(dFFT->sigOut);
    free(dFFT->kissFFTState);
    free(dFFT);

}



