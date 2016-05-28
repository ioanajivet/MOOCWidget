package analysis;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import javafx.util.Pair;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Ioana on 5/25/2016.
 */
public class Engagement {

    private static List<String> testUsers;
    private static List<String> controlUsers;


    private static HashMap<String, String> lastLoginDate;   //user, date
    private static HashMap<String, Integer> learningActivities; //user, #learning activities

    private static HashMap<String, List<Integer>> loggedUsers;   //user, list of weeks in which she logged in
    private static HashMap<String, List<Integer>> loggedVideoUsers;   //user, list of weeks in which she watched a video
    private static HashMap<String, List<Integer>> loggedQuizUsers;   //user, list of weeks in which she submitted a quiz
    private static HashMap<String, List<Integer>> loggedForumUsers;   //user, list of weeks in which she visited the forum

    public static void analysis(String course, int weeks) throws IOException {

        initialize();
        
        //0. read active users
        testUsers = readUsers("data\\analysis\\" + course + "\\1. activity\\" + course.toUpperCase() + "_test_active.csv");
        controlUsers = readUsers("data\\analysis\\" + course + "\\1. activity\\" + course.toUpperCase() + "_control_active.csv");

        //A. activity persistence
        //activityPersistence(course, weeks);

        //B. Course engagement over time
        // TODO: 5/28/2016
        readLoggedSessions(course, "data\\" + course + "\\2016\\week" + weeks + "\\data\\sessions.csv");
        readLoggedVideoSessions(course, "data\\" + course + "\\2016\\week" + weeks + "\\data\\observations.csv");
        readLoggedQuizSessions(course, "data\\" + course + "\\2016\\week" + weeks + "\\data\\submissions.csv");
        readLoggedForumSessions(course, "data\\" + course + "\\2016\\week" + weeks + "\\data\\forum_sessions.csv");

        writeActivity(weeks, "data\\analysis\\" + course + "\\5. engagement\\" + course.toUpperCase() + "_activity.csv");


        //C. course persistence
        //coursePersistence(course, weeks);

        //D. Number of learners that submit assignments per week
        //quizSubmitterCount(course, weeks);

        }

