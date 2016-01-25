/**
 * Created by Ioana on 1/9/2016.
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//TODO: check file paths for file read and generation so they don't overwrite stuff
//TODO: check consistency of week numbering: starting with 0 or with 1
public class MetricComputation {

    static HashMap<String, UserForMetricsComputation> graduates;
    static HashMap<Integer, ArrayList<String>> videosPerWeek;
    static HashMap<String, Integer> problemsWeek;
    static HashMap<Integer, ArrayList<String>> problemsPerWeek;
    static ArrayList<Date> problemXSubmissions;
    static double[] thresholds;

    static HashMap<String, Integer> scaledWeeklyTimes;
    static HashMap<String, Integer> scaledVideoTimes;
    static HashMap<String, Integer> scaledRatio;
    static HashMap<String, Integer> scaledVideos;
    static HashMap<String, Integer> scaledAssignments;
    static HashMap<String, Integer> scaledDeadlines;

        public static void main(String[] args) throws IOException,ParseException
        {
            initialize();
            generateMetrics();

            //todo: write metrics for all weeks
            //write metrics only for week 1
            for(int i = 1; i < 12; i++)
              writeMetrics("data\\2015\\user_metrics\\metrics" + i + ".csv", i);


        }

    private static void initialize() {
        graduates = new HashMap<>();
        videosPerWeek = new HashMap<>();
        problemsWeek = new HashMap<>();
        problemsPerWeek = new HashMap<>();
        thresholds = new double[6];

        scaledWeeklyTimes = new HashMap<>();
        scaledVideoTimes = new HashMap<>();
        scaledRatio = new HashMap<>();
        scaledVideos = new HashMap<>();
        scaledAssignments = new HashMap<>();
        scaledDeadlines = new HashMap<>();

    }

    private static void generateMetrics() throws IOException, ParseException {
        readUsers(0.6);

        computeWeeklyPlatformTimes();
        computeWeeklyVideoTimes();
        //computeWeeklyRatioTimes();

        readVideosPublished();
        computeDistinctVideos();

        readProblems();
        readSubmissions();

        cumulateMetrics();

    }

    private static void computeUntilDeadline() {
        for (Map.Entry<String, UserForMetricsComputation> entry : graduates.entrySet())
            entry.getValue().computeUntilDeadlines();
    }

    private static void computeWeeklyRatioTimes() {
        for (Map.Entry<String, UserForMetricsComputation> entry : graduates.entrySet())
            entry.getValue().computeRatioTimes();
    }

    private static void cumulateMetrics() {
        UserForMetricsComputation current;

        for (Map.Entry<String, UserForMetricsComputation> entry : graduates.entrySet()) {
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

        for (Map.Entry<String, UserForMetricsComputation> entry : graduates.entrySet()) {
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
    private static void readUsers(double threshold) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2015\\course_user.csv"));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(Double.parseDouble(nextLine[1]) >= threshold)
                graduates.put(nextLine[0], new UserForMetricsComputation(nextLine[0],nextLine[1]));
        }

        csvReader.close();

    }

    //todo check if both HashMaps are needed
    private static void readProblems() throws IOException, ParseException{
        CSVReader csvReader = new CSVReader(new FileReader("data\\2015\\problems.csv"));
        String [] nextLine;
        int week;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            week = Integer.parseInt(nextLine[3]) + 1;

            if (week <= 8) {
                //less than 8, because the rest are not educational problems i.e: "opinion about the course" and exam published in march
                problemsWeek.put(nextLine[0], week);

                if(!problemsPerWeek.containsKey(week))
                    problemsPerWeek.put(week, new ArrayList<>());

                problemsPerWeek.get(week).add(nextLine[0]);
            }
        }
        csvReader.close();
    }

    private static void readSubmissions() throws IOException,ParseException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2015\\submissions.csv"));
        String [] nextLine;
        UserForMetricsComputation user;
        int week;
        long hours;
        String submissionTime;
        int sub = 0;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            user = graduates.get(nextLine[1]);

            if (user == null)    //user does not have a grade above the required threshold -> ignore submission
                continue;

            if(!problemsWeek.containsKey(nextLine[2]))   //ignore problems that are not in "problems.csv"
                continue;

            sub++;

            week = problemsWeek.get(nextLine[2]);
            submissionTime = nextLine[3].substring(0, 22);
            hours = differenceBetweenDatesInHours(getProblemDeadlineForWeek(week), getDateFromString(submissionTime));

            user.addSubmission(week, nextLine[2], hours, getWeek(submissionTime));
        }

        System.out.println("Finished reading submissions: " + sub);
        csvReader.close();

    }

    private static void readVideosPublished() throws IOException {
        CSVReader resources = new CSVReader(new FileReader("data\\2015\\resources.csv"));
        String [] nextLine;
        int week;

        resources.readNext();

        while ((nextLine = resources.readNext()) != null) {
            if("video".compareTo(nextLine[1]) == 0) {
                week = Integer.parseInt(nextLine[2]);
                if(videosPerWeek.get(week) == null)
                    videosPerWeek.put(week, new ArrayList<>());
                videosPerWeek.get(week).add(nextLine[0]);
            }
        }
        resources.close();
    }

    //************************
    //************ Computations

    private static void computeWeeklyPlatformTimes() throws IOException {
        //session_id, course_user_id, start_time, duration
        CSVReader csvReader = new CSVReader(new FileReader("data\\2015\\sessions.csv"));
        String[] nextLine;
        int duration, week;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(graduates.containsKey(nextLine[1])) {
                duration = Integer.parseInt(nextLine[3]);
                week = getWeek(nextLine[2]);
                graduates.get(nextLine[1]).addSessionTime(week, duration);
            }
        }

        csvReader.close();
    }

    private static void computeWeeklyVideoTimes() throws IOException {
        CSVReader observations = new CSVReader(new FileReader("data\\2015\\observations.csv"));
        String[] nextLine;
        String videoStart;

        observations.readNext();

        while ((nextLine = observations.readNext()) != null) {
            if(graduates.containsKey(nextLine[1])) {
                videoStart = nextLine[0].substring(nextLine[0].indexOf("_2014-")+1);
                graduates.get(nextLine[1]).addVideoTime(getWeek(videoStart), Integer.parseInt(nextLine[3]));
            }
        }
        observations.close();
    }

    //-------**********-------------
    //TODO: merge it with computeWeeklyVideoTimes not to read observations.csv twice
    private static void computeDistinctVideos() throws IOException {
        CSVReader observations = new CSVReader(new FileReader("data\\2015\\observations.csv"));
        String[] nextLine;
        String videoID;

        observations.readNext();

        while ((nextLine = observations.readNext()) != null) {
            if(graduates.containsKey(nextLine[1])) {
                videoID = nextLine[2];
                graduates.get(nextLine[1]).addVideo(videoID, getWeekForVideo(videoID));
            }
        }
        observations.close();
    }


    //************************
    //************ Utils

    private static int getWeekForVideo(String videoID) {
        //returns the week in which a video was published
        for (Map.Entry<Integer,ArrayList<String>> entry : videosPerWeek.entrySet()) {
            if(entry.getValue().contains(videoID))
                return entry.getKey();
        }
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
                deadline = "2014-11-10";
                break;
            case 2:
                deadline = "2014-11-17";
                break;
            case 3:
                deadline = "2014-11-24";
                break;
            case 4:
                deadline = "2014-12-01";
                break;
            case 5:
                deadline = "2014-12-08";
                break;
            case 6:
                deadline = "2014-12-15";
                break;
            case 7:
                deadline = "2014-12-22";
                break;
            case 8:
                deadline = "2014-12-29";
                break;
            default:
                deadline = "2015-01-13";
        }

        return getDateFromString(deadline + "T23:59:59Z");
    }

    private static long differenceBetweenDatesInHours(Date deadline, Date submission){
        long diff = deadline.getTime() - submission.getTime();

        if(diff > 0)
            return TimeUnit.MILLISECONDS.toHours(diff);

        return -1;
    }

    private static int getWeek(String startTime){
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

}