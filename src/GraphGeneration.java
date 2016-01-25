/**
 * Created by Ioana on 1/9/2016.
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.text.ParseException;
import java.util.*;

//TODO: check file paths for file read and generation so they don't overwrite stuff
//TODO: check consistency of week numbering: starting with 0 or with 1
public class GraphGeneration {

    static HashMap<String, UserForGraphGeneration> users;
    static double[] thresholds;

    static HashMap<String, Integer> scaledWeeklyTimes;
    static HashMap<String, Integer> scaledVideoTimes;
    static HashMap<String, Integer> scaledRatio;
    static HashMap<String, Integer> scaledVideos;
    static HashMap<String, Integer> scaledAssignments;
    static HashMap<String, Integer> scaledDeadlines;

        public static void main(String[] args) throws IOException,ParseException
        {
            int week = 2;

            initialize();

            readMetrics(week, "data\\2015\\user_metrics\\");
            scaleMetrics(week);

            writeScaledMetrics(week, "data\\2015\\user_metrics\\scaled_metrics");

            readThresholds(week, "data\\thresholds\\thresholds5.csv");
            scaleThresholds(week);

            generateJS(week, "generate\\2015\\js\\");
            generatePNG();


        }


    //************************
    //************ Loading data

    private static void readMetrics(int week, String filepath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filepath + "metrics" + week + ".csv"));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            UserForGraphGeneration current = new UserForGraphGeneration(nextLine[0]);

            current.setPlatformTime(week, Integer.parseInt(nextLine[1]));
            current.setVideoTime(week, Integer.parseInt(nextLine[2]));
            current.setRatioTime(week, Double.parseDouble(nextLine[3]));
            current.setDistinctVideos(week, Integer.parseInt(nextLine[4]));
            current.setAssignments(week, Integer.parseInt(nextLine[5]));
            current.setUntilDeadline(week, Integer.parseInt(nextLine[6]));

            users.put(nextLine[0], current);
        }

        csvReader.close();

      //  readAnonymizedIds();

    }

    private static void readAnonymizedIds() throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2015\\anon-ids.csv"));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(users.containsKey(nextLine[0]))
                users.get(nextLine[0]).setAnonymousId(nextLine[1]);
        }

        csvReader.close();
    }

    private static void readThresholds(int week, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        int i = 0;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            thresholds[i++] = Double.parseDouble(nextLine[week]);
        }

        csvReader.close();
    }

    //************************
    //************ Writing data

    private static void writeScaledMetrics(int week, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename + week + ".csv"), ',');
        String[] toWrite;
        String current;

        toWrite = "User_id#Time on platform#Time on videos#Ratio video/platform#Distict videos#Assignments#Until deadline".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getKey();
            toWrite[0] = current;

            toWrite[1] = String.valueOf(scaledWeeklyTimes.get(current));
            toWrite[2] = String.valueOf(scaledVideoTimes.get(current));
            toWrite[3] = String.valueOf(scaledRatio.get(current));
            toWrite[4] = String.valueOf(scaledVideos.get(current));
            toWrite[5] = String.valueOf(scaledAssignments.get(current));
            toWrite[6] = String.valueOf(scaledDeadlines.get(current));

            output.writeNext(toWrite);
        }
        output.close();
    }

    private static void generateJS(int week, String filepath) throws IOException {
        Writer output;
        String toWrite, threshold, values;
        UserForGraphGeneration current;
        String filename;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            filename = entry.getKey().substring(18);    //todo: update with anonymousID

            output = new FileWriter(filepath + filename + ".js");

            threshold = getThresholdsString();

            values = getValuesString(current.getId(), week);
            toWrite = getAreaChartOptionsAsString(threshold, values);

            output.write(toWrite);
            output.close();
        }
    }

    private static void generatePNG() throws IOException {
        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            generateChartPNG(entry.getKey().substring(18));
        }
    }

    private static void generateChartPNG(String user) throws IOException{
        String phantom;

        phantom = "phantomjs highcharts-convert.js -infile 2015\\js\\" + user + ".js -outfile 2015\\out\\" + user + ".png -scale 2.5 -width 600";

        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "cd \"C:\\Users\\Ioana\\Desktop\\Thesis\\Widget\\generate\" && " + phantom);

        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            System.out.println(line);
        }
    }

    //************************
    //************ Computations

    private static void scaleThresholds(int week){
        int maxWeekTime = maxWeekTime(week);
        int maxVideoTime = maxVideoWeekTime(week);
        double maxRatio = maxRatioVideoPlatform(week);
        int maxVideos = maxDistinctVideos(week);
        int maxAssignment = maxAssignments(week);
        long maxUntilDeadline = maxUntilDeadline(week);

        thresholds[0] = Math.round(thresholds[0]*10/maxWeekTime);
        thresholds[1] = Math.round(thresholds[1]*10/maxVideoTime);
        thresholds[2] = Math.round(thresholds[2]*10/maxRatio);
        thresholds[3] = Math.round(thresholds[3]*10/maxVideos);

        if(maxAssignment == 0)
            thresholds[4] = 0;
        else
            thresholds[4] = Math.round(thresholds[4]*10/maxAssignment);

        if(maxUntilDeadline == 0)
            thresholds[5] = 0;
        else
            thresholds[5] = Math.round(thresholds[5]*10/maxUntilDeadline);

        for(int i=0;i<6;i++)
            System.out.println(i + ": " + thresholds[i]);
    }

    private static void scaleMetrics(int week){
        scalePlatformTime(week);
        scaleVideoTime(week);
        scaleRatioTime(week);
        scaleVideos(week);
        scaleAssignments(week);
        scaleUntilDeadline(week);
    }

    private static void scalePlatformTime(int week){
        int max = maxWeekTime(week);    //todo: see what to scale against - thresholds and
        int weekTime;
        UserForGraphGeneration current;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            weekTime = current.getPlatformTime(week);
            scaledWeeklyTimes.put(entry.getKey(), (int) Math.round(weekTime*10.0/max));
        }
    }

    private static void scaleVideoTime(int week){
        int max = maxVideoWeekTime(week);
        int videoTime;
        UserForGraphGeneration current;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            videoTime = current.getVideoTime(week);
            scaledVideoTimes.put(entry.getKey(), (int) Math.round(videoTime*10.0/max));
        }
    }

    private static void scaleRatioTime(int week){
        double max = maxRatioVideoPlatform(week);
        double ratio;
        UserForGraphGeneration current;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            ratio = current.getRatioTime(week);
            scaledRatio.put(entry.getKey(), (int) Math.round(ratio*10.0/max));
        }
    }

    private static void scaleVideos(int week){
        int max = maxDistinctVideos(week);
        int videos;
        UserForGraphGeneration current;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            videos = current.getDistinctVideos(week);
            scaledVideos.put(entry.getKey(), (int) Math.round(videos*10.0/max));
        }
    }

    private static void scaleAssignments(int week){
        int max = maxAssignments(week);
        int assignments;
        UserForGraphGeneration current;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            assignments = current.getAssignments(week);
            scaledAssignments.put(entry.getKey(), (int) Math.round(assignments*10.0/max));
        }
    }

    private static void scaleUntilDeadline(int week){
        long max = maxUntilDeadline(week);
        long untilDeadline;
        UserForGraphGeneration current;

        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            current = entry.getValue();
            untilDeadline = current.getUntilDeadline(week);
            scaledDeadlines.put(entry.getKey(), (int) Math.round(untilDeadline*10.0/max));
        }
    }

    //************************
    //************ Utils

    private static void initialize() {
        users = new HashMap<>();
        thresholds = new double[6];

        scaledWeeklyTimes = new HashMap<>();
        scaledVideoTimes = new HashMap<>();
        scaledRatio = new HashMap<>();
        scaledVideos = new HashMap<>();
        scaledAssignments = new HashMap<>();
        scaledDeadlines = new HashMap<>();

    }

    private static String getValuesString(String user, int week){
        String values;

        values = "[" + scaledWeeklyTimes.get(user) + ","
                + scaledVideoTimes.get(user) + ","
                + scaledRatio.get(user) + ","
                + scaledVideos.get(user) + ","
                + scaledAssignments.get(user) + ","
                + scaledDeadlines.get(user) + "]";

        return values;
    }

    private static String getThresholdsString() {
        String threshold = "[";
        for (int i=0; i<5; i++)
            threshold += thresholds[i] + ",";
        threshold += thresholds[5] + "]";
        return threshold;
    }

    private static String getAreaChartOptionsAsString(String threshold, String values) {
        String options = "{\n" +
                "\n" +
                "        chart: {\n" +
                "            polar: true,\n" +
                "            style: {\n" +
                "               fontFamily: 'Open Sans, sans-serif'\n" +
                "            }\n" +
                "        },\n" +
                "\n" +
                "        title: {\n" +
                "            text: 'Learning tracker'\n" +
                "        },\n" +
                "\n" +
                "        pane: {\n" +
                "            startAngle: 0,\n" +
                "            endAngle: 360\n" +
                "        },\n" +
                "\n" +
                "        xAxis: {\n" +
                "            tickInterval: 60,\n" +
                "            min: 0,\n" +
                "            max: 360,\n" +
                "            labels: {\n" +
                "                formatter: function () {\n" +
                "\t\t\t\t\tvar metricNames = [\n" +
                "\t\t\t\t\t\t'Time on the platform', \n" +
                "\t\t\t\t\t\t'Time watching videos', \n" +
                "\t\t\t\t\t\t'Fraction of time spent watching videos while on the platform', \n" +
                "\t\t\t\t\t\t'Videos watched', \n" +
                "\t\t\t\t\t\t'Quiz answers submitted', \n" +
                "\t\t\t\t\t\t'Timeliness of quiz answer submission'\n" +
                "\t\t\t\t\t\t];\n" +
                "                    return metricNames[this.value/60];\n" +
                "                }\n" +
                "            },\n" +
                "\t\t\t\t\t\tgridLineWidth: 1\n" +
                "        },\n" +
                "\n" +
                "        yAxis: {\n" +
                "            min: 0,\n" +
                "            max: 10,\n" +
                "            gridLineWidth: 1,\n" +
                "            labels: {\n" +
                "            \tenabled: false\n" +
                "            }\n" +
                "        },\n" +
                "\n" +
                "        plotOptions: {\n" +
                "            series: {\n" +
                "                pointStart: 0,\n" +
                "                pointInterval: 60,\n" +
                "            },\n" +
                "            column: {\n" +
                "                pointPadding: 0,\n" +
                "                groupPadding: 0\n" +
                "            }\n" +
                "        },\n" +
                "\n" +
                "        series: [{type: 'area',\n" +
                "        \t\tcolor: 'rgba(255, 255, 102, 0.75)',\n" +
                "            name: 'Average graduate',\n" +
                "            data: " + threshold + ", \n" +
                "            borderWidth: 0.5,\n" +
                "            borderColor: '#000000'          \n" +
                "      \n" +
                "        },\n" +
                "        {\n" +
                "            type: 'area',\n" +
                "            name: 'You',\n" +
                "            color: 'rgba(144,202,249 ,0.75)',\n" +
                "            data: " + values + "\n" +
                "        }],\n" +
                "\t\t\n" +
                "\t\texporting: {\n" +
                "\t\t\tenabled: false\n" +
                "\t\t}\n" +
                "    }";

        return options;
    }

    private static int maxWeekTime(int week){
        return  users.values().stream()
                .mapToInt(e -> e.getPlatformTime(week))
                .max()
                .getAsInt();
    }

    private static int maxVideoWeekTime(int week) {
        return  users.values().stream()
                .mapToInt(e -> e.getVideoTime(week))
                .max()
                .getAsInt();
    }

    private static double maxRatioVideoPlatform(int week){
        return  users.values().stream()
                .mapToDouble(e -> e.getRatioTime(week))
                .max()
                .getAsDouble();
    }

    private static int maxDistinctVideos(int week){
        return  users.values().stream()
                .mapToInt(e -> e.getDistinctVideos(week))
                .max()
                .getAsInt();
    }

    private static int maxAssignments(int week){
        return  users.values().stream()
                .mapToInt(e -> e.getAssignments(week))
                .max()
                .getAsInt();
    }

    private static long maxUntilDeadline(int week){
        return  users.values().stream()
                .mapToLong(e -> e.getUntilDeadline(week))
                .max()
                .getAsLong();
    }
}
