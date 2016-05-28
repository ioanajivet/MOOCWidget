package analysis;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import st.UserMetricComputation;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Ioana on 5/24/2016.
 */
public class Interaction {

    private static List<String> activeUsersTest = new ArrayList<>();

    public static void analysis(String course, int weeks) throws IOException {

        //0. process the raw GA file to make it easier to be read afterwards;
        //// TODO: 5/24/2016 update it with the final version of the file for the final analysis
        //processRawGAFile("data\\analysis\\" + course + "\\2. interaction\\GA_24-05.csv", "data\\analysis\\" + course + "\\2. interaction\\GA.csv");

        activeUsersTest = readActiveUsers("data\\analysis\\" + course + "\\1. activity\\" + course.toUpperCase() + "_test_active.csv");

        //reads events only from active users
        List<Event> events = readEvents("data\\analysis\\" + course + "\\2. interaction\\GA_24-05.csv");

        //0. Print the breakdown of events based on the type
        writeTypeOfEvent(events, "data\\analysis\\" + course + "\\2. interaction\\" + course.toUpperCase() + "_type_of_events.csv");

        //1. Print the events per day in the file "eventsPerDay.csv"
        loadEventsPerDay(events, "data\\analysis\\" + course + "\\2. interaction\\" + course.toUpperCase() + "_loadEventsPerDay.csv");
        loadEventsPerWeek(course, events, "data\\analysis\\" + course + "\\2. interaction\\" + course.toUpperCase() + "_loadEventsPerWeek.csv", weeks);

        //2. Print the unique number of users + returning users that used the widget in each week
        usersPerWeek(course, events, "data\\analysis\\" + course + "\\2. interaction\\" + course.toUpperCase() + "_usersPerWeek.csv");

        //3. Print the names of the learners that viewed the widget
        writeWidgetUsers(events, "data\\analysis\\" + course + "\\2. interaction\\" + course.toUpperCase() + "_widget_users.csv");
    }


    //READING DATA
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

    private static List<Event> readEvents(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        String user, widget, type, timestamp;
        List<Event> events = new ArrayList<>();

        //skip headers
        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            user = nextLine[0].split("_")[0];

            if(!activeUsersTest.contains(user))
                continue;

            widget = nextLine[0].split("_")[1];
            type = nextLine[1].split("_")[0];
            timestamp = adjustTimestamp(nextLine[1].split("_")[1]);

            //only the relevant data was used - after 09.05
            if(timestamp.compareTo("2016-05-09") > 0) {
                events.add(new Event(user, widget, type, timestamp));
            }
        }
        csvReader.close();

