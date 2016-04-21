package st; /**
 * Created by Ioana on 4/20/2016.
 */

import com.opencsv.CSVReader;

import java.io.*;
import java.text.ParseException;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Map;


public class ScriptGeneration {

    private static HashMap<String, UserS> users = new HashMap<>();

    private static String thresholds, nextWeek, scaledThresholds, scaledNextWeek;

    public static void generateScripts(int week) throws IOException {
        readMetrics("data\\st\\2016\\week" + week + "\\metrics\\ST2015_metrics.csv");
        readScaledMetrics("data\\st\\2016\\week" + week + "\\metrics\\scaled_ST2015_metrics.csv");

        readThresholds(week);

        writeScripts("data\\st\\2016\\week" + week + "\\scripts\\");
    }

    public static void readMetrics(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine;
        String id;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            id = nextLine[0];

            UserS current = new UserS(id);

            current.setValues(nextLine);

            users.put(id, current);
        }

        csvReader.close();

    }

    public static void readScaledMetrics(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine;
        String id;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            id = nextLine[0];

            UserS current = users.get(id);

            current.setScaledValues(nextLine);
        }

        csvReader.close();

    }

    public static void readThresholds(int week) throws IOException {
        readThreshold(week, "data\\st\\thresholds\\thresholds5.csv");
        readScaledThreshold(week, "data\\st\\thresholds\\scaled_thresholds5.csv");
    }

    public static void readThreshold(int week, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine;
        int line = 1;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null && line < week);

        //todo: check if it is last week; if yes, then there is no next week;

        thresholds = getString(nextLine);

        if (week != 10) {
            nextLine = csvReader.readNext();
            nextWeek = getString(nextLine);
        }

        /*if(week != 10) {
            nextLine = csvReader.readNext();
            for(int i = 0; i < 6; i++)
                nextWeek[i] = Integer.parseInt(nextLine[i+1]);
        }*/

        csvReader.close();

    }

    public static void readScaledThreshold(int week, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine;
        int line = 1;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null && line < week);

        scaledThresholds = getString(nextLine);

        if(week != 10) {
            nextLine = csvReader.readNext();
            scaledNextWeek = getString(nextLine);
        }
        /*//todo: check if it is last week; if yes, then there is no next week;
        for(int i = 0; i < 6; i++)
            scaledThresholds[i] = Double.parseDouble(nextLine[i+1]);

        if(week != 10) {
            nextLine = csvReader.readNext();
            for(int i = 0; i < 6; i++)
                scaledNextWeek[i] = Double.parseDouble(nextLine[i+1]);
        }*/

        csvReader.close();

    }

    public static void writeScripts(String destinationFolder) throws IOException {

        UserS me = users.get("5524478");
        writeUserScript(destinationFolder, me.id, getChartOptions(me.values, me.scaledValues));

        /*for (Map.Entry<String, UserS> entry : users.entrySet()) {
            writeUserScript(destinationFolder, entry.getKey(), getChartOptions(entry.getValue().values, entry.getValue().scaledValues));
            System.out.println(entry.getKey() + ": " + entry.getValue().values + " == " + entry.getValue().scaledValues);
        }*/

    }

    private static void writeUserScript(String destination, String id, String content) throws IOException {
        Writer output;
        output = new FileWriter(destination + id + ".js");
        output.write(content);
        output.close();
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

    private static String getChartOptions(String values, String scaledValues) {
        String options = "function loadScript(url, callback)\n" +
                "{\n" +
                "    // Adding the script tag to the head as suggested before\n" +
                "    var head = document.getElementsByTagName('head')[0];\n" +
                "    var script = document.createElement('script');\n" +
                "    script.type = 'text/javascript';\n" +
                "    script.src = url;\n" +
                "\n" +
                "    // Then bind the event to the callback function.\n" +
                "    // There are several events for cross browser compatibility.\n" +
                "    script.onreadystatechange = callback;\n" +
                "    script.onload = callback;\n" +
                "\n" +
                "    // Fire the loading\n" +
                "    head.appendChild(script);\n" +
                "}\n" +
                "\n" +
                "  var loadWidget = function() {\n" +
                "\t\n" +
                "\tvar metricNames = [\n" +
                "       'Sessions per week', \n" +
                "       'Average length of a session', \n" +
                "       'Average time between sessions', \n" +
                "       'Forum sessions', \n" +
                "       'Weekly assessment answers submitted', \n" +
                "       'Timeliness of weekly assessment submission'\n" +
                "    ];\n" +
                "\tvar metricUnits = ['', 'min', 'h', '', '', 'h'];\n" +
                "\t\n" +
                "\tvar values = " + values + ";\n" +
                "\tvar thisWeek = " + thresholds + ";\n" +
                "\tvar nextWeek = " + nextWeek + ";\n" +
                "\n" +
                "    $('#container').highcharts({\n" +
                "        chart: {\n" +
                "            polar: true,\n" +
                "\t\t\tstyle: {\n" +
                "\t\t\t\tfontFamily: 'Open Sans, sans-serif'\n" +
                "\t\t\t},\n" +
                "\t\t\ttype: 'area'\n" +
                "                 \n" +
                "        },\n" +
                "\n" +
                "        title: {\n" +
                "            text: 'Learning tracker',\n" +
                "\t\t\tstyle: {\n" +
                "\t\t\t\talign: 'left'\n" +
                "\t\t\t}\n" +
                "        },\n" +
                "\t\t\n" +
                "\t\tcredits: {\n" +
                "            enabled: false\n" +
                "        },\n" +
                "\t\t\n" +
                "\t\tlegend: {\n" +
                "            reversed: true\n" +
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
                "                allowPointSelect: true,\n" +
                "\t\t\t\tpointStart: 0,\n" +
                "                pointInterval: 60,\n" +
                "\t\t\t\tcursor: 'pointer',\n" +
                "\t\t\t\tmarker: {\n" +
                "\t\t\t\t\tsymbol: 'diamond',\n" +
                "\t\t\t\t\tradius: 3\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\tpoint: {\n" +
                "\t\t\t\t\tevents: {\n" +
                "\t\t\t\t\t\tmouseOver: function () {\n" +
                "                            var chart = this.series.chart;\n" +
                "                            \n" +
                "                            chart.lbl\n" +
                "                                .show()\n" +
                "                                .attr({\n" +
                "                                    text: 'x: ' + this.x + ', y: ' + this.y\n" +
                "                                });\n" +
                "                        }\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "            },\n" +
                "            column: {\n" +
                "                pointPadding: 0,\n" +
                "                groupPadding: 0\n" +
                "            }\n" +
                "        },\n" +
                "\t\ttooltip: {\n" +
                "\t\t\tshared: true,\n" +
                "\t\t\tformatter: function () {\n" +
                "                var s = '<b>' + metricNames[this.x/60] + '</b>';\n" +
                "\t\t\t\tvar unit = metricUnits[this.x/60];\n" +
                "\n" +
                "\t\t\t\tif(this.points.length == 2) {\t\t\t\t\n" +
                "\t\t\t\t\ts += '<br/>' + this.points[1].series.name + ': <b>' + values[this.x/60] + ' ' + unit + '</b>';\n" +
                "\t\t\t\t\ts += '<br/>' + this.points[0].series.name + ': <b>' + thisWeek[this.x/60] + ' ' + unit + '</b>';\n" +
                "\t\t\t\t}\n" +
                "\t\t\t\telse {\n" +
                "\t\t\t\t\ts += '<br/>' + this.points[2].series.name + ': <b>' + values[this.x/60] + ' ' + unit + '</b>';\n" +
                "\t\t\t\t\ts += '<br/>' + this.points[1].series.name + ': <b>' + thisWeek[this.x/60] + ' ' + unit + '</b>';\n" +
                "\t\t\t\t\ts += '<br/>' + this.points[0].series.name + ': <b>' + nextWeek[this.x/60] + ' ' + unit + '</b>';\n" +
                "\t\t\t\t}\n" +
                "\t\t\t\t\n" +
                "                return s;\n" +
                "            },\n" +
                "\t\t},\n" +
                "\t\t\n" +
                "        series: [\t\t\n" +
                "\t\t{\n" +
                "            type: 'line',\n" +
                "            name: 'Average graduate this week',\n" +
                "            color: 'rgba(188, 64, 119, 0.5)',\n" +
                "            data: " + scaledNextWeek + ",\n" +
                "\t\t\tvisible: false\n" +
                "        },\n" +
                "\t\t\n" +
                "\t\t{\n" +
                "        \tcolor: 'rgba(255, 255, 102, 0.5)',\n" +
                "            name: 'Average graduate last week',\n" +
                "            data: " + scaledThresholds + ", \n" +
                "            borderWidth: 0.5,\n" +
                "            borderColor: '#000000'\t\t\t\t\n" +
                "      \n" +
                "        },\n" +
                "\t\t\n" +
                "\t\t{\n" +
                "            name: 'You',\n" +
                "            color: 'rgba(144, 202, 249, 0.5)',\n" +
                "            data: " + scaledValues + "\n" +
                "        }\n" +
                "\t\t]\n" +
                "    });\n" +
                "\t\n" +
                "    \n" +
                "};\n" +
                "\t\t\n" +
                "loadScript(\"https://code.highcharts.com/highcharts-more.js\", loadWidget);";

        return options;
    }


    private static String getString(String []line){
        String values = "[";

        for (int i = 1; i < 6; i++)
            values += line[i] + ",";
        values += line[6] + "]";

        return values;

    }
}

class UserS {
    public String id;

    public String values;
    public String scaledValues;

    public UserS (String id) {
        this.id = id;
    }

    public void setValues(String[] val) {
        values = "[";

        for (int i = 1; i < 6; i++)
            values += val[i] + ",";
        values += val[6] + "]";

    }

    public void setScaledValues(String[] val) {
        scaledValues = "[";

        for (int i = 1; i < 6; i++) {
            System.out.println(val[i]);
            if(val[i].compareTo("NaN") == 0)
                scaledValues += "0,";
            else
                scaledValues += val[i] + ",";
        }

        if(val[6].compareTo("NaN") == 0)
            scaledValues += "0]";
        else
            scaledValues += val[6] + "]";

    }
}