/**
 * Created by Ioana on 1/9/2016.
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

//TODO: check file paths for file read and generation so they don't overwrite stuff
//TODO: check consistency of week numbering: starting with 0 or with 1
public class ScalingComputation {

    static HashMap<String, UserForGraphGeneration> users;
    static int[] thresholds;
    static double[] maximums;

    static int[] maximumAssignments;
    static int[] maximumVideos;

    static HashMap<String, Integer> scaledWeeklyTimes;
    static HashMap<String, Integer> scaledVideoTimes;
    static HashMap<String, Integer> scaledRatio;
    static HashMap<String, Integer> scaledVideos;
    static HashMap<String, Integer> scaledAssignments;
    static HashMap<String, Integer> scaledDeadlines;

   /*     public static void main(String[] args) throws IOException,ParseException
        {
            int week = 7;

            initialize();

            readMetrics(week, "data\\2016\\user_metrics\\");

            readMaximums(week, "data\\thresholds\\maximum5.csv");
            readScaledThresholds(week, "data\\thresholds\\scaled_thresholds5.csv");

            scaleMetrics(week);

            writeScaledMetrics(week, "data\\2016\\user_metrics\\scaled_metrics");

        }*/

    public static void scalingComputation(int week) throws IOException {
        initialize();

        readMetrics(week, "data\\2016\\user_metrics\\");

        readMaximums(week, "data\\thresholds\\maximum5.csv");
        readScaledThresholds(week, "data\\thresholds\\scaled_thresholds5.csv");

        scaleMetrics(week);

        writeScaledMetrics(week, "data\\2016\\user_metrics\\scaled_metrics");
    }

    //************************
    //************ Loading data

    private static void readMetrics(int week, String filepath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filepath + "metrics" + week + ".csv"));
        String [] nextLine;
        String shortId;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            shortId = nextLine[0].substring(nextLine[0].indexOf("1T2016_") + 7);

            if(Integer.parseInt(shortId) % 2 == 1 && shortId.compareTo("7538013") !=0 && shortId.compareTo("7592701") != 0)
                continue;

            UserForGraphGeneration current = new UserForGraphGeneration(shortId);

            current.setPlatformTime(week, Integer.parseInt(nextLine[1]));
            current.setVideoTime(week, Integer.parseInt(nextLine[2]));
            current.setRatioTime(week, Double.parseDouble(nextLine[3]));
            current.setDistinctVideos(week, Integer.parseInt(nextLine[4]));
            current.setAssignments(week, Integer.parseInt(nextLine[5]));
            current.setUntilDeadline(week, Integer.parseInt(nextLine[6]));

            users.put(shortId, current);

            if(Integer.parseInt(nextLine[5]) > maximumAssignments[week-1])
                maximumAssignments[week-1] = Integer.parseInt(nextLine[5]);
        }

        csvReader.close();

        System.out.println("Maximum Assignments for week " + week + ": " + maximumAssignments[week-1]);

        readAnonymizedIds();

    }

    private static void readAnonymizedIds() throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\anon-ids.csv"));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(users.containsKey(nextLine[0]))
                users.get(nextLine[0]).setAnonymousId(nextLine[1]);
        }

        csvReader.close();
    }

    private static void readScaledThresholds(int week, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        int i = 0;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            thresholds[i++] = Integer.parseInt(nextLine[week]);
        }

        csvReader.close();
    }

    private static void readMaximums(int week, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        int i = 0;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            maximums[i++] = Double.parseDouble(nextLine[week]);
        }

        csvReader.close();
    }
    //************************
    //************ Writing data

    private static void writeScaledMetrics(int week, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename + week + ".csv"), ',');
        String[] toWrite;
        String current;

        toWrite = "User_id#Time on platform#Time on videos#Ratio video/platform#Distict videos#Assignments#Until deadline".split("#");

        output.writeNext(toWrite);

        //write thresholds on line 2
        current = "Thresholds";
        for(int i = 0; i < 6; i++)
            current += "#" + thresholds[i];

        output.writeNext(current.split("#"));

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getKey();
            toWrite[0] = entry.getValue().getAnonymousId();

            if(toWrite[0] == null)
               continue;

            toWrite[1] = String.valueOf(scaledWeeklyTimes.get(current));
            toWrite[2] = String.valueOf(scaledVideoTimes.get(current));
            toWrite[3] = String.valueOf(scaledRatio.get(current));
            toWrite[4] = String.valueOf(scaledVideos.get(current));
            toWrite[5] = String.valueOf(scaledAssignments.get(current));
            toWrite[6] = String.valueOf(scaledDeadlines.get(current));

            output.writeNext(toWrite);
        }

        output.close();
    }

    //************************
    //************ Computations

    private static void scaleMetrics(int week){
        scalePlatformTime(week);
        scaleVideoTime(week);
        scaleRatioTime(week);
        scaleVideos(week);
        scaleAssignments(week);
        scaleUntilDeadline(week);
    }

    private static void scalePlatformTime(int week){
        int weekTime;
        UserForGraphGeneration current;
        int scaledValue;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            weekTime = current.getPlatformTime(week);
            scaledValue = (int) Math.round(weekTime*10.0/maximums[0]);

            scaledWeeklyTimes.put(entry.getKey(), regularized(scaledValue));
        }
    }

    private static int regularized(int scaledValue) {
        if (scaledValue > 10)
            return 10;
        return scaledValue;
    }

    private static void scaleVideoTime(int week){
        int videoTime, scaledValue;
        UserForGraphGeneration current;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            videoTime = current.getVideoTime(week);
            scaledValue = (int) Math.round(videoTime*10.0/maximums[1]);

            scaledVideoTimes.put(entry.getKey(), regularized(scaledValue));
        }
    }

    private static void scaleRatioTime(int week){
        double ratio;
        UserForGraphGeneration current;
        int scaledValue;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            ratio = current.getRatioTime(week);
            scaledValue = (int) Math.round(ratio*10.0/maximums[2]);
            scaledRatio.put(entry.getKey(), regularized(scaledValue));
        }
    }

    private static void scaleVideos(int week){
        int videos;
        UserForGraphGeneration current;
        int scaledValue;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            videos = current.getDistinctVideos(week);
            scaledValue = (int) Math.round(videos*10.0/maximums[3]);
            scaledVideos.put(entry.getKey(), regularized(scaledValue));
        }
    }

    private static void scaleAssignments(int week){
        int assignments;
        UserForGraphGeneration current;
        int scaledValue;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            assignments = current.getAssignments(week);
            if(maximumAssignments[week-1] == 0)
                scaledValue = 0;
            else
                scaledValue = (int) Math.round(assignments*10.0/maximumAssignments[week-1]);
            scaledAssignments.put(entry.getKey(), regularized(scaledValue));
        }
    }

    private static void scaleUntilDeadline(int week){
        long untilDeadline;
        UserForGraphGeneration current;
        int scaledValue;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            untilDeadline = current.getUntilDeadline(week);
            scaledValue = (int) Math.round(untilDeadline*10.0/maximums[5]);
            scaledDeadlines.put(entry.getKey(), regularized(scaledValue));
        }
    }

    //************************
    //************ Utils

    private static void initialize() {
        users = new HashMap<>();
        thresholds = new int[6];
        maximums = new double[6];

        maximumAssignments = new int[11];
        maximumVideos = new int[11];

        scaledWeeklyTimes = new HashMap<>();
        scaledVideoTimes = new HashMap<>();
        scaledRatio = new HashMap<>();
        scaledVideos = new HashMap<>();
        scaledAssignments = new HashMap<>();
        scaledDeadlines = new HashMap<>();

    }

}
