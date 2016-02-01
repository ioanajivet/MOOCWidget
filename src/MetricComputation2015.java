/**
 * Created by Ioana on 1/9/2016.
 *
 * Command line arguments:
 * args[0] = week for which the metrics computation is done
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//TODO: check file paths for file read and generation so they don't overwrite stuff
//TODO: check consistency of week numbering: starting with 0 or with 1
public class MetricComputation2015 {

    static HashMap<String, UserForMetricsComputation> users;
    static HashMap<Integer, ArrayList<String>> videosPerWeek;
    static HashMap<String, Integer> videosWeek;
    static HashMap<String, Integer> problemsWeek;
    static HashMap<Integer, ArrayList<String>> problemsPerWeek;
    static double[] thresholds;

        public static void main(String[] args) throws IOException,ParseException
        {
            int week = Integer.parseInt(args[0]);

            initialize();
            generateMetrics(week);

            //write metrics only for week 1
           // for(int i = 1; i < 12; i++)
            writeMetrics("data\\2016\\user_metrics\\metrics" + week + ".csv", week);

        }

    private static void initialize() {
        users = new HashMap<>();
        videosPerWeek = new HashMap<>();
        videosWeek = new HashMap<>();

        problemsWeek = new HashMap<>();
        problemsPerWeek = new HashMap<>();

        thresholds = new double[6];

    }

    private static void generateMetrics(int week) throws IOException, ParseException {
        //readUsersThreshold(0.6);

        readUsers();
        readVideosPublished();
        readProblems();

        readSessions(week);
        readObservations(week);
        readSubmissions(week);

        cumulateMetrics();

    }

    private static void cumulateMetrics() {
        UserForMetricsComputation current;

        for (Map.Entry<String, UserForMetricsComputation> entry : users.entrySet()) {
            current = entry.getValue();

            current.cumulatePlatformTimes();
            current.cumulateVideoTimes();
            current.computeRatioTimes();

            current.cumulateVideos();
            current.cumulateAssignments();
            current.computeUntilDeadlines();
        }
    }

    private static void writeMetrics(String filename, int week) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        UserForMetricsComputation current;

        toWrite = "User_id#Time on platform#Time on videos#Ratio video/platform#Distict videos#Assignments#Until deadline".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserForMetricsComputation> entry : users.entrySet()) {
            current = entry.getValue();
            toWrite[0] = entry.getKey();

            toWrite[1] = String.valueOf(current.getWeekTime(week));
            toWrite[2] = String.valueOf(current.getWeekVideoTime(week));
            toWrite[3] = String.format("%.2f", current.getRatio(week));
            toWrite[4] = String.valueOf(current.getDistinctVideosPerWeek(week));
            toWrite[5] = String.valueOf(current.getAssignmentsSubmittedPerWeek(week));
            toWrite[6] = String.valueOf(current.getUntilDeadlinesPerWeekAverage(week));

            output.writeNext(toWrite);
        }
        output.close();
    }

    //************************
    //************ Loading data
    private static void readUsersThreshold(double threshold) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2015\\course_user.csv"));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(Double.parseDouble(nextLine[1]) >= threshold)
                users.put(nextLine[0], new UserForMetricsComputation(nextLine[0],nextLine[1]));
        }

        csvReader.close();

    }

    private static void readUsers() throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\user_pii.csv"));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            users.put(nextLine[0], new UserForMetricsComputation(nextLine[0]));
        }

        System.out.println("Users read: " + users.size());
        csvReader.close();

    }

    private static void readVideosPublished() throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\resources.csv"));
        String [] nextLine;
        int week;
        String videoId;
        int videoCount = 0;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if("video".compareTo(nextLine[1]) == 0) {

                week = Integer.parseInt(nextLine[3]);

                if (week > 11)
                    continue;

                if(videosPerWeek.get(week) == null)
                    videosPerWeek.put(week, new ArrayList<>());

                videoId = nextLine[0].substring(nextLine[0].lastIndexOf('@') + 1);

                videosWeek.put(videoId, week);

                videosPerWeek.get(week).add(nextLine[0]);
                videoCount++;
            }
        }

        csvReader.close();

        System.out.println("Videos read: " + videoCount);
    }

    //todo check if both HashMaps are needed
    private static void readProblems() throws IOException, ParseException{
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\problems.csv"));
        String [] nextLine;
        int week;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            if(!nextLine[1].startsWith("Weekly assessment"))
                continue;

            week = Integer.parseInt(nextLine[3]) + 1;

            problemsWeek.put(nextLine[0], week);

            if(!problemsPerWeek.containsKey(week))
                problemsPerWeek.put(week, new ArrayList<>());

            problemsPerWeek.get(week).add(nextLine[0]);

        }

        csvReader.close();

        System.out.println("Problems read: " + problemsWeek.size());
    }

    private static void readSessions(int weekToRead) throws IOException {
        //session_id, course_user_id, start_time, duration
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\week" + weekToRead + "\\sessions.csv"));
        String[] nextLine;
        int duration, week;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(users.containsKey(nextLine[1])) {
                duration = Integer.parseInt(nextLine[3]);
                week = getWeek(nextLine[2]);
                users.get(nextLine[1]).addSessionTime(week, duration);
            }
        }

        csvReader.close();
    }

    private static void readSubmissions(int weekToRead) throws IOException,ParseException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\week" + weekToRead + "\\submissions.csv"));
        String [] nextLine;
        UserForMetricsComputation user;
        int week;
        long hours;
        String submissionTime;
        int sub = 0;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            user = users.get(nextLine[1]);

            if (user == null)    //user are not in the test base -> ignore submission
                continue;

            if(!problemsWeek.containsKey(nextLine[2]))   //ignore problems that are not graded
                continue;

            sub++;

            week = problemsWeek.get(nextLine[2]);
            submissionTime = nextLine[3].substring(0, 22);
            hours = differenceBetweenDatesInHours(getProblemDeadlineForWeek(week), getDateFromString(submissionTime));

            user.addSubmission(week, nextLine[2], hours, getWeek(submissionTime));
        }

        csvReader.close();

        System.out.println("Submissions read: " + sub);

    }

    private static void readObservations(int weekToRead) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\week" + weekToRead + "\\observations.csv"));
        String[] nextLine;
        String videoID;
        String videoStart;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(users.containsKey(nextLine[1])) {
                //video time computations
                videoStart = nextLine[0].substring(nextLine[0].indexOf("_2016-")+1);
                users.get(nextLine[1]).addVideoTime(getWeek(videoStart), Integer.parseInt(nextLine[3]));

                //distinct videos computations
                videoID = nextLine[2];
                users.get(nextLine[1]).addVideo(videoID, getWeekForVideo(videoID));
            }
        }
        csvReader.close();
    }

    //************************
    //************ Utils

    private static int getWeekForVideo(String videoID) {
        //returns the week in which a video was published
        if(videosWeek.containsKey(videoID))
            return videosWeek.get(videoID);
        return -1;
    }

    //todo: fix this ugly hack
    private static Date getDateFromString(String dateString) throws ParseException{
        //input date: "2014-11-11T12:00:00Z"
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return format.parse(dateString.replace('T', ' ').substring(0,dateString.length()-2));
    }

    //todo fix the ugly hack after the getDateFromString() is also fixed
    private static Date getProblemDeadlineForWeek(int week) throws ParseException{
        String deadline;

        switch (week) {
            case 1:
                deadline = "2016-01-25";
                break;
            case 2:
                deadline = "2016-02-01";
                break;
            case 3:
                deadline = "2016-02-08";
                break;
            case 4:
                deadline = "2016-02-15";
                break;
            case 5:
                deadline = "2016-02-22";
                break;
            case 6:
                deadline = "2016-02-29";
                break;
            case 7:
                deadline = "2016-03-07";
                break;
            case 8:
                deadline = "2016-03-14";
                break;
            default:
                deadline = "2016-04-04";
        }

        return getDateFromString(deadline + "T23:59:59Z");
    }

    private static long differenceBetweenDatesInHours(Date deadline, Date submission){
        long diff = deadline.getTime() - submission.getTime();

        if(diff > 0)
            return TimeUnit.MILLISECONDS.toHours(diff);

        return -1;
    }

    private static int getWeek2015(String startTime){
        if(startTime.compareTo("2014-10-28") > 0 && startTime.compareTo("2014-11-04") < 0)
            return 1;
        if(startTime.compareTo("2014-11-04") > 0 && startTime.compareTo("2014-11-11") < 0)
            return 2;
        if(startTime.compareTo("2014-11-11") > 0 && startTime.compareTo("2014-11-18") < 0)
            return 3;
        if(startTime.compareTo("2014-11-18") > 0 && startTime.compareTo("2014-11-25") < 0)
            return 4;
        if(startTime.compareTo("2014-11-25") > 0 && startTime.compareTo("2014-12-02") < 0)
            return 5;
        if(startTime.compareTo("2014-12-02") > 0 && startTime.compareTo("2014-12-09") < 0)
            return 6;
        if(startTime.compareTo("2014-12-09") > 0 && startTime.compareTo("2014-12-16") < 0)
            return 7;
        if(startTime.compareTo("2014-12-16") > 0 && startTime.compareTo("2014-12-23") < 0)
            return 8;
        if(startTime.compareTo("2014-12-23") > 0 && startTime.compareTo("2014-12-30") < 0)
            return 9;
        if(startTime.compareTo("2014-12-30") > 0 && startTime.compareTo("2015-01-06") < 0)
            return 10;
        if(startTime.compareTo("2015-01-06") > 0 && startTime.compareTo("2015-01-13") < 0)
            return 11;
        return 99;
    }

    private static int getWeek(String startTime){
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

}
