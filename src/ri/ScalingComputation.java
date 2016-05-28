package ri; /**
 * Created by Ioana on 1/9/2016.
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

//TODO: check file paths for file read and generation so they don't overwrite stuff
//TODO: check consistency of week numbering: starting with 0 or with 1


public class ScalingComputation {



    private static HashMap<String, User> users;
    private static int[] maximums;

        public static void main(String[] args) throws IOException,ParseException
        {
            int week = 1;

            scalingMetrics(week);

        }

    private static void initialize() {
        users = new HashMap<>();
        maximums = new int[6];

    }

    public static void scalingMetrics(int week) throws IOException {
        initialize();

        readMetrics("data\\ri\\2016\\week" + week + "\\metrics\\RI2016_metrics.csv");
        readMaximums(week, "data\\st\\thresholds\\maximum5.csv");
        //getMaximums();

        writeScaledMetrics("data\\ri\\2016\\week" + week + "\\metrics\\RI2016_scaled_metrics.csv");
    }

    //************************
    //************ Loading data

    private static void readMetrics(String filepath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filepath));
        String [] nextLine;
        String id;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            id = nextLine[0];

            User current = new User(id);

            current.sessionsPerWeek = Integer.parseInt(nextLine[1]);
            current.lengthOfSession = Integer.parseInt(nextLine[2]);
            current.betweenSessions = Integer.parseInt(nextLine[3]);
            current.timeOnTask = Integer.parseInt(nextLine[4]);
            current.assignments = Integer.parseInt(nextLine[5]);
            current.timeliness = Integer.parseInt(nextLine[6]);

            users.put(id, current);

        }

        csvReader.close();

    }

    private static void readMaximums(int week, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        int line = 1;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null && line < week)
            line++;

        for(int i = 0; i < 6; i++)
            maximums[i] = Integer.parseInt(nextLine[i+1]);

        csvReader.close();
    }
    //************************
    //************ Writing data

    private static void writeScaledMetrics(String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        User current;
        double[] scaled = new double[6];

        toWrite = "User_id#Sessions/week#Length of session#Between sessions#Time on task#Assignments#Until deadline".split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, User> entry : users.entrySet()) {
            current = entry.getValue();

            scaled[0] = current.sessionsPerWeek * 10.0 / maximums[0];
            scaled[1] = current.lengthOfSession * 10.0 / maximums[1];
            if(current.betweenSessions == -1)
                scaled[2] = 0;
            else
                scaled[2] = current.betweenSessions * 10.0 / maximums[2];
            scaled[3] = current.timeOnTask * 10.0/ maximums[3];
            scaled[4] = current.assignments * 10.0 / maximums[4];

            if(current.timeliness == -1)
                scaled[5] = 0;
            else
                scaled[5] = current.timeliness * 10.0 / maximums[5];

            toWrite[0] = entry.getKey();

            for(int i = 0 ; i < 6; i++)
                if (Double.isNaN(scaled[i]))
                    toWrite[i+1] = "0";
                else if(scaled[i] > 10)
                    toWrite[i+1] = "10";
                else
                    toWrite[i+1] = String.format("%.1f", scaled[i]);

            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void getMaximums() {
        maximums[0] = users.values().stream().mapToInt(e -> e.sessionsPerWeek).max().getAsInt();
        maximums[1] = users.values().stream().mapToInt(e -> e.lengthOfSession).max().getAsInt();
        maximums[2] = users.values().stream().mapToInt(e -> e.betweenSessions).max().getAsInt();
        maximums[3] = users.values().stream().mapToInt(e -> e.timeOnTask).max().getAsInt();
        maximums[4] = users.values().stream().mapToInt(e -> e.assignments).max().getAsInt();
        maximums[5] = users.values().stream().mapToInt(e -> e.timeliness).max().getAsInt();
    }

}

class User {
    public String id;

    public int sessionsPerWeek;
    public int lengthOfSession;
    public int betweenSessions;
    public int timeOnTask;
    public int assignments;
    public int timeliness;

    public int[] values;

    public User(String id, int sessionsPerWeek, int lengthOfSession, int betweenSessions, int timeOnTask, int assignments, int timeliness) {
        this.id = id;
        this.sessionsPerWeek = sessionsPerWeek;
        this.lengthOfSession = lengthOfSession;
        this.betweenSessions = betweenSessions;
        this.timeOnTask = timeOnTask;
        this.assignments = assignments;
        this.timeliness = timeliness;

        values = new int[6];
    }

    public User (String id) {
        this.id = id;
        values = new int[6];
    }
}