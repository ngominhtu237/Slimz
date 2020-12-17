package com.samsung.lookup.fragment.stack;

import java.util.Stack;

/**
 * Created by tu.nm1 on 14,December,2020
 */
public class WordStack {
    public static Stack<String> stackOfWords = new Stack<>();

    public static void addToStack(String word) {
        if (!WordStack.stackOfWords.contains(word)) {
            WordStack.stackOfWords.add(word);
        }
    }
}
