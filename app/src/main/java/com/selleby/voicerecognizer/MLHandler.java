package com.selleby.voicerecognizer;

import android.util.Log;

import com.selleby.voicerecognizer.isolationForest.IsolationForest;
import com.selleby.voicerecognizer.simpleMatcher.SimpleMatcher;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class MLHandler implements Serializable {
    private Map<String, IsolationForest> matchMap;
    //private Map<String, SimpleMatcher> matchMap;


    MLHandler() {
        matchMap = new HashMap<>();
    }

    void trainNewModel(String userName, int[][] binValues) throws Exception {
        Log.d("SIMPLE_MATCHER", "'Training' model");

        IsolationForest isolationForest = new IsolationForest();
        isolationForest.train(binValues, 100);
        matchMap.put(userName, isolationForest);
        /*
        SimpleMatcher matcher = new SimpleMatcher();
        matcher.trainMatcher(binValues);
        matchMap.put(userName, matcher);
        */
    }

    String[] evaluate(int[] evalBins) throws Exception {
        Log.d("SIMPLE_MATCHER", "Evaluating audio");
        String[] validUsers = new String[matchMap.size()];
        int nbrValid = 0;
        for(IsolationForest matcher : matchMap.values()) {
            int result = matcher.predict(evalBins);
            Log.d("SIMPLE_MATCHER", "Result: " + result);
            if(result > 4) {
                String key = getKeyFromValue(matcher);
                if(key != null) {
                    validUsers[nbrValid++] = key;
                }
            }
        }
        return validUsers;
    }

    private String getKeyFromValue(IsolationForest isolationForest) {
        for(Map.Entry<String, IsolationForest> entry : matchMap.entrySet()) {
            if (Objects.equals(isolationForest, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}

