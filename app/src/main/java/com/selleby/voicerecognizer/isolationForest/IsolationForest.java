package com.selleby.voicerecognizer.isolationForest;



import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Modified version of JeeremyJohn's Anomaly Detection implementation
 */

public class IsolationForest {

    private Double center0;
    private Double center1;

    private int subSampleSize;

    private List<TreeNode> iTreeList;


    public IsolationForest() {
        this.center0 = null;
        this.center1 = null;
        this.subSampleSize = 256;
        this.iTreeList = new ArrayList<>();
    }


    public int[] train(int[][] samples, int t) throws Exception {
        return train(samples, t, 256,100);
    }

    public int[] train(int[][] samples, int t, int subSampleSize, int iters) throws Exception {

        this.subSampleSize = subSampleSize;
        if (this.subSampleSize > samples.length) {
            this.subSampleSize = samples.length;
        }

        createIForest(samples, t);

        double[] scores = computeAnomalyIndex(samples);

        return classifyByCluster(scores, iters);
    }


    public int predict(int[] sample) throws Exception {
        double score = computeAnomalyIndex(sample);
        double dis0 = Math.abs(score - center0);
        double dis1 = Math.abs(score - center1);

        if (dis0 > dis1) {
            return 1;
        } else {
            return 0;
        }
    }


    private int[] classifyByCluster(double[] scores, int iters) {

        center0 = scores[0];
        center1 = scores[0];

        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > center0) {
                center0 = scores[i];
            }

            if (scores[i] < center1) {
                center1 = scores[i];
            }
        }

        int cnt0, cnt1;
        double diff0, diff1;
        int[] labels = new int[scores.length];

        for (int n = 0; n < iters; n++) {
            cnt0 = 0;
            cnt1 = 0;

            for (int i = 0; i < scores.length; i++) {
                diff0 = Math.abs(scores[i] - center0);
                diff1 = Math.abs(scores[i] - center1);

                if (diff0 < diff1) {
                    labels[i] = 0;
                    cnt0++;
                } else {
                    labels[i] = 1;
                    cnt1++;
                }
            }

            diff0 = center0;
            diff1 = center1;

            center0 = 0.0;
            center1 = 0.0;
            for (int i = 0; i < scores.length; i++) {
                if (labels[i] == 0) {
                    center0 += scores[i];
                } else {
                    center1 += scores[i];
                }
            }

            center0 /= cnt0;
            center1 /= cnt1;

            if (center0 - diff0 <= 1e-6 && center1 - diff1 <= 1e-6) {
                break;
            }
        }
        return labels;
    }


    private void createIForest(int[][] samples, int t) throws Exception {

        if (samples == null || samples.length == 0) {
            throw new Exception("Samples is null or empty, please check...");
        } else if (t <= 0) {
            throw new Exception("Number of subtree t must be a positive...");
        } else if (subSampleSize <= 0) {
            throw new Exception("subSampleSize must be a positive...");
        }

        int limitHeight = (int) Math.ceil(Math.log(subSampleSize) / Math.log(2));

        TreeNode iTree;
        int[][] subSample;

        for (int i = 0; i < t; i++) {
            subSample = this.getSubSamples(samples, subSampleSize);
            iTree = TreeNode.createITree(subSample, 0, limitHeight);
            this.iTreeList.add(iTree);
        }
    }


    private double[] computeAnomalyIndex(int[][] samples) throws Exception {

        if (samples == null || samples.length == 0) {
            throw new Exception("Samples is null or empty, please check...");
        }

        double[] scores = new double[samples.length];
        for (int i = 0; i < samples.length; i++) {
            scores[i] = computeAnomalyIndex(samples[i]);
        }

        return scores;
    }


    private double computeAnomalyIndex(int[] sample) throws Exception {

        if (iTreeList == null || iTreeList.size() == 0) {
            throw new Exception("iTreeList is empty，please create IForest...");
        } else if (sample == null || sample.length == 0) {
            throw new Exception("Sample is null or empty, please check...");
        }

        double ehx = 0;
        double pathLength;
        for (TreeNode iTree : iTreeList) {
            pathLength = computePathLength(sample, iTree);
            ehx += pathLength;
        }
        ehx /= iTreeList.size();

        double cn = computeCn(subSampleSize);
        double index = ehx / cn;

        return Math.pow(2, -index);
    }


    private double computePathLength(int[] sample, final TreeNode iTree) throws Exception {

        if (sample == null || sample.length == 0) {
            throw new Exception("Sample is null or empty, please check...");
        } else if (iTree == null || iTree.leafNodes == 0) {
            throw new Exception("iTree is null or empty, please check...");
        }

        double pathLength = -1;
        double attrValue;
        TreeNode tmpITree = iTree;

        while (true) {
            pathLength += 1;
            attrValue = sample[tmpITree.attrIndex];

            if (tmpITree.lTree == null || tmpITree.rTree == null || attrValue == tmpITree.attrValue) {
                break;
            } else if (attrValue < tmpITree.attrValue) {
                tmpITree = tmpITree.lTree;
            } else {
                tmpITree = tmpITree.rTree;
            }
        }

        return pathLength + computeCn(tmpITree.leafNodes);
    }


    private int[][] getSubSamples(int[][] samples, int sampleNum) throws Exception {

        if (samples == null || samples.length == 0) {
            throw new Exception("Samples is null or empty, please check...");
        } else if (sampleNum <= 0) {
            throw new Exception("Number of sampleNum must be a positive...");
        }

        if (samples.length < sampleNum) {
            sampleNum = samples.length;
        }
        int cols = samples[0].length;
        int[][] subSamples = new int[sampleNum][cols];

        int randomIndex;
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < sampleNum; i++) {
            randomIndex = random.nextInt(samples.length);
            subSamples[i] = samples[randomIndex];
        }

        return subSamples;
    }

    private double computeCn(double n) {
        if (n <= 1) {
            return 0;
        }
        return 2 * (Math.log(n - 1) + 0.5772156649) - 2 * ((n - 1) / n);
    }
}
