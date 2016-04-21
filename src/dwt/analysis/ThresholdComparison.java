package dwt.analysis;
/**
 * Created by Ioana on 3/1/2016.
 */


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ThresholdComparison {

    private static HashMap<String, UserForDataAnalysis> testGroup;
    private static HashMap<String, UserForDataAnalysis> controlGroup;

    public static void main(String[] args) throws IOException {
        int endWeek = 9;

        //thresholdComparison(endWeek);
        thresholdComparisonSumEachWeek(endWeek);

    }

    private static void thresholdComparisonSumEachWeek(int endWeek) throws IOException {
        initialize();

        readActive(endWeek);
        readScaledMetrics(endWeek, "data\\2016\\user_metrics\\");

        writeSumForEachWeek(testGroup, endWeek, "data\\2016\\post-data\\active_test_threshold_sums_" + endWeek + ".csv");

    }

    private static void writeSumForEachWeek(HashMap<String, UserForDataAnalysis> group, int endWeek, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        String toWriteString;
        UserForDataAnalysis current;

        toWriteString = "User_id";
        for(int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;

        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataAnalysis> entry : group.entrySet()) {
            current = entry.getValue();
            toWrite[0] = entry.getKey();

            for(int i = 1; i <= endWeek; i++)
                toWrite[i] = String.valueOf(sumThresholdDifferences(current, i));

            output.writeNext(toWrite);
        }
        output.close();
    }

    private static int sumThresholdDifferences(UserForDataAnalysis user, int week) {
        int sum = 0;

        sum += user.getPlatformTime(week);
        sum += user.getVideoTime(week);
        sum += user.getRatioTime(week);
        sum += user.getDistinctVideos(week);
        sum += user.getAssignments(week);
        sum += user.getUntilDeadline(week);

        return sum;
    }


    public static void thresholdComparison(int endWeek) throws IOException {
        initialize();

        readActive(endWeek);
        readScaledMetrics(endWeek, "data\\2016\\user_metrics\\");

        //writeDifferences(testGroup, 7, "data\\2016\\post-data\\threshold_comparison\\");
        writeDifferencesForWeek(testGroup, endWeek, "data\\2016\\post-data\\active_test_threshold_differences_" + endWeek + ".csv");
        //writeDifferencesForWeek(controlGroup, endWeek, "data\\2016\\post-data\\active_control_threshold_differences_" + endWeek + ".csv");
    }

    private static void readActive(int endWeek) throws IOException {
        readActiveUsers(testGroup, "data\\2016\\post-data\\all_metrics_test_5min_" + endWeek + ".csv");
        readActiveUsers(controlGroup, "data\\2016\\post-data\\all_metrics_control_5min_" + endWeek + ".csv");
    }

    private static void readActiveUsers(HashMap<String, UserForDataAnalysis> group, String filepath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filepath));
        String[] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null)
            group.put(nextLine[0], new UserForDataAnalysis(nextLine[0]));

        csvReader.close();
    }

    //***********************
    //*********** Loading data

    private static void initialize() {

        testGroup = new HashMap<>();
        controlGroup = new HashMap<>();

    }

    private static void readScaledMetrics(int endWeek, String filepath) throws IOException {
        for (int i = 1; i <= endWeek; i++)
            readWeeklyScaledMetrics(i, filepath);
    }

    private static void readWeeklyScaledMetrics(int week, String filepath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filepath + "non_anon_scaled_metrics" + week + ".csv"));
        String[] nextLine;
        String shortId;
        UserForDataAnalysis current;
        int[] thresholds = new int[6];

        csvReader.readNext();   //headers

        nextLine = csvReader.readNext(); //thresholds
        for(int i = 0; i < 6; i++)
            thresholds[i] = Integer.parseInt(nextLine[i + 1]);

        while ((nextLine = csvReader.readNext()) != null) {
            shortId = nextLine[0];

            if (!testGroup.containsKey(shortId))
                continue;

            current = testGroup.get(shortId);

            current.setPlatformTime(week, Integer.parseInt(nextLine[1]) - thresholds[0]);
            current.setVideoTime(week, Integer.parseInt(nextLine[2]) - thresholds[1]);
            current.setRatioTime(week, Integer.parseInt(nextLine[3]) - thresholds[2]);
            current.setDistinctVideos(week, Integer.parseInt(nextLine[4]) - thresholds[3]);
            current.setAssignments(week, Integer.parseInt(nextLine[5]) - thresholds[4]);
            current.setUntilDeadline(week, Integer.parseInt(nextLine[6]) - thresholds[5]);

        }

        csvReader.close();

    }

    private static void writeDifferences(HashMap<String, UserForDataAnalysis> group, int endWeek, String filepath) throws IOException {
        writeDifferencesPlatformTime(group, endWeek,filepath);
        writeDifferencesVideoTime(group, endWeek,filepath);
        writeDifferencesRatio(group, endWeek,filepath);
        writeDifferencesVideos(group, endWeek,filepath);
        writeDifferencesAssignments(group, endWeek,filepath);
        writeDifferencesDeadline(group, endWeek,filepath);
    }

    private static void writeDifferencesPlatformTime(HashMap<String, UserForDataAnalysis> group, int endWeek, String filepath) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filepath + "platform_differences_" + endWeek + ".csv"), ',');
        String[] toWrite;
        String toWriteString;
        UserForDataAnalysis current;

        toWriteString = "User_id";
        for(int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;

        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataAnalysis> entry : testGroup.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            System.out.println(current.getId());
            for(int i = 1; i <= endWeek; i++) {
                System.out.println(current.getPlatformTime(i));
                toWriteString += "#" + current.getPlatformTime(i);
            }

            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeDifferencesVideoTime(HashMap<String, UserForDataAnalysis> group, int endWeek, String filepath) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filepath + "video_time_differences_" + endWeek + ".csv"), ',');
        String[] toWrite;
        String toWriteString;
        UserForDataAnalysis current;

        toWriteString = "User_id";
        for(int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;

        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataAnalysis> entry : testGroup.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            for(int i = 1; i <= endWeek; i++)
                toWriteString += "#" + current.getVideoTime(i);

            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeDifferencesRatio(HashMap<String, UserForDataAnalysis> group, int endWeek, String filepath) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filepath + "ratio_differences_" + endWeek + ".csv"), ',');
        String[] toWrite;
        String toWriteString;
        UserForDataAnalysis current;

        toWriteString = "User_id";
        for(int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;

        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataAnalysis> entry : testGroup.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            for(int i = 1; i <= endWeek; i++)
                toWriteString += "#" + current.getRatioTime(i);

            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeDifferencesVideos(HashMap<String, UserForDataAnalysis> group, int endWeek, String filepath) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filepath + "videos_differences_" + endWeek + ".csv"), ',');
        String[] toWrite;
        String toWriteString;
        UserForDataAnalysis current;

        toWriteString = "User_id";
        for(int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;

        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataAnalysis> entry : testGroup.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            for(int i = 1; i <= endWeek; i++)
                toWriteString += "#" + current.getDistinctVideos(i);

            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeDifferencesAssignments(HashMap<String, UserForDataAnalysis> group, int endWeek, String filepath) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filepath + "assignments_differences_" + endWeek + ".csv"), ',');
        String[] toWrite;
        String toWriteString;
        UserForDataAnalysis current;

        toWriteString = "User_id";
        for(int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;

        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataAnalysis> entry : testGroup.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            for(int i = 1; i <= endWeek; i++)
                toWriteString += "#" + current.getAssignments(i);

            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeDifferencesDeadline(HashMap<String, UserForDataAnalysis> group, int endWeek, String filepath) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filepath + "deadline_differences_" + endWeek + ".csv"), ',');
        String[] toWrite;
        String toWriteString;
        UserForDataAnalysis current;

        toWriteString = "User_id";
        for(int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;

        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataAnalysis> entry : testGroup.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            for(int i = 1; i <= endWeek; i++)
                toWriteString += "#" + current.getUntilDeadline(i);

            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeDifferencesForWeek(HashMap<String, UserForDataAnalysis> group, int week, String filepath) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filepath), ',');
        String[] toWrite;
        UserForDataAnalysis current;

        toWrite = "User_id#Time on platform#Time on videos#Ratio video/platform#Distict videos#Assignments#Until deadline".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataAnalysis> entry : group.entrySet()) {
            current = entry.getValue();
            toWrite[0] = entry.getKey();

            System.out.println(entry.getKey());
            toWrite[1] = String.valueOf(current.getPlatformTime(week));
            toWrite[2] = String.valueOf(current.getVideoTime(week));
            toWrite[3] = String.valueOf(current.getRatioTime(week));
            toWrite[4] = String.valueOf(current.getDistinctVideos(week));
            toWrite[5] = String.valueOf(current.getAssignments(week));
            toWrite[6] = String.valueOf(current.getUntilDeadline(week));

            output.writeNext(toWrite);
        }
        output.close();

    }



    //***********************
    //*********** Utils

    private static Date getDateFromString(String dateString) throws ParseException {
        //input date: "2014-11-11 12:00:00"
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return format.parse(dateString);
    }

    private static int getWeekFromDate(String date) {
        if(date.compareTo("2016-01-12") >= 0 && date.compareTo("2016-01-19") < 0)
            return 1;
        if(date.compareTo("2016-01-19") >= 0 && date.compareTo("2016-01-26") < 0)
            return 2;
        if(date.compareTo("2016-01-26") >= 0 && date.compareTo("2016-02-02") < 0)
            return 3;
        if(date.compareTo("2016-02-02") >= 0 && date.compareTo("2016-02-09") < 0)
            return 4;
        if(date.compareTo("2016-02-09") >= 0 && date.compareTo("2016-02-16") < 0)
            return 5;
        if(date.compareTo("2016-02-16") >= 0 && date.compareTo("2016-02-23") < 0)
            return 6;
        if(date.compareTo("2016-02-23") >= 0 && date.compareTo("2016-03-01") < 0)
            return 7;
        if(date.compareTo("2016-03-01") >= 0 && date.compareTo("2016-03-08") < 0)
            return 8;
        if(date.compareTo("2016-03-08") >= 0 && date.compareTo("2016-03-15") < 0)
            return 9;
        if(date.compareTo("2016-03-15") >= 0 && date.compareTo("2016-03-22") < 0)
            return 10;
        if(date.compareTo("2016-03-22") >= 0 && date.compareTo("2016-03-29") < 0)
            return 11;
        return 99;
    }


}
