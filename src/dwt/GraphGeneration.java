package dwt; /**
 * Created by Ioana on 1/9/2016.
 *
 * Command line arguments:
 * args[0] = input file that contains the scaled metrics that will be used for drawing the charts
 * args[1] = jsPath for where the .js files with options will be saved
 * args[2] = chartPath for where the .png files will be saved
 *
 * Examples:
 * -- inputPath: "data/2016/user_metrics/scaled_metrics2.csv"
 * -- jsPath: "generate/2016/week2/js/"
 * -- chartPath: "generate/2016/week2/out/"
 */

import com.opencsv.CSVReader;

import java.io.*;
import java.text.ParseException;

public class GraphGeneration {

        public static void main(String[] args) throws IOException,ParseException
        {
            String inputPath = args[0];
            String jsPath = args[1];
            String chartPath = args[2];

            generateCharts(inputPath, jsPath, chartPath);

        }

    //************************
    //************ Loading data

    private static void generateCharts(String inputPath, String jsPath, String chartPath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(inputPath));
        Writer output;
        String [] nextLine;
        String thresholds, values, filename, toWrite;

        csvReader.readNext();

        //read thresholds
        nextLine = csvReader.readNext();
        thresholds = getThresholdsString(nextLine);

        while ((nextLine = csvReader.readNext()) != null) {
            filename = nextLine[0];
            output = new FileWriter(jsPath + filename + ".js");

            values = getValuesString(nextLine);
            toWrite = getAreaChartOptionsAsString(thresholds, values);

            output.write(toWrite);
            output.close();

            generateChartPNG(filename, jsPath, chartPath);
        }

        System.out.println("Finished generating charts.");
        csvReader.close();

    }

    //************************
    //************ Writing data

    private static void generateChartPNG(String user, String jsPath, String chartPath) throws IOException{
        String phantom;

        phantom = "phantomjs highcharts-convert.js -infile " + jsPath + user + ".js -outfile " + chartPath + user + ".png -scale 2.5 -width 600";

        //for Windows
        //ProcessBuilder builder = new ProcessBuilder(
          //      "cmd.exe", "/c", "cd \"C:\\Users\\Ioana\\Desktop\\Thesis\\Widget\\generate\" && " + phantom);

        String[] args = new String[] {"/bin/bash", "-c", phantom};
        ProcessBuilder builder = new ProcessBuilder(args);

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
    //************ Utils

    private static String getValuesString(String []line){
        String values = "[";

        for (int i = 1; i < 6; i++)
            values += line[i] + ",";
        values += line[6] + "]";

        return values;

    }

    private static String getThresholdsString(String []line) {
        String threshold = "[";

        for (int i=1; i<6; i++)
            threshold += line[i] + ",";
        threshold += line[5] + "]";

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

}
