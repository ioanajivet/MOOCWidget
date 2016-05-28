package analysis;

//select for analysis only the active users from both groups
//Active - >5 mins on the platform

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ioana on 5/25/2016.
 */
public class cleanUpUserName {


    public static void main(String[] args) throws IOException {

        correctId("data\\dw\\2016\\week9\\metrics\\DW2016_metrics_all.csv",
                "data\\dw\\2016\\week9\\metrics\\DW2016_metrics_all-done.csv");

    }

    private static void correctId(String input, String output) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(input));
        CSVWriter csvWriter = new CSVWriter(new FileWriter(output));
        String [] nextLine;

        nextLine = csvReader.readNext();
        csvWriter.writeNext(nextLine);

        while ((nextLine = csvReader.readNext()) != null) {
            nextLine[0] = nextLine[0].split("_")[1];
            csvWriter.writeNext(nextLine);
        }

        csvReader.close();
        csvWriter.close();

    }

}
