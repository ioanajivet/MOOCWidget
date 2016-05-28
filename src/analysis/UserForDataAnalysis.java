package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ioana on 5/24/2016.
 */
public class UserForDataAnalysis {
    private String id;

    private HashMap<Integer, Double> metric1;
    private HashMap<Integer, Double> metric2;
    private HashMap<Integer, Double> metric3;
    private HashMap<Integer, Double> metric4;
    private HashMap<Integer, Double> metric5;
    private HashMap<Integer, Double> metric6;

    private List<Double> status;

    public UserForDataAnalysis(String id) {
        this.id = id;
        metric1 = new HashMap<>();
        metric2 = new HashMap<>();
        metric3 = new HashMap<>();
        metric4 = new HashMap<>();
        metric5 = new HashMap<>();
        metric6 = new HashMap<>();

        status = new ArrayList<>();
        status.add(0.0);
    }

    public void setMetric(int metric, int week, double value){
        switch (metric) {
            case 1: metric1.put(week, value); break;
            case 2: metric2.put(week, value); break;
            case 3: metric3.put(week, value); break;
            case 4: metric4.put(week, value); break;
            case 5: metric5.put(week, value); break;
            default: metric6.put(week, value);
        }

    }

    public double getMetric(int metric, int week){
        switch (metric) {
            case 1: return metric1.get(week);
            case 2: return metric2.get(week);
            case 3: return metric3.get(week);
            case 4: return metric4.get(week);
            case 5: return metric5.get(week);
            default: return metric6.get(week);
        }
    }

    public void calculateStatus(int week, List<Double> threholds) {

        List<Double> difference = new ArrayList<>();
        difference.add(0.0);

        for(int i = 1; i < threholds.size(); i++) {
            double diff = getMetric(i, week) - threholds.get(i);
            if(Math.abs(diff) <= 0.5)
                difference.add(i, 0.0);
            else if (diff > 0)
                difference.add(i, diff - 0.5);
            else
                difference.add(i, diff + 0.5);

        }

        status.add(week, weightedSum(difference));
    }

    private double weightedSum(List<Double> difference) {
        double sum = difference.stream().reduce(Double::sum).get().doubleValue();  //pure sum
        double weightedSum = 0;

        for (int i = 1; i < difference.size(); i++) {
            weightedSum += (10 - difference.get(i)) * difference.get(i);
        }

        return weightedSum / (60 - sum);
    }

    public List<Double> getStatus() {
        return status;
    }

    public String getId(){
        return this.id;
    }

}
