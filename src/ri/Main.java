package ri;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by Ioana on 4/7/2016.
 */
public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        int week = 11;
        MetricComputation.computeMetrics(week);
        //for(int i = 1; i < 12; i++)
          //  MetricComputation.computeMetrics2015(i);


    }
}
