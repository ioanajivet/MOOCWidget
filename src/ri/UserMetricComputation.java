package ri;


import dwt.UserForMetricsComputation;
import javafx.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ioana on 4/7/2016.
 */
public class UserMetricComputation {

    private String id;

    //sessions
    private int[] session_count;
    private int[] session_length;
    private List<Session> sessionTimestamps;

    private HashMap<Integer, List<Session>> sessions;

    private int[] forum_session_time;
    private int[] quiz_session_time;
    private int[] video_session_time;

    private int []assignmentsPerWeek;
    private List<String> submittedAssignments;

    //Pairs - submission date and deadline
    //private List<Pair<Date, Date>> submissions = new ArrayList<>();
    private HashMap<Integer, HashMap<String, Date>> submissions;
    private HashMap<String, Date> deadlines;

    public UserMetricComputation(String id) {
        this.id =  id;

        session_count = new int[11];
        session_length = new int[11];
        sessionTimestamps = new ArrayList<>();

        sessions = new HashMap<>();

        forum_session_time = new int[11];
        quiz_session_time = new int[11];
        video_session_time = new int[11];

        assignmentsPerWeek = new int[11];
        submittedAssignments = new ArrayList<>();

        submissions = new HashMap<>();
        deadlines = new HashMap<>();

    }

    //Methods for adding data
    public void addSession(int week, int sessionLength, String start, String end) {
        session_count[week-1]++;
        session_length[week-1] += sessionLength;

        sessionTimestamps.add(new Session(this.id, start, end));

        if(!sessions.containsKey(week)) {
            List<Session> list = new ArrayList<Session>();
            list.add(new Session(this.id, start, end));
            sessions.put(week, list);
        }
        else
            sessions.get(week).add(new Session(this.id, start, end, sessionLength));

    }

    public void addForumSession(int week, int sessionLength, String start, String end) {
        List<Session> sessionsThisWeek = sessions.get(week);

        if (sessionsThisWeek == null)
            return;

        for (Session s: sessionsThisWeek) {
            if (s.includes(this.id, start, end)) {
                forum_session_time[week - 1] += sessionLength;
                return;
            }
        }
    }

    public void addQuizSession(int week, int sessionLength, String start, String end) {
        List<Session> sessionsThisWeek = sessions.get(week);

        if (sessionsThisWeek == null)
            return;

        for (Session s: sessionsThisWeek) {
            if (s.includes(this.id, start, end)) {
                quiz_session_time[week-1] += sessionLength;
                return;
            }
        }
    }

    public void addVideoSession(int week, int sessionLength, String start) {
        List<Session> sessionsThisWeek = sessions.get(week);

        if (sessionsThisWeek == null)
            return;

        for (Session s: sessionsThisWeek) {
            if (s.includesStart(this.id, start)) {
                video_session_time[week-1] += sessionLength;
                return;
            }
        }
    }

    public void addSubmission(String problemId, int submissionWeek, String submissionTime, String problemDeadline){
        HashMap<String, Date> weekSubmissions;

        if(!submittedAssignments.contains(problemId)) {
            submittedAssignments.add(problemId);
            assignmentsPerWeek[submissionWeek-1]++;
        }

        deadlines.put(problemId, getDateFromString(problemDeadline));

        if(!submissions.containsKey(submissionWeek)) {
            weekSubmissions = new HashMap<>();
            weekSubmissions.put(problemId, getDateFromString(submissionTime));

            submissions.put(submissionWeek, weekSubmissions);
            return;
        }

        weekSubmissions = submissions.get(submissionWeek);
        if(!weekSubmissions.containsKey(problemId))
            weekSubmissions.put(problemId, getDateFromString(submissionTime));
        else if(weekSubmissions.get(problemId).compareTo(getDateFromString(submissionTime)) < 0)
            weekSubmissions.put(problemId, getDateFromString(submissionTime));

    }

    //Metric 1: Sessions per week
    public int getSessionsPerWeek(int week) {
        int sessionCount = 0;

        for(int i = 0; i < week; i++)
            sessionCount += session_count[i];

        if(sessionCount == 0)
            return 0;

        return (int) Math.round(sessionCount * 1.0/week);
    }

    //Metric 2: Average length of a session - min
    public int getAverageSessionLength(int week) {
        int time = 0;
        int sessions = 0;

        for(int i = 0; i < week; i++) {
            time += session_length[i];
            sessions += session_count[i];
        }

        if(sessions == 0)
            return 0;

        return (int) Math.round(time/(sessions * 60.0));
    }

