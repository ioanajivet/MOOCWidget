package analysis;

//select for analysis only the active users from both groups
//Active - >5 mins on the platform

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import st.UserMetricComputation;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ioana on 5/25/2016.
 */
public class Activity {

    private static HashMap<String, Integer> timeOnPlatform = new HashMap<>();
    private static HashMap<String, Integer> sessionsLogged = new HashMap<>();

    public static void selectActive(String course, int week, int timeLimit) throws IOException {
        //read sessions and count time on the platform

        //1. read all users in a HashMap
        readUsers("data\\" + course + "\\2016\\" + course.toUpperCase() + "2016_student_profile.csv");
        System.out.println("Users read: " + timeOnPlatform.size());

        //2. read sessions and add time in total
        readSessions("data\\" + course + "\\2016\\week" + week + "\\data\\sessions.csv");

        //3. print the learner list with their time -> [course]_time_on_platform.csv
        writeTimeOnPlatform("data\\analysis\\" + course + "\\1. activity\\" + course.toUpperCase() + "_time_on_platform.csv");

        //4. print the active learners -> [course]_test_active.csv & [course]_test_active.csv
        writeActive("data\\analysis\\" + course + "\\1. activity\\" + course.toUpperCase() + "_test_active.csv", 0, timeLimit);
        writeActive("data\\analysis\\" + course + "\\1. activity\\" + course.toUpperCase() + "_control_active.csv", 1, timeLimit);

        //5. write the results I want directly in a file
        writeResults("data\\analysis\\" + course + "\\1. activity\\" + course.toUpperCase() + "_activity_results.csv", timeLimit);
    }

    private static void readUsers(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            timeOnPlatform.put(nextLine[0], 0);
            sessionsLogged.put(nextLine[0], 0);
        }

        csvReader.close();

    }

    private static void readSessions(String filename) throws IOException {
        //session_id, course_user_id, duration
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine, session_attr;
        int duration, week;

        while ((nextLine = csvReader.readNext()) != null) {
            session_attr = nextLine[0].split("_");

            if(timeOnPlatform.containsKey(session_attr[2])) {
                duration = Integer.parseInt(nextLine[2]);

                timeOnPlatform.put(session_attr[2], timeOnPlatform.get(session_attr[2]) + duration);
                sessionsLogged.put(session_attr[2], sessionsLogged.get(session_attr[2]) + 1);


            }
        }

        csvReader.close();
    }

    private static void writeTimeOnPlatform(String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "User_id#Time on platform#Sessions logged".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, Integer> entry : timeOnPlatform.entrySet()) {
            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(entry.getValue());
            toWrite[2] = String.valueOf(sessionsLogged.get(entry.getKey()));

            output.writeNext(toWrite);
        }
        output.close();
    }

    private static void writeActive(String filename, int parity, int timeLimit) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "User_id#Time on platform#Sessions logged".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, Integer> entry : timeOnPlatform.entrySet()) {

            if(Integer.parseInt(entry.getKey()) % 2 != parity)
                continue;

            if(entry.getValue() < timeLimit)
                continue;

            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(entry.getValue());
            toWrite[2] = String.valueOf(sessionsLogged.get(entry.getKey()));

            output.writeNext(toWrite);
        }
        output.close();
    }

    private static void writeResults(String filename, int timeLimit) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "Condition#Test#%#Control#%".split("#");

        output.writeNext(toWrite);

        //1. logged at least one session
        toWrite[0] = "Logged at least 1 session";
        toWrite[1] = String.valueOf(sessionsLogged.entrySet().stream()
                .filter(e -> e.getValue() > 0 && Integer.parseInt(e.getKey()) % 2 == 0)
                .count());
        toWrite[3] = String.valueOf(sessionsLogged.entrySet().stream()
                .filter(e -> e.getValue() > 0 && Integer.parseInt(e.getKey()) % 2 == 1)
                .count());
        output.writeNext(toWrite);

        //2. logged at least five sessions
        toWrite[0] = "Logged at least 5 sessions";
        toWrite[1] = String.valueOf(sessionsLogged.entrySet().stream()
                .filter(e -> e.getValue() > 4 && Integer.parseInt(e.getKey()) % 2 == 0)
                .count());
        toWrite[3] = String.valueOf(sessionsLogged.entrySet().stream()
                .filter(e -> e.getValue() > 4 && Integer.parseInt(e.getKey()) % 2 == 1)
                .count());
        output.writeNext(toWrite);

        //3. logged at least 5 minutes on the platform
        toWrite[0] = "Logged at least 5 minutes on the platform";
        toWrite[1] = String.valueOf(timeOnPlatform.entrySet().stream()
                .filter(e -> e.getValue() >= 300 && Integer.parseInt(e.getKey()) % 2 == 0)
                .count());
        toWrite[3] = String.valueOf(timeOnPlatform.entrySet().stream()
                .filter(e -> e.getValue() >= 300 && Integer.parseInt(e.getKey()) % 2 == 1)
                .count());
        output.writeNext(toWrite);

        //4. logged at least 10 minutes on the platform
        toWrite[0] = "Logged at least 10 minutes on the paltform";
        toWrite[1] = String.valueOf(timeOnPlatform.entrySet().stream()
                .filter(e -> e.getValue() >= 600 && Integer.parseInt(e.getKey()) % 2 == 0)
                .count());
        toWrite[3] = String.valueOf(timeOnPlatform.entrySet().stream()
                .filter(e -> e.getValue() >= 600 && Integer.parseInt(e.getKey()) % 2 == 1)
                .count());
        output.writeNext(toWrite);

        //5. total
        toWrite[0] = "Total";
        toWrite[1] = String.valueOf(timeOnPlatform.entrySet().stream()
                .filter(e -> Integer.parseInt(e.getKey()) % 2 == 0)
                .count());
        toWrite[3] = String.valueOf(timeOnPlatform.entrySet().stream()
                .filter(e -> Integer.parseInt(e.getKey()) % 2 == 1)
                .count());
        output.writeNext(toWrite);

        output.close();

    }
}
