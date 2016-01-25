/**
 * Created by Ioana on 1/9/2016.
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

//TODO: check file paths for file read and generation so they don't overwrite stuff
//TODO: check consistency of week numbering: starting with 0 or with 1
public class DataProcessing {

    static HashMap<String, UserForDataProcessing> graduates;
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
            scaleMetrics();

            //read thresholds - per week and cutOffPercentage
            readThresholds(1);
            scaleThresholds();

         //   generateJS(1);
            //readUsers(0.6);
            //generatePNG();
           /* int i=1;
            for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
                if( i > 6)
                    break;
                generateChartPNG(entry.getKey().substring(18));
                i++;
            }*/

            //todo: write metrics for all weeks
            //write metrics only for week 1
            //for(int i = 1; i < 12; i++)
            //  writeMetrics("metrics"+ + i + ".csv", 1);

            //write .js files that is used to generate the charts - per week

            //-------------- Distinct Videos Watched ---------
            //-----Graduates only
            /*readUsers(0.6);
            readVideosPublished();
            computeDistinctVideos();
            writeDistinctVideos("weeklyDistinctVideos.csv");*/

            //-----All users
            /*readUsers(0);
            readVideosPublished();
            computeDistinctVideos();
            writeDistinctVideos("weeklyDistinctVideosAll.csv");*/

            //------------------- Times --------------------
            //Compute Times spent on the platform, watching video and their ratio
            //readUsers(0.6);
            //computeWeeklyVideoTimes();
            //computeWeeklyPlatformTimes();
            //writeVideoTimes();
            //writePlatformTime("weeklyPlatformTime.csv");
            //writePlatformTime("weeklyPlatformTimeAll.csv");
            //writeAllData("weeklyData.csv");

            //------------------- Assignments and proactivity vs. procrastination --------------------
            //Time between the problem was submitted and the deadline
            //-----Graduates only
            /*readProblems();
            readUsers(0.6);
            readSubmissions();
            writeAssignments("untilDeadline.csv");*/

            //-----All users
           /* readProblems();
            readUsers(0.0);
            readSubmissions();
            writeAssignments("untilDeadlineAll.csv");*/


            //------Plot only one problem-------
            /*problemXSubmissions = new ArrayList<>();
            Iterator<String> it = problemsWeek.keySet().iterator();
            String plottedProblem = it.next();
            System.out.println("Plotted problem: " + plottedProblem);
            plotOneProblem(plottedProblem);*/


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
        computeWeeklyVideoTimes();
        computeWeeklyPlatformTimes();

        readVideosPublished();
        computeDistinctVideos();

        readSubmissions();
    }

    private static void generateChartPNG(String user) throws IOException{
        String phantom;

        phantom = "phantomjs highcharts-convert.js -infile js\\" + user + ".js -outfile out\\" + user + ".png -scale 2.5 -width 600";

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

    private static void generatePNG() throws IOException {
        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            generateChartPNG(entry.getKey().substring(18));
        }
    }

    private static void writeMetrics(String filename, int week) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        UserForDataProcessing current;
        double ratio;

        toWrite = "User_id#Time on platform#Time on videos#Ratio video/platform#Distict videos#Assignments#Until deadline".split("#");

        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();
            toWrite[0] = entry.getKey();

            toWrite[1] = String.valueOf(current.getWeekTime(week));
            toWrite[2] = String.valueOf(current.getWeekVideoTime(week));
            toWrite[3] = computeRatio(current.getWeekVideoTime(week), current.getWeekTime(week));
            toWrite[4] = String.valueOf(current.getDistinctVideosPerWeek(week));
            toWrite[5] = String.valueOf(current.getAssignmentsSubmittedPerWeek(week));
            toWrite[6] = String.valueOf(current.getUntilDeadlinesPerWeekAverage(week));

            output.writeNext(toWrite);
        }
        output.close();
    }

    private static int minWeekTime(int week){
        return graduates.values().stream()
                .mapToInt(e -> e.getWeekTime(week))
                .min()
                .getAsInt();
    }

    private static int maxWeekTime(int week){
        return  graduates.values().stream()
                .mapToInt(e -> e.getWeekTime(week))
                .max()
                .getAsInt();
    }

    private static int maxVideoWeekTime(int week) {
        return  graduates.values().stream()
                .mapToInt(e -> e.getWeekVideoTime(week))
                .max()
                .getAsInt();
    }

    private static double maxRatioVideoPlatform(int week){
        return  graduates.values().stream()
                .mapToDouble(e -> Double.parseDouble(computeRatio(e.getWeekVideoTime(week),e.getWeekTime(week))))
                .max()
                .getAsDouble();
    }

    private static int maxDistinctVideos(int week){
        return  graduates.values().stream()
                .mapToInt(e -> e.getDistinctVideosPerWeek(week))
                .max()
                .getAsInt();
    }

    private static int maxAssignments(int week){
        return  graduates.values().stream()
                .mapToInt(e -> e.getAssignmentsSubmittedPerWeek(week))
                .max()
                .getAsInt();
    }

    private static long maxUntilDeadline(int week){
        return  graduates.values().stream()
                .mapToLong(e -> e.getUntilDeadlinesPerWeekAverage(week))
                .max()
                .getAsLong();
    }

    private static void scaleMetrics(){
        scaleWeekTime(1);
        scaleVideoTime(1);
        scaleRatioTime(1);
        scaleVideos(1);
        scaleAssignments(1);
        scaleUntilDeadline(1);
    }

    private static void scaleWeekTime(int week){
        int max = maxWeekTime(week);
        int weekTime;
        UserForDataProcessing current;

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();
            weekTime = current.getWeekTime(week);
            scaledWeeklyTimes.put(entry.getKey(), (int) Math.ceil(weekTime*10.0/max));
        }
    }

    private static void scaleVideoTime(int week){
        int max = maxVideoWeekTime(week);
        int videoTime;
        UserForDataProcessing current;

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();
            videoTime = current.getWeekVideoTime(week);
            scaledVideoTimes.put(entry.getKey(), (int) Math.ceil(videoTime*10.0/max));
        }
    }

    private static void scaleRatioTime(int week){
        double max = maxRatioVideoPlatform(week);
        double ratio;
        UserForDataProcessing current;

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();
            ratio = Double.parseDouble(computeRatio(current.getWeekVideoTime(week),current.getWeekTime(week)));
            scaledRatio.put(entry.getKey(), (int) Math.ceil(ratio*10.0/max));
        }
    }

    private static void scaleVideos(int week){
        int max = maxDistinctVideos(week);
        int videos;
        UserForDataProcessing current;

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();
            videos = current.getDistinctVideosPerWeek(week);
            scaledVideos.put(entry.getKey(), (int) Math.ceil(videos*10.0/max));
        }
    }

    private static void scaleAssignments(int week){
        int max = maxAssignments(week);
        int assignments;
        UserForDataProcessing current;

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();
            assignments = current.getAssignmentsSubmittedPerWeek(week);
            scaledAssignments.put(entry.getKey(), (int) Math.ceil(assignments*10.0/max));
        }
    }

    private static void scaleUntilDeadline(int week){
        long max = maxUntilDeadline(week);
        long untilDeadline;
        UserForDataProcessing current;

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();
            untilDeadline = current.getUntilDeadlinesPerWeekAverage(week);
            scaledDeadlines.put(entry.getKey(), (int) Math.ceil(untilDeadline*10.0/max));
        }
    }

    private static void generateJS(int week) throws IOException {
        Writer output;
        String toWrite, threshold, passing, values;
        UserForDataProcessing current;
        String filename;

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();
            filename = entry.getKey().substring(18);

            output = new FileWriter("generate\\js\\" + filename + ".js");

            threshold = getThresholdsString();

            passing = getPassingString();
            values = getValuesString(current.getId(), week);
           // toWrite = getColumnAndLineChartOptionsAsString(current.getId(), threshold, passing, values);
            toWrite = getAreaChartOptionsAsString(current.getId(), threshold, values);

            output.write(toWrite);
            output.close();
        }
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

    private static String getPassingString() {
        String passing = "[";

        for (int i=0; i<5; i++)
            passing += (10-thresholds[i]) + ",";
        passing += (10-thresholds[5]) + "]";

        return passing;
    }

    private static String getColumnAndLineChartOptionsAsString(String user, String threshold, String passing, String values) {
        return "{\n" +
                "        chart: {\n" +
                "            polar: true,\n" +
                "\t\t\tanimation: false\n" +
                "        },\n" +
                "        title: {\n" +
                "            text: '" + user + " - Your learning style'\n" +
                "        },\n" +
                "        pane: {\n" +
                "            startAngle: 0,\n" +
                "            endAngle: 360\n" +
                "        },\n" +
                "        xAxis: {\n" +
                "            tickInterval: 60,\n" +
                "            min: 0,\n" +
                "            max: 360,\n" +
                "            labels: {\n" +
                "                formatter: function () {\n" +
                "\t\t\t\t\tvar metricNames = [\n" +
                "\t\t\t\t\t   'Time on platform', \n" +
                "\t\t\t\t\t   'Time watching videos', \n" +
                "\t\t\t\t\t   'Ratio videos/platform', \n" +
                "\t\t\t\t\t   'Distinct videos', \n" +
                "\t\t\t\t\t   'Assignments', \n" +
                "\t\t\t\t\t   'Until deadline'\n" +
                "\t\t\t\t\t];\n" +
                "\t\t\t\t\t\n" +
                "                    return metricNames[this.value/60];\n" +
                "                }\n" +
                "            },\n" +
                "\t\t\tgridLineWidth: 0\n" +
                "        },\n" +
                "\n" +
                "        yAxis: {\n" +
                "            min: 0,\n" +
                "            max: 10,\n" +
                "            gridLineWidth: 1,\n" +
                "            minorTickInterval: 1,\n" +
                "            minorGridLineWidth: 1\n" +
                "        },\n" +
                "\n" +
                "        plotOptions: {\n" +
                "            series: {\n" +
                "                pointStart: 0,\n" +
                "                pointInterval: 60,\n" +
                "                stacking: 'normal'\n" +
                "            },\n" +
                "            column: {\n" +
                "                pointPadding: 0,\n" +
                "                groupPadding: 0\n" +
                "            }\n" +
                "        },\n" +
                "        \n" +
                "        series: [{\n" +
                "            type: 'column',\n" +
                "            color: 'rgba(172, 209, 233, 0.5)',\n" +
                "            name: 'Passing',\n" +
                "            fillOpacity: 0.75,\n" +
                "            threshold: 4,\n" +
                "            data: " + passing + ",\n" +
                "           \tborderWidth: 0.5,\n" +
                "            borderColor: '#000000'\n" +
                "            \n" +
                "        }, {type: 'column',\n" +
                "        \t\tcolor: 'rgba(255, 255, 102, 0.5)',\n" +
                "            name: 'Threshold',\n" +
                "            data: " + threshold + ", \n" +
                "            borderWidth: 0.5,\n" +
                "            borderColor: '#000000'          \n" +
                "      \n" +
                "        },\n" +
                "        {\n" +
                "            type: 'area',\n" +
                "            name: 'You',\n" +
                "            color: 'rgba(124,179,66 ,0.9)',\n" +
                "            data: " + values + "\n" +
                "        }]\n" +
                "    };";
    }

    private static String getAreaChartOptionsAsString(String user, String threshold, String values) {
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

    private static void readThresholds(int week) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("thresholds10.csv"));
        String [] nextLine;
        int i = 0;
        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            thresholds[i++] = Double.parseDouble(nextLine[week]);
        }

        csvReader.close();
    }

    private static void scaleThresholds(){
        int maxWeekTime = maxWeekTime(1);
        int maxVideoTime = maxVideoWeekTime(1);
        double maxRatio = maxRatioVideoPlatform(1);
        int maxVideos = maxDistinctVideos(1);
        int maxAssignment = maxAssignments(1);
        long maxUntilDeadline = maxUntilDeadline(1);

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


    //************************
    //************ Loading data
    private static void readUsers(double threshold) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2015\\course_user.csv"));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(Double.parseDouble(nextLine[1]) >= threshold)
                graduates.put(nextLine[0], new UserForDataProcessing(nextLine[0],nextLine[1]));
        }

        csvReader.close();

      //  readAnonymizedIds();

    }

    private static void readAnonymizedIds() throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader("data\\2015\\anon-ids.csv"));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(graduates.containsKey(nextLine[0]))
                graduates.get(nextLine[0]).setAnonymousId(nextLine[1]);
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
        UserForDataProcessing user;
        int week;
        long hours;
        String submissionTime;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            user = graduates.get(nextLine[1]);

            if (user == null)    //user does not have a grade above the required threshold -> ignore submission
                continue;

            if(!problemsWeek.containsKey(nextLine[2]))   //ignore problems that are not in "problems.csv"
                continue;

            week = problemsWeek.get(nextLine[2]);
            submissionTime = nextLine[3].substring(0, 22);
            hours = differenceBetweenDatesInHours(getProblemDeadlineForWeek(week), getDateFromString(submissionTime));

            user.addSubmission(week, nextLine[2], hours, getWeek(submissionTime));
        }

        System.out.println("Finished reading submissions.");
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

    //-------**********-------------

    //************************
    //************ Writing data
    private static void writeAssignments(String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String header = "user_id#grade";
        String[] toWrite;
        UserForDataProcessing current;
        int toBeSubmitted;
        int submitted;

        for(int i=1;i<12;i++) {
            toBeSubmitted = getNumberOfProblemsPublishedOnWeek(i);
            header += "#untilDeadline" + String.valueOf(i)
                    + "#assignmentsSubmitted (" + toBeSubmitted +")"
                    + "#ratio";
        }
        toWrite = header.split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();

            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(current.getGrade());
            for(int i=1; i<12; i++) {
                toBeSubmitted = getNumberOfProblemsPublishedOnWeek(i);
                submitted = current.getAssignmentsSubmittedPerWeek(i);
                toWrite[3*i-1] = String.valueOf(current.getUntilDeadlinesPerWeekAverage(i));
                toWrite[3*i] = String.valueOf(submitted);
                toWrite[3*i+1] = computeRatio(submitted, toBeSubmitted) ;
            }

            output.writeNext(toWrite);
        }

        output.close();
    }

    //-------**********-------------
    //todo: add cummulative values for each week for time spent on videos - people watch videos even in the last weeks
    private static void writeDistinctVideos(String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String header = "user_id#grade";
        String[] toWrite;
        UserForDataProcessing current;
        int publishedUntilNow = 0;
        int publishedThisWeek;
        int watchedUntilNow;
        int watchedThisWeek;
        double ratio;

        for(int i=1;i<13;i++) {
            //todo: make an array publishedPerWeek and cummulative publishes so it access it easier and does not compute every time
            publishedThisWeek = getNumberOfVideosPublishedOnWeek(i);
            publishedUntilNow += publishedThisWeek;

            header += "#week" + String.valueOf(i) + " (" + publishedThisWeek + ")" + "#ratio"
                    + "#cummulative" + String.valueOf(i) + " (" + publishedUntilNow + ")" + "#cummulativeRatio";
        }
        toWrite = (header).split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();
            watchedUntilNow = 0;
            publishedUntilNow = 0;

            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(current.getGrade());
            for(int i=1; i<13; i++) {
                publishedThisWeek = getNumberOfVideosPublishedOnWeek(i);
                publishedUntilNow += publishedThisWeek;

                watchedThisWeek = current.getDistinctVideosPerWeek(i);
                watchedUntilNow += watchedThisWeek;

                toWrite[4*i - 2] = String.valueOf(watchedThisWeek);
                toWrite[4*i - 1] = computeRatio(watchedThisWeek, publishedThisWeek) ;

                toWrite[4*i] = String.valueOf(watchedUntilNow);
                toWrite[4*i + 1] = computeRatio(watchedUntilNow, publishedUntilNow);
            }
            output.writeNext(toWrite);
        }
        output.close();
    }

    private static void writeVideoTimes() throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter("weeklyVideoTimes.csv"), ',');
        String header = "user_id#grade";
        String[] toWrite;
        UserForDataProcessing current;

        for(int i=1;i<12;i++)
            header += "#week" + String.valueOf(i);
        toWrite = (header + "#total").split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();

            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(current.getGrade());
            for(int i=2; i<13; i++)
                toWrite[i] = String.valueOf(current.getWeekVideoTime(i-1));
            toWrite[13] = String.valueOf(current.getVideoTime());
            output.writeNext(toWrite);
        }
        output.close();
    }

    private static void writePlatformTime(String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String header = "user_id#grade";
        String[] toWrite;
        UserForDataProcessing current;
        double ratio;

        for(int i=1;i<12;i++)
            header += "#week" + String.valueOf(i) + "#ratio" + String.valueOf(i);
        toWrite = (header + "#total").split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();

            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(current.getGrade());
            for(int i=1; i<12; i++) {
                toWrite[2*i] = String.valueOf(current.getWeekTime(i));
                if(current.getWeekTime(i) == 0)
                    toWrite[2*i + 1] = "0";
                else {
                    ratio = 100.0 * current.getWeekVideoTime(i) / current.getWeekTime(i);
                    toWrite[2 * i + 1] = String.format("%.2f", ratio);
                }

            }

            toWrite[24] = String.valueOf(current.getTotalTime());
            output.writeNext(toWrite);
        }
        output.close();
    }

    private static void writeAllData(String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String header = "user_id#grade";
        String[] toWrite;
        UserForDataProcessing current;
        double ratio;

        for(int i=1;i<12;i++)
            header += "#platformTime" + String.valueOf(i) + "#videoTime" + String.valueOf(i) + "#ratioTime" + String.valueOf(i);
        toWrite = (header + "#totalPlatform").split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, UserForDataProcessing> entry : graduates.entrySet()) {
            current = entry.getValue();

            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(current.getGrade());
            for(int i=1; i<12; i++) {
                toWrite[3*i-1] = String.valueOf(current.getWeekTime(i));
                toWrite[3*i] = String.valueOf(current.getWeekVideoTime(i));
                if(current.getWeekTime(i) == 0)
                    toWrite[3*i + 1] = "0";
                else {
                    ratio = 100.0 * current.getWeekVideoTime(i) / current.getWeekTime(i);
                    toWrite[3*i + 1] = String.format("%.2f", ratio);
                }

            }

            toWrite[35] = String.valueOf(current.getTotalTime());
            output.writeNext(toWrite);
        }
        output.close();
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

    //get all submissions for problem X and plot it to see if there really is a deadline!!! - there is ;)
    private static void plotOneProblem(String problem_id) throws IOException, ParseException{
        CSVReader csvReader = new CSVReader(new FileReader("data\\2015\\submissions.csv"));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null)
            if(problem_id.compareTo(nextLine[2]) == 0) {
                System.out.println(getDateFromString(nextLine[3].substring(0,22)));
                problemXSubmissions.add(getDateFromString(nextLine[3].substring(0,22)));
            }

        csvReader.close();

        //write
        CSVWriter output = new CSVWriter(new FileWriter("oneProblemPlotted.csv"), ',');
        String[] toWrite = "problem_id#submissions".split("#");

        output.writeNext(toWrite);

        for (Date entry : problemXSubmissions) {

            toWrite[0] = problem_id;
            toWrite[1] = entry.toString();
            output.writeNext(toWrite);
        }
        output.close();
    }

    //************************
    //************ Utils
    private static String computeRatio(int part, int total) {
        double ratio;

        if(part == 0)
            return "0";

        ratio = 100.0 * part / total;

        return String.format("%.2f", ratio);
    }

    private static int getWeekForVideo(String videoID) {
        //returns the week in which a video was published
        for (Map.Entry<Integer,ArrayList<String>> entry : videosPerWeek.entrySet()) {
            if(entry.getValue().contains(videoID))
                return entry.getKey();
        }
        return -1;
    }

    private static int getNumberOfVideosPublishedOnWeek(int week){
        if(!videosPerWeek.containsKey(week))
            return 0;
        return videosPerWeek.get(week).size();
    }

    //todo: fix ugly cast
    private static int getNumberOfProblemsPublishedOnWeek(int week) {
        return ((int) problemsWeek.values().stream().filter(e -> e == week).count());
        /*if(!problemsPerWeek.containsKey(week))
            return 0;
        return problemsPerWeek.get(week).size();*/
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
