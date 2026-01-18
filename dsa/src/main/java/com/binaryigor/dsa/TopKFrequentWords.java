package com.binaryigor.dsa;

import java.util.*;

public class TopKFrequentWords {

    static List<String> topKFrequent(String[] words, int k) {
        var wordsToFrequencies = new HashMap<String, Integer>();
        for (var w : words) {
            wordsToFrequencies.merge(w, 1, Integer::sum);
        }

        var frequenciesToWords = new TreeMap<Integer, List<String>>(Comparator.reverseOrder());
        wordsToFrequencies.forEach((word, freq) -> {
            frequenciesToWords.computeIfAbsent(freq, $ -> new ArrayList<>()).add(word);
            if (frequenciesToWords.size() > k) {
                frequenciesToWords.pollLastEntry();
            }
        });

        var topKFrequent = new ArrayList<String>();
        while (topKFrequent.size() < k) {
            var nextWords = frequenciesToWords.pollFirstEntry().getValue();
            var sortedWordsOfFrequency = nextWords.stream().sorted().toList();
            for (var sw : sortedWordsOfFrequency) {
                if (topKFrequent.size() == k) {
                    break;
                }
                topKFrequent.add(sw);
            }
        }

        return topKFrequent;
    }

    record Case(String[] words, int k, List<String> expected) {

        static List<Case> cases() {
            return List.of(
                    new Case(new String[]{"i", "love", "leetcode", "i", "love", "coding"}, 2,
                            List.of("i", "love")),
                    new Case(new String[]{"the", "day", "is", "sunny", "the", "the", "the", "sunny", "is", "is"}, 4,
                            List.of("the", "is", "sunny", "day")),
                    new Case(new String[]{"i", "love", "leetcode", "i", "love", "coding"}, 3,
                            List.of("i", "love", "coding"))
            );
        }
    }
}
