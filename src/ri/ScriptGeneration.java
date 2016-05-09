package ri; /**
 * Created by Ioana on 4/20/2016.
 */

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


public class ScriptGeneration {

    private static HashMap<String, UserS> users = new HashMap<>();

    private static String[] thresholds = new String[6],
            nextWeek = new String[6],
            scaledThresholds = new String[6],
            scaledNextWeek = new String[6];

    public static void generateScripts(int week) throws IOException {
        readMetrics("data\\ri\\2016\\week" + week + "\\metrics\\RI2016_metrics.csv");
        readScaledMetrics("data\\ri\\2016\\week" + week + "\\metrics\\scaled_RI2016_metrics.csv");
        readAnonIds("data\\ri\\2016\\RI2016_anon.csv");

        readThresholds(week);

        //writeScripts("data\\ri\\2016\\week" + week + "\\scripts\\", week);
        writeSimpleScripts("data\\ri\\2016\\week" + week + "\\scripts\\", week);
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

        System.out.println("Users: " + users.size());
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

    public static void readAnonIds(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine;
        String id;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            id = nextLine[0];

            UserS current = users.get(id);

            if(current == null)
                continue;

            current.setAnonId(nextLine[1]);
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

        while ((nextLine = csvReader.readNext()) != null && line < week)
            line++;

        //todo: check if it is last week; if yes, then there is no next week;

        for(int i = 0; i < 6; i++)
            thresholds[i] = nextLine[i+1];

        if (week != 10) {
            nextLine = csvReader.readNext();
            for(int i = 0; i < 6; i++)
                nextWeek[i] = nextLine[i+1];
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

        while ((nextLine = csvReader.readNext()) != null && line < week)
            line++;

        for(int i = 0; i < 6; i++)
            scaledThresholds[i] = nextLine[i+1];

        if(week != 10) {
            nextLine = csvReader.readNext();
            for(int i = 0; i < 6; i++)
                scaledNextWeek[i] = nextLine[i+1];
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

    public static void writeScripts(String destinationFolder, int week) throws IOException {

        //UserS me = users.get("8206618");

        //writeUserScript(destinationFolder, me.id, getChartOptions(me.values, me.scaledValues, me.id, week));

        for (Map.Entry<String, UserS> entry : users.entrySet()) {
            UserS current = entry.getValue();
            writeUserScript(destinationFolder, current.anonId, getChartOptions(current.values, current.scaledValues, current.id, week));
        }

    }

    public static void writeSimpleScripts(String destinationFolder, int week) throws IOException {

        //UserS me = users.get("8206618");

        //writeUserScript(destinationFolder, me.id, getChartOptions(me.values, me.scaledValues, me.id, week));

        for (Map.Entry<String, UserS> entry : users.entrySet()) {
            UserS current = entry.getValue();
            writeUserScript(destinationFolder, current.anonId, getSimpleChartOptions(current.values, current.scaledValues, current.id, week));
        }

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

    private static String getChartOptions(String[] values, String[] scaledValues, String userId, int week) {
        String[] metricNames = ("Sessions per week#Average length of a session#Average time between sessions#" +
                "Time on task#Weekly assessment answers submitted#Timeliness of weekly assessment submission").split("#");
        String[] metricUnits = "#min#h#%##h".split("#");
        String[] thresh, scaledThresh, nextW, scaledNextW;


        metricNames = randomizeOrder(metricNames, Integer.parseInt(userId));
        metricUnits = randomizeOrder(metricUnits, Integer.parseInt(userId));

        values = randomizeOrder(values, Integer.parseInt(userId));
        scaledValues = randomizeOrder(scaledValues, Integer.parseInt(userId));

        thresh = randomizeOrder(thresholds, Integer.parseInt(userId));
        scaledThresh = randomizeOrder(scaledThresholds, Integer.parseInt(userId));

        nextW = randomizeOrder(nextWeek, Integer.parseInt(userId));
        scaledNextW = randomizeOrder(scaledNextWeek, Integer.parseInt(userId));

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
                "function timeStamp() {\n" +
                "  var now = new Date();\n" +
                "  var date = [ now.getFullYear(), now.getMonth() + 1, now.getDate() ];\n" +
                "  var time = [ now.getHours(), now.getMinutes(), now.getSeconds() ];\n" +
                "\n" +
                "  for ( var i = 1; i < 3; i++ ) {\n" +
                "    if ( time[i] < 10 ) {\n" +
                "      time[i] = \"0\" + time[i];\n" +
                "    }\n" +
                "  }\n" +
                "\n" +
                "  if( date[1] < 10 ) {\n" +
                "\tdate[1] = \"0\" + date[1];\n" +
                "  }\n" +
                "  \n" +
                "  return date.join(\"-\") + \"Z\" + time.join(\":\");\n" +
                "}\n" +
                "\n" +
                "  var loadWidget = function() {\n" +
                "\t\n" +
                "\tvar metricNames = " + getStringOfString(metricNames) + ";\n" +
                "\tvar metricUnits = " + getStringOfString(metricUnits) + ";\n" +
                "\t\n" +
                "\tvar values = " + getString(values) + ";\n" +
                "\tvar thisWeek = " + getString(thresh) + ";\n" +
                "\tvar nextWeek = " + getString(nextW) + ";\n" +
                "\n" +
                "\tvar user_id = analytics.user().id();\n" +
                "\t\n" +
                "\t$('#container').highcharts({\n" +
                "        chart: {\n" +
                "            polar: true,\n" +
                "\t\t\tstyle: {\n" +
                "\t\t\t\tfontFamily: 'Open Sans, sans-serif'\n" +
                "\t\t\t},\n" +
                "\t\t\ttype: 'area',\n" +
                "\t\t\tevents: {\n" +
                "\t\t\t\tload: function () {\n" +
                "\t\t\t\t\tvar category = user_id + '_week" + week + "';\n" +
                "\t\t\t\t\tga('send', 'event', category, 'load_' + timeStamp());\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
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
                "            data: " + getString(scaledNextW) + ",\n" +
                "\t\t\tvisible: false,\n" +
                "\t\t\tevents: {\n" +
                "                    show: function () {\n" +
                "\t\t\t\t\t\tga('send', 'event', user_id + '_week" + week + "', 'show-this-week_' + timeStamp());\n" +
                "                    },\n" +
                "\t\t\t\t\thide: function () {\n" +
                "\t\t\t\t\t\tga('send', 'event', user_id + '_week" + week + "', 'hide-this-week_' + timeStamp());\n" +
                "                    }\n" +
                "                }\n" +
                "        },\n" +
                "\t\t\n" +
                "\t\t{\n" +
                "        \tcolor: 'rgba(255, 255, 102, 0.5)',\n" +
                "            name: 'Average graduate last week',\n" +
                "            data: " + getString(scaledThresh) + ", \n" +
                "            borderWidth: 0.5,\n" +
                "            borderColor: '#000000',\n" +
                "\t\t\tevents: {\n" +
                "                    show: function () {\n" +
                "\t\t\t\t\t\tga('send', 'event', user_id + '_week" + week + "', 'show-last-week_' + timeStamp());\n" +
                "                    },\n" +
                "\t\t\t\t\thide: function () {\n" +
                "\t\t\t\t\t\tga('send', 'event', user_id + '_week" + week + "', 'hide-last-week_' + timeStamp());\n" +
                "                    }\n" +
                "                }\t\t\t\t\n" +
                "      \n" +
                "        },\n" +
                "\t\t\n" +
                "\t\t{\n" +
                "            name: 'You',\n" +
                "            color: 'rgba(144, 202, 249, 0.5)',\n" +
                "            data: " + getString(scaledValues) + ",\n" +
                "\t\t\tevents: {\n" +
                "                    show: function () {\n" +
                "\t\t\t\t\t\tga('send', 'event', user_id + '_week" + week + "', 'show-you_' + timeStamp());\n" +
                "                    },\n" +
                "\t\t\t\t\thide: function () {\n" +
                "\t\t\t\t\t\tga('send', 'event', user_id + '_week" + week + "', 'hide-you_' + timeStamp());\n" +
                "                    }\n" +
                "                }\n" +
                "        }\n" +
                "\t\t]\n" +
                "    });\n" +
                "\t\n" +
                "    \n" +
                "};\n" +
                "\t\t\n" +
                "  window.onload = function(e) {\n" +
                "    loadScript(\"https://code.highcharts.com/highcharts-more.js\", loadWidget);\n" +
                "  }";

        return options;
    }

    private static String getSimpleChartOptions(String[] values, String[] scaledValues, String userId, int week) {
        String[] metricNames = ("Sessions per week#Average length of a session#Average time between sessions#" +
                "Time on task#Weekly assessment answers submitted#Timeliness of weekly assessment submission").split("#");
        String[] metricUnits = "#min#h#%##h".split("#");
        String[] thresh, scaledThresh, nextW, scaledNextW;


        metricNames = randomizeOrder(metricNames, Integer.parseInt(userId));
        metricUnits = randomizeOrder(metricUnits, Integer.parseInt(userId));

        values = randomizeOrder(values, Integer.parseInt(userId));
        scaledValues = randomizeOrder(scaledValues, Integer.parseInt(userId));

        thresh = randomizeOrder(thresholds, Integer.parseInt(userId));
        scaledThresh = randomizeOrder(scaledThresholds, Integer.parseInt(userId));

        nextW = randomizeOrder(nextWeek, Integer.parseInt(userId));
        scaledNextW = randomizeOrder(scaledNextWeek, Integer.parseInt(userId));

        String options =
                "function timeStamp() {\n" +
                "  var now = new Date();\n" +
                "  var date = [ now.getFullYear(), now.getMonth() + 1, now.getDate() ];\n" +
                "  var time = [ now.getHours(), now.getMinutes(), now.getSeconds() ];\n" +
                "\n" +
                "  for ( var i = 1; i < 3; i++ ) {\n" +
                "    if ( time[i] < 10 ) {\n" +
                "      time[i] = \"0\" + time[i];\n" +
                "    }\n" +
                "  }\n" +
                "\n" +
                "  if( date[1] < 10 ) {\n" +
                "\tdate[1] = \"0\" + date[1];\n" +
                "  }\n" +
                "  \n" +
                "  return date.join(\"-\") + \"Z\" + time.join(\":\");\n" +
                "}\n" +
                "\n" +
                "function loadWidget() {\n" +
                "\t\n" +
                "\tvar metricNames = " + getStringOfString(metricNames) + ";\n" +
                "\tvar metricUnits = " + getStringOfString(metricUnits) + ";\n" +
                "\t\n" +
                "\tvar values = " + getString(values) + ";\n" +
                "\tvar thisWeek = " + getString(thresh) + ";\n" +
                "\tvar nextWeek = " + getString(nextW) + ";\n" +
                "\n" +
                "\tvar user_id = analytics.user().id();\n" +
                "\t\n" +
                "\t$('#container').highcharts({\n" +
                "        chart: {\n" +
                "            polar: true,\n" +
                "\t\t\tstyle: {\n" +
                "\t\t\t\tfontFamily: 'Open Sans, sans-serif'\n" +
                "\t\t\t},\n" +
                "\t\t\ttype: 'area',\n" +
                "\t\t\tevents: {\n" +
                "\t\t\t\tload: function () {\n" +
                "\t\t\t\t\tvar category = user_id + '_week" + week + "';\n" +
                "\t\t\t\t\tgaRI('send', 'event', category, 'load_' + timeStamp());\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
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
                "            data: " + getString(scaledNextW) + ",\n" +
                "\t\t\tvisible: false,\n" +
                "\t\t\tevents: {\n" +
                "                    show: function () {\n" +
                "\t\t\t\t\t\tgaRI('send', 'event', user_id + '_week" + week + "', 'show-this-week_' + timeStamp());\n" +
                "                    },\n" +
                "\t\t\t\t\thide: function () {\n" +
                "\t\t\t\t\t\tgaRI('send', 'event', user_id + '_week" + week + "', 'hide-this-week_' + timeStamp());\n" +
                "                    }\n" +
                "                }\n" +
                "        },\n" +
                "\t\t\n" +
                "\t\t{\n" +
                "        \tcolor: 'rgba(255, 255, 102, 0.5)',\n" +
                "            name: 'Average graduate last week',\n" +
                "            data: " + getString(scaledThresh) + ", \n" +
                "            borderWidth: 0.5,\n" +
                "            borderColor: '#000000',\n" +
                "\t\t\tevents: {\n" +
                "                    show: function () {\n" +
                "\t\t\t\t\t\tgaRI('send', 'event', user_id + '_week" + week + "', 'show-last-week_' + timeStamp());\n" +
                "                    },\n" +
                "\t\t\t\t\thide: function () {\n" +
                "\t\t\t\t\t\tgaRI('send', 'event', user_id + '_week" + week + "', 'hide-last-week_' + timeStamp());\n" +
                "                    }\n" +
                "                }\t\t\t\t\n" +
                "      \n" +
                "        },\n" +
                "\t\t\n" +
                "\t\t{\n" +
                "            name: 'You',\n" +
                "            color: 'rgba(144, 202, 249, 0.5)',\n" +
                "            data: " + getString(scaledValues) + ",\n" +
                "\t\t\tevents: {\n" +
                "                    show: function () {\n" +
                "\t\t\t\t\t\tgaRI('send', 'event', user_id + '_week" + week + "', 'show-you_' + timeStamp());\n" +
                "                    },\n" +
                "\t\t\t\t\thide: function () {\n" +
                "\t\t\t\t\t\tgaRI('send', 'event', user_id + '_week" + week + "', 'hide-you_' + timeStamp());\n" +
                "                    }\n" +
                "                }\n" +
                "        }\n" +
                "\t\t]\n" +
                "    });\n" +
                "\t\n" +
                "    \n" +
                "};\n" +
                "\t\t\n" +
                "loadWidget();";

        return options;
    }

    private static String[] randomizeOrder(String[] metricNames, int user) {
        int init = user;
        String[] newOrder = new String[6];

        for(int i = 0; i < 6; i++, init++) {
            newOrder[i] = metricNames[init%6];
        }

        return newOrder;

    }

    private static String getString(String []line){
        String values = "[";

        for (int i = 0; i < 5; i++)
            values += line[i] + ",";
        values += line[5] + "]";

        return values;

    }

    private static String getStringOfString(String []line){
        String values = "[";

        for (int i = 0; i < 5; i++)
            values += "'" + line[i] + "',\n";
        values += "'" + line[5] + "']";

        return values;

    }
}

class UserS {
    public String id;
    public String anonId;

    public String[] values;
    public String[] scaledValues;

    public UserS (String id) {
        this.id = id;

        values = new String[6];
        scaledValues = new String[6];

    }

    public void setAnonId(String anon) {
        anonId = anon;
    }

    public void setValues(String[] val) {

        for(int i = 0; i < 6; i++)
            values[i] = val[i+1];

    }

    public void setScaledValues(String[] val) {

        for(int i = 0; i < 6; i++)
            scaledValues[i] = val[i+1];
    }
}