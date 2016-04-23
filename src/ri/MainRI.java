package ri;

import st.ScalingComputation;
import st.ScriptGeneration;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by Ioana on 4/7/2016.
 */
public class MainRI {

    public static void main(String[] args) throws IOException, ParseException {


        //Compute thresholds for 2015
        int maxWeek =11;
        //for(int i = 1; i <= maxWeek; i++)
        //    MetricComputation.computeMetrics2014(i);
        ThresholdComputation.computeThresholds(maxWeek);

        //Compute metrics for 2016
        //int week = 1;
        //MetricComputation.computeMetrics(week);
        //ScalingComputation.scalingMetrics(week);

        //ScriptGeneration.generateScripts(week);
    }
}
