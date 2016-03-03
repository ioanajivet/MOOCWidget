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

public class PostDataAnalysis {

    static HashMap<String, UserForGraphGeneration> testGroup;
    static HashMap<String, UserForGraphGeneration> controlGroup;

    static HashMap<String, Date> testGroupSession;
    static HashMap<String, Date> controlGroupSession;

    static HashMap<String, Integer> testAggregatedSessions;
    static HashMap<String, Integer> controlAggregatedSessions;

    static double[] thresholds;

    public static void main(String[] args) throws IOException, ParseException {
        int endWeek = 7;

        initialize();

        //readUsers();
        //readMetrics("data\\2016\\user_metrics\\");

        //writeMetrics(endWeek);
        //analyseMetrics(endWeek);

        lastSession(endWeek);

    }

    private static void lastSession(int endWeek) throws IOException, ParseException {

        readSessions(endWeek);

        writeSessions(controlGroupSession, "data\\2016\\post-data\\control_last_session_" + endWeek + ".csv");
        writeSessions(testGroupSession, "data\\2016\\post-data\\test_last_session_" + endWeek + ".csv");

        aggregateLastSessions(testGroupSession, testAggregatedSessions);
        aggregateLastSessions(controlGroupSession, controlAggregatedSessions);

        writeAggregatedSessions(testAggregatedSessions, "data\\2016\\post-data\\test_aggregated_sessions_" + endWeek + ".csv");
        writeAggregatedSessions(controlAggregatedSessions, "data\\2016\\post-data\\control_aggregated_sessions_" + endWeek + ".csv");
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

    //************************
    //************ Loading data

    private static void initialize() {
        testGroup = new HashMap<>();
        controlGroup = new HashMap<>();

        testGroupSession = new HashMap<>();
        controlGroupSession = new HashMap<>();

        testAggregatedSessions = new HashMap<>();
        controlAggregatedSessions = new HashMap<>();

        thresholds = new double[6];

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

    private static void readMetrics(String filepath) throws IOException {
        //TODO: update end week
        for (int i = 1; i < 8; i++)
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

    //************************
    //************ Writing data

    private static void writeMetrics(int endWeek) throws IOException {
        writePlatformTime(testGroup, "data\\2016\\post-data\\test_after_week" + endWeek + "_platformTime.csv", endWeek);
        writePlatformTime(controlGroup, "data\\2016\\post-data\\control_after_week" + endWeek + "_platformTime.csv", endWeek);

        writeVideoTime(testGroup, "data\\2016\\post-data\\test_after_week" + endWeek + "_videoTime.csv", endWeek);
        writeVideoTime(controlGroup, "data\\2016\\post-data\\control_after_week" + endWeek + "_videoTime.csv", endWeek);

        writeRatioTime(testGroup, "data\\2016\\post-data\\test_after_week" + endWeek + "_ratioTime.csv", endWeek);
        writeRatioTime(controlGroup, "data\\2016\\post-data\\control_after_week" + endWeek + "_ratioTime.csv", endWeek);

        writeVideos(testGroup, "data\\2016\\post-data\\test_after_week" + endWeek + "_videos.csv", endWeek);
        writeVideos(controlGroup, "data\\2016\\post-data\\control_after_week" + endWeek + "_videos.csv", endWeek);

        writeAssignments(testGroup, "data\\2016\\post-data\\test_after_week" + endWeek + "_assignments.csv", endWeek);
        writeAssignments(controlGroup, "data\\2016\\post-data\\control_after_week" + endWeek + "_assignments.csv", endWeek);

        writeUntilDeadline(testGroup, "data\\2016\\post-data\\test_after_week" + endWeek + "_untilDeadline.csv", endWeek);
        writeUntilDeadline(controlGroup, "data\\2016\\post-data\\control_after_week" + endWeek + "_untilDeadline.csv", endWeek);
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

    //************************
    //************ Analysis

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


    //************************
    //************ Utils

    private static Date getDateFromString(String dateString) throws ParseException {
        //input date: "2014-11-11 12:00:00"
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return format.parse(dateString);
    }

}