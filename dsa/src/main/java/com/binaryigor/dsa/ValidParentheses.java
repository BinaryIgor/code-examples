package com.binaryigor.dsa;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ValidParentheses {

    private static final Map<Character, Character> OPENING_TO_CLOSING = Map.of(
            '(', ')',
            '{', '}',
            '[', ']'
    );

    static boolean isValid(String s) {
        if (s.length() < 2) {
            return false;
        }

        var opening = new LinkedList<Character>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (OPENING_TO_CLOSING.containsKey(c)) {
                opening.add(c);
            } else {
                var lastOpening = opening.pollLast();
                if (lastOpening == null || !OPENING_TO_CLOSING.get(lastOpening).equals(c)) {
                    return false;
                }
            }
        }

        return opening.isEmpty();
    }

    record Case(String string, boolean isValid) {

        public static List<Case> cases() {
            return List.of(
                    new Case("()", true),
                    new Case("()[]{}", true),
                    new Case("()", true),
                    new Case("([)]", false)
            );
        }
    }
}
