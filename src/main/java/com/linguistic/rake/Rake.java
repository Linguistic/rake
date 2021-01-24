package com.linguistic.rake;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Rapid Automatic Keyword Extraction (RAKE)
 * =========================================
 *
 * Rose, Stuart & Engel, Dave & Cramer, Nick & Cowley, Wendy. (2010).
 * Automatic Keyword Extraction from Individual Documents.
 * Text Mining: Applications and Theory. 1 - 20. 10.1002/9780470689646.ch1.
 *
 * Implementation based on https://github.com/aneesha/RAKE
 */
public class Rake {
    String language;
    String stopWordsPattern;

    public Rake(String language) {
        this.language = language;

        // Read the stop words file for the given language
        InputStream stream = this.getClass().getResourceAsStream("/data/stopwords/stopwords/stopwords-0.1/languages/" + language + ".txt");
        String line;

        if (stream != null) {
            try {
                ArrayList<String> stopWords = new ArrayList<>();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

                // Loop through each stop word and add it to the list
                while ((line = bufferedReader.readLine()) != null)
                    stopWords.add(line.trim());

                ArrayList<String> regexList = new ArrayList<>();

                // Turn the stop words into an array of regex
                for (String word : stopWords) {
                    String regex = "\\b" + word + "(?![\\w-])";
                    regexList.add(regex);
                }

                // Join all regexes into global pattern
                this.stopWordsPattern = String.join("|", regexList);
            } catch (Exception e) {
                throw new Error("An error occurred reading stop words for language " + language);
            }
        } else throw new Error("Could not find stop words required for language " + language);

    }

    /**
     * Returns a list of all sentences in a given string of text
     *
     * @param text
     * @return String[]
     */
    private String[] getSentences(String text) {
        return text.split("[.!?,;:\\t\\\\\\\\\"\\\\(\\\\)\\\\'\\u2019\\u2013]|\\\\s\\\\-\\\\s");
    }

    /**
     * Returns a list of all words that are have a length greater than a specified number of characters
     *
     * @param text given text
     * @param size minimum size
     */
    private String[] separateWords(String text, int size) {
        String[] split = text.split("[^a-zA-Z0-9_\\\\+/-\\\\]");
        ArrayList<String> words = new ArrayList<>();

        for (String word : split) {
            String current = word.trim().toLowerCase();
            int len = current.length();

            if (len > size && len > 0 && !StringUtils.isNumeric(current))
                words.add(current);
        }

        return words.toArray(new String[words.size()]);
    }

    /**
     * Generates a list of keywords by splitting sentences by their stop words
     *
     * @param sentences
     * @return
     */
    private String[] getKeywords(String[] sentences) {
        ArrayList<String> phraseList = new ArrayList<>();

        for (String sentence : sentences) {
            String temp = sentence.trim().replaceAll(this.stopWordsPattern, "|");
            String[] phrases = temp.split("\\|");

            for (String phrase : phrases) {
                phrase = phrase.trim().toLowerCase();

                if (phrase.length() > 0)
                    phraseList.add(phrase);
            }
        }

        return phraseList.toArray(new String[phraseList.size()]);
    }

    /**
     * Calculates word scores for each word in a collection of phrases
     * <p>
     * Scores is calculated by dividing the word degree (collective length of phrases the word appears in)
     * by the number of times the word appears
     *
     * @param phrases
     * @return
     */
    private LinkedHashMap<String, Double> calculateWordScores(String[] phrases) {
        LinkedHashMap<String, Integer> wordFrequencies = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> wordDegrees = new LinkedHashMap<>();
        LinkedHashMap<String, Double> wordScores = new LinkedHashMap<>();

        for (String phrase : phrases) {
            String[] words = this.separateWords(phrase, 0);
            int length = words.length;
            int degree = length - 1;

            for (String word : words) {
                wordFrequencies.put(word, wordDegrees.getOrDefault(word, 0) + 1);
                wordDegrees.put(word, wordFrequencies.getOrDefault(word, 0) + degree);
            }
        }

        for (String item : wordFrequencies.keySet()) {
            wordDegrees.put(item, wordDegrees.get(item) + wordFrequencies.get(item));
            wordScores.put(item, wordDegrees.get(item) / (wordFrequencies.get(item) * 1.0));
        }

        return wordScores;
    }

    /**
     * Returns a list of keyword candidates and their respective word scores
     *
     * @param phrases
     * @param wordScores
     * @return
     */
    private LinkedHashMap<String, Double> getCandidateKeywordScores(String[] phrases, LinkedHashMap<String, Double> wordScores) {
        LinkedHashMap<String, Double> keywordCandidates = new LinkedHashMap<>();

        for (String phrase : phrases) {
            double score = 0.0;

            String[] words = this.separateWords(phrase, 0);

            for (String word : words) {
                score += wordScores.get(word);
            }

            keywordCandidates.put(phrase, score);
        }

        return keywordCandidates;
    }

    /**
     * Sorts a LinkedHashMap by value from lowest to highest
     *
     * @param map
     * @return
     */
    private LinkedHashMap<String, Double> sortHashMap(LinkedHashMap<String, Double> map) {
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        List<Map.Entry<String, Double>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(list);

        for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Double> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Extracts keywords from the given text body using the RAKE algorithm
     *
     * @param text
     */
    public LinkedHashMap<String, Double> getKeywordsFromText(String text) {
        String[] sentences = this.getSentences(text);
        String[] keywords = this.getKeywords(sentences);

        LinkedHashMap<String, Double> wordScores = this.calculateWordScores(keywords);
        LinkedHashMap<String, Double> keywordCandidates = this.getCandidateKeywordScores(keywords, wordScores);

        return this.sortHashMap(keywordCandidates);
    }

}
