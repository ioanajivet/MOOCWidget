package analysis;

import java.io.IOException;

/**
 * Created by Ioana on 5/24/2016.
 */
public class MainAnalysis {

    public static void main(String[] args) throws IOException {

        //activeThreshhold is the threshold for time spent on the platform
        int activeThreshold = 300;
        //Activity.selectActive("st", 6, activeThreshold);
        //Activity.selectActive("ri", 6, activeThreshold);
        //Activity.selectActive("dw", 9, activeThreshold);

        //Interaction.analysis("st", 6);
        //Interaction.analysis("ri", 6);

        //BehaviourIndicators.analysis("st", 6);
        //BehaviourIndicators.analysis("ri", 6);
        //BehaviourIndicators.analysis("dw", 9);

        //Engagement.analysis("st",6);
        //Engagement.analysis("ri",6);
        //todo the data files for DW have to be adjusted to the ones of the other two to be able to run it
        //Engagement.analysis("dw",9);

        //Motivation.analysis("ri", 6);
        //Motivation.analysis("st", 6);
    }
}
