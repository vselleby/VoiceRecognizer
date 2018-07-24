package com.selleby.voicerecognizer.isolationForest;


import java.util.Random;

/**
 * Modified version of JeeremyJohn's Anomaly Detection implementation
 */


public class TreeNode {

    public int attrIndex;
    public double attrValue;
    public int leafNodes;
    public int curHeight;
    public TreeNode lTree, rTree;

    public TreeNode(int attrIndex, double attrValue) {
        this.curHeight = 0;
        this.lTree = null;
        this.rTree = null;
        this.leafNodes = 1;
        this.attrIndex = attrIndex;
        this.attrValue = attrValue;
    }


    public static TreeNode createITree(int[][] samples, int curHeight, int limitHeight) {

        TreeNode iTree;

        if (samples.length == 0) {
            return null;
        } else if (curHeight >= limitHeight || samples.length == 1) {
            iTree = new TreeNode(0, samples[0][0]);
            iTree.leafNodes = 1;
            iTree.curHeight = curHeight;
            return iTree;
        }

        int rows = samples.length;
        int cols = samples[0].length;

        boolean isAllSame = true;
        break_label:
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < cols; j++) {
                if (samples[i][j] != samples[i + 1][j]) {
                    isAllSame = false;
                    break break_label;
                }
            }
        }

        if (isAllSame) {
            iTree = new TreeNode(0, samples[0][0]);
            iTree.leafNodes = samples.length;
            iTree.curHeight = curHeight;
            return iTree;
        }


        Random random = new Random(System.currentTimeMillis());
        int attrIndex = random.nextInt(cols);

        double min, max;
        min = samples[0][attrIndex];
        max = min;
        for (int i = 1; i < rows; i++) {
            if (samples[i][attrIndex] < min) {
                min = samples[i][attrIndex];
            }
            if (samples[i][attrIndex] > max) {
                max = samples[i][attrIndex];
            }
        }

        double attrValue = random.nextDouble() * (max - min) + min;

        int lnodes = 0, rnodes = 0;
        double curValue;
        for (int[] sample : samples) {
            curValue = sample[attrIndex];
            if (curValue < attrValue) {
                lnodes++;
            } else {
                rnodes++;
            }
        }

        int[][] lSamples = new int[lnodes][cols];
        int[][] rSamples = new int[rnodes][cols];

        lnodes = 0;
        rnodes = 0;
        for (int[] sample : samples) {
            curValue = sample[attrIndex];
            if (curValue < attrValue) {
                lSamples[lnodes++] = sample;
            } else {
                rSamples[rnodes++] = sample;
            }
        }

        TreeNode parent = new TreeNode(attrIndex, attrValue);
        parent.leafNodes = rows;
        parent.curHeight = curHeight;
        parent.lTree = createITree(lSamples, curHeight + 1, limitHeight);
        parent.rTree = createITree(rSamples, curHeight + 1, limitHeight);

        return parent;
    }
}