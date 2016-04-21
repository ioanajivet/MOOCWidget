package dwt; /**
 * Created by Ioana on 3/1/2016.
 */


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class WorkingData {

    private static List<String> activeTestUserIds;
    private static List<String> activeControlUserIds;

    static HashMap<String, UserForGraphGeneration> testGroup;
    static HashMap<String, UserForGraphGeneration> controlGroup;

    static HashMap<String, Integer> problemsWeek;
    //static HashMap<Integer, ArrayList<String>> problemsPerWeek;
    static HashMap<String, List<String>> testAssignments;
    static HashMap<String, List<String>> controlAssignments;

    static HashMap<String, HashMap<Integer, Integer>> sessionsPerWeek;


    public static void main(String[] args) throws IOException, ParseException {
        int endWeek = 9;

        initialize();

        //1. Select active users
        //selectActiveUsers(endWeek);

        //2. Generate data for analysis only for active users = registered at least one session
        //readActive(endWeek);
        //readMetrics("data\\2016\\user_metrics\\", endWeek);
        //writeAllMetricsForAWeek(endWeek, "data\\2016\\post-data\\active_");

        //3. Generate data for analysis for all users
        //readUsers();
        //readMetrics("data\\2016\\user_metrics\\");
        //writeAllMetricsForAWeek(endWeek, "data\\2016\\post-data\\");

        //4. Calculate non-graded assignments submitted for all users
        //readUsersAssignments();
        //readProblems();
        //readSubmissions(endWeek);
        //writeNonGradedAssignments(endWeek, "data\\2016\\post-data\\non-graded_assignments_" + endWeek + ".csv");

        //5. Calculate non-graded assignments submitted for active users
        readActiveUsersAssignments(testAssignments,  "data\\2016\\post-data\\all_metrics_test_5min_" + endWeek + ".csv");
        readActiveUsersAssignments(controlAssignments, "data\\2016\\post-data\\all_metrics_control_5min_" + endWeek + ".csv");
        readProblems();
        readSubmissions(endWeek);
        writeNonGradedAssignments(endWeek, "data\\2016\\post-data\\non-graded_assignments_active_" + endWeek + ".csv");

        //6. Number of sessions per week
        //selectActiveUsers(endWeek);
        //readSessionsPerWeek(endWeek);
        //writeSessionsPerWeek(endWeek, "data\\2016\\post-data\\sessionsPerWeek_" + endWeek + ".csv");

        //7. Active users (>X min spent on the platform)
        //readUsersMoreThanXPlatformMins(600, endWeek);
        //readMetrics("data\\2016\\user_metrics\\", endWeek);
        //writeMetricsForMoreThanXMin(endWeek, "data\\2016\\post-data\\", 600);

        //8. Active users (>X min spent on the platform && >5 submitted assignments)
        //readUsersMoreThanXPlatformMinsAndAssignments(600, 5, endWeek);
        //readMetrics("data\\2016\\user_metrics\\", endWeek);
        //writeMetricsForMoreThanXMinAndAssignments(endWeek, "data\\2016\\post-data\\", 600, 5);
    }

    private static void writeSessionsPerWeek(int endWeek, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        String toWriteString;
        String shortId;
        HashMap<Integer, Integer> current;

        toWriteString = "Group";
        for (int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;

        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, HashMap<Integer, Integer>> entry : sessionsPerWeek.entrySet()) {
            current = entry.getValue();
            shortId = entry.getKey();

            if(Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0)
                toWriteString = "Test";
            else
                toWriteString = "Control";

            for(int i = 1; i <= endWeek; i++)
                if(current.containsKey(i))
                    toWriteString += "#" + current.get(i);
                else
                    toWriteString += "#0";

            toWrite = toWriteString.split("#");

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void readSessionsPerWeek(int endWeek) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\week" + endWeek + "\\sessions.csv"));
        String [] nextLine;
        String shortId;
        int week;
        HashMap<Integer, Integer> weeks;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            shortId = nextLine[1].substring(nextLine[1].indexOf("1T2016_") + 7);

            week = getWeekFromDate(nextLine[2]);

            if(!activeControlUserIds.contains(shortId) && !activeTestUserIds.contains(shortId))
                continue;

            if(sessionsPerWeek.containsKey(shortId)){
                weeks = sessionsPerWeek.get(shortId);
                if(weeks.containsKey(week))
                    weeks.put(week, weeks.get(week) + 1);
                else
                    weeks.put(week, 1);
            }
            else {
                weeks = new HashMap<>();
                weeks.put(week, 1);
                sessionsPerWeek.put(shortId, weeks);
            }

        }

        csvReader.close();

        System.out.println("Active users with sessions: " + sessionsPerWeek.size());
    }

    private static int getWeekFromDate(String startTime) {
        if(startTime.compareTo("2016-01-12") > 0 && startTime.compareTo("2016-01-19") < 0)
            return 1;
        if(startTime.compareTo("2016-01-19") > 0 && startTime.compareTo("2016-01-26") < 0)
            return 2;
        if(startTime.compareTo("2016-01-26") > 0 && startTime.compareTo("2016-02-02") < 0)
            return 3;
        if(startTime.compareTo("2016-02-02") > 0 && startTime.compareTo("2016-02-09") < 0)
            return 4;
        if(startTime.compareTo("2016-02-09") > 0 && startTime.compareTo("2016-02-16") < 0)
            return 5;
        if(startTime.compareTo("2016-02-16") > 0 && startTime.compareTo("2016-02-23") < 0)
            return 6;
        if(startTime.compareTo("2016-02-23") > 0 && startTime.compareTo("2016-03-01") < 0)
            return 7;
        if(startTime.compareTo("2016-03-01") > 0 && startTime.compareTo("2016-03-08") < 0)
            return 8;
        if(startTime.compareTo("2016-03-08") > 0 && startTime.compareTo("2016-03-15") < 0)
            return 9;
        if(startTime.compareTo("2016-03-15") > 0 && startTime.compareTo("2016-03-22") < 0)
            return 10;
        if(startTime.compareTo("2016-03-22") > 0 && startTime.compareTo("2016-03-29") < 0)
            return 11;
        return 99;
    }

    private static void readUsersAssignments() throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\user_pii.csv"));
        String[] nextLine;
        String shortId;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            shortId = nextLine[0].substring(nextLine[0].indexOf("1T2016_") + 7);

            if (Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0)
                testAssignments.put(shortId, new ArrayList<>());
            else
                controlAssignments.put(shortId, new ArrayList<>());
        }

        csvReader.close();

        System.out.println("Users under test: " + testAssignments.size());
        System.out.println("Control group: " + controlAssignments.size());

    }

    private static void readActiveUsersAssignments(HashMap<String, List<String>> group, String filepath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filepath));
        String[] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null)
            group.put(nextLine[0], new ArrayList<>());

        csvReader.close();

    }

    private static void readProblems() throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\problems_non.csv"));
        String [] nextLine;
        String problemId;
        int week;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            week = Integer.parseInt(nextLine[3]) + 1;

            problemId = nextLine[0].substring(nextLine[0].indexOf("block@") + 6);

            problemsWeek.put(problemId, week);

            /*if(!problemsPerWeek.containsKey(week))
                problemsPerWeek.put(week, new ArrayList<>());

            problemsPerWeek.get(week).add(problemId);*/

        }

        csvReader.close();

        System.out.println("Problems read: " + problemsWeek.size());
    }

    private static void readSubmissions(int endWeek) throws IOException,ParseException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\week" + endWeek + "\\submissions.csv"));
        String [] nextLine;
        String problemId, shortId;
        List<String> submitted;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            problemId = nextLine[2].substring(nextLine[2].indexOf("block@") + 6);

            if(!problemsWeek.containsKey(problemId))   //ignore problems that are graded
                continue;

            shortId = nextLine[1].substring(nextLine[1].indexOf("1T2016_") + 7);

            submitted = testAssignments.get(shortId);

            if (submitted == null)
                submitted = controlAssignments.get(shortId);

            if (submitted == null)    //user is not an active user -> ignore submission
                continue;

            if (submitted.contains(problemId))
                continue;

            submitted.add(problemId);

        }

        csvReader.close();


    }

    private static void writeNonGradedAssignments(int endWeek, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        List<String> current;

        toWrite = "Group#Assignments submitted".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, List<String>> entry : testAssignments.entrySet()) {
            current = entry.getValue();
            toWrite[0] = "Test";
            toWrite[1] = String.valueOf(current.size());

            output.writeNext(toWrite);
        }

        for (Map.Entry<String, List<String>> entry : controlAssignments.entrySet()) {
            current = entry.getValue();
            toWrite[0] = "Control";
            toWrite[1] = String.valueOf(current.size());

            output.writeNext(toWrite);
        }

        output.close();
    }

    //************************
    //************ Loading data

    private static void initialize() {
        testGroup = new HashMap<>();
        controlGroup = new HashMap<>();

        activeTestUserIds = new ArrayList<>();
        activeControlUserIds = new ArrayList<>();

        problemsWeek = new HashMap();
        //problemsPerWeek = new HashMap<>();
        testAssignments = new HashMap<>();
        controlAssignments = new HashMap<>();

        sessionsPerWeek = new HashMap<>();
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

        csvReader.close();

        System.out.println("Users under test: " + testGroup.size());
        System.out.println("Control group: " + controlGroup.size());

    }

    private static void readUsersMoreThanXPlatformMins(int limit, int endWeek) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\user_metrics\\metrics" + endWeek + ".csv"));
        String[] nextLine;
        String shortId;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(Integer.parseInt(nextLine[1]) < limit)
                continue;

            shortId = nextLine[0].substring(nextLine[0].indexOf("1T2016_") + 7);

            if (Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0)
                testGroup.put(shortId, new UserForGraphGeneration(shortId));
            else
                controlGroup.put(shortId, new UserForGraphGeneration(shortId));
        }

        csvReader.close();

        System.out.println("Users under test: " + testGroup.size());
        System.out.println("Control group: " + controlGroup.size());

    }

    private static void readUsersMoreThanXPlatformMinsAndAssignments(int time_limit, int assign_limit, int endWeek) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\user_metrics\\metrics" + endWeek + ".csv"));
        String[] nextLine;
        String shortId;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(Integer.parseInt(nextLine[1]) < time_limit)
                continue;

            if(Integer.parseInt(nextLine[5]) < assign_limit)
                continue;

            shortId = nextLine[0].substring(nextLine[0].indexOf("1T2016_") + 7);

            if (Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0)
                testGroup.put(shortId, new UserForGraphGeneration(shortId));
            else
                controlGroup.put(shortId, new UserForGraphGeneration(shortId));
        }

        csvReader.close();

        System.out.println("Users under test: " + testGroup.size());
        System.out.println("Control group: " + controlGroup.size());

    }

    private static void readActive(int endWeek) throws IOException {
        readActiveUsers(testGroup, "data\\2016\\post-data\\active_test_" + endWeek + ".csv");
        readActiveUsers(controlGroup, "data\\2016\\post-data\\active_control_" + endWeek + ".csv");
    }

    private static void readActiveUsers(HashMap<String, UserForGraphGeneration> group, String filepath) throws IOException {
        System.out.println("Reading active from: " + filepath);

        CSVReader csvReader = new CSVReader(new FileReader(filepath));
        String[] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null)
            group.put(nextLine[0], new UserForGraphGeneration(nextLine[0]));

        csvReader.close();
    }

    private static void readMetrics(String filepath, int endWeek) throws IOException {
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

    }

    private static void selectActiveUsers(int endWeek) throws IOException {
        //session_id, course_user_id, start_time, duration
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\week" + endWeek + "\\sessions.csv"));
        String[] nextLine;
        String shortId;

        //readUsersForActiveSelection();
        readUsers();

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            if( nextLine[1].compareTo("course-v1:DelftX+CTB3365DWx+1T2016_None") == 0)
                continue;

            shortId = nextLine[1].substring(nextLine[1].indexOf("1T2016_") + 7);

            if(testGroup.containsKey(shortId))
                if (activeTestUserIds.contains(shortId))
                    continue;
                else
                    activeTestUserIds.add(shortId);

            if(controlGroup.containsKey(shortId))
                if (activeControlUserIds.contains(shortId))
                    continue;
                else
                    activeControlUserIds.add(shortId);
           /* if(testGroup.containsKey(shortId))

            if(!users.contains(shortId))
                continue;

            if (activeTestUserIds.contains(shortId))
                continue;

            if(activeControlUserIds.contains(shortId))
                continue;

            if (Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0)
                activeTestUserIds.add(shortId);
            else
                activeControlUserIds.add(shortId);*/
        }

        csvReader.close();

        System.out.println("control: " + activeControlUserIds.size());
        System.out.println("test: " + activeTestUserIds.size());

        //=== write active users ===
        writeActive(activeTestUserIds, "data\\2016\\post-data\\active_test_" + endWeek + ".csv");
        writeActive(activeControlUserIds, "data\\2016\\post-data\\active_control_" + endWeek + ".csv");

    }

    //************************
    //************ Writing data

    private static void writeAllMetricsForAWeek(int endWeek, String filepath) throws IOException {
        writeAllMetricsForAWeekUsers(testGroup, endWeek, filepath + "all_metrics_test_" + endWeek + ".csv");
        writeAllMetricsForAWeekUsers(controlGroup, endWeek, filepath + "all_metrics_control_" + endWeek + ".csv");
    }

    private static void writeMetricsForMoreThanXMin(int endWeek, String filepath, int limit) throws IOException {
        writeAllMetricsForAWeekUsers(testGroup, endWeek, filepath + "all_metrics_test_" + (limit/60) + "min_" + endWeek + ".csv");
        writeAllMetricsForAWeekUsers(controlGroup, endWeek, filepath + "all_metrics_control_" + (limit/60) + "min_" + endWeek + ".csv");
    }

    private static void writeMetricsForMoreThanXMinAndAssignments(int endWeek, String filepath, int time_limit, int assign_limit) throws IOException {
        writeAllMetricsForAWeekUsers(testGroup, endWeek, filepath + "all_metrics_test_" + (time_limit/60) + "min_" + assign_limit + "assign_" + endWeek + ".csv");
        writeAllMetricsForAWeekUsers(controlGroup, endWeek, filepath + "all_metrics_control_" + (time_limit/60) + "min_" + assign_limit + "assign_" + endWeek + ".csv");
    }

    private static void writeAllMetricsForAWeekUsers(HashMap<String, UserForGraphGeneration> users, int endWeek, String filename) throws IOException {
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

    private static void writeActive(List<String> activeUserIds, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "User_id".split("#");

        output.writeNext(toWrite);

        for (String id: activeUserIds) {
            toWrite[0] = id;
            output.writeNext(toWrite);
        }

        output.close();
    }
}
