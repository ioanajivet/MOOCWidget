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
 * Created by Ioana on 5/24/2016.
 */
public class Motivation {

    private static HashMap<String, UserForDataAnalysis> users = new HashMap<>();
    private static HashMap<Integer, List<Double>> thresholds = new HashMap<>();

    public static void analysis(String course, int weeks) throws IOException {

        //1. read scaled thresholds
        readScaledThresholds(course);
        System.out.println("Read: " + thresholds.size() + " weeks");

        //2. read users - scaled values
        readScaledMetrics(course, weeks);
        System.out.println("Read: " + users.size() + " users");

        //3. for each user, calculate the status per week

        //4. write it in a file
        writeStatus(course, weeks);

    }

    private static void writeStatus(String course, int weeks) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter("data\\analysis\\" + course + "\\motivation\\status.csv"), ',');
        String[] toWrite;
        String write = "User id";
        UserForDataAnalysis current;

        for(int i = 1; i <= weeks; i++)
            write += "#Week" + i;
        toWrite = write.split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataAnalysis> entry : users.entrySet()) {
            current = entry.getValue();
            toWrite[0] = current.getId();

            for(int i = 1; i <= weeks; i++)
                current.calculateStatus(i, thresholds.get(i));

            List<Double> results = current.getStatus();

            for (int i = 1; i <= weeks; i++)
                toWrite[i] = results.get(i).toString();

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void readScaledMetrics(String course, int weeks) {
        for (int i = 1; i <= weeks; i++)
            try {
                readWeeklyScaledMetrics(i, "data\\" + course + "\\2016\\week" + i + "\\metrics\\" + course.toUpperCase() + "2016_scaled_metrics.csv");
            } catch (IOException e) {
                System.out.println(e);
            }
    }

    private static void readWeeklyScaledMetrics(int week, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine;
        UserForDataAnalysis current;

        csvReader.readNext();   //headers

        while ((nextLine = csvReader.readNext()) != null) {

            current = users.get(nextLine[0]);

            if(current == null)
                current = new UserForDataAnalysis(nextLine[0]);

            for(int i = 1; i < 7; i++)
                current.setMetric(i, week, Double.parseDouble(nextLine[i]));

            users.put(nextLine[0], current);
        }

        csvReader.close();

    }

    private static void readScaledThresholds(String course) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\" + course + "\\thresholds\\scaled_thresholds5.csv"));
        String[] nextLine;
        List<Double> currentWeek;
        int week;

        csvReader.readNext();   //headers

        while ((nextLine = csvReader.readNext()) != null) {

            week = Integer.parseInt(nextLine[0].split(" ")[1]);

            currentWeek = new ArrayList<>(7);
            currentWeek.add(0.0);   //needs to be initialized

            for(int i = 1; i < 7; i++)
                currentWeek.add(i, Double.parseDouble(nextLine[i]));

            thresholds.put(week, currentWeek);
        }

        csvReader.close();

    }
}
