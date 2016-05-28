package ri;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Ioana on 4/7/2016.
 */
public class MetricComputation {

    static HashMap<String, UserMetricComputation> users = new HashMap<>();
    static HashMap<String, Integer> problems = new HashMap<>();

    public static void readData2014() throws IOException, ParseException {
        readUsers(users, "data\\ri\\2014\\RI2014_graduates.csv");
        //readSessions("data\\ri\\2014\\sessions.csv");
        readNewSessions("data\\ri\\2014\\new_sessions.csv");

        //readForumSessions("data\\ri\\2014\\new_forum_sessions.csv");
        //readQuizSessions("data\\ri\\2014\\new_quiz_sessions.csv");
        //readObservations("data\\ri\\2014\\observations.csv");
        readActivitySessions("data\\ri\\2014\\activity_sessions.csv");

        readProblems("data\\ri\\2014\\problems.csv");
        readSubmissions("data\\ri\\2014\\submissions.csv");
    }

    public static void computeMetrics2014(int week) throws IOException, ParseException {
        writeMetrics(users, week, "data\\ri\\2014\\output\\RI2014_metrics_" + week + ".csv");
    }

    public static void computeMetrics(int week) throws IOException, ParseException {
        //readUsers(users, "data\\ri\\2016\\RI2016_test.csv");
        readUsers(users, "data\\ri\\2016\\RI2016_student_profile.csv");

        //readSessions("data\\ri\\2014\\sessions.csv");
        readNewSessions("data\\ri\\2016\\week" + week + "\\data\\curated\\new_sessions.csv");

        //readForumSessions("data\\ri\\2014\\new_forum_sessions.csv");
        //readQuizSessions("data\\ri\\2014\\new_quiz_sessions.csv");
        //readObservations("data\\ri\\2014\\observations.csv");
        readActivitySessions("data\\ri\\2016\\week" + week + "\\data\\curated\\activity_sessions.csv");

        readProblems("data\\ri\\2016\\week" + week + "\\data\\problems.csv");
        readSubmissions("data\\ri\\2016\\week" + week + "\\data\\submissions.csv");

        writeMetrics(users, week, "data\\ri\\2016\\week" + week + "\\metrics\\RI2016_metrics_all.csv");
        writeMetricsForWidget(users, week, "data\\ri\\2016\\week" + week + "\\metrics\\RI2016_metrics.csv");
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
                week = RI_getWeek2014(session_attr[3]);

                if(week == 99)
                    continue;

                users.get(session_attr[2]).addSession(week, duration, session_attr[3], session_attr[4]);

            }
        }

        csvReader.close();
    }

    private static void readNewSessions(String filename) throws IOException, ParseException {
        //user_id, start_time, end_time, duration
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine;
        int duration, week;

        while ((nextLine = csvReader.readNext()) != null) {

            if(users.containsKey(nextLine[0])) {
                duration = Integer.parseInt(nextLine[3]);
                //week = RI_getWeek2014(nextLine[1]);
                week = RI_getWeek(nextLine[1]);

                if(week == 99)
                    continue;

                users.get(nextLine[0]).addSession(week, duration, nextLine[1], nextLine[2]);

            }
        }

        csvReader.close();
    }

    private static void readForumSessions(String filename) throws IOException, ParseException {
        //user_id, start_time, end_time, duration
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine, session_attr;
        int duration, week;

        while ((nextLine = csvReader.readNext()) != null) {

            if(users.containsKey(nextLine[0])) {
                duration = Integer.parseInt(nextLine[3]);
                week = RI_getWeek2014(nextLine[1]);

                if(week == 99)
                    continue;

                users.get(nextLine[0]).addForumSession(week, duration, nextLine[1], nextLine[2]);

            }
        }

        csvReader.close();
    }

    private static void readQuizSessions(String filename) throws IOException, ParseException {
        //user_id, start_time, end_time, duration
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine, session_attr;
        int duration, week;

        while ((nextLine = csvReader.readNext()) != null) {

            if(users.containsKey(nextLine[0])) {
                duration = Integer.parseInt(nextLine[3]);
                week = RI_getWeek2014(nextLine[1]);

                if(week == 99)
                    continue;

                users.get(nextLine[0]).addQuizSession(week, duration, nextLine[1], nextLine[2]);

            }
        }

        csvReader.close();
    }

    private static void readObservations(String filename) throws IOException, ParseException {
        //obs_id, course_user_id, video_id, duration, start_time, end_time
        //obs_id: DelftX/RI101x/3T2014_623509_i4x-DelftX-RI101x-video-a40b7388bcf04df992403da80db8b3ec_2014-12-12 14:30:11
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine, session_attr;
        int duration, week;

        while ((nextLine = csvReader.readNext()) != null) {

            if(users.containsKey(nextLine[0])) {
                duration = Integer.parseInt(nextLine[3]);
                week = RI_getWeek2014(nextLine[1]);

                if(week == 99)
                    continue;

                users.get(nextLine[0]).addVideoSession(week, duration, nextLine[1]);

            }
        }

        csvReader.close();
    }

    private static void readActivitySessions(String filename) throws IOException, ParseException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine;
        int duration, week;

        while ((nextLine = csvReader.readNext()) != null) {

            if(users.containsKey(nextLine[0])) {
                duration = Integer.parseInt(nextLine[3]);
                //week = RI_getWeek2014(nextLine[1]);
                week = RI_getWeek(nextLine[1]);

                if(week == 99)
                    continue;

                users.get(nextLine[0]).addActivitySession(week, duration);

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
            week = Integer.parseInt(nextLine[2]) + 1;
            problemId = nextLine[0].substring(nextLine[0].indexOf("block@") + 6);
            problems.put(problemId, week);
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

            //week = RI_getWeek2014(nextLine[5]);
            week = RI_getWeek(nextLine[5]);

            if(week > 9)
                continue;

            //String problemId, int submissionWeek, Date submissionTime, Date problemDeadline
            //user.addSubmission(nextLine[4], week, submissionTime, getProblemDeadline2014(problems.get(nextLine[4])));
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

        toWrite = "User_id#Sessions/week#Length of session#Between sessions#Time on-task#Assignments#Until deadline#Sessions".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserMetricComputation> entry : users.entrySet()) {
            current = entry.getValue();
            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(current.getSessionsPerWeek(week));
            toWrite[2] = String.valueOf(current.getAverageSessionLength(week));
            toWrite[3] = String.valueOf(current.getAverageTimeBetweenSessions(week));
            toWrite[4] = String.valueOf(current.getTimeOnTask(week));
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

        toWrite = "User_id#Sessions/week#Length of session#Between sessions#Time on-task#Assignments#Until deadline#Sessions".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserMetricComputation> entry : users.entrySet()) {

            if(Integer.parseInt(entry.getKey()) % 2 == 1)
                continue;

            current = entry.getValue();
            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(current.getSessionsPerWeek(week));
            toWrite[2] = String.valueOf(current.getAverageSessionLength(week));
            toWrite[3] = String.valueOf(current.getAverageTimeBetweenSessions(week));
            toWrite[4] = String.valueOf(current.getTimeOnTask(week));
            toWrite[5] = String.valueOf(current.getQuizSubmissions(week));
            toWrite[6] = String.valueOf(current.getRecommendedTimeliness(week));
            toWrite[7] = String.valueOf(current.getSessions(week));

            output.writeNext(toWrite);
        }
        output.close();
    }

    //UTILS
    private static int RI_getWeek(String startTime){
        if(startTime.compareTo("2016-04-11") > 0 && startTime.compareTo("2016-04-18") < 0)
            return 1;
        if(startTime.compareTo("2016-04-18") > 0 && startTime.compareTo("2016-04-25") < 0)
            return 2;
        if(startTime.compareTo("2016-04-25") > 0 && startTime.compareTo("2016-05-02") < 0)
            return 3;
        if(startTime.compareTo("2016-05-02") > 0 && startTime.compareTo("2016-05-09") < 0)
            return 4;
        if(startTime.compareTo("2016-05-09") > 0 && startTime.compareTo("2016-05-16") < 0)
            return 5;
        if(startTime.compareTo("2016-05-16") > 0 && startTime.compareTo("2016-05-23") < 0)
            return 6;
        if(startTime.compareTo("2016-05-23") > 0 && startTime.compareTo("2016-05-30") < 0)
            return 7;
        if(startTime.compareTo("2016-05-30") > 0 && startTime.compareTo("2016-06-06") < 0)
            return 8;
        if(startTime.compareTo("2016-06-06") > 0 && startTime.compareTo("2016-06-15") < 0)
            return 9;
        return 99;
    }

    private static int RI_getWeek2014(String startTime){
        if(startTime.compareTo("2014-11-25") > 0 && startTime.compareTo("2014-12-02") < 0)
            return 1;
        if(startTime.compareTo("2014-12-02") > 0 && startTime.compareTo("2014-12-09") < 0)
            return 2;
        if(startTime.compareTo("2014-12-09") > 0 && startTime.compareTo("2014-12-16") < 0)
            return 3;
        if(startTime.compareTo("2014-12-16") > 0 && startTime.compareTo("2014-12-23") < 0)
            return 4;
        if(startTime.compareTo("2014-12-23") > 0 && startTime.compareTo("2014-12-30") < 0)
            return 4;
        if(startTime.compareTo("2014-12-30") > 0 && startTime.compareTo("2015-01-06") < 0)
            return 4;
        if(startTime.compareTo("2015-01-06") > 0 && startTime.compareTo("2015-01-13") < 0)
            return 5;
        if(startTime.compareTo("2015-01-13") > 0 && startTime.compareTo("2015-01-20") < 0)
            return 6;
        if(startTime.compareTo("2015-01-20") > 0 && startTime.compareTo("2015-01-27") < 0)
            return 7;
        if(startTime.compareTo("2015-01-27") > 0 && startTime.compareTo("2015-02-03") < 0)
            return 8;
        if(startTime.compareTo("2015-02-03") > 0 && startTime.compareTo("2015-02-14") < 0)
            return 9;
        return 99;
    }

    private static String getProblemDeadline2014(int problemWeek) throws ParseException{
        String deadline;

        switch (problemWeek) {
            case 1:
                deadline = "2014-12-02";
                break;
            case 2:
                deadline = "2014-12-09";
                break;
            case 3:
                deadline = "2014-12-16";
                break;
            case 4:
                deadline = "2014-12-23";
                break;
            case 5:
                deadline = "2015-01-13";
                break;
            case 6:
                deadline = "2015-01-20";
                break;
            default:
                deadline = "2015-01-27";
        }

        return deadline + " 12:00:00";
    }

    private static String getProblemDeadline(int problemWeek) throws ParseException{
        String deadline;

        switch (problemWeek) {
            case 1:
                deadline = "2016-04-18"; break;
            case 2:
                deadline = "2016-04-25"; break;
            case 3:
                deadline = "2016-05-02"; break;
            case 4:
                deadline = "2016-05-09"; break;
            case 5:
                deadline = "2016-05-16"; break;
            case 6:
                deadline = "2016-05-23"; break;
            case 7:
                deadline = "2016-05-30"; break;
            case 8:
                deadline = "2016-06-06"; break;
            default:
                deadline = "2015-06-14";
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

}
