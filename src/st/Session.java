package st;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ioana on 4/18/2016.
 */
public class Session implements Comparable<Session> {
    private String start_time;
    private String end_time;
    private int duration;

    private String user;

    public Session(String user, String start, String end) {
        this.user = user;
        start_time = start;
        end_time = end;
    }

    public Session(String user, String start, String end, int duration) {
        this.user = user;
        this.start_time = start;
        this.end_time = end;
        this.duration = duration;
    }

    public boolean includes(String user_id, String start, String end) {

        if(user.compareTo(user_id) != 0)
            return false;

        if(start_time.compareTo(start) <= 0 && end_time.compareTo(end) >= 0)
            return true;

        return false;
    }

    public boolean includesStart(String user_id, String start) {

        if(user.compareTo(user_id) != 0)
            return false;

        if(start_time.compareTo(start) <= 0 && end_time.compareTo(start) > 0)
            return true;

        return false;
    }

    private Date getDateFromString(String dateString) {
        //input date: "2014-11-11 12:00:00"
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(dateString);
        }
        catch (ParseException e) {
            System.out.println("Invalid date");
            return new Date();
        }
    }

    public int compareTo(Session next) {
        return this.start_time.compareTo(next.start_time);
    }

    public String getStartTime() {
        return this.start_time;
    }

    public String getEndTime() {
        return this.end_time;
    }

    public Date getStartDate() {
        return getDateFromString(start_time);
    }

    public Date getEndDate() {
        return getDateFromString(end_time);
    }
}
