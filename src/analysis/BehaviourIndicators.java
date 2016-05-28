package analysis;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ioana on 5/27/2016.
 */
public class BehaviourIndicators {


    private static List<String> testUsers = new ArrayList<>();
    private static List<String> controlUsers = new ArrayList<>();

    public static void analysis(String course, int weeks) throws IOException {

        //1. read active users
        testUsers = readActiveUsers("data\\analysis\\" + course + "\\1. activity\\" + course.toUpperCase() + "_test_active.csv");
        controlUsers = readActiveUsers("data\\analysis\\" + course + "\\1. activity\\" + course.toUpperCase() + "_control_active.csv");

        //2. read metrics for active users in test and control group and divide them for test or control
        writeMetrics(course, weeks, "data\\" + course + "\\2016\\week" + weeks + "\\metrics\\" + course.toUpperCase() + "2016_metrics_all.csv",
                "data\\analysis\\" + course + "\\4. metrics\\" + course.toUpperCase() + "_test_metrics.csv",
                "data\\analysis\\" + course + "\\4. metrics\\" + course.toUpperCase() + "_control_metrics.csv");

    }

    private static List<String> readActiveUsers(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;

        List<String> users = new ArrayList<>();

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null)
            users.add(nextLine[0]);

        csvReader.close();

        return users;
    }

    private static void writeMetrics(String course, int week, String input_filename, String test_filename, String control_filename) throws IOException {
        CSVReader input = new CSVReader(new FileReader(input_filename), ',');
        CSVWriter output_test = new CSVWriter(new FileWriter(test_filename), ',');
        CSVWriter output_control = new CSVWriter(new FileWriter(control_filename), ',');
        String [] nextLine;

        //metric names
        nextLine = input.readNext();
        output_test.writeNext(nextLine);
        output_control.writeNext(nextLine);

        while ((nextLine = input.readNext()) != null) {
            if (testUsers.contains(nextLine[0]))
                output_test.writeNext(nextLine);
            else if (controlUsers.contains(nextLine[0]))
                output_control.writeNext(nextLine);
        }

        input.close();
        output_test.close();
        output_control.close();
    }
}
