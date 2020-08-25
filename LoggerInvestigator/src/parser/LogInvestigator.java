package parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LogInvestigator {

    public static final String MESSAGE_PREFIX = "The changing word was ";

    public static void main(String[] args) {

        LocalDateTime start = LocalDateTime.now();
        List<String> textsOriginal = new ArrayList<>();
        textsOriginal.add("01-01-2012 19:45:00 Naomi C. is getting into the car");
        textsOriginal.add("01-01-2012 20:12:39 Naomi is eating at a restaurant");
        textsOriginal.add("02-01-2012 09:13:15 George II is getting into the car");
        textsOriginal.add("02-01-2012 10:14:00 George is eating at a diner");
        textsOriginal.add("03-01-2012 10:14:00 Naomi is eating at a diner");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        List<String> texts  = new ArrayList<>(textsOriginal);

        if (!texts.isEmpty()) {

            AtomicInteger listIndex = new AtomicInteger(0);
            texts.forEach(text -> {
                try {
                    LocalDateTime.parse(text, formatter);
                } catch (DateTimeParseException exception) {
                    int index = exception.getErrorIndex();
                    text = text.substring(index).trim();
                    texts.set(listIndex.getAndIncrement(), text);
                }
            });
        }

        Map<String, String> occurencesMap = new HashMap();

        while (!texts.isEmpty() ) {
            AtomicReference<String> longest = new AtomicReference<>("");
            for (String textStr : texts) {

                longest.set("");
                texts.forEach(text -> {

                    if (!text.equals(textStr)) {
                        String sequence = findLCS(text, textStr);
                        if (sequence.length() > longest.get().length() && !Character.isUpperCase(sequence.charAt(0)))
                            longest.set(sequence);
                    }
                });
            }

            StringBuffer message = new StringBuffer(MESSAGE_PREFIX);
            texts.removeIf(text -> {
                if (text.contains(longest.get())) {
                    String[] words = text.split(longest.get());
                    message.append(words[0] + ", ");
                    return true;
                }
                return false;
            });

            occurencesMap.put(longest.get() , message.deleteCharAt(message.length()-2).toString());
        }
        occurencesMap.forEach((sequence, message) -> {
            int found = 0;
            int occ = message.split(", ").length;
            if (message.contains(", ")){ // more than one occurrence
                for (String original : textsOriginal) {
                    if (original.contains(sequence)){
                        System.out.println(original);
                        found++;
                        if (found == occ)
                            System.out.println(message);
                    }
                }
            }
        });


        System.out.println("Parsing took: " + ChronoUnit.MILLIS.between(start, LocalDateTime.now()) + " msec");

    }

    public static String findLCS(String str1, String str2) {
        int longest = 0;
        String longestSubstring = "";

        for (int i=0; i < str1.length(); ++i) {
            for (int j=i+1; j <= str1.length(); ++j) {
                String substring = str1.substring(i, j);
                if (str2.contains(substring) && substring.length() > longest) {
                    longest = substring.length();
                    longestSubstring = substring;
                }
            }
        }

        return longestSubstring;
    }
}
