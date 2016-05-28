import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map;

/**
 * Created by Ioana on 5/16/2016.
 */
public class Demographics {

    public static void main(String[] args) throws IOException {

        //** Demographics
        //analyse("dwt");
        //analyse("st");
        //analyse("ri");

        //** Connect countries with the code and the HDI value
        //readCountries();

        //** Divide into very high, high, medium and low HDI
        //assignHDI("dwt");
        assignHDI("st");
        assignHDI("ri");

    }

    private static void assignHDI(String course) throws IOException {
        //read country_HDI
        HashMap<String, Country> countryHDI = new HashMap<>();

        CSVReader csvReader;
        String [] nextLine;
        csvReader = new CSVReader(new FileReader("data\\country_HDI.csv"));
        csvReader.readNext();
        while ((nextLine = csvReader.readNext()) != null)
            countryHDI.put(nextLine[1], new Country(nextLine[0], nextLine[1], nextLine[2]));
        csvReader.close();

        //prepare output file
        CSVWriter output = new CSVWriter(new FileWriter("data\\" + course + "\\country_HDI_participants.csv"), ',');
        String[] toWrite;
        toWrite = "Country#Code#HDI#Participants".split("#");
        output.writeNext(toWrite);

        //read HDI
        csvReader = new CSVReader(new FileReader("data\\" + course + "\\per_country.csv"));
        csvReader.readNext();
        while ((nextLine = csvReader.readNext()) != null) {
            if(countryHDI.containsKey(nextLine[0])) {
                Country c = countryHDI.get(nextLine[0]);

                toWrite[0] = c.name;
                toWrite[1] = c.code;
                toWrite[2] = c.index;
                toWrite[3] = nextLine[1];

                output.writeNext(toWrite);
            }
            else
                System.out.println(nextLine[0] + ": " + nextLine[1]);
        }

        csvReader.close();
        output.close();

    }


    private static void analyse(String course) throws IOException {
        HashMap<String, Integer> counter;

        // 0 - user id
        // 1 - gender
        // 2 - year of birth
        // 3 - level of education
        // 4 - country

        //-----------------------------
        //Cluster by level of education
        //-----------------------------

        System.out.println("TEST");
        counter = readAndCluster("data\\" + course + "\\demo_test.csv", 3);
        //print counter

        for (Map.Entry<String, Integer> entry : counter.entrySet())
          System.out.println(entry.getKey() + ": " + entry.getValue());

        System.out.println("Highschool or less: " + (counter.get("el") + counter.get("hs") + counter.get("jhs")));
        System.out.println("College: " + (counter.get("a") + counter.get("b")));
        //System.out.println("Advanced: " + (counter.get("p") + counter.get("m") + counter.get("p_se") + counter.get("p_oth")));
        //System.out.println("Rest: " + (counter.get("none") + counter.get("None") + counter.get("") + counter.get("NULL") + counter.get("other")));
        //for (Map.Entry<String, Integer> entry : counter.entrySet())
          //  System.out.println(entry.getKey() + ": " + entry.getValue());

        System.out.println("CONTROL");
        counter = readAndCluster("data\\" + course + "\\demo_control.csv", 3);

        for (Map.Entry<String, Integer> entry : counter.entrySet())
            System.out.println(entry.getKey() + ": " + entry.getValue());

        //print counter
        System.out.println("Highschool or less: " + (counter.get("el") + counter.get("hs") + counter.get("jhs")));
        System.out.println("College: " + (counter.get("a") + counter.get("b")));
        //System.out.println("Advanced: " + (counter.get("p") + counter.get("m") + counter.get("p_se") + counter.get("p_oth")));
        //System.out.println("Rest: " + (counter.get("none") + counter.get("") + counter.get("NULL") + counter.get("other")));


        //-----------------------------
        //Cluster by age
        //-----------------------------

        counter = readAndClusterAge("data\\" + course + "\\demo_test.csv", 2);
        writeCSV(counter, "data\\" + course + "\\demo_age_test.csv");

        counter = readAndClusterAge("data\\" + course  + "\\demo_control.csv", 2);
        writeCSV(counter, "data\\" + course + "\\demo_age_control.csv");

        //For highcharts or highmaps the data object
        //writeJSON(counter, "data\\per_countryJSON.txt");
        //writeCSV(counter, "data\\dwt\\demographics_age.csv");

        //Change user id from the long one (with course code) to the short one
        //minimizeUserId("data\\dwt\\user_pii.csv", "data\\dwt\\demographics.csv");
    }

    private static HashMap<String, Integer> readAndCluster(String filename, int field) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;

        HashMap<String, Integer> counter = new HashMap<>();

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
            if(counter.containsKey(nextLine[field]))
                counter.put(nextLine[field], counter.get(nextLine[field]) + 1);
            else
                counter.put(nextLine[field], 1);
        }

        csvReader.close();

        return counter;
    }

    private static HashMap<String, Integer> readAndClusterAge(String filename, int field) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;
        int age;

        HashMap<String, Integer> counter = new HashMap<>();
        counter.put("NULL", 0);

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {

            if(nextLine[field] == null || "NULL".compareTo(nextLine[field]) == 0 || "None".compareTo(nextLine[field]) == 0) {
                counter.put("NULL", counter.get("NULL") + 1);
                continue;
            }

            age = 2016 - Integer.valueOf(nextLine[field]);
            if(counter.containsKey(String.valueOf(age)))
                counter.put(String.valueOf(age), counter.get(String.valueOf(age)) + 1);
            else
                counter.put(String.valueOf(age), 1);
        }

        csvReader.close();

        return counter;
    }

    private static HashMap<String, Integer> readUsers(String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;

        HashMap<String, Integer> counter = new HashMap<>();

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null) {
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

    private static void minimizeUserId(String inFile, String outFile) throws IOException {
        CSVReader input = new CSVReader(new FileReader(inFile));
        CSVWriter output = new CSVWriter(new FileWriter(outFile), ',');
        String [] inLine;
        String[] outLine;

        output.writeNext(input.readNext());

        while ((inLine = input.readNext()) != null) {
            outLine = inLine;
            outLine[0] = (outLine[0].split("_"))[1];

            output.writeNext(outLine);
        }

        input.close();
        output.close();
    }

    private static void readCountries() throws IOException {
        CSVReader csvReader;
        String [] nextLine;

        HashMap<String, String> countries = new HashMap<>();

        //read list of countries + their code
        csvReader = new CSVReader(new FileReader("data\\country_code.csv"));
        csvReader.readNext();
        while ((nextLine = csvReader.readNext()) != null)
            countries.put(nextLine[0], nextLine[1]);
        csvReader.close();

        //prepare output file
        CSVWriter output = new CSVWriter(new FileWriter("data\\country_HDI.csv"), ',');
        String[] toWrite;
        toWrite = "Country#Code#HDI".split("#");
        output.writeNext(toWrite);

        //read HDI
        csvReader = new CSVReader(new FileReader("data\\HDI.csv"));
        csvReader.readNext();
        while ((nextLine = csvReader.readNext()) != null) {
            if(countries.containsKey(nextLine[1])) {
                toWrite[0] = nextLine[1];
                toWrite[1] = countries.get(nextLine[1]);
                toWrite[2] = nextLine[2];

                output.writeNext(toWrite);
            }
            else
                System.out.println(nextLine[1]);
        }

        csvReader.close();
        output.close();
    }
}

class Country {
    String name;
    String code;
    String index;

    public Country(String n, String c, String i) {
        this.name = n;
        this.code = c;
        this.index = i;
    }

    public void setIndex(String i) {
        this.index = i;
    }

}
