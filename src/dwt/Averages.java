package dwt; /**
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

public class Averages {

    static HashMap<String, UserForGraphGeneration> testGroup;
    static HashMap<String, UserForGraphGeneration> controlGroup;

    static HashMap<String, Date> testGroupSession;
    static HashMap<String, Date> controlGroupSession;

    static HashMap<String, Integer> testAggregatedSessions;
    static HashMap<String, Integer> controlAggregatedSessions;

    static HashMap<String, List<String>> testUniquePerDay;
    static HashMap<String, List<String>> controlUniquePerDay;

    static HashMap<Integer, List<String>> testUniquePerWeek;
    static HashMap<Integer, List<String>> controlUniquePerWeek;

    static List<String> activeTestUserIds;
    static List<String> activeControlUserIds;

   // static HashMap<String, UserForDataAnalysis> testGroup;
    //static HashMap<String, UserForDataAnalysis> controlGroup;

    public static void main(String[] args) throws IOException, ParseException {
        int endWeek = 9;

        initialize();


//** Average for indicators
        readUsers();
        readMetrics(endWeek, "data\\2016\\user_metrics\\");
        //writeMetrics(endWeek);
        analyseMetrics(endWeek);


//** Engagement
        //lastSession(endWeek);
        //uniqueUsers(endWeek);

//** Threshold Comparison
        //selectActiveUsers(endWeek);
        //readActive(endWeek);
        //readMetrics("data\\2016\\user_metrics\\");
        //writeMetrics(endWeek, "data\\2016\\post-data\\active\\");
        //writeMetricsForWeek(endWeek, "data\\2016\\post-data\\");

    }

    private static void writeMetricsForWeek(int endWeek, String filepath) throws IOException {
        writeMetricsForWeekUsers(testGroup, endWeek, filepath + "total_active_test_" + endWeek + ".csv");
        writeMetricsForWeekUsers(controlGroup, endWeek, filepath + "total_active_control_" + endWeek + ".csv");
    }

    private static void writeMetricsForWeekUsers(HashMap<String, UserForGraphGeneration> users, int endWeek, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        UserForGraphGeneration current;

        toWrite = "User_id#Time on platform#Time on videos#Ratio video/platform#Distict videos#Assignments#Until deadline".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            toWrite[0] = entry.getKey();

            toWrite[1] = String.valueOf(current.getPlatformTime(endWeek));
            toWrite[2] = String.valueOf(current.getVideoTime(endWeek));
            toWrite[3] = String.format("%.2f", current.getRatioTime(endWeek));
            toWrite[4] = String.valueOf(current.getDistinctVideos(endWeek));
            toWrite[5] = String.valueOf(current.getAssignments(endWeek));
            toWrite[6] = String.valueOf(current.getUntilDeadline(endWeek));

            output.writeNext(toWrite);
        }
        output.close();
    }

    private static void readActive(int endWeek) throws IOException {
        readActiveUsers(testGroup, "data\\2016\\post-data\\threshold_comparison\\active_test_" + endWeek + ".csv");
        readActiveUsers(controlGroup, "data\\2016\\post-data\\threshold_comparison\\active_control_" + endWeek + ".csv");
    }

    private static void readActiveUsers(HashMap<String, UserForGraphGeneration> group, String filepath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filepath));
        String[] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null)
            group.put(nextLine[0], new UserForGraphGeneration(nextLine[0]));

        csvReader.close();
    }




//************************
//************ Loading data

    private static void initialize() {
        testGroup = new HashMap<>();
        controlGroup = new HashMap<>();

        //testGroupSession = new HashMap<>();
        //controlGroupSession = new HashMap<>();

        //testAggregatedSessions = new HashMap<>();
        //controlAggregatedSessions = new HashMap<>();

        //testUniquePerDay = new HashMap<>();
        //controlUniquePerDay = new HashMap<>();

        //testUniquePerWeek = new HashMap<>();
        //controlUniquePerWeek = new HashMap<>();

        //activeTestUserIds = new ArrayList<>();
        //activeControlUserIds = new ArrayList<>();
    }

    private static void readUsers() throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\user_pii.csv"));
        String[] nextLine;
        String shortId;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            shortId = nextLine[0].substring(nextLine[0].indexOf("1T2016_") + 7);

            if (Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0)
                testGroup.put(shortId, new UserForGraphGeneration(shortId));
            else
                controlGroup.put(shortId, new UserForGraphGeneration(shortId));
        }

        System.out.println("Users under test: " + testGroup.size());
        System.out.println("Control group: " + controlGroup.size());
        csvReader.close();

    }

    private static void readAnonymizedIds() throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\anon-ids.csv"));
        String[] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if (testGroup.containsKey(nextLine[0]))
                testGroup.get(nextLine[0]).setAnonymousId(nextLine[1]);
            else if (controlGroup.containsKey(nextLine[0]))
                controlGroup.get(nextLine[0]).setAnonymousId(nextLine[1]);
        }

        csvReader.close();
    }


    //========= Average on metrics ==========

    private static void readMetrics(int endWeek, String filepath) throws IOException {
        //TODO: update end week
        for (int i = 1; i <= endWeek; i++)
            readWeeklyMetrics(i, filepath);
    }

    private static void readWeeklyMetrics(int week, String filepath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filepath + "metrics" + week + ".csv"));
        String[] nextLine;
        String shortId;
        UserForGraphGeneration current;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            shortId = nextLine[0].substring(nextLine[0].indexOf("1T2016_") + 7);

            if (Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0)
                current = testGroup.get(shortId);
            else
                current = controlGroup.get(shortId);

            if(current == null)
                continue;

            current.setPlatformTime(week, Integer.parseInt(nextLine[1]));
            current.setVideoTime(week, Integer.parseInt(nextLine[2]));
            current.setRatioTime(week, Double.parseDouble(nextLine[3]));
            current.setDistinctVideos(week, Integer.parseInt(nextLine[4]));
            current.setAssignments(week, Integer.parseInt(nextLine[5]));
            current.setUntilDeadline(week, Integer.parseInt(nextLine[6]));

        }

        csvReader.close();

        readAnonymizedIds();
    }

    private static void analyseMetrics(int endWeek) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter("data\\2016\\post-data\\metrics_overview.csv"), ',');

        writePlatformTimeAnalysis(output, endWeek);
        writeVideoTimeAnalysis(output, endWeek);
        writeRatioAnalysis(output, endWeek);
        writeVideosAnalysis(output, endWeek);
        writeAssignmentAnalysis(output, endWeek);
        writeUntilDeadlineAnalysis(output, endWeek);

        output.close();

    }

    private static void writeMetrics(int endWeek, String filepath) throws IOException {
        writePlatformTime(testGroup, filepath + "test_after_week" + endWeek + "_platformTime.csv", endWeek);
        writePlatformTime(controlGroup, filepath + "control_after_week" + endWeek + "_platformTime.csv", endWeek);

        writeVideoTime(testGroup, filepath + "test_after_week" + endWeek + "_videoTime.csv", endWeek);
        writeVideoTime(controlGroup, filepath + "control_after_week" + endWeek + "_videoTime.csv", endWeek);

        writeRatioTime(testGroup, filepath + "test_after_week" + endWeek + "_ratioTime.csv", endWeek);
        writeRatioTime(controlGroup, filepath + "control_after_week" + endWeek + "_ratioTime.csv", endWeek);

        writeVideos(testGroup, filepath + "test_after_week" + endWeek + "_videos.csv", endWeek);
        writeVideos(controlGroup, filepath + "control_after_week" + endWeek + "_videos.csv", endWeek);

        writeAssignments(testGroup, filepath + "test_after_week" + endWeek + "_assignments.csv", endWeek);
        writeAssignments(controlGroup, filepath + "control_after_week" + endWeek + "_assignments.csv", endWeek);

        writeUntilDeadline(testGroup, filepath + "test_after_week" + endWeek + "_untilDeadline.csv", endWeek);
        writeUntilDeadline(controlGroup, filepath + "control_after_week" + endWeek + "_untilDeadline.csv", endWeek);
    }

    private static void writePlatformTimeAnalysis(CSVWriter output, int endWeek) {

        String[] toWrite = {"Platform Time [s]"};

        output.writeNext(toWrite);

        String toWriteString = "";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        toWriteString = "Average control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averagePlatformTime(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averagePlatformTime(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averagePlatformTimeForActiveUsers(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averagePlatformTimeForActiveUsers(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + activePlatformUsers(controlGroup.values(), i);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + activePlatformUsers(testGroup.values(), i);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "% active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", activePlatformUsers(controlGroup.values(), i) * 100.0 / 5481);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "% active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", activePlatformUsers(testGroup.values(), i) * 100.0 / 5462);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

    }

    private static void writeVideoTimeAnalysis(CSVWriter output, int endWeek) {

        String[] toWrite = {"Video Time [s]"};

        output.writeNext(toWrite);

        String toWriteString = "";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        toWriteString = "Average control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageVideoTime(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageVideoTime(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageVideoTimeForActiveUsers(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageVideoTimeForActiveUsers(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + activeVideoUsers(controlGroup.values(), i);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + activeVideoUsers(testGroup.values(), i);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "% active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", activeVideoUsers(controlGroup.values(), i) * 100.0 / 5481);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "% active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", activeVideoUsers(testGroup.values(), i) * 100.0 / 5462);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

    }

    private static void writeRatioAnalysis(CSVWriter output, int endWeek) {

        String[] toWrite = {"Ratio video/platform time"};

        output.writeNext(toWrite);

        String toWriteString = "";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        toWriteString = "Average control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageRatio(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageRatio(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageRatioForActiveUsers(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageRatioForActiveUsers(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

    }

    private static void writeVideosAnalysis(CSVWriter output, int endWeek) {

        String[] toWrite = {"Videos"};

        output.writeNext(toWrite);

        String toWriteString = "";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        toWriteString = "Average control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageVideos(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageVideos(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageVideosForActiveUsers(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageVideosForActiveUsers(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

    }

    private static void writeAssignmentAnalysis(CSVWriter output, int endWeek) {

        String[] toWrite = {"Assignments"};

        output.writeNext(toWrite);

        String toWriteString = "";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        toWriteString = "Average control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageAssignments(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageAssignments(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageAssignmentsForActiveUsers(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageAssignmentsForActiveUsers(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + activeAssignmentUsers(controlGroup.values(), i);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + activeAssignmentUsers(testGroup.values(), i);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "% active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", activeAssignmentUsers(controlGroup.values(), i) * 100.0 / 5481);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "% active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", activeAssignmentUsers(testGroup.values(), i) * 100.0 / 5462);
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

    }

    private static void writeUntilDeadlineAnalysis(CSVWriter output, int endWeek) {

        String[] toWrite = {"Time until deadline [h]"};

        output.writeNext(toWrite);

        String toWriteString = "";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        toWriteString = "Average control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageUntilDeadline(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageUntilDeadline(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active control group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageUntilDeadlineForActiveUsers(controlGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

        toWriteString = "Average active test group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#" + String.format("%.2f", averageUntilDeadlineForActiveUsers(testGroup.values(), i));
        toWrite = toWriteString.split("#");
        output.writeNext(toWrite);

    }

    private static double averagePlatformTime(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToInt(value -> value.getPlatformTime(week))
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static double averagePlatformTimeForActiveUsers(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToInt(user -> user.getPlatformTime(week))
                .filter(value -> value > 0)
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static long activePlatformUsers(Collection<UserForGraphGeneration> users, int week) {
        return users.stream()
                .mapToInt(user -> user.getPlatformTime(week))
                .filter(value -> value > 0)
                .count();
    }

    private static double averageVideoTime(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToInt(value -> value.getVideoTime(week))
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static double averageVideoTimeForActiveUsers(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToInt(user -> user.getVideoTime(week))
                .filter(value -> value > 0)
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static long activeVideoUsers(Collection<UserForGraphGeneration> users, int week) {
        return users.stream()
                .mapToInt(user -> user.getVideoTime(week))
                .filter(value -> value > 0)
                .count();
    }

    private static double averageRatio(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToDouble(value -> value.getRatioTime(week))
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static double averageRatioForActiveUsers(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToDouble(user -> user.getRatioTime(week))
                .filter(value -> value > 0)
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static double averageVideos(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToInt(value -> value.getDistinctVideos(week))
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static double averageVideosForActiveUsers(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToInt(user -> user.getDistinctVideos(week))
                .filter(value -> value > 0)
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static double averageAssignments(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToInt(value -> value.getAssignments(week))
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static double averageAssignmentsForActiveUsers(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToInt(user -> user.getAssignments(week))
                .filter(value -> value > 0)
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static long activeAssignmentUsers(Collection<UserForGraphGeneration> users, int week) {
        return users.stream()
                .mapToInt(user -> user.getAssignments(week))
                .filter(value -> value > 0)
                .count();
    }

    private static double averageUntilDeadline(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToInt(value -> value.getUntilDeadline(week))
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static double averageUntilDeadlineForActiveUsers(Collection<UserForGraphGeneration> users, int week) {
        OptionalDouble result = users.stream()
                .mapToInt(user -> user.getUntilDeadline(week))
                .filter(value -> value > 0)
                .average();

        if (result.isPresent())
            return result.getAsDouble();

        return 0;
    }

    private static void writePlatformTime(HashMap<String, UserForGraphGeneration> users, String filename, int endWeek) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        UserForGraphGeneration current;

        String toWriteString = "User_id";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            for (int i = 1; i <= endWeek; i++)
                toWriteString += "#" + String.valueOf(current.getPlatformTime(i));
            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeVideoTime(HashMap<String, UserForGraphGeneration> users, String filename, int endWeek) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        UserForGraphGeneration current;

        String toWriteString = "User_id";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            for (int i = 1; i <= endWeek; i++)
                toWriteString += "#" + String.valueOf(current.getVideoTime(i));
            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeRatioTime(HashMap<String, UserForGraphGeneration> users, String filename, int endWeek) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        UserForGraphGeneration current;

        String toWriteString = "User_id";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            for (int i = 1; i <= endWeek; i++)
                toWriteString += "#" + String.valueOf(current.getRatioTime(i));
            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeVideos(HashMap<String, UserForGraphGeneration> users, String filename, int endWeek) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        UserForGraphGeneration current;

        String toWriteString = "User_id";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            for (int i = 1; i <= endWeek; i++)
                toWriteString += "#" + String.valueOf(current.getDistinctVideos(i));
            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeAssignments(HashMap<String, UserForGraphGeneration> users, String filename, int endWeek) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        UserForGraphGeneration current;

        String toWriteString = "User_id";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            for (int i = 1; i <= endWeek; i++)
                toWriteString += "#" + String.valueOf(current.getAssignments(i));
            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeUntilDeadline(HashMap<String, UserForGraphGeneration> users, String filename, int endWeek) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        UserForGraphGeneration current;

        String toWriteString = "User_id";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            toWriteString = entry.getKey();

            for (int i = 1; i <= endWeek; i++)
                toWriteString += "#" + String.valueOf(current.getUntilDeadline(i));
            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }


    //========= Engagement ==========
    //-------- Day users drop out ---------

    private static void lastSession(int endWeek) throws IOException, ParseException {

        readSessions(endWeek);

        writeSessions(controlGroupSession, "data\\2016\\post-data\\control_last_session_" + endWeek + ".csv");
        writeSessions(testGroupSession, "data\\2016\\post-data\\test_last_session_" + endWeek + ".csv");

        aggregateLastSessions(testGroupSession, testAggregatedSessions);
        aggregateLastSessions(controlGroupSession, controlAggregatedSessions);

        writeAggregatedSessions(testAggregatedSessions, "data\\2016\\post-data\\test_aggregated_sessions_" + endWeek + ".csv");
        writeAggregatedSessions(controlAggregatedSessions, "data\\2016\\post-data\\control_aggregated_sessions_" + endWeek + ".csv");
    }

    private static void readSessions(int weekToRead) throws IOException, ParseException {
        //session_id, course_user_id, start_time, duration
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\week" + weekToRead + "\\sessions.csv"));
        String[] nextLine;
        String shortId;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            if( nextLine[1].compareTo("course-v1:DelftX+CTB3365DWx+1T2016_None") == 0)
                continue;

            shortId = nextLine[1].substring(nextLine[1].indexOf("1T2016_") + 7);
            System.out.println(shortId);

            if (testGroupSession.containsKey(shortId)) {
                if(testGroupSession.get(shortId).compareTo(getDateFromString(nextLine[2])) > 0)
                    testGroupSession.put(shortId, getDateFromString(nextLine[2]));
                continue;
            }

            if (controlGroupSession.containsKey(shortId)) {
                if(controlGroupSession.get(shortId).compareTo(getDateFromString(nextLine[2])) > 0)
                    controlGroupSession.put(shortId, getDateFromString(nextLine[2]));
                continue;
            }

            if (Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0)
                testGroupSession.put(shortId, getDateFromString(nextLine[2]));
            else
                controlGroupSession.put(shortId, getDateFromString(nextLine[2]));

        }

        csvReader.close();

        System.out.println("control: " + controlGroupSession.size());
        System.out.println("test: " + testGroupSession.size());
    }

    private static void aggregateLastSessions(HashMap<String, Date> lastSessions, HashMap<String, Integer> aggregatedSessions) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String day;
        for (Map.Entry<String, Date> entry : lastSessions.entrySet()) {
            day = dateFormat.format(entry.getValue());

            if(aggregatedSessions.containsKey(day))
                aggregatedSessions.put(day, aggregatedSessions.get(day) + 1);
            else
                aggregatedSessions.put(day, 1);

        }
    }

    private static void writeSessions(HashMap<String, Date> lastSessions, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH");

        toWrite = "User_id#Last session".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, Date> entry : lastSessions.entrySet()) {
            toWrite = (timeFormat.format(entry.getValue()) + "#" + dateFormat.format(entry.getValue())).split("#");
            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeAggregatedSessions(HashMap<String, Integer> aggregatedSessions, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "Day#Count".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, Integer> entry : aggregatedSessions.entrySet()) {
            toWrite = (entry.getKey() + "#" + entry.getValue()).split("#");
            output.writeNext(toWrite);
        }

        output.close();
    }

    //-------- Unique users per day/week ---------

    private static void uniqueUsers(int endWeek) throws IOException, ParseException {

        readUniqueUsers(endWeek);

        writeUniqueUsersPerDay(testUniquePerDay, "data\\2016\\post-data\\engagement\\test_unique_daily_" + endWeek + ".csv");
        writeUniqueUsersPerDay(controlUniquePerDay, "data\\2016\\post-data\\engagement\\control_unique_daily_" + endWeek + ".csv");

        writeUniqueUsersPerWeek(testUniquePerWeek, "data\\2016\\post-data\\engagement\\test_unique_weekly_" + endWeek + ".csv");
        writeUniqueUsersPerWeek(controlUniquePerWeek, "data\\2016\\post-data\\engagement\\control_unique_weekly_" + endWeek + ".csv");

        System.out.println("Test: " + testUniquePerDay.size());
        System.out.println("Control: " + controlUniquePerDay.size());

    }

    private static void readUniqueUsers(int endWeek) throws IOException, ParseException {
        //session_id, course_user_id, start_time, duration
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\week" + endWeek + "\\sessions.csv"));
        String[] nextLine;
        String day;
        int week;
        String shortId;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            if( nextLine[1].compareTo("course-v1:DelftX+CTB3365DWx+1T2016_None") == 0)
                continue;

            shortId = nextLine[1].substring(nextLine[1].indexOf("1T2016_") + 7);
            day = nextLine[2].substring(0, 10);
            week = getWeekFromDate(day);

            if (Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0) {
                addUniqueUser(testUniquePerDay, day, shortId);
                addUniqueUserPerWeek(testUniquePerWeek, week, shortId);
            }
            else {
                addUniqueUser(controlUniquePerDay, day, shortId);
                addUniqueUserPerWeek(controlUniquePerWeek, week, shortId);
            }

        }

        csvReader.close();

    }

    private static void addUniqueUser(HashMap<String, List<String>> uniquePerDay, String day, String shortId) {
        List<String> uniqueUsers = uniquePerDay.get(day);

        if (uniqueUsers == null) {
            uniqueUsers = new ArrayList<>();
            uniqueUsers.add(shortId);
            uniquePerDay.put(day, uniqueUsers);
        }
        else if(!uniqueUsers.contains(shortId))
            uniqueUsers.add(shortId);

    }

    private static void addUniqueUserPerWeek(HashMap<Integer, List<String>> uniquePerWeek, int week, String shortId) {
        List<String> uniqueUsers = uniquePerWeek.get(week);

        if (uniqueUsers == null) {
            uniqueUsers = new ArrayList<>();
            uniqueUsers.add(shortId);
            uniquePerWeek.put(week, uniqueUsers);
        }
        else if(!uniqueUsers.contains(shortId))
            uniqueUsers.add(shortId);
    }

    private static void writeUniqueUsersPerWeek(HashMap<Integer, List<String>> uniquePerWeek, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "Week#Count".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<Integer, List<String>> entry : uniquePerWeek.entrySet()) {
            toWrite = (entry.getKey() + "#" + entry.getValue().size()).split("#");
            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeUniqueUsersPerDay(HashMap<String, List<String>> uniquePerDay, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "Day#Count".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, List<String>> entry : uniquePerDay.entrySet()) {
            toWrite = (entry.getKey() + "#" + entry.getValue().size()).split("#");
            output.writeNext(toWrite);
        }

        output.close();
    }


    //========= Threshold Comparison ==========

//************************
//************ Utils

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
