package com.hansight.kunlun.analysis.utils.v2;

import java.util.HashMap;
import java.util.Map;

public class DocumentVector {
    Map<Long, Integer> wordMap = new HashMap<Long, Integer>();

    public int count() {
        return wordMap.size();
    }

    public void incCount(String word) {
        incCount(HashUtil.hash(word));
    }

    public void incCount(long word) {
        if (norm_ != 0) throw new IllegalStateException("No changes allowed");

        Integer oldCount = wordMap.get(word);
        wordMap.put(word, oldCount == null ? 1 : oldCount + 1);
    }

    double getCosineSimilarityWith(DocumentVector otherVector) {
        double innerProduct = 0;
        for(Long w: this.wordMap.keySet()) {
            innerProduct += this.getCount(w) * otherVector.getCount(w);
        }
        return innerProduct / (this.getNorm() * otherVector.getNorm());
    }

    private double norm_ = 0;

    double getNorm() {
        if (norm_ == 0) {
            double sum = 0;
            for (Integer count : wordMap.values()) {
                sum += count * count;
            }
            norm_ = Math.sqrt(sum);
        }
        return norm_;
    }

    int getCount(String word) {
        return getCount(HashUtil.hash(word));
    }

    int getCount(long word) {
        return wordMap.containsKey(word) ? wordMap.get(word) : 0;
    }

    public static void main(String[] args) {
        String doc1 = "A B C A A B C. D D E A B. D A B C B A.";
        String doc2 = "A B C A A B C. D A B C B A.";

        DocumentVector v1 = new DocumentVector();
        for(String w:doc1.split("[^a-zA-Z]+")) {
            v1.incCount(w);
        }

        DocumentVector v2 = new DocumentVector();
        for(String w:doc2.split("[^a-zA-Z]+")) {
            v2.incCount(w);
        }

        System.out.println("Similarity = " + v1.getCosineSimilarityWith(v2));
    }

}