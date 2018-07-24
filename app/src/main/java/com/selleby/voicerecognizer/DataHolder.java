package com.selleby.voicerecognizer;


class DataHolder {
    private static DataHolder dataHolder;
    private MLHandler mlHandler;
    private AudioHandler audioHandler;

    static DataHolder getInstance() {
        if(dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }

    private DataHolder() {}

    void setHandlers(AudioHandler audioHandler, MLHandler mlHandler) {
        this.audioHandler = audioHandler;
        this.mlHandler = mlHandler;
    }

    AudioHandler getAudioHandler() {
        return audioHandler;
    }

    MLHandler getMlHandler() {
        return mlHandler;
    }
}
