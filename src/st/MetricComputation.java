package st;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ioana on 4/7/2016.
 */
public class MetricComputation {

    static HashMap<String, UserMetricComputation> users = new HashMap<>();
    static HashMap<String, Integer> problems = new HashMap<>();

    public static void computeMetrics2015(int week) throws IOException, ParseException {
        readUsers(users, "data\\st\\2015\\ST2015_graduates.csv");
        readSessions("data\\st\\2015\\sessions.csv");

        readForumSessions("data\\st\\2015\\forum_sessions.csv");

        readProblems("data\\st\\2015\\problems.csv");
        readSubmissions("data\\st\\2015\\submissions.csv");

        writeMetrics(users, week, "data\\st\\2015\\output\\ST2015_metrics_" + week + ".csv");
    }

    public static void computeMetrics(int week) throws IOException, ParseException {
        //readUsers(users, "data\\st\\2016\\ST2016_test.csv");
        //readUsers(users, "data\\st\\2016\\ST2016_control.csv");
        readUsers(users, "data\\st\\2016\\ST2016_student_profile.csv");

        System.out.println(users.size());

        readSessions("data\\st\\2016\\week" + week + "\\data\\sessions.csv");

        readForumSessions("data\\st\\2016\\week" + week + "\\data\\forum_sessions.csv");

        readProblems("data\\st\\2016\\week" + week + "\\data\\problems.csv");
        readSubmissions("data\\st\\2016\\week" + week + "\\data\\submissions.csv");

        writeMetrics(users, week, "data\\st\\2016\\week" + week + "\\metrics\\ST2016_metrics_all.csv");
        writeMetricsForWidget(users, week, "data\\st\\2016\\week" + week + "\\metrics\\ST2016_metrics.csv");
    }

    //READ
    //Reads from a file with ids on the first column and creates a Hashmap of UserMetricComputation objects with the id as key
    private static void readUsers(HashMap<String, UserMetricComputation> group, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null)
            group.put(nextLine[0], new UserMetricComputation(nextLine[0]));