        return events;
    }

    //ANALYSIS
    private static void usersPerWeek(String course, List<Event> events, String filename) throws IOException {
        HashMap<String, List<String>> usersPerWeek = new HashMap<>();

        List<Event> loadEvents = events.stream().filter(e -> e.getType().compareTo("load") == 0 ).collect(Collectors.toList());

        for (Event e: loadEvents) {

            String week = String.valueOf(Utils.getWeek(course, e.getTimestamp()));

            if(!usersPerWeek.containsKey(week))
                usersPerWeek.put(week, new ArrayList<>());

            if(usersPerWeek.get(week).contains(e.getUser()))
                continue;

            usersPerWeek.get(week).add(e.getUser());
        }

        writeUniqueUsers(usersPerWeek, filename, "Week#Returning#Unique#Total");
    }

    private static void writeUniqueUsers(HashMap<String, List<String>> usersPerWeek, String filename, String header) throws IOException{
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        toWrite = header.split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, List<String>> entry : usersPerWeek.entrySet()) {

            toWrite[0] = entry.getKey();


            int unique = 0;
            for (String user: entry.getValue()){
                boolean found = false;
                for (int i = 1; i < Integer.parseInt(entry.getKey()); i++) {

                    if (usersPerWeek.containsKey(String.valueOf(i))) {
                        if (usersPerWeek.get(String.valueOf(i)).contains(user)) {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found)
                    unique++;
            }


            toWrite[1] = String.valueOf(entry.getValue().size() - unique);
            toWrite[2] = String.valueOf(unique);
            toWrite[3] = String.valueOf(entry.getValue().size());
            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void loadEventsPerDay(List<Event> events, String filename) throws IOException {
        HashMap<String, Integer> loadEventsPerDay = new HashMap<>();

        List<Event> loadEvents = events.stream().filter(e -> e.getType().compareTo("load") == 0 ).collect(Collectors.toList());

        for (Event e: loadEvents) {
            if(loadEventsPerDay.containsKey(e.getDate()))
                loadEventsPerDay.put(e.getDate(), loadEventsPerDay.get(e.getDate()) + 1);
            else
                loadEventsPerDay.put(e.getDate(), 1);
        }

        writeHashMap(loadEventsPerDay, filename, "Date#Events");
    }

    private static void loadEventsPerWeek(String course, List<Event> events, String filename, int weeks) throws IOException {
        //week, total_load_events, for the current week, all the others
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        toWrite = "Week#Total load events#Last published widget#Previously published widgets".split("#");
        output.writeNext(toWrite);

        for (int i = 1; i <= weeks; i++) {
            toWrite[0] = String.valueOf(i);
            toWrite[1] = String.valueOf(getLoadEventsInWeek(course, events, i));
            toWrite[2] = String.valueOf(getLoadEventsForWeek(course, events, i));
            toWrite[3] = String.valueOf(Integer.parseInt(toWrite[1]) - Integer.parseInt(toWrite[2]));

            output.writeNext(toWrite);

        }

        output.close();
    }

    //the total number of load events in week i
    private static long getLoadEventsInWeek(String course, List<Event> events, int week) {
        return events.stream()
                .filter(e -> e.getType().compareTo("load") == 0
                        && Utils.getWeek(course, e.getTimestamp()) == week)
                .count();
    }

    //the number of load events for the last published widget in week i
    private static long getLoadEventsForWeek(String course, List<Event> events, int week) {
        return events.stream()
                .filter(e -> e.getType().compareTo("load") == 0
                 && e.getWidget().compareTo("week" + (week-1)) == 0
                 && Utils.getWeek(course, e.getTimestamp()) == week)
                .count();
    }

    private static void writeHashMap(HashMap<String, Integer> hashMap, String filename, String header) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        toWrite = header.split("#");
        output.writeNext(toWrite);

        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            toWrite[0] = entry.getKey();
            toWrite[1] = entry.getValue().toString();
            output.writeNext(toWrite);
        }

        output.close();
    }



    private static void writeWidgetUsers(List<Event> events, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        toWrite = "User".split("#");
        output.writeNext(toWrite);

        List<String> ids = events.stream().map(e -> e.getUser()).distinct().collect(Collectors.toList());

        for (String id: ids) {
            toWrite[0] = id;
            output.writeNext(toWrite);
        }

        output.close();
    }

    private static void writeTypeOfEvent(List<Event> events, String filename) throws IOException {

        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;
        toWrite = "Event type#Count".split("#");
        output.writeNext(toWrite);

        //1. Load events
        toWrite[0] = "Load";
        toWrite[1] = String.valueOf(events.stream().map(e -> e.getType()).filter(e -> e.compareTo("load") == 0).count());
        output.writeNext(toWrite);

        //2. Show/hide_you events
        toWrite[0] = "Show/hide you";
        toWrite[1] = String.valueOf(events.stream().map(e -> e.getType()).filter(e -> e.compareTo("show-you") == 0 || e.compareTo("hide-you") == 0).count());
        output.writeNext(toWrite);

        //3. Show/hide_last_week events
        toWrite[0] = "Show/hide last week";
        toWrite[1] = String.valueOf(events.stream().map(e -> e.getType()).filter(e -> e.compareTo("show-last-week") == 0 || e.compareTo("hide-last-week") == 0).count());
        output.writeNext(toWrite);

        //4. Show/hide_this_week events
        toWrite[0] = "Show/hide this week";
        toWrite[1] = String.valueOf(events.stream().map(e -> e.getType()).filter(e -> e.compareTo("show-this-week") == 0 || e.compareTo("hide-this-week") == 0).count());
        output.writeNext(toWrite);

        //5. Users that generated other events than load with the widget
        toWrite[0] = "Users that generated other events than load";
        toWrite[1] = String.valueOf(events.stream()
                .filter(e -> e.getType().compareTo("load") != 0)
                .map(e -> e.getUser())
                .distinct()
                .count());

        output.writeNext(toWrite);

        output.close();
    }

    //UTILS

    private static String adjustTimestamp(String timestamp) {
        String date = timestamp.split("Z")[0];
        String time = timestamp.split("Z")[1];

        String day = date.split("-")[2];
        String year_month = date.substring(0, 8);

        if(day.length() == 1)
            day = "0" + day;

        return year_month + day + " " + time;
    }

    private static void processRawGAFile(String input_filename, String output_filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(input_filename));
        String [] nextLine;

        //prepare output file
        CSVWriter output = new CSVWriter(new FileWriter(output_filename), ',');
        String[] toWrite;
        toWrite = "User#Widget#Event#Timestamp".split("#");
        output.writeNext(toWrite);

        //skip headers
        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            toWrite[0] = nextLine[0].split("_")[0];
            toWrite[1] = nextLine[0].split("_")[1];
            toWrite[2] = nextLine[1].split("_")[0];
            toWrite[3] = nextLine[1].split("_")[1].replace("Z", " ");

            output.writeNext(toWrite);
        }

        csvReader.close();
        output.close();
    }

}

class Event implements Comparable<Event> {
    String user;
    String widget;
    String type;
    String timestamp;

    public Event(String u, String w, String ty, String ti) {
        this.user = u;
        this.widget = w;
        this.type = ty;
        this.timestamp = ti;
    }

    public String getUser() { return this.user;}

    public String getWidget() { return this.widget;}

    public String getType() { return this.type;}

    public String getTimestamp() {return this.timestamp;}

    public String getDate() { return this.timestamp.split(" ")[0];}

    public int compareTo(Event next) {
        return this.timestamp.compareTo(next.timestamp);
    }
}