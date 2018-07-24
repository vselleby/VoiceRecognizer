package com.selleby.voicerecognizer.simpleMatcher;


import java.util.HashSet;

public class SimpleMatcher {
    private int[][] binMatrix;


    public SimpleMatcher() {
        binMatrix = new int[10][10];
    }

    public void trainMatcher(int[][] binMatrix) {
        this.binMatrix = binMatrix;
    }

    public int predict(int[] binArray) {
        HashSet<Integer> map = new HashSet<>();
        int numberMatches = 0;
        for(int value : binArray) {
            map.add(value);
        }
        for(int[] bins : binMatrix) {
            for(int bin : bins) {
                if (map.contains(bin)) {
                    numberMatches++;
                }
            }
        }
        return numberMatches / binMatrix.length;
    }
}
