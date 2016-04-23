package ri;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ioana on 4/7/2016.
 */
public class MetricComputation {

    static HashMap<String, UserMetricComputation> users = new HashMap<>();
    static HashMap<String, Integer> problems = new HashMap<>();

    public static void computeMetrics2014(int week) throws IOException, ParseException {
        readUsers(users, "data\\ri\\2014\\RI2014_graduates.csv");
        readSessions("data\\ri\\2014\\sessions.csv");

        //readForumSessions("data\\ri\\2014\\forum_sessions.csv");
        //readQuizSessions("data\\ri\\2014\\quiz_sessions.csv");
        //readObservations("data\\ri\\2014\\observations.csv");

        readProblems("data\\ri\\2014\\problems.csv");
        readSubmissions("data\\ri\\2014\\submissions.csv");

        //writeSessions(week, "data\\ri\\2014\\output\\" + week + "_#sessions.csv");
        //writeAverageTimePerSession(week, "data\\ri\\2014\\output\\" + week + "_averageSessionTime.csv");
        //writeAverageSessionsPerWeek(week, "data\\ri\\2014\\output\\" + week + "_averageSessionsPerWeek.csv");
        //writeAverageLengthBetweenSessions(week, "data\\ri\\2014\\output\\" + week + "_averageLengthBetweenSessions.csv");
        writeMetrics(users, week, "data\\ri\\2014\\output\\RI2014_metrics_" + week + ".csv");
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

    private static void readForumSessions(String filename) throws IOException, ParseException {
        //session_id, course_user_id, ??, start_time, end_time, duration
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine, session_attr;
        int duration, week;

        while ((nextLine = csvReader.readNext()) != null) {
            session_attr = nextLine[0].split("_");

            if(users.containsKey(session_attr[3])) {
                duration = Integer.parseInt(nextLine[5]);
                week = RI_getWeek2014(nextLine[3]);

                if(week == 99)
                    continue;

                users.get(session_attr[3]).addForumSession(week, duration, session_attr[4], session_attr[5]);

            }
        }

        csvReader.close();
    }

    private static void readQuizSessions(String filename) throws IOException, ParseException {
        //session_id, course_user_id, start_time, end_time, duration
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine, session_attr;
        int duration, week;

        while ((nextLine = csvReader.readNext()) != null) {
            session_attr = nextLine[0].split("_");

            if(users.containsKey(session_attr[4])) {
                duration = Integer.parseInt(nextLine[4]);
                week = RI_getWeek2014(nextLine[2]);

                if(week == 99)
                    continue;

                users.get(session_attr[4]).addQuizSession(week, duration, session_attr[5], session_attr[6]);

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
            session_attr = nextLine[0].split("_");

            if(users.containsKey(session_attr[1])) {
                duration = Integer.parseInt(nextLine[3]);
                week = RI_getWeek2014(nextLine[4]);

                if(week == 99)
                    continue;

                users.get(session_attr[1]).addVideoSession(week, duration, session_attr[3]);

            }
        }

        csvReader.close();
    }

    private static void readProblems(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        int week;

        while ((nextLine = csvReader.readNext()) != null) {
            week = Integer.parseInt(nextLine[2] + 1);
            problems.put(nextLine[0], week);
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

            week = RI_getWeek2014(nextLine[5]);
            if(week > 11)
                continue;

            //String problemId, int submissionWeek, Date submissionTime, Date problemDeadline
            user.addSubmission(nextLine[4], week, submissionTime, getProblemDeadlineForProblem2014(problems.get(nextLine[4])));

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
            return 5;
        if(startTime.compareTo("2014-12-30") > 0 && startTime.compareTo("2015-01-06") < 0)
            return 6;
        if(startTime.compareTo("2015-01-06") > 0 && startTime.compareTo("2015-01-13") < 0)
            return 7;
        if(startTime.compareTo("2015-01-13") > 0 && startTime.compareTo("2015-01-20") < 0)
            return 8;
        if(startTime.compareTo("2015-01-20") > 0 && startTime.compareTo("2015-01-27") < 0)
            return 9;
        if(startTime.compareTo("2015-01-27") > 0 && startTime.compareTo("2015-02-03") < 0)
            return 10;
        if(startTime.compareTo("2015-02-03") > 0 && startTime.compareTo("2015-02-14") < 0)
            return 11;
        return 99;
    }

    private static String getProblemDeadlineForProblem2014(int problemWeek) throws ParseException{
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
                deadline = "2015-01-14";
                break;
            case 6:
                deadline = "2015-01-21";
                break;
            default:
                deadline = "2015-01-28";
        }

        return deadline + " 00:00:00";
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
