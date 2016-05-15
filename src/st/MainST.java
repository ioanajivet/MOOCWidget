package st;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by Ioana on 4/7/2016.
 */
public class MainST {

    public static void main(String[] args) throws IOException, ParseException {

        //Compute thresholds for 2015
        //int weekMax = 10;
        //for(int i = 1; i <= weekMax; i++)
        //    MetricComputation.computeMetrics2015(i);
        //ThresholdComputation.computeThresholds();

        //Compute metrics for 2016
        int week = 4;
        MetricComputation.computeMetrics(week);
        ScalingComputation.scalingMetrics(week);

        ScriptGeneration.generateScripts(week);

    }
}
