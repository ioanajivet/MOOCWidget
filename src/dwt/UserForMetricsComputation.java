package dwt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ioana on 1/9/2016.
 */
public class UserForMetricsComputation {
    private String id;
    private String anonymousId;
    private float grade;

    private int []timePerWeek;
    private int totalTime;  //in seconds
    private int sessions;

    private int []videoTimePerWeek;
    private int videoTime;

    private int []videosPerWeek;

    private double []ratioTimes;

    private HashMap<Integer, ArrayList<String>> videosWatched;


    //private HashMap<Integer, HashMap<String, String>> assignmentSubmissions;
    private HashMap<Integer, HashMap<String, Long>> assignmentUntilDeadlineInHours;
    private int []assignmentsPerWeek;
    private ArrayList<String> submittedAssignments;
    private int []untilDeadlinePerWeek;

    public UserForMetricsComputation(String id) {
        this.id =  id;

        timePerWeek = new int[11];
        totalTime = 0;
        sessions = 0;

        videoTimePerWeek = new int[11];
        videoTime = 0;

        ratioTimes = new double[11];

        videosWatched = new HashMap<>();
        videosPerWeek = new int[11];

        //assignmentSubmissions = new HashMap<>();
        submittedAssignments = new ArrayList<>();
        assignmentUntilDeadlineInHours = new HashMap<>();

        assignmentsPerWeek = new int[11];
        untilDeadlinePerWeek = new int[11];
    }

    public UserForMetricsComputation(String id, String grade){
        this.id =  id;
        this.grade = Float.parseFloat(grade);
        timePerWeek = new int[11];
        totalTime = 0;
        sessions = 0;

        videoTimePerWeek = new int[11];
        videoTime = 0;

        ratioTimes = new double[11];

        videosWatched = new HashMap<>();
        videosPerWeek = new int[11];

        //assignmentSubmissions = new HashMap<>();
        submittedAssignments = new ArrayList<>();
        assignmentUntilDeadlineInHours = new HashMap<>();

        assignmentsPerWeek = new int[11];
        untilDeadlinePerWeek = new int[11];
    }

    public void setAnonymousId(String anonId){
        this.anonymousId = anonId;
    }

    public void addSessionTime(int week, int duration){
        timePerWeek[week-1] += duration;
        totalTime += duration;
        sessions++;
    }

    public void addVideoTime(int week, int duration){
        videoTimePerWeek[week-1] += duration;
        videoTime += duration;
    }

    public int getTotalTime(){
        return totalTime;
    }

    public int getSessions(){
        return sessions;
    }

    public float getGrade(){
        return grade;
    }

    public int getVideoTime(){
        return videoTime;
    }

    //time spent on the platform during week i
    public int getWeekTime(int i){
        return timePerWeek[i-1];
    }

    public void setWeekTime(int i, int value){
        timePerWeek[i-1] = value;
    }

    //time spent watching videos during week i
    public int getWeekVideoTime(int i){
        return videoTimePerWeek[i-1];
    }

    public void setWeekVideoTime(int i, int value){
        videoTimePerWeek[i-1] = value;
    }

    //todo: refactor: move as static to GraphGeneration.java


    public void addVideo(String videoID, int week) {
        if(week == -1)
            return;

        if(!videosWatched.containsKey(week))
            videosWatched.put(week, new ArrayList<>());

        if(!videosWatched.get(week).contains(videoID)) {
            videosWatched.get(week).add(videoID);

            videosPerWeek[week - 1]++;
        }
    }

    public int getDistinctVideosPerWeek(int week){
        return videosPerWeek[week - 1];
    }

    public void addSubmission(int week, String problemId, long hours, int submissionWeek){
        HashMap<String, Long> untilDeadlines;

        if(!submittedAssignments.contains(problemId)) {
            submittedAssignments.add(problemId);
            assignmentsPerWeek[submissionWeek-1]++;
        }

        if(!assignmentUntilDeadlineInHours.containsKey(submissionWeek))
            assignmentUntilDeadlineInHours.put(submissionWeek, new HashMap<>());

        untilDeadlines = assignmentUntilDeadlineInHours.get(submissionWeek);

        //todo refactor
        if(!untilDeadlines.containsKey(problemId))
            untilDeadlines.put(problemId, hours);
        else if(untilDeadlines.get(problemId) > hours)
            untilDeadlines.put(problemId, hours);
    }

    //todo: calculate mean/average - find best way to measure "time until deadline"
    //todo: put this in an array for easy access and compute by calling once from GraphGeneration.java

    public void computeUntilDeadlines(){
        for (int i = 1; i < 12; i++)
            computeUntilDeadlinesPerWeek(i);

    }

    public void computeUntilDeadlinesPerWeek(int week){
        HashMap<String, Long> untilDeadlineWeeki;
        int sum = 0;

        for(int i = 1; i <= week; i++) {
            untilDeadlineWeeki = assignmentUntilDeadlineInHours.get(i);

            if (untilDeadlineWeeki == null)
                continue;

            for (Map.Entry<String, Long> entry : untilDeadlineWeeki.entrySet())
                if (entry.getValue() > 0)
                    sum += entry.getValue();
        }

        if(assignmentsPerWeek[week-1] == 0)
            untilDeadlinePerWeek[week-1] = 0;
        else
            untilDeadlinePerWeek[week-1] = sum/assignmentsPerWeek[week-1];

    }

    public long getUntilDeadlinesPerWeekAverage(int week){

        return untilDeadlinePerWeek[week-1];
        //it does not take into account the problems that were not submitted for week i
        //it considers problem submitted after the deadline to be 0h until deadline
    }

    public int getAssignmentsSubmittedPerWeek(int week){
        return assignmentsPerWeek[week-1];
    }

    public HashMap<String, Long> getUntilDeadlinesPerWeek(int i){
        return assignmentUntilDeadlineInHours.get(i);
    }

    public void computeWeeklySubmissionMeans(){

    }
    public String getId(){
        return this.id;
    }

    public String getAnonymousId(){
        return this.anonymousId;
    }

    public void computeRatioTimes(){
        for(int i = 0; i < 11; i++){
            if(timePerWeek[i] == 0)
                ratioTimes[i] = 0;
            else
                ratioTimes[i] = 100.0 * videoTimePerWeek[i] / timePerWeek[i];
        }
    }

    public double getRatio(int week){
        return ratioTimes[week-1];
    }

    public void cumulatePlatformTimes() {
        for (int i = 1; i < 11; i++)
            timePerWeek[i] += timePerWeek[i - 1];
    }

    public void cumulateVideoTimes(){
        for(int i = 1; i < 11; i++)
            videoTimePerWeek[i] += videoTimePerWeek[i-1];
    }

    public void cumulateVideos(){
       int watchedInCurrentWeek;
        for(int i = 1; i < 11; i++) {
            if(videosWatched.get(i + 1) == null)
                watchedInCurrentWeek = 0;
            else
                watchedInCurrentWeek = videosWatched.get(i + 1).size();

            videosPerWeek[i] = videosPerWeek[i - 1] + watchedInCurrentWeek;
        }
    }

    public void cumulateAssignments(){
        for(int i = 1; i < 11; i++)
            assignmentsPerWeek[i] += assignmentsPerWeek[i-1];
    }


}