    //Metric 3: Average time between sessions - h
    // if the time between sessions <2h, it is not calculated
    public int getAverageTimeBetweenSessions(int week) {
        int i, skipped = 0;
        long time_ms = 0, diff;

        if (sessionTimestamps.size() == 0)
            return 0;

        Collections.sort(sessionTimestamps);

        for(i = 1; i < sessionTimestamps.size() && sessionTimestamps.get(i).getStartTime().compareTo(weedEndDate(week)) < 0; i++) {

            //todo: check if it calculates right with i and i-1 indices
            diff = sessionTimestamps.get(i).getStartDate().getTime() - sessionTimestamps.get(i-1).getEndDate().getTime();

            //if there is less than 1h in between the sessions, they are considered the same one
            if(diff >= 3600000)
                time_ms += diff;
            else
                skipped++;
        }

        //if there is only one session or only one session block the difference between the consecutive sessions is infinite
        if(i == 1 || i-1 == skipped)
            return -1;

        return (int) Math.round(TimeUnit.MILLISECONDS.toHours(time_ms)*1.0/(i-1-skipped));

    }

    //Metric 4. Time on-task (video, on quiz pages, in the forum)
    public int getTimeOnTask(int week) {
        int onTaskTime = 0, totalTime = 0;

        for(int i = 0; i < week; i++) {
            onTaskTime += quiz_session_time[i] + video_session_time[i] + forum_session_time[i];
            //onTaskTime += video_session_time[i];
            totalTime += session_length[i];
        }

        if(totalTime == 0)
            return 0;

        return onTaskTime*100/totalTime;
    }

    //Metric 5. Quiz answers submitted
    public int getQuizSubmissions(int week) {
        int submissions = 0;

        for(int i = 0; i < week; i++)
            submissions += assignmentsPerWeek[i];

        return submissions;
    }

    //Metric 6. Timeliness according to the recommended deadline - one week after publication of new material
    public int getRecommendedTimeliness(int week) {
        HashMap<String, Date> weekSubmissions;
        long totalHours = 0;
        for(int i = 1; i <= week; i++) {
            weekSubmissions = submissions.get(i);

            if(weekSubmissions == null)
                continue;

            for (Map.Entry<String, Date> entry : weekSubmissions.entrySet()) {
                totalHours += differenceBetweenDatesInHours(deadlines.get(entry.getKey()), entry.getValue());
            }
        }

        if(getQuizSubmissions(week) == 0)
            return 0;

        return (int) totalHours/getQuizSubmissions(week);
    }

    //Auxiliary methods
    public String getId() {
        return id;
    }

    public int getSessions(int week) {
        int count = 0;

        for(int i = 0; i < week; i++)
            count += session_count[i];

        return count;
    }

    public int getSessionTimeInWeek(int week) {
        return session_length[week-1];
    }

    public int getAverageSessionTimeInWeek(int week) {
        if(session_count[week-1] == 0)
            return 0;

        return session_length[week-1]/ session_count[week-1];
    }



    /*private Date weedEndDate(int week) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            switch (week) {
                case 1:
                    return format.parse("2014-12-02 00:00:00");
                case 2:
                    return format.parse("2014-12-09 00:00:00");
                case 3:
                    return format.parse("2014-12-16 00:00:00");
                case 4:
                    return format.parse("2014-12-23 00:00:00");
                case 5:
                    return format.parse("2014-12-30 00:00:00");
                case 6:
                    return format.parse("2015-01-06 00:00:00");
                case 7:
                    return format.parse("2015-01-13 00:00:00");
                case 8:
                    return format.parse("2015-01-20 00:00:00");
                case 9:
                    return format.parse("2015-01-27 00:00:00");
                case 10:
                    return format.parse("2015-02-03 00:00:00");
                default:
                    return format.parse("2015-02-14 00:00:00");
            }
        } catch (ParseException e) {
            System.out.println();
        }
        return new Date();
    }*/

    private String weedEndDate(int week) {
            switch (week) {
                case 1:
                    return "2014-12-02 00:00:00";
                case 2:
                    return "2014-12-09 00:00:00";
                case 3:
                    return "2014-12-16 00:00:00";
                case 4:
                    return "2014-12-23 00:00:00";
                case 5:
                    return "2014-12-30 00:00:00";
                case 6:
                    return "2015-01-06 00:00:00";
                case 7:
                    return "2015-01-13 00:00:00";
                case 8:
                    return "2015-01-20 00:00:00";
                case 9:
                    return "2015-01-27 00:00:00";
                case 10:
                    return "2015-02-03 00:00:00";
                default:
                    return "2015-02-14 00:00:00";
            }

    }

    private Date getDateFromString(String dateString) {
        //input date: "2014-11-11 12:00:00"
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(dateString.replace("T", " "));
        }
        catch (ParseException e) {
            System.out.println("Invalid date");
            return new Date();
        }
    }

    private static long differenceBetweenDatesInHours(Date deadline, Date submission){
        long diff = deadline.getTime() - submission.getTime();

        if(diff > 0)
            return TimeUnit.MILLISECONDS.toHours(diff);

        return -1;
    }

}
