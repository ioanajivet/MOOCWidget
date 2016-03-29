/**
 * Created by Ioana on 3/1/2016.
 *
 * Command line arguments:
 * args[0] = week for which the metrics computation is done
 */

package analysis;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Engagement {

    static List<String> users;
    static List<String> testUsers;
    static List<String> controlUsers;

    static HashMap<String, Date> testGroupSession;
    static HashMap<String, Date> controlGroupSession;

    static HashMap<String, Integer> testAggregatedSessions;
    static HashMap<String, Integer> controlAggregatedSessions;

    static HashMap<String, List<String>> testUniquePerDay;
    static HashMap<String, List<String>> controlUniquePerDay;

    static HashMap<Integer, List<String>> testUniquePerWeek;
    static HashMap<Integer, List<String>> controlUniquePerWeek;

    static HashMap<String, Integer> testLastActivity;
    static HashMap<String, Integer> controlLastActivity;


    public static void main(String[] args) throws IOException, ParseException {
        int endWeek = 9;

        initialize();


        //** Engagement
        //lastSession(endWeek);
        //uniqueUsers(endWeek);
        //lastActivity(endWeek);
        lastActivityActiveUsers(endWeek);

    }

    private static void initialize() {

        users = new ArrayList<>();
        testUsers = new ArrayList<>();
        controlUsers = new ArrayList<>();

        testGroupSession = new HashMap<>();
        controlGroupSession = new HashMap<>();

        testAggregatedSessions = new HashMap<>();
        controlAggregatedSessions = new HashMap<>();

        testUniquePerDay = new HashMap<>();
        controlUniquePerDay = new HashMap<>();

        testUniquePerWeek = new HashMap<>();
        controlUniquePerWeek = new HashMap<>();

        testLastActivity = new HashMap<>();
        controlLastActivity = new HashMap<>();

    }


    //========= Engagement ==========
    //-------- Day users drop out ---------

    private static void lastSession(int endWeek) throws IOException, ParseException {

        readUsers();
        //readActive(endWeek);
        readSessions(endWeek);

        writeSessions(controlGroupSession, "data\\2016\\post-data\\control_last_session_" + endWeek + ".csv");
        writeSessions(testGroupSession, "data\\2016\\post-data\\test_last_session_" + endWeek + ".csv");

        //aggregateLastSessions(testGroupSession, testAggregatedSessions);
        //aggregateLastSessions(controlGroupSession, controlAggregatedSessions);

        //writeAggregatedSessions(testAggregatedSessions, "data\\2016\\post-data\\test_aggregated_sessions_" + endWeek + ".csv");
        //writeAggregatedSessions(controlAggregatedSessions, "data\\2016\\post-data\\control_aggregated_sessions_" + endWeek + ".csv");
    }

    private static void readActive(int endWeek) throws IOException {
        readActiveUsers(testUsers, "data\\2016\\post-data\\threshold_comparison\\active_test_" + endWeek + ".csv");
        readActiveUsers(controlUsers, "data\\2016\\post-data\\threshold_comparison\\active_control_" + endWeek + ".csv");
    }

    private static void readActiveUsers(List<String> group, String filepath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filepath));
        String[] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null)
            group.add(nextLine[0]);

        csvReader.close();

        System.out.println(group.size());
    }


    private static void readUsers() throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\user_pii.csv"));
        String[] nextLine;
        String shortId;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            shortId = nextLine[0].substring(nextLine[0].indexOf("1T2016_") + 7);

            users.add(shortId);
        }

        csvReader.close();

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

            if(!users.contains(shortId))
                continue;

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

        System.out.println("all control: " + controlGroupSession.size());
        System.out.println("all test: " + testGroupSession.size());
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
            toWrite = (entry.getKey() + "#" + dateFormat.format(entry.getValue())).split("#");
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


    //-------- Course persistence - last week with video watch or assignment submission ---------

    private static void lastActivity(int endWeek) throws IOException, ParseException {
        readUsers();

        readVideos(endWeek);
        readSubmissions(endWeek);

        writeActivity(testLastActivity, "data\\2016\\post-data\\test_last_activity_" + endWeek + ".csv");
        writeActivity(controlLastActivity, "data\\2016\\post-data\\control_last_activity_" + endWeek + ".csv");

    }

    private static void lastActivityActiveUsers(int endWeek) throws IOException, ParseException {

        readActiveUsers(users, "data\\2016\\post-data\\all_metrics_test_5min_" + endWeek + ".csv");
        readActiveUsers(users, "data\\2016\\post-data\\all_metrics_control_5min_" + endWeek + ".csv");

        System.out.println("Users size: " + users.size());

        readVideos(endWeek);
        readSubmissions(endWeek);

        writeActivity(testLastActivity, "data\\2016\\post-data\\test_last_activity_" + endWeek + ".csv");
        writeActivity(controlLastActivity, "data\\2016\\post-data\\control_last_activity_" + endWeek + ".csv");

    }

    private static void writeActivity(HashMap<String, Integer> group, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "User#Last activity week".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, Integer> entry : group.entrySet()) {
            toWrite = (entry.getKey() + "#" + entry.getValue()).split("#");
            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void readVideos(int endWeek) throws IOException, ParseException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\week" + endWeek + "\\observations.csv"));
        String[] nextLine;
        String shortId, timestamp;
        int week;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            if( nextLine[1].compareTo("course-v1:DelftX+CTB3365DWx+1T2016_None") == 0)
                continue;

            shortId = nextLine[1].substring(nextLine[1].indexOf("1T2016_") + 7);

            if(!users.contains(shortId))
                continue;

            timestamp = nextLine[0].substring(nextLine[0].length() - 19);

            week = getWeekFromDate(timestamp);

            if(week == 99)
                continue;

            if (testLastActivity.containsKey(shortId)) {
                if(testLastActivity.get(shortId) < week)
                    testLastActivity.put(shortId, week);
                continue;
            }

            if (controlLastActivity.containsKey(shortId)) {
                if(controlLastActivity.get(shortId) < week)
                    controlLastActivity.put(shortId, week);
                continue;
            }

            if (Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0)
                testLastActivity.put(shortId, week);
            else
                controlLastActivity.put(shortId, week);

        }

        csvReader.close();

        System.out.println("Control with last video watch: " + controlLastActivity.size());
        System.out.println("Test with last video watch: " + testLastActivity.size());

    }

    private static void readSubmissions(int endWeek) throws IOException, ParseException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\week" + endWeek + "\\submissions.csv"));
        String[] nextLine;
        String shortId, timestamp;
        int week;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            if( nextLine[1].compareTo("course-v1:DelftX+CTB3365DWx+1T2016_None") == 0)
                continue;

            shortId = nextLine[1].substring(nextLine[1].indexOf("1T2016_") + 7);

            if(!users.contains(shortId))
                continue;

            timestamp = nextLine[3];
            week = getWeekFromDate(timestamp);

            if(week == 99)
                System.out.println(timestamp);

            if (testLastActivity.containsKey(shortId)) {
                if(testLastActivity.get(shortId) < week)
                    testLastActivity.put(shortId, week);
                continue;
            }

            if (controlLastActivity.containsKey(shortId)) {
                if(controlLastActivity.get(shortId) < week)
                    controlLastActivity.put(shortId, week);
                continue;
            }

            if (Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0)
                testLastActivity.put(shortId, week);
            else
                controlLastActivity.put(shortId, week);

        }

        csvReader.close();

        System.out.println("Control with last submission: " + controlLastActivity.size());
        System.out.println("Test with last submission: " + testLastActivity.size());

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
