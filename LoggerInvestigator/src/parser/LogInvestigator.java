package parser;

import java.io.*;
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

        List<String> textsOriginal = readFromLog();

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
        Set<String> sequences = new HashSet<>();
        boolean isChanged = false;

        do {
            for (String textStr : texts) {
                isChanged = false;
                AtomicReference<String> longest = new AtomicReference<>("");
                texts.forEach(text -> {

                    if (!text.equals(textStr)) {
                        String sequence = findLCS(text, textStr);
                        if (sequence.length() > longest.get().length() && !Character.isUpperCase(sequence.charAt(0)) && textStr.endsWith(sequence))
                            longest.set(sequence);
                    }
                });

                if (!sequences.contains(longest.get()) && !longest.get().isEmpty()) {
                    sequences.add(longest.get());
                    isChanged = true;
                }
            }
        }
        while (isChanged);

        for (String sequence : sequences) {
            StringBuffer message = new StringBuffer(MESSAGE_PREFIX);
            texts.removeIf(text -> {
                if (text.contains(sequence)) {
                    String[] words = text.split(sequence);
                    message.append(words[0] + ", ");
                    return true;
                }
                return false;
            });

            occurencesMap.put(sequence , message.deleteCharAt(message.length()-2).toString());
        }

        occurencesMap.forEach((sequence, message) -> {
            int found = 0;
            int occ = message.split(", ").length;
            if (message.contains(", ")){ // more than one occurrence
                for (String original : textsOriginal) {
                    if (original.contains(sequence)){
                        System.out.println(original);
                        found++;
                        if (found == occ) {
                            System.out.println(message);
                            System.out.println("===============================");
                        }
                    }
                }
            }
        });


        System.out.println("Parsing took: " + ChronoUnit.MILLIS.between(start, LocalDateTime.now()) + " msec");

    }

    private static List<String>  readFromLog() {
        List<String> logs = new ArrayList<>();
        InputStream inputStream = null;
        try {
            File file = new File(LogInvestigator.class.getClassLoader().getResource("spyLog.txt").getFile());
            inputStream = new FileInputStream(file);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            while (line != null) {
                logs.add(line);
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return logs;
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
