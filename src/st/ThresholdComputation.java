package st; /**
 * Created by Ioana on 4/20/2016.
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

//TODO: check consistency of week numbering: starting with 0 or with 1
public class ThresholdComputation {

    static HashMap<Integer, ArrayList<Integer>> weeklySessionsPerWeek;
    static HashMap<Integer, ArrayList<Integer>> weeklySessionLength;
    static HashMap<Integer, ArrayList<Integer>> weeklyBetweenSessions;
    static HashMap<Integer, ArrayList<Integer>> weeklyForumSessions;
    static HashMap<Integer, ArrayList<Integer>> weeklyAssignments;
    static HashMap<Integer, ArrayList<Integer>> weeklyUntilDeadline;


    public static void computeThresholds() throws IOException, ParseException {

        int cutOffPercent = 10;

        initialize();
        readMetrics("data\\st\\2015\\output\\ST2015_metrics_");

        writeThresholds("data\\st\\thresholds\\thresholds" + cutOffPercent + ".csv", cutOffPercent);
        writeMaximums("data\\st\\thresholds\\maximum" + cutOffPercent + ".csv", cutOffPercent);
        writeScaledThresholds("data\\st\\thresholds\\scaled_thresholds" + cutOffPercent + ".csv", cutOffPercent);
    }

    //************************
    //************ Loading data
    private static void initialize() {
        weeklySessionsPerWeek = new HashMap<>();
        weeklySessionLength = new HashMap<>();
        weeklyBetweenSessions = new HashMap<>();
        weeklyForumSessions = new HashMap<>();
        weeklyAssignments = new HashMap<>();
        weeklyUntilDeadline = new HashMap<>();

        for (int i = 1; i < 11; i++) {
            weeklySessionsPerWeek.put(i, new ArrayList<>());
            weeklySessionLength.put(i, new ArrayList<>());
            weeklyBetweenSessions.put(i, new ArrayList<>());
            weeklyForumSessions.put(i, new ArrayList<>());
            weeklyAssignments.put(i, new ArrayList<>());
            weeklyUntilDeadline.put(i, new ArrayList<>());
        }

    }

    private static void readMetrics(String filepath) throws IOException {
        for (int i = 1; i < 11; i++)
            readMetricsPerWeek(i, filepath + i + ".csv");
    }

    private static void readMetricsPerWeek(int week, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine;

        int sessionsPerWeek, sessionLength, betweenSessions, forumSessions, assignments, timeliness;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            sessionsPerWeek = Integer.parseInt(nextLine[1]);
            sessionLength = Integer.parseInt(nextLine[2]);
            betweenSessions = Integer.parseInt(nextLine[3]);
            forumSessions = Integer.parseInt(nextLine[4]);
            assignments = Integer.parseInt(nextLine[5]);
            timeliness = Integer.parseInt(nextLine[6]);

            weeklySessionsPerWeek.get(week).add(sessionsPerWeek);
            weeklySessionLength.get(week).add(sessionLength);
            if(betweenSessions > -1)
                weeklyBetweenSessions.get(week).add(betweenSessions);
            weeklyForumSessions.get(week).add(forumSessions);
            weeklyAssignments.get(week).add(assignments);
            weeklyUntilDeadline.get(week).add(timeliness);

        }

        csvReader.close();
    }

    //************************
    //************ Write data
    private static void writeThresholds(String filename, int cutOffPercent) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "User_id#Sessions/week#Length of session (min) #Between sessions (h)#Forum sessions#Assignments#Until deadline (h)".split("#");
        output.writeNext(toWrite);

        for(int i = 1; i < 11; i++) {
            toWrite[0] = "Week " + i;
            toWrite[1] = String.valueOf(getAverage(weeklySessionsPerWeek.get(i), cutOffPercent));
            toWrite[2] = String.valueOf(getAverage(weeklySessionLength.get(i), cutOffPercent));
            toWrite[3] = String.valueOf(getAverage(weeklyBetweenSessions.get(i), cutOffPercent));
            toWrite[4] = String.valueOf(getAverage(weeklyForumSessions.get(i), cutOffPercent));
            toWrite[5] = String.valueOf(getAverage(weeklyAssignments.get(i), cutOffPercent));
            toWrite[6] = String.valueOf(getAverage(weeklyUntilDeadline.get(i), cutOffPercent));

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeMaximums(String filename, int cutOffPercent) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "User_id#Sessions/week#Length of session (min) #Between sessions (h)#Forum sessions#Assignments#Until deadline (h)".split("#");
        output.writeNext(toWrite);

        for(int i = 1; i < 11; i++) {
            toWrite[0] = "Week " + i;
            toWrite[1] = String.valueOf(getMaximum(weeklySessionsPerWeek.get(i), cutOffPercent));
            toWrite[2] = String.valueOf(getMaximum(weeklySessionLength.get(i), cutOffPercent));
            toWrite[3] = String.valueOf(getMaximum(weeklyBetweenSessions.get(i), cutOffPercent));
            toWrite[4] = String.valueOf(getMaximum(weeklyForumSessions.get(i), cutOffPercent));
            toWrite[5] = String.valueOf(getMaximum(weeklyAssignments.get(i), cutOffPercent));
            toWrite[6] = String.valueOf(getMaximum(weeklyUntilDeadline.get(i), cutOffPercent));

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeScaledThresholds(String filename, int cutOffPercent) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "User_id#Sessions/week#Length of session (min) #Between sessions (h)#Forum sessions#Assignments#Until deadline (h)".split("#");
        output.writeNext(toWrite);

        for(int i = 1; i < 11; i++) {
            toWrite[0] = "Week " + i;
            toWrite[1] = String.valueOf(Math.round(getAverage(weeklySessionsPerWeek.get(i), cutOffPercent) * 10.0
                    / getMaximum(weeklySessionsPerWeek.get(i), cutOffPercent)));
            toWrite[2] = String.valueOf(Math.round(getAverage(weeklySessionLength.get(i), cutOffPercent) * 10.0
                    / getMaximum(weeklySessionLength.get(i), cutOffPercent)));
            toWrite[3] = String.valueOf(Math.round(getAverage(weeklyBetweenSessions.get(i), cutOffPercent) * 10.0
                    / getMaximum(weeklyBetweenSessions.get(i), cutOffPercent)));
            toWrite[4] = String.valueOf(Math.round(getAverage(weeklyForumSessions.get(i), cutOffPercent) * 10.0
                    / getMaximum(weeklyForumSessions.get(i), cutOffPercent)));
            toWrite[5] = String.valueOf(Math.round(getAverage(weeklyAssignments.get(i), cutOffPercent) * 10.0
                    / getMaximum(weeklyAssignments.get(i), cutOffPercent)));
            toWrite[6] = String.valueOf(Math.round(getAverage(weeklyUntilDeadline.get(i), cutOffPercent) * 10.0
                    / getMaximum(weeklyUntilDeadline.get(i), cutOffPercent)));

            output.writeNext(toWrite);
        }

        output.close();
    }


    //************************
    //************ Utils

    private static int getAverage(List<Integer> integers, int cutOffPercent) {
        int min, max, cutOffMin, cutOffMax;

        min = getMinimum(integers);
        max = getMaximum(integers);

        cutOffMin = min + (max - min) * cutOffPercent / 100;
        cutOffMax = max - (max - min) * cutOffPercent / 100;

        return (int) Math.round(getCutOffRange(integers, cutOffMin, cutOffMax)
                .stream()
                .mapToInt(e -> e)
                .average()
                .getAsDouble());
    }

    private static int getMaximum(List<Integer> integers) {
        return integers.stream().max(Comparator.naturalOrder()).get();
    }

    private static int getMinimum(List<Integer> integers) {
        return integers.stream().min(Comparator.naturalOrder()).get();
    }

    private static List<Integer> getCutOffRange(List<Integer> values, int min, int max) {
        return values.stream()
                .filter(e -> e >= min)
                .filter(e -> e <= max)
                .collect(Collectors.toList());
    }

    private static int getMaximum(List<Integer> integers, int cutOffPercent) {
        int min, max, cutOffMin, cutOffMax;

        min = getMinimum(integers);
        max = getMaximum(integers);

        cutOffMin = min + (max - min) * cutOffPercent / 100;
        cutOffMax = max - (max - min) * cutOffPercent / 100;

        return getMaximum(getCutOffRange(integers, cutOffMin, cutOffMax));
    }
}