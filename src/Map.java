import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ioana on 5/16/2016.
 */
public class Map {

    public static void main(String[] args) throws IOException {

        //clusterByCountry("dwt");
        clusterByCountry("st");
        //clusterByCountry("ri");

    }

    private static void clusterByCountry(String course) throws IOException {
        List<String> users;

        users = readUserIds("data\\" + course + "\\student_profile.csv");
        HashMap<String, Integer> counter = readContries(users, "data\\" + course + "\\user_pii.csv");

        //writeJSON(counter, "data\\per_countryJSON.txt");
        writeCSV(counter, "data\\" + course + "\\per_country.csv");
    }

    private static List<String> readUserIds(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;

        List<String> users = new ArrayList<>();

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            users.add(nextLine[0]);
        }

        csvReader.close();

        return users;
    }

    private static HashMap<String, Integer> readContries(List<String> users, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;

        HashMap<String, Integer> counter = new HashMap<>();

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            String user_id = nextLine[0].split("_")[1];

            if(!users.contains(user_id))
                continue;

            if(counter.containsKey(nextLine[4]))
                counter.put(nextLine[4], counter.get(nextLine[4]) + 1);
            else
                counter.put(nextLine[4], 1);
        }

        csvReader.close();

        return counter;
    }

    private static void writeJSON(HashMap<String, Integer> counter, String filename) throws IOException {
        FileWriter output = new FileWriter(filename);
        String toWrite;

        toWrite = "var data = [\n";

        for (java.util.Map.Entry<String, Integer> entry : counter.entrySet()) {
            toWrite +=
                    "        {\n" +
                            "            \"code\": \"" + entry.getKey() + "\",\n" +
                            "            \"value\": " + entry.getValue() + "\n" +
                            "        },\n";
        }

        toWrite += "    ];";

        output.write(toWrite);
        output.close();
    }

    private static void writeCSV(HashMap<String, Integer> counter, String filename) throws IOException {
        CSVWriter output = new CSVWriter(new FileWriter(filename), ',');
        String[] toWrite;

        toWrite = "Country#Users".split("#");

        output.writeNext(toWrite);

        for (java.util.Map.Entry<String, Integer> entry : counter.entrySet()) {
            toWrite[0] = entry.getKey();
            toWrite[1] = String.valueOf(entry.getValue());

            output.writeNext(toWrite);
        }
        output.close();
    }
}