        csvReader.close();
    }

    private static void readSessions(String filename) throws IOException, ParseException {
        //session_id, course_user_id, start_time, duration
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine, session_attr;
        int duration, week;

        while ((nextLine = csvReader.readNext()) != null) {
            session_attr = nextLine[0].split("_");

            //System.out.println(session_attr[3]);

            if(users.containsKey(session_attr[2])) {
                duration = Integer.parseInt(nextLine[2]);
                week = ST_getWeek(session_attr[3]);

                if(week == 99)
                    continue;

                users.get(session_attr[2]).addSession(week, duration, session_attr[3], session_attr[4]);

            }
        }

        csvReader.close();
    }

    private static void readForumSessions(String filename) throws IOException, ParseException {
        //session_id, course_user_id, ??, start_time, end_time, duration
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine, session_attr;
        int duration, week;

        while ((nextLine = csvReader.readNext()) != null) {
            session_attr = nextLine[0].split("_");

            if(users.containsKey(session_attr[3])) {

                week = ST_getWeek(nextLine[3]);

                if(week == 99)
                    continue;

                users.get(session_attr[3]).addForumSession(week);

            }
        }

        csvReader.close();
    }

    private static void readProblems(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        int week;
        String problemId;

        while ((nextLine = csvReader.readNext()) != null) {
            if(nextLine[1].startsWith("Weekly assessment")) {
                week = Integer.parseInt(nextLine[2]) + 1;
                problemId = nextLine[0].substring(nextLine[0].indexOf("block@") + 6);
                problems.put(problemId, week);
            }
        }

        csvReader.close();

        System.out.println("Problems read: " + problems.size());
    }

    private static void readSubmissions(String filename) throws IOException, ParseException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine, user_id;
        UserMetricComputation user;
        int week;
        String submissionTime;
        int sub = 0;

        while ((nextLine = csvReader.readNext()) != null) {
            if(nextLine[3].compareTo("problem_graded") != 0)
                continue;

            user_id = nextLine[1].split("_");
            user = users.get(user_id[1]);
            if (user == null)    //user are not in the test base -> ignore submission
                continue;


            if(!problems.containsKey(nextLine[4]))   //ignore problems that are not graded
                continue;

            //todo: check the submission time format
            submissionTime = nextLine[5].substring(0, 19);

            week = ST_getWeek(nextLine[5]);
            if(week > 10)
                continue;

            //String problemId, int submissionWeek, Date submissionTime, Date problemDeadline
            user.addSubmission(nextLine[4], week, submissionTime, getProblemDeadline(problems.get(nextLine[4])));

            sub++;
        }

        csvReader.close();

        System.out.println("Submissions read: " + sub);
    }

    //WRITE
    private static void writeMetrics(HashMap<String, UserMetricComputation> group, int week, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        UserMetricComputation current;

        toWrite = "User_id#Sessions/week#Length of session#Between sessions#Forum sessions#Assignments#Until deadline#Sessions".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserMetricComputation> entry : users.entrySet()) {
            current = entry.getValue();
            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(current.getSessionsPerWeek(week));
            toWrite[2] = String.valueOf(current.getAverageSessionLength(week));
            toWrite[3] = String.valueOf(current.getAverageTimeBetweenSessions(week));
            toWrite[4] = String.valueOf(current.getForumSessions(week));
            toWrite[5] = String.valueOf(current.getQuizSubmissions(week));
            toWrite[6] = String.valueOf(current.getRecommendedTimeliness(week));
            toWrite[7] = String.valueOf(current.getSessions(week));

            output.writeNext(toWrite);
        }
        output.close();
    }

    private static void writeMetricsForWidget(HashMap<String, UserMetricComputation> group, int week, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        UserMetricComputation current;

        toWrite = "User_id#Sessions/week#Length of session#Between sessions#Forum sessions#Assignments#Until deadline#Sessions".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserMetricComputation> entry : users.entrySet()) {

            if(Integer.parseInt(entry.getKey()) % 2 == 1)
                continue;

            current = entry.getValue();
            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(current.getSessionsPerWeek(week));
            toWrite[2] = String.valueOf(current.getAverageSessionLength(week));
            toWrite[3] = String.valueOf(current.getAverageTimeBetweenSessions(week));
            toWrite[4] = String.valueOf(current.getForumSessions(week));
            toWrite[5] = String.valueOf(current.getQuizSubmissions(week));
            toWrite[6] = String.valueOf(current.getRecommendedTimeliness(week));
            toWrite[7] = String.valueOf(current.getSessions(week));

            output.writeNext(toWrite);
        }
        output.close();
    }

    //UTILS
    private static int ST_getWeek(String startTime){
        if(startTime.compareTo("2016-04-12") > 0 && startTime.compareTo("2016-04-19") < 0)
            return 1;
        if(startTime.compareTo("2016-04-19") > 0 && startTime.compareTo("2016-04-26") < 0)
            return 2;
        if(startTime.compareTo("2016-04-26") > 0 && startTime.compareTo("2016-05-03") < 0)
            return 3;
        if(startTime.compareTo("2016-05-03") > 0 && startTime.compareTo("2016-05-10") < 0)
            return 4;
        if(startTime.compareTo("2016-05-10") > 0 && startTime.compareTo("2016-05-17") < 0)
            return 5;
        if(startTime.compareTo("2016-05-17") > 0 && startTime.compareTo("2016-05-24") < 0)
            return 6;
        if(startTime.compareTo("2016-05-24") > 0 && startTime.compareTo("2016-05-31") < 0)
            return 7;
        if(startTime.compareTo("2016-05-31") > 0 && startTime.compareTo("2016-06-07") < 0)
            return 8;
        if(startTime.compareTo("2016-06-07") > 0 && startTime.compareTo("2016-06-14") < 0)
            return 9;
        if(startTime.compareTo("2016-06-14") > 0 && startTime.compareTo("2016-06-20") < 0)
            return 10;
        return 99;
    }

    private static int ST_getWeek2015(String startTime){
        if(startTime.compareTo("2015-01-27") > 0 && startTime.compareTo("2015-02-03") < 0)
            return 1;
        if(startTime.compareTo("2015-02-03") > 0 && startTime.compareTo("2015-02-10") < 0)
            return 2;
        if(startTime.compareTo("2015-02-10") > 0 && startTime.compareTo("2015-02-17") < 0)
            return 3;
        if(startTime.compareTo("2015-02-17") > 0 && startTime.compareTo("2015-02-24") < 0)
            return 4;
        if(startTime.compareTo("2015-02-24") > 0 && startTime.compareTo("2015-03-03") < 0)
            return 5;
        if(startTime.compareTo("2015-03-03") > 0 && startTime.compareTo("2015-03-10") < 0)
            return 6;
        if(startTime.compareTo("2015-03-10") > 0 && startTime.compareTo("2015-03-17") < 0)
            return 7;
        if(startTime.compareTo("2015-03-17") > 0 && startTime.compareTo("2015-03-24") < 0)
            return 8;
        if(startTime.compareTo("2015-03-24") > 0 && startTime.compareTo("2015-03-31") < 0)
            return 9;
        if(startTime.compareTo("2015-03-31") > 0 && startTime.compareTo("2015-04-08") < 0)
            return 10;
        return 99;
    }

    private static String getProblemDeadlineForProblem2014(int problemWeek) throws ParseException{
        String deadline;

        switch (problemWeek) {
            case 2:
                deadline = "2015-02-17";
                break;
            case 3:
                deadline = "2015-02-24";
                break;
            case 4:
                deadline = "2015-03-03";
                break;
            case 5:
                deadline = "2015-03-10";
                break;
            case 6:
                deadline = "2015-03-17";
                break;
            case 7:
                deadline = "2015-03-24";
                break;
            default:
                deadline = "2015-04-07";
        }

        return deadline + " 12:00:00";
    }

    private static String getProblemDeadline(int problemWeek) throws ParseException{
        String deadline;

        switch (problemWeek) {
            case 2:
                deadline = "2016-05-03";
                break;
            case 3:
                deadline = "2016-05-10";
                break;
            case 4:
                deadline = "2016-05-17";
                break;
            case 5:
                deadline = "2016-05-24";
                break;
            case 6:
                deadline = "2016-05-31";
                break;
            case 7:
                deadline = "2016-06-07";
                break;
            default:
                deadline = "2015-06-20";
        }

        return deadline + " 12:00:00";
    }

    private static Date getDateFromString(String dateString) throws ParseException {
        //input date: "2014-11-11 12:00:00"
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(dateString);
        }
        catch (ParseException e) {
            return format.parse(dateString.replace('T', ' ').substring(0, dateString.length() - 2));
        }
    }




    //OTHERS = each metrics written individually
    private static void writeSessions(int endWeek, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        String toWriteString;
        UserMetricComputation current;

        toWriteString = "User";
        for(int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserMetricComputation> entry : users.entrySet()) {
            current = entry.getValue();
            toWrite[0] = entry.getKey();

            for(int i = 1; i <= endWeek; i++)
                toWrite[i] = String.valueOf(current.getSessions(i));

            output.writeNext(toWrite);
        }
        output.close();
    }

    private static void writeAverageTimePerSession(int endWeek, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        String toWriteString;
        UserMetricComputation current;

        toWriteString = "User";
        for(int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserMetricComputation> entry : users.entrySet()) {
            current = entry.getValue();
            toWrite[0] = entry.getKey();

            for(int i = 1; i <= endWeek; i++)
                toWrite[i] = String.valueOf(current.getAverageSessionLength(i));

            output.writeNext(toWrite);
        }
        output.close();
    }

    private static void writeAverageSessionsPerWeek(int endWeek, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        String toWriteString;
        UserMetricComputation current;

        toWriteString = "User";
        for(int i = 1; i <= endWeek; i++)
            toWriteString += "#Week " + i;
        toWrite = toWriteString.split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserMetricComputation> entry : users.entrySet()) {
            current = entry.getValue();
            toWrite[0] = entry.getKey();

            for(int i = 1; i <= endWeek; i++)
                toWrite[i] = String.valueOf(current.getSessionsPerWeek(i));

            output.writeNext(toWrite);
        }
        output.close();
    }
}