    private static void writeActivity(int weeks, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "Week#T - logged in#C - logged in#T - watched a video#C - watched a video#T - submitted a quiz#C - submitted a quiz#T - visited forum#C - visited forum".split("#");
        output.writeNext(toWrite);

        for (int i = 1; i <= weeks; i++) {
            toWrite[0] = String.valueOf(i);
            toWrite[1] = String.valueOf(filterPerWeek(loggedUsers, i, 0));
            toWrite[2] = String.valueOf(filterPerWeek(loggedUsers, i, 1));
            toWrite[3] = String.valueOf(filterPerWeek(loggedVideoUsers, i, 0));
            toWrite[4] = String.valueOf(filterPerWeek(loggedVideoUsers, i, 1));
            toWrite[5] = String.valueOf(filterPerWeek(loggedQuizUsers, i, 0));
            toWrite[6] = String.valueOf(filterPerWeek(loggedQuizUsers, i, 1));
            toWrite[7] = String.valueOf(filterPerWeek(loggedForumUsers, i, 0));
            toWrite[8] = String.valueOf(filterPerWeek(loggedForumUsers, i, 1));

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static long filterPerWeek(HashMap<String, List<Integer>> loggedUsers, int week, int parity) {
        return loggedUsers.entrySet().stream()
                .filter(e -> Integer.parseInt(e.getKey()) % 2 == parity)
                .map(e -> e.getValue())
                .filter(e -> e.contains(week))
                .count();

    }

    private static void readLoggedSessions(String course, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        String user;
        int week;

        loggedUsers.clear();

        while ((nextLine = csvReader.readNext()) != null) {
            user = nextLine[0].split("_")[2];
            week = Utils.getWeek(course, nextLine[0].split("_")[3]);
            if(!testUsers.contains(user) && !controlUsers.contains(user))
                continue;

            if(loggedUsers.containsKey(user)) {
                if (loggedUsers.get(user).contains(week))
                    continue;
                loggedUsers.get(user).add(week);
            }
            else {
                List<Integer> weeks = new ArrayList<>();
                weeks.add(week);
                loggedUsers.put(user, weeks);
            }

        }

        csvReader.close();

    }

    private static void readLoggedVideoSessions(String course, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        String user;
        int week;

        loggedVideoUsers.clear();

        while ((nextLine = csvReader.readNext()) != null) {
            user = nextLine[0].split("_")[1];
            week = Utils.getWeek(course, nextLine[0].split("_")[3]);
            if(!testUsers.contains(user) && !controlUsers.contains(user))
                continue;

            if(loggedVideoUsers.containsKey(user)) {
                if (loggedVideoUsers.get(user).contains(week))
                    continue;
                loggedVideoUsers.get(user).add(week);
            }
            else {
                List<Integer> weeks = new ArrayList<>();
                weeks.add(week);
                loggedVideoUsers.put(user, weeks);
            }

        }

        csvReader.close();

    }

    private static void readLoggedQuizSessions(String course, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        String user;
        int week;

        loggedQuizUsers.clear();

        while ((nextLine = csvReader.readNext()) != null) {
            user = nextLine[0].split("_")[1];
            week = Utils.getWeek(course, nextLine[5]);
            if(!testUsers.contains(user) && !controlUsers.contains(user))
                continue;

            if(loggedQuizUsers.containsKey(user)) {
                if (loggedQuizUsers.get(user).contains(week))
                    continue;
                loggedQuizUsers.get(user).add(week);
            }
            else {
                List<Integer> weeks = new ArrayList<>();
                weeks.add(week);
                loggedQuizUsers.put(user, weeks);
            }

        }

        csvReader.close();

    }

    private static void readLoggedForumSessions(String course, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        String user;
        int week;

        loggedForumUsers.clear();

        while ((nextLine = csvReader.readNext()) != null) {
            user = nextLine[0].split("_")[3];
            week = Utils.getWeek(course, nextLine[0].split("_")[4]);
            if(!testUsers.contains(user) && !controlUsers.contains(user))
                continue;

            if(loggedForumUsers.containsKey(user)) {
                if (loggedForumUsers.get(user).contains(week))
                    continue;
                loggedForumUsers.get(user).add(week);
            }
            else {
                List<Integer> weeks = new ArrayList<>();
                weeks.add(week);
                loggedForumUsers.put(user, weeks);
            }

        }

        csvReader.close();

    }

    private static void activityPersistence(String course, int weeks) throws IOException {
        // 1. last login date
        lastLogin("data\\" + course + "\\2016\\week" + weeks + "\\data\\sessions.csv");

        writeLastLogin("data\\analysis\\" + course + "\\5. engagement\\" + course.toUpperCase() + "_last_login.csv");
        writeLastLoginCount("data\\analysis\\" + course + "\\5. engagement\\" + course.toUpperCase() + "_last_login_count.csv");
        writeLastLoginCountWeek(course, weeks, "data\\analysis\\" + course + "\\5. engagement\\" + course.toUpperCase() + "_last_login_count_week.csv");

        // 2. last learning activity session
        //TODO it might not be relevant
    }

    private static void coursePersistence(String course, int weeks) throws IOException {
        //Calculates how many of the learners covered 30, 50, 80 % of learning materials
        int maximum;

        //1. Graded quiz questions
        maximum = Utils.getMaximumGradedAssignments(course);
        learningActivities.clear();
        gradedAssignments("data\\" + course + "\\2016\\week" + weeks + "\\metrics\\" + course.toUpperCase() + "2016_metrics_all.csv");
        writeLearningActivitiesPercentages(maximum, "data\\analysis\\" + course + "\\5. engagement\\" + course.toUpperCase() + "_graded_assignments.csv");

        //2. Non-graded quiz questions
        maximum = Utils.getMaximumNonGradedAssignments(course);
        learningActivities.clear();
        if(maximum > 0) {
            nonGradedAssignments("data\\" + course + "\\2016\\" + course.toUpperCase() + "2016_non-graded.csv",
                    "data\\" + course + "\\2016\\week" + weeks + "\\data\\submissions.csv");
            writeLearningActivitiesPercentages(maximum, "data\\analysis\\" + course + "\\5. engagement\\" + course.toUpperCase() + "_non-graded_assignments.csv");
        }

        //3. Video material
        maximum = Utils.getMaximumVideos(course);
        learningActivities.clear();
        videos("data\\" + course + "\\2016\\" + course.toUpperCase() + "2016_videos.csv",
                "data\\" + course + "\\2016\\week" + weeks + "\\data\\observations.csv");
        writeLearningActivitiesPercentages(maximum, "data\\analysis\\" + course + "\\5. engagement\\" + course.toUpperCase() + "_videos.csv");
    }

    private static void quizSubmitterCount(String course, int weeks) throws IOException {
        testUsers = readUsers("data\\" + course + "\\2016\\" + course.toUpperCase() + "2016_test.csv");
        controlUsers = readUsers("data\\" + course + "\\2016\\" + course.toUpperCase() + "2016_control.csv");
        quizSubmitters(course, weeks, "data\\analysis\\" + course + "\\5. engagement\\" + course.toUpperCase() + "_quizSubmitters.csv");
    }


    //INITIALIZATION
    private static void initialize() {
        testUsers = new ArrayList<>();
        controlUsers = new ArrayList<>();

        lastLoginDate = new HashMap<>();
        learningActivities = new HashMap<>();

        loggedUsers = new HashMap<>();
        loggedVideoUsers = new HashMap<>();
        loggedQuizUsers = new HashMap<>();
        loggedForumUsers = new HashMap<>();
    }

    private static List<String> readUsers(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;

        List<String> users = new ArrayList<>();

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null)
            users.add(nextLine[0]);

        csvReader.close();

        return users;
    }


    //A. ACTIVITY PERSISTENCE
    private static void lastLogin(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        String date, user;

        while ((nextLine = csvReader.readNext()) != null) {
            user = nextLine[0].split("_")[2];
            date = (nextLine[0].split("_")[3]).split(" ")[0];

            if(testUsers.contains(user) || controlUsers.contains(user)) {
                String last_date = lastLoginDate.get(user);

                if (last_date == null || date.compareTo(last_date) > 0)
                    lastLoginDate.put(user, date);

            }

        }

        csvReader.close();
    }

    private static void writeLastLogin(String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "User id#Last login".split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, String> entry: lastLoginDate.entrySet()) {
            toWrite[0] = entry.getKey();
            toWrite[1] = entry.getValue();

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeLastLoginCount(String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "Date#Test#Control".split("#");
        output.writeNext(toWrite);

        List<String> dates = lastLoginDate.entrySet().stream()
                .map(e -> e.getValue())
                .distinct()
                .collect(Collectors.toList());

        Collections.sort(dates);

        for (String date: dates) {
            toWrite[0] = date;
            toWrite[1] = String.valueOf(lastLoginDate.entrySet().stream()
                    .filter(e -> Integer.parseInt(e.getKey()) % 2 == 0)
                    .filter(e -> e.getValue().compareTo(date) == 0)
                    .count());
            toWrite[2] = String.valueOf(lastLoginDate.entrySet().stream()
                    .filter(e -> Integer.parseInt(e.getKey()) % 2 == 1)
                    .filter(e -> e.getValue().compareTo(date) == 0)
                    .count());

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeLastLoginCountWeek(String course, int weeks, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "Week#Test#Control".split("#");
        output.writeNext(toWrite);

        List<String> dates = lastLoginDate.entrySet().stream()
                .map(e -> e.getValue())
                .distinct()
                .collect(Collectors.toList());

        Collections.sort(dates);

        for (int i = 1; i <= weeks; i++) {
            toWrite[0] = String.valueOf(i);
            toWrite[1] = String.valueOf(filterDays(course, i, 0));
            toWrite[2] = String.valueOf(filterDays(course, i, 1));

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static long filterDays(String course, int week, int parity) {
        return lastLoginDate.entrySet().stream()
                .filter(e -> Integer.parseInt(e.getKey()) % 2 == parity)
        .filter(e -> Utils.getWeek(course, e.getValue()) == week)
                .count();
    }


    //B. COURSE PERSISTENCE
    private static void writeLearningActivitiesPercentages(int max, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "Percentage#Test#Control".split("#");
        output.writeNext(toWrite);

        toWrite = getLearningActivityPercentages(max, 30);
        output.writeNext(toWrite);

        toWrite = getLearningActivityPercentages(max, 50);
        output.writeNext(toWrite);

        toWrite = getLearningActivityPercentages(max, 80);
        output.writeNext(toWrite);

        output.close();
    }

    private static void gradedAssignments(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null)
            if(testUsers.contains(nextLine[0]) || controlUsers.contains(nextLine[0]))
                learningActivities.put(nextLine[0], Integer.parseInt(nextLine[5]));

        csvReader.close();

    }

    private static void nonGradedAssignments(String problems, String submissions) throws IOException {

        List<String> quizIds;

        quizIds = readResources(problems);
        readSubmissions(quizIds, submissions);

    }

    private static void videos(String videos, String observations) throws IOException {

        List<String> videoIds;

        videoIds = readResources(videos);
        readObservations(videoIds, observations);

    }

    private static String[] getLearningActivityPercentages(int max, int percentage) throws IOException {
        String[] toWrite = new String[3];
        int threshold = max * percentage / 100;

        System.out.println(percentage + "% threshold: " + threshold);
        toWrite[0] = "> " + percentage + "%";
        toWrite[1] =  String.valueOf(learningActivities.entrySet().stream()
                .filter(e -> Integer.parseInt(e.getKey()) % 2 == 0)
                .filter(e -> e.getValue() >= threshold)
                .count());
        toWrite[2] = String.valueOf(learningActivities.entrySet().stream()
                .filter(e -> Integer.parseInt(e.getKey()) % 2 == 1)
                .filter(e -> e.getValue() >= threshold)
                .count());

        return toWrite;
    }

    private static List<String> readResources(String problems) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(problems));
        String [] nextLine;
        List<String> resourceIds = new ArrayList<>();

        while ((nextLine = csvReader.readNext()) != null) {
            resourceIds.add(nextLine[0].split("@")[2]);
        }

        csvReader.close();

        return resourceIds;
    }

    private static void readSubmissions(List<String> quizIds, String submissions) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(submissions));
        String [] nextLine;

        while ((nextLine = csvReader.readNext()) != null) {
            if(nextLine[3].compareTo("problem_graded") != 0)
                continue;

            if(!quizIds.contains(nextLine[4]))
                continue;

            String user = nextLine[0].split("_")[1];

            if(!testUsers.contains(user) && !controlUsers.contains(user))
                continue;

            if(learningActivities.containsKey(user))
                learningActivities.put(user, learningActivities.get(user) + 1);
            else
                learningActivities.put(user, 1);
        }

        csvReader.close();
    }

    private static void readObservations(List<String> videoIds, String observations) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(observations));
        String [] nextLine;

