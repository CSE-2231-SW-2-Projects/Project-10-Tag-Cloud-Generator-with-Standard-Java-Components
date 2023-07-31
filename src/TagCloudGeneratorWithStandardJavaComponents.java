import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @note I am doing Additional Activities 2 and so the program will be
 *       "case-insensitive". <pre>
 *
 * Tag Cloud Generator project: A program that counts word occurrences in a user
 * given input file and outputs an HTML file with a tag cloud of the words.
 *
 *</pre>
 * @author Zheyuan Gao
 * @author Cedric Fausey
 */
public final class TagCloudGeneratorWithStandardJavaComponents {

    /**
     * Default constructor--private to prevent instantiation.
     */
    private TagCloudGeneratorWithStandardJavaComponents() {
        // no code needed here
    }

    /**
     * Override the comparator to compare the alphabet of keys of two pairs in a
     * map.
     *
     * @ensure return negative number if first pair's key is in front of the
     *         second pair's key first character in alphabet order. 0 if
     *         pair1.equals(pair2). Positive number otherwise.(Ignore cases).
     */
    public static class KeyLT
            implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public final int compare(Entry<String, Integer> entry1,
                Entry<String, Integer> entry2) {
            int compare;
            if (entry1.equals(entry2)) {
                //(pair1.compareTo(pair2) == 0) == pair1.equals(pair2)
                compare = 0;
            } else {
                compare = entry1.getKey().compareTo(entry2.getKey());
                if (compare == 0) {
                    /*
                     * If only the key of two pairs are equal, we cannot just
                     * return 0 since they may not have the same value. It does
                     * not matter which pair goes first since they have the same
                     * key. Return the result of comparing two pairs' values.
                     */
                    compare = entry1.getValue().compareTo(entry2.getValue());
                }
            }
            return compare;
        }

    }

    /**
     * Override the comparator to compare the values of two pairs in a map.
     *
     * @ensure return positive number if first pair's value is less than the
     *         second pair's value. 0 if pair1.equals(pair2). negative number
     *         otherwise. The order is decreasing order.
     */
    public static class ValueLT
            implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public final int compare(Entry<String, Integer> entry1,
                Entry<String, Integer> entry2) {
            int compare;
            if (entry1.equals(entry2)) {
                //(pair1.compareTo(pair2) == 0) == pair1.equals(pair2)
                compare = 0;
            } else {
                compare = entry2.getValue().compareTo(entry1.getValue());
                if (compare == 0) {
                    /*
                     * If only the value of two pairs are equal, we cannot just
                     * return 0 since they may not have the same key. It does
                     * not matter which pair goes first since they have the same
                     * value. Return the result of comparing two pairs' keys.
                     */
                    compare = entry2.getKey().compareTo(entry1.getKey());
                }
            }
            return compare;
        }

    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    public static String nextWordOrSeparator(String text, int position,
            String separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        char c = text.charAt(position);
        boolean isSeparator = separators.indexOf(c) >= 0;

        StringBuilder subString = new StringBuilder();
        int endIndex = position;
        boolean end = false;
        /*
         * if the character is not a separator, then return the substring start
         * from position to the appearance of the next separator
         */
        if (!isSeparator) {
            while (!end && endIndex < text.length()) {

                if (!(separators.indexOf(text.charAt(endIndex)) >= 0)) {
                    subString.append(text.charAt(endIndex));
                } else {
                    end = true;
                }
                endIndex++;

            }

        } else {
            /*
             * if the character is a separator, then return the substring start
             * at position and end at the first appearance of the character that
             * not in the separator set.
             */
            while (!end && endIndex < text.length()) {

                if (separators.indexOf(text.charAt(endIndex)) >= 0) {
                    subString.append(text.charAt(endIndex));
                } else {
                    end = true;
                }
                endIndex++;
            }

        }
        return subString.toString();
    }

    /**
     * Read the input file and store all the words and the time of appearance in
     * a map.
     *
     * @param input
     *            Simple Reader to read the user input file
     * @param wordCount
     *            Map to store the words and the number of appearance in the
     *            give file
     * @param separator
     *            set that contains all the separator characters
     * @require [Input is open. The user file exists]
     * @ensure [The map will contains the words in the file and the
     *         corresponding times it appears]
     */
    public static void readFileConvertToMap(BufferedReader input,
            Map<String, Integer> wordCount, String separator) {
        /*
         * Reading from file.
         */
        try {
            String nextLine = input.readLine();
            while (nextLine != null) {

                /*
                 * Introduce the integer value startPosition to keep track the
                 * process of recording the words in the sentence to the map
                 */
                int startPosition = 0;
                /*
                 * We process can only proceed when the start position is in the
                 * scope of the given sentence
                 */
                while (startPosition < nextLine.length()) {
                    String token = nextWordOrSeparator(nextLine, startPosition,
                            separator);
                    /*
                     * If the token is a word
                     */
                    if (!(separator.indexOf(token.charAt(0)) >= 0)) {
                        //Convert the word to lower case.
                        token = token.toLowerCase();
                        /*
                         * if the map does not have this word yet, add it to the
                         * map.
                         */
                        if (!wordCount.containsKey(token)) {
                            wordCount.put(token, 1);

                        } else {
                            /*
                             * if the word has already exist, add one to its
                             * corresponding value in map
                             */
                            wordCount.put(token, wordCount.get(token) + 1);
                        }
                    }
                    /*
                     * Update the start position and contain to check next token
                     */
                    startPosition += token.length();
                }
                nextLine = input.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading from file");
        }

    }

    /**
     * Arrange all the keys in the map into the ValueLT sorting machine. And
     * then put first n elements into KeyLT sorting machine. Return the KeyLT
     * sorting machine in extraction mode.
     *
     * @param wordCount
     *            The map to store the word and corresponding numbers of
     *            appearance in the content
     * @param n
     *            The number of words I need to generate the tag cloud.
     * @param keySort
     *            The sorting machine to sort the words in alphabetical order.
     * @require [n >= 0, and n <= wordCount.size]
     * @ensure [Sorting machine that contains n most appearance words in KeyLT
     *         order and in Extraction Mode]
     */
    public static void putWordsInSortedList(Map<String, Integer> wordCount,
            int n, List<Map.Entry<String, Integer>> keySort) {
        assert n >= 0 : "violation of n >= 0.";
        assert n <= wordCount.size() : "violation of n <= wordCount.size.";

        /*
         * First put all the entries in the wordCount into a list to sort the
         * values from large to small.
         */
        List<Map.Entry<String, Integer>> valueSort = new ArrayList<>();
        Set<Map.Entry<String, Integer>> entrySet = wordCount.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet) {
            valueSort.add(entry);
        }
        //Sort the list.
        valueSort.sort(new ValueLT());

        /*
         * Arrange the first n pairs in the valueSort in to another List sorted
         * in alphabetical order.
         */
        int count = 0;
        while (count < n) {
            keySort.add(valueSort.remove(0));
            count++;
        }
        keySort.sort(new KeyLT());

    }

    /**
     * Generate the corresponding HTML file to the user choice location.
     *
     * @param location
     *            The location of user given writing file location.
     * @param file
     *            The location of the user given text.
     * @param keySort
     *            The sorting machine contains the pairs' key in alphabetical
     *            order.
     * @require [The output is open.]
     * @ensure [Output the file to same location as the user input file. The
     *         file contains the top appearance of first n numbers of words' Tag
     *         Cloud in HTML format.]
     */
    public static void outputHTML(String location, String file,
            List<Map.Entry<String, Integer>> keySort) {

        //Declare the minimum and max display font size for tag cloud.
        final int minSize = 11;
        final int maxSize = 48;

        /*
         * Open output stream.
         */
        PrintWriter output;
        try {
            output = new PrintWriter(
                    new BufferedWriter(new FileWriter(location)));
        } catch (IOException e) {
            System.err.println("Error opening file output stream");
            return;
        }

        /*
         * output the header of the file
         */
        output.println("<html>");
        output.println(" <head>");
        output.println("  <title>Top " + keySort.size() + " words in " + file
                + "</title>");
        output.print(
                "  <link href=\"http://web.cse.ohio-state.edu/software/2231/web-"
                        + "sw2/assignments/projects/tag-cloud-generator/data/"
                        + "tagcloud.css\"");
        output.println("rel=\"stylesheet\" type=\"text/css\">");
        output.println(" </head>");
        output.println(" <body>");
        output.println(
                "  <h2>Top " + keySort.size() + " words in " + file + "</h2>");
        output.println("  <hr>");
        output.println("   <div class=\"cdiv\">");
        output.println("    <p class=\"cbox\">");

        /*
         * output the body of the file
         */
        /*
         * First we need to find max count and minimum count in order to
         * calculate the display font size.
         */
        int maxCount = 0;
        int minCount = 0;
        for (Map.Entry<String, Integer> entry : keySort) {
            /*
             * Initiate the minCount to the first entry's value for first time
             * of the loop only.
             */
            if (minCount == 0) {
                minCount = entry.getValue();
            }
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
            } else if (entry.getValue() < minCount) {
                minCount = entry.getValue();
            }
        }

        while (keySort.size() > 0) {
            Map.Entry<String, Integer> wordCount = keySort.remove(0);
            //Calculate the display size for this word.
            double ratio = (double) (wordCount.getValue() - minCount)
                    / (maxCount - minCount);
            int size = minSize + (int) (ratio * (maxSize - minSize));
            //Output the corresponding HTML format.
            output.println("     <span style=\"cursor:default\" class=\"f"
                    + size + "\" title=\"count: " + wordCount.getValue() + "\">"
                    + wordCount.getKey() + "</span>");

        }
        /*
         * output the footer of the file
         */
        output.println("    </p>");
        output.println("   </div>");
        output.println(" </body>");
        output.println("</html>");

        /*
         * Close the stream.
         */
        output.close();

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        /*
         * Generate a String of separators
         */
        String separators = "\n\t\r,- .?!:;/'\"[]()*_~";

        /*
         * Open keyboard input streams
         */
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));

        /*
         * Ask user for file location and output location
         */
        System.out.print("Please input the name of the input file: ");
        String file = "";
        try {
            file = in.readLine();
        } catch (IOException e) {
            System.err.println("Error reading user input");
        }
        System.out.print("Please enter the name of the output file: ");
        String location = "";
        try {
            location = in.readLine();
        } catch (IOException e) {
            System.err.println("Error reading user input");
        }

        /*
         * Open file input streams.
         */
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            System.err.println("Error: invalid input file.");
            return;
        }

        /*
         * Store the text data in the map.
         */
        Map<String, Integer> wordCount = new HashMap<String, Integer>();
        readFileConvertToMap(input, wordCount, separators);

        /*
         * Ask user for number n. n has to be an positive integer and n cannot
         * be larger than the size of word map.
         */

        String userInput;
        int n = 0;
        boolean valid = false;
        while (!valid) {
            System.out.print("Enter the number of words (in the range of 0 to "
                    + wordCount.size()
                    + ") you would like to have in your tag cloud: ");
            try {
                userInput = in.readLine();
                n = Integer.parseInt(userInput);
            } catch (IOException e) {
                //Can not read the user input.
                System.err.println("Error reading user input.");
            } catch (NumberFormatException ex) {
                //User input is not a number.
                System.err.println("Error: Please enter an integer!");
            }
            if (n < 0 || n > wordCount.size()) {
                //User input in an invalid integer.
                System.out.println(
                        "Please enter a valid integer! n has to be an positive "
                                + "integer and n cannot be larger than "
                                + wordCount.size());
            } else {
                //User input is valid integer.
                valid = true;
            }

        }

        /*
         * Generate the HTML file to user choose location
         */
        List<Map.Entry<String, Integer>> keySort = new ArrayList<>();
        putWordsInSortedList(wordCount, n, keySort);
        outputHTML(location, file, keySort);

        /*
         * Close the input and output streams
         */
        try {
            in.close();
            input.close();
        } catch (IOException e) {
            System.err.println("Error closing streams");
        }
    }

}
