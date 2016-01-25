/**
 * Created by Ioana on 1/9/2016.
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

//TODO: check consistency of week numbering: starting with 0 or with 1
public class ThresholdComputation {

    static HashMap<Integer, ArrayList<Integer>> weeklyPlatformTimes;
    static HashMap<Integer, ArrayList<Integer>> weeklyVideoTimes;
    static HashMap<Integer, ArrayList<Double>> weeklyRatioTimes;
    static HashMap<Integer, ArrayList<Integer>> weeklyDistinctVideos;
    static HashMap<Integer, ArrayList<Integer>> weeklyAssignments;
    static HashMap<Integer, ArrayList<Integer>> weeklyUntilDeadline;

    public static void main(String[] args) throws IOException, ParseException {

        initialize();

        readMetrics();

        //writeThresholds("thresholds0.csv", 0);
        //writeThresholds("thresholds10.csv", 10);
        writeThresholds("data\\thresholds\\thresholds5.csv", 5);

    }

    //************************
    //************ Loading data
    private static void initialize() {
        weeklyPlatformTimes = new HashMap<>();
        weeklyVideoTimes = new HashMap<>();
        weeklyRatioTimes = new HashMap<>();
        weeklyDistinctVideos = new HashMap<>();
        weeklyAssignments = new HashMap<>();
        weeklyUntilDeadline = new HashMap<>();

        for (int i = 1; i < 12; i++) {
            weeklyPlatformTimes.put(i, new ArrayList<>());
            weeklyVideoTimes.put(i, new ArrayList<>());
            weeklyRatioTimes.put(i, new ArrayList<>());
            weeklyDistinctVideos.put(i, new ArrayList<>());
            weeklyAssignments.put(i, new ArrayList<>());
            weeklyUntilDeadline.put(i, new ArrayList<>());
        }
    }

    private static void readMetrics() throws IOException {
        for (int i = 1; i < 12; i++)
            readMetricsPerWeek(i);
    }

    private static void readMetricsPerWeek(int week) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2015\\user_metrics\\metrics" + week + ".csv"));
        String[] nextLine;

        int platformTime, videoTime, videos, assignments, timeliness;
        double ratio;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            platformTime = Integer.parseInt(nextLine[1]);
            videoTime = Integer.parseInt(nextLine[2]);
            ratio = Double.parseDouble(nextLine[3]);
            videos = Integer.parseInt(nextLine[4]);
            assignments = Integer.parseInt(nextLine[5]);
            timeliness = Integer.parseInt(nextLine[6]);

            weeklyPlatformTimes.get(week).add(platformTime);
            weeklyVideoTimes.get(week).add(videoTime);
            weeklyRatioTimes.get(week).add(ratio);
            weeklyDistinctVideos.get(week).add(videos);
            weeklyAssignments.get(week).add(assignments);
            weeklyUntilDeadline.get(week).add(timeliness);

        }

        csvReader.close();
    }

    //************************
    //************ Write data
    private static void writeThresholds(String filename, int cutOffPercent) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String header = "Metric";
        String[] toWrite;
        String row;
        HashMap<Integer, Double> averages;

        for(int i = 1; i < 12; i++)
            header += "#week " + String.valueOf(i);

        toWrite = header.split("#");
        output.writeNext(toWrite);

        //Time on platform
        row = "Time on the platform (s)";
        averages = calculatePlatformAverageWithCutOffValues(cutOffPercent);
        for (int i = 1; i < 12; i++) {
            row += "#" + averages.get(i);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Video watching
        row = "Time on videos (s)";
        averages = calculateVideoAverageWithCutOffValues(cutOffPercent);
        for(int i = 1; i < 12; i++) {
            row += "#" + averages.get(i);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Ratio of video/platform
        row = "Ratio video/platform";
        averages = calculateRatioAverageWithCutOffValues(cutOffPercent);
        for(int i = 1; i < 12; i++) {
            row += "#" + averages.get(i);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Distinct videos
        row = "Distinct videos";
        averages = calculateDistinctVideoAverageWithCutOffValues(cutOffPercent);
        for(int i = 1; i < 12; i++) {
            row += "#" + averages.get(i);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Assignments
        row = "Assignments";
        averages = calculateAssignmentsAverageWithCutOffValues(cutOffPercent);
        for(int i = 1; i < 12; i++) {
            row += "#" + averages.get(i);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Until deadline
        row = "Until deadline (h)";
        averages = calculateUntilDeadlineAverageWithCutOffValues(cutOffPercent);
        for(int i = 1; i < 12; i++) {
            row += "#" + averages.get(i);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        output.close();
    }

    //************************
    //************ Compute data
    private static HashMap<Integer, Double> calculatePlatformAverageWithCutOffValues(int cutOffPercent) {
        int min, max;
        int cutOffMin, cutOffMax;
        long count;
        double sum;
        ArrayList<Integer> weekValues;
        HashMap<Integer, Double> averages = new HashMap<>();

        for (int i = 1; i < 12; i++) {
            weekValues = weeklyPlatformTimes.get(i);

            min = weekValues.stream().min(Comparator.naturalOrder()).get();
            max = weekValues.stream().max(Comparator.naturalOrder()).get();

            cutOffMin = min + (max - min) * cutOffPercent / 100;
            cutOffMax = max - (max - min) * cutOffPercent / 100;

            count = getCountOfCutOffRange(weekValues, cutOffMin, cutOffMax);
            sum = getSum(weekValues, cutOffMin, cutOffMax);

            averages.put(i, sum / count);

        }

        return averages;
    }

    private static HashMap<Integer, Double> calculateVideoAverageWithCutOffValues(int cutOffPercent) {
        int min, max;
        int cutOffMin, cutOffMax;
        long count;
        double sum;
        ArrayList<Integer> weekValues;
        HashMap<Integer, Double> averages = new HashMap<>();

        for (int i = 1; i < 12; i++) {
            weekValues = weeklyVideoTimes.get(i);

            min = weekValues.stream().min(Comparator.naturalOrder()).get();
            max = weekValues.stream().max(Comparator.naturalOrder()).get();

            cutOffMin = min + (max - min) * cutOffPercent / 100;
            cutOffMax = max - (max - min) * cutOffPercent / 100;

            count = getCountOfCutOffRange(weekValues, cutOffMin, cutOffMax);
            sum = getSum(weekValues, cutOffMin, cutOffMax);

            averages.put(i, sum / count);
        }

        return averages;
    }

    private static HashMap<Integer, Double> calculateRatioAverageWithCutOffValues(int cutOffPercent) {
        double min, max;
        double cutOffMin, cutOffMax;
        long count;
        double sum;
        ArrayList<Double> weekValues;
        HashMap<Integer, Double> averages = new HashMap<>();

        for (int i = 1; i < 12; i++) {
            weekValues = weeklyRatioTimes.get(i);

            min = weekValues.stream().min(Comparator.naturalOrder()).get();
            max = weekValues.stream().max(Comparator.naturalOrder()).get();

            cutOffMin = min + (max - min) * cutOffPercent / 100;
            cutOffMax = max - (max - min) * cutOffPercent / 100;

            count = getCountOfCutOffRange(weekValues, cutOffMin, cutOffMax);
            sum = getSum(weekValues, cutOffMin, cutOffMax);

            averages.put(i, sum / count);

            System.out.println("week " + i + ": " + sum + " - " + count + " - " + sum/count);
        }

        return averages;
    }

    private static HashMap<Integer, Double> calculateDistinctVideoAverageWithCutOffValues(int cutOffPercent) {
        int min, max;
        int cutOffMin, cutOffMax;
        long count;
        double sum;
        ArrayList<Integer> weekValues;
        HashMap<Integer, Double> averages = new HashMap<>();

        for (int i = 1; i < 12; i++) {
            weekValues = weeklyDistinctVideos.get(i);

            min = weekValues.stream().min(Comparator.naturalOrder()).get();
            max = weekValues.stream().max(Comparator.naturalOrder()).get();

            cutOffMin = min + (max - min) * cutOffPercent / 100;
            cutOffMax = max - (max - min) * cutOffPercent / 100;

            count = getCountOfCutOffRange(weekValues, cutOffMin, cutOffMax);
            sum = getSum(weekValues, cutOffMin, cutOffMax);

            averages.put(i, (double) Math.round(sum / count));
        }

        return averages;
    }

    private static HashMap<Integer, Double> calculateAssignmentsAverageWithCutOffValues(int cutOffPercent) {
        int min, max;
        int cutOffMin, cutOffMax;
        long count;
        double sum;
        ArrayList<Integer> weekValues;
        HashMap<Integer, Double> averages = new HashMap<>();

        for (int i = 1; i < 12; i++) {
            weekValues = weeklyAssignments.get(i);

            min = weekValues.stream().min(Comparator.naturalOrder()).get();
            max = weekValues.stream().max(Comparator.naturalOrder()).get();

            cutOffMin = min + (max - min) * cutOffPercent / 100;
            cutOffMax = max - (max - min) * cutOffPercent / 100;

            count = getCountOfCutOffRange(weekValues, cutOffMin, cutOffMax);
            sum = getSum(weekValues, cutOffMin, cutOffMax);

            averages.put(i, sum / count);
        }

        return averages;
    }

    private static HashMap<Integer, Double> calculateUntilDeadlineAverageWithCutOffValues(int cutOffPercent) {
        int min, max;
        int cutOffMin, cutOffMax;
        long count;
        double sum;
        ArrayList<Integer> weekValues;
        HashMap<Integer, Double> averages = new HashMap<>();

        for (int i = 1; i < 12; i++) {
            weekValues = weeklyUntilDeadline.get(i);

            min = weekValues.stream().min(Comparator.naturalOrder()).get();
            max = weekValues.stream().max(Comparator.naturalOrder()).get();

            cutOffMin = min + (max - min) * cutOffPercent / 100;
            cutOffMax = max - (max - min) * cutOffPercent / 100;

            count = getCountOfCutOffRange(weekValues, cutOffMin, cutOffMax);
            sum = getSum(weekValues, cutOffMin, cutOffMax);

            averages.put(i, sum / count);
        }

        return averages;
    }

    private static long getCountOfCutOffRange(ArrayList<Integer> values, int min, int max) {
        return values.stream()
                .filter(e -> e >= min)
                .filter(e -> e <= max)
                .count();
    }

    private static long getCountOfCutOffRange(ArrayList<Double> values, double min, double max) {
        return values.stream()
                .filter(e -> e >= min)
                .filter(e -> e <= max)
                .count();
    }
    //************************
    //************ Utils
    private static double getSum(ArrayList<Integer> values, int cutOffMin, int cutOffMax) {
        return values.stream()
                .filter(e -> e >= cutOffMin && e <= cutOffMax)
                .mapToDouble(e -> e)
                .reduce(0, Double::sum);
    }

    private static double getSum(ArrayList<Double> values, double cutOffMin, double cutOffMax) {
        return values.stream()
                .filter(e -> e >= cutOffMin && e <= cutOffMax)
                .mapToDouble(e -> e)
                .reduce(0, Double::sum);
    }

    private static long toHours(int seconds) {
        return Math.round(seconds / 3600.0);
    }


}