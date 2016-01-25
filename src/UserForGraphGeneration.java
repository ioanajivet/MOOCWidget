import java.util.HashMap;

/**
 * Created by Ioana on 1/16/2016.
 */
public class UserForGraphGeneration {
    private String id;
    private String anonymousId;

    private HashMap<Integer, Integer> weeklyPlatformTimes;
    private HashMap<Integer, Integer> weeklyVideoTimes;
    private HashMap<Integer, Double> weeklyRatioTimes;
    private HashMap<Integer, Integer> weeklyDistinctVideos;
    private HashMap<Integer, Integer> weeklyAssignments;
    private HashMap<Integer, Integer> weeklyUntilDeadline;

    public UserForGraphGeneration(String id) {
        this.id = id;
        weeklyPlatformTimes = new HashMap<>();
        weeklyVideoTimes = new HashMap<>();
        weeklyRatioTimes = new HashMap<>();
        weeklyDistinctVideos = new HashMap<>();
        weeklyAssignments = new HashMap<>();
        weeklyUntilDeadline = new HashMap<>();
    }

    public void setAnonymousId(String anon){
        this.anonymousId = anon;
    }

    public void setPlatformTime(int week, int time){
        weeklyPlatformTimes.put(week, time);
    }

    public void setVideoTime(int week, int time){
        weeklyVideoTimes.put(week, time);
    }

    public void setRatioTime(int week, double ratio){
        weeklyRatioTimes.put(week, ratio);
    }

    public void setDistinctVideos(int week, int videos){
        weeklyDistinctVideos.put(week, videos);
    }

    public void setAssignments(int week, int assignment){
        weeklyAssignments.put(week, assignment);
    }

    public void setUntilDeadline(int week, int hours){
        weeklyUntilDeadline.put(week, hours);
    }

    public String getId(){
        return this.id;
    }

    public String getAnonymousId(){
        return this.anonymousId;
    }

    public int getPlatformTime(int week){
        return weeklyPlatformTimes.get(week);
    }

    public int getVideoTime(int week){
        return weeklyVideoTimes.get(week);
    }

    public double getRatioTime(int week) {
        return weeklyRatioTimes.get(week);
    }

    public int getDistinctVideos(int week){
        return weeklyDistinctVideos.get(week);
    }

    public int getAssignments(int week){
        return weeklyAssignments.get(week);
    }

    public int getUntilDeadline(int week) {
        return weeklyUntilDeadline.get(week);
    }
}
