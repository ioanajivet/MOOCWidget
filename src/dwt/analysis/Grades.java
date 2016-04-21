/**
 * Created by Ioana on 3/1/2016.
 *
 * Command line arguments:
 * args[0] = week for which the metrics computation is done
 */

package dwt.analysis;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Grades {

    static List<String> users;
    static List<String> testUsers;
    static List<String> controlUsers;

    static HashMap<String, Double> testGrades;
    static HashMap<String, Double> controlGrades;

    public static void main(String[] args) throws IOException, ParseException {

        initialize();

        //readGrades();
        readGradesActive();

    }

    private static void initialize() {

        users = new ArrayList<>();
        testUsers = new ArrayList<>();
        controlUsers = new ArrayList<>();

        testGrades = new HashMap<>();
        controlGrades = new HashMap<>();

    }


    //========= Final grades ==========
    //-------- Read final grades ---------

    private static void readGrades() throws IOException {

        readUsers();
        readFinalGrades();

        System.out.println("Users size: " + users.size());
        System.out.println("Control grades: " + controlGrades.size());
        System.out.println("Test grades: " + testGrades.size());

        writeGrades(controlGrades, "data\\2016\\post-data\\week9\\week9\\control_final_grades.csv");
        writeGrades(testGrades, "data\\2016\\post-data\\week9\\week9\\test_final_grades.csv");


    }

    private static void writeGrades(HashMap<String, Double> group, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "User_id#Grade".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, Double> entry : group.entrySet()) {
            toWrite = (entry.getKey() + "#" + entry.getValue()).split("#");
            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void readFinalGrades() throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2016\\grades.csv"));
        String[] nextLine;
        String shortId;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            shortId = nextLine[0];

            if(!users.contains(shortId))
                continue;

            if (Integer.parseInt(shortId) % 2 == 0 || shortId.compareTo("7538013") == 0 || shortId.compareTo("7592701") == 0)
                testGrades.put(shortId, Double.parseDouble(nextLine[1]));
            else
                controlGrades.put(shortId, Double.parseDouble(nextLine[1]));

        }

        csvReader.close();

    }

    //-------- Read grades for active users (>5min on the platform) ---------

    private static void readGradesActive() throws IOException {

        readActiveUsers(users, "data\\2016\\post-data\\all_metrics_control_5min_9.csv");
        readActiveUsers(users, "data\\2016\\post-data\\all_metrics_test_5min_9.csv");

        readFinalGrades();

        System.out.println("Users size: " + users.size());
        System.out.println("Control grades: " + controlGrades.size());
        System.out.println("Test grades: " + testGrades.size());

        writeGrades(controlGrades, "data\\2016\\post-data\\week9\\week9\\control_final_grades_active.csv");
        writeGrades(testGrades, "data\\2016\\post-data\\week9\\week9\\test_final_grades_active.csv");


    }

    //========= Engagement ==========
    //-------- Day users drop out ---------


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