        List<Pair<String, String>> videoObservations = new ArrayList<>();

        while ((nextLine = csvReader.readNext()) != null) {
            if(!videoIds.contains(nextLine[2]))
                continue;

            String user = nextLine[0].split("_")[1];

            if(!testUsers.contains(user) && !controlUsers.contains(user))
                continue;

            videoObservations.add(new Pair<>(user, nextLine[2]));

        }

        csvReader.close();

        for (String user: testUsers)
            learningActivities.put(user, (int) videoObservations.stream()
                    .filter(e -> e.getKey().compareTo(user) == 0)
                    .map(e -> e.getValue())
                    .distinct()
                    .count());
        for (String user: controlUsers)
            learningActivities.put(user, (int) videoObservations.stream()
                    .filter(e -> e.getKey().compareTo(user) == 0)
                    .map(e -> e.getValue())
                    .distinct()
                    .count());


    }

    //D. QUIZ SUBMITTERS
    private static void quizSubmitters(String course, int weeks, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        List<String> uniqueSubmitters = new ArrayList<>();

        toWrite = "Week#Test#Control".split("#");
        output.writeNext(toWrite);

        for(int i = 1; i <= weeks; i++) {
            learningActivities.clear();
            gradedAssignments("data\\" + course + "\\2016\\week" + i + "\\metrics\\" + course.toUpperCase() + "2016_metrics_all.csv");

            //add new unique submitters
            learningActivities.entrySet().stream()
                    .filter(e -> e.getValue() > 0 && !uniqueSubmitters.contains(e.getKey()))
                    .map(e -> {
                        uniqueSubmitters.add(e.getKey());
                        return e;
                    })
                    .count();

            System.out.println(uniqueSubmitters.size());

            toWrite[0] = String.valueOf(i);
            toWrite[1] = String.valueOf(uniqueSubmitters.stream()
                    .filter(e -> Integer.parseInt(e) % 2 == 0)
                    .count());

            toWrite[2] = String.valueOf(uniqueSubmitters.stream()
                    .filter(e -> Integer.parseInt(e) % 2 == 1)
                    .count());

            output.writeNext(toWrite);
        }

        output.close();

    }
}
