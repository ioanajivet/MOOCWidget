package dwt; /**
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

    static int[] maximumPlatformTime;
    static int[] maximumVideoTime;
    static double[] maximumRatio;
    static int[] maximumVideos;
    static int[] maximumAssignments;
    static int[] maximumUntilDeadline;

    public static void main(String[] args) throws IOException, ParseException {

        initialize();

        readMetrics();

        //writeThresholds("thresholds0.csv", 0);
        //writeThresholds("thresholds10.csv", 10);
        writeThresholds("data\\thresholds\\thresholds5.csv", 5);
        writeMaximum("data\\thresholds\\maximum5.csv");
        writeScaledThresholds("data\\thresholds\\scaled_thresholds5.csv", 5);

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

        maximumPlatformTime = new int[11];
        maximumVideoTime = new int[11];
        maximumRatio = new double[11];
        maximumVideos = new int[11];
        maximumAssignments = new int[11];
        maximumUntilDeadline = new int[11];
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

    private static void writeScaledThresholds(String filename, int cutOffPercent) throws IOException {
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
            if(maximumPlatformTime[i-1] == 0)
                row += "#0";
            else
                row += "#" + Math.round(averages.get(i)*10/maximumPlatformTime[i-1]);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Video watching
        row = "Time on videos (s)";
        averages = calculateVideoAverageWithCutOffValues(cutOffPercent);
        for(int i = 1; i < 12; i++) {
            if(maximumVideoTime[i-1] == 0)
                row += "#0";
            else
                row += "#" + Math.round(averages.get(i)*10/maximumVideoTime[i-1]);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Ratio of video/platform
        row = "Ratio video/platform";
        averages = calculateRatioAverageWithCutOffValues(cutOffPercent);
        for(int i = 1; i < 12; i++) {
            if(maximumRatio[i-1] == 0)
                row += "#0";
            else
                row += "#" + Math.round(averages.get(i)*10/maximumRatio[i-1]);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Distinct videos
        row = "Distinct videos";
        averages = calculateDistinctVideoAverageWithCutOffValues(cutOffPercent);
        for(int i = 1; i < 12; i++) {
            if(maximumVideos[i-1] == 0)
                row += "#0";
            else
                row += "#" + Math.round(averages.get(i)*10/maximumVideos[i-1]);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Assignments
        row = "Assignments";
        averages = calculateAssignmentsAverageWithCutOffValues(cutOffPercent);
        for(int i = 1; i < 12; i++) {
            if(maximumAssignments[i-1] == 0)
                row += "#0";
            else
                row += "#" + Math.round(averages.get(i)*10/maximumAssignments[i-1]);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Until deadline
        row = "Until deadline (h)";
        averages = calculateUntilDeadlineAverageWithCutOffValues(cutOffPercent);
        for(int i = 1; i < 12; i++) {
            if(maximumUntilDeadline[i-1] == 0)
                row += "#0";
            else
                row += "#" + Math.round(averages.get(i)*10/maximumUntilDeadline[i-1]);
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        output.close();
    }

    private static void writeMaximum(String filename) throws IOException {
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
        for (int i = 1; i < 12; i++) {
            row += "#" + maximumPlatformTime[i-1];
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Video watching
        row = "Time on videos (s)";
        for(int i = 1; i < 12; i++) {
            row += "#" + maximumVideoTime[i-1];
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Ratio of video/platform
        row = "Ratio video/platform";
        for(int i = 1; i < 12; i++) {
            row += "#" + maximumRatio[i-1];
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Distinct videos
        row = "Distinct videos";
        for(int i = 1; i < 12; i++) {
            row += "#" + maximumVideos[i-1];
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Assignments
        row = "Assignments";
        for(int i = 1; i < 12; i++) {
            row += "#" + maximumAssignments[i-1];
        }
        toWrite = row.split("#");
        output.writeNext(toWrite);

        //Until deadline
        row = "Until deadline (h)";
        for(int i = 1; i < 12; i++) {
            row += "#" + maximumUntilDeadline[i-1];
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

            maximumPlatformTime[i-1] = cutOffMax;

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

            maximumVideoTime[i-1] = cutOffMax;

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

            maximumRatio[i-1] = cutOffMax;

            count = getCountOfCutOffRange(weekValues, cutOffMin, cutOffMax);
            sum = getSum(weekValues, cutOffMin, cutOffMax);

            averages.put(i, sum / count);
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

            maximumVideos[i-1] = cutOffMax;

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

            maximumAssignments[i-1] = cutOffMax;

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

            maximumUntilDeadline[i-1] = cutOffMax;

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

}