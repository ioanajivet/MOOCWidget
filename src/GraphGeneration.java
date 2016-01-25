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

        public static void main(String[] args) throws IOException,ParseException
        {
            int week = Integer.parseInt(args[0]);
            String inputPath = args[1] + "scaled_metrics" + week + ".csv";
            String outputPath = args[2];
            //inputPath: "data\\2015\\user_metrics\\"
            //outputPath: "generate\\2015\\"

            generateJS(week, inputPath, outputPath);
            //generatePNG();

        }


    //************************
    //************ Loading data

    private static void generateJS(int week, String inputPath, String outputPath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(inputPath));
        Writer output;
        String [] nextLine;
        String thresholds, values, filename, toWrite;

        csvReader.readNext();

        //read thresholds
        nextLine = csvReader.readNext();
        thresholds = getThresholdsString(nextLine);

        while ((nextLine = csvReader.readNext()) != null) {
            filename = nextLine[0].substring(nextLine[0].indexOf("3T"));
            output = new FileWriter(outputPath + filename + ".js");

            values = getValuesString(nextLine);
            toWrite = getAreaChartOptionsAsString(thresholds, values);

            output.write(toWrite);
            output.close();

            generateChartPNG(filename);
        }

        System.out.println("Generated .js files.");
        csvReader.close();

    }

    //************************
    //************ Writing data

/*    private static void generatePNG() throws IOException {
        for (Map.Entry<String, UserForGraphGeneration> entry : users.entrySet()) {
            generateChartPNG(entry.getKey().substring(18));
        }
    }*/

    private static void generateChartPNG(String user) throws IOException{
        String phantom;

        phantom = "phantomjs highcharts-convert.js -infile 2015/js/" + user + ".js -outfile 2015/out/" + user + ".png -scale 2.5 -width 600";

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
    //************ Computations


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
