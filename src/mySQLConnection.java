/**
 * Created by Ioana on 1/14/2016.
 */
import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class mySQLConnection
{

    public static void main(String[] args) throws IOException, SQLException {
        DBase db = new DBase();
        Connection conn = db.connect(
                "jdbc:mysql://localhost:3306/data_2014","root","");
        //DBase.readSessions(conn);
        //DBase.readObservations(conn);
       //DBase.readSubmissions(conn);

        //ResultSet results = DBase.getPlatformTimePerWeek(conn, 1);

        conn.close();

    }

}

class DBase {
    public static String[] weekDays = {"2014-10-28", "2014-11-04", "2014-11-11", "2014-11-18", "2014-11-25", "2014-12-02", "2014-12-09", "2014-12-16", "2014-12-23",
            "2014-12-30", "2015-01-06", "2015-01-13"};

    public Connection connect(String db_connect_str, String db_userid, String db_password){
        Connection conn;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(db_connect_str, db_userid, db_password);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }

        System.out.println("Connected");
        return conn;
    }

    public static void addSession(Connection conn, String session_id, String user_id, String start_time, int duration){
        String query;
        Statement stmt;

        try
        {
            query="insert into sessions " +
                    "(session_id, course_user_id, start_time, duration) " +
                    "values ('" + session_id + "','" + user_id + "','" + start_time + "','" + duration + "');";
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void addObservation(Connection conn, String session_id, String user_id, String start_time, int duration){
        String query;
        Statement stmt;

        try
        {
            query="insert into observations " +
                    "(observation_id, course_user_id, resource_id, duration) " +
                    "values ('" + session_id + "','" + user_id + "','" + start_time + "','" + duration + "');";
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void readSessions(Connection connection) throws IOException {
        CSVReader sessions = new CSVReader(new FileReader("data\\2015\\sessions.csv"));
        String [] nextLine;
        int counter = 0;

        sessions.readNext();

        while ((nextLine = sessions.readNext()) != null) {
            addSession(connection, nextLine[0], nextLine[1], nextLine[2], Integer.parseInt(nextLine[3]));
            System.out.println("Session: " + counter++);
        }
        sessions.close();
    }

    public static void readObservations(Connection connection) throws IOException, SQLException {
        CSVReader observations = new CSVReader(new FileReader("data\\2015\\observations.csv"));
        String [] nextLine;
        String query;
        Statement stmt = connection.createStatement();;
        int counter = 0;

        observations.readNext();

        for(;counter < 1000;counter++){
            nextLine = observations.readNext();
            addObservation(connection, nextLine[0], nextLine[1], nextLine[2], Integer.parseInt(nextLine[3]));
            //System.out.println("Observation: " + counter);
        }

        observations.close();
        stmt.close();
    }

    public static void readSubmissions(Connection connection) throws IOException {
        CSVReader observations = new CSVReader(new FileReader("data\\2015\\submissions.csv"));
        String [] nextLine;
        int counter = 0;

        observations.readNext();

        while ((nextLine = observations.readNext()) != null) {
            addSubmission(connection, nextLine[0], nextLine[1], nextLine[2], nextLine[3].substring(0,19));
            System.out.println("Submission: " + counter++);
        }
        observations.close();
    }

    public static void addSubmission(Connection conn, String submission_id, String user_id, String problem_id, String time){
        String query;
        Statement stmt;

        try
        {
            query="insert into submissions " +
                    "(submission_id, course_user_id, problem_id, time) " +
                    "values ('" + submission_id + "','" + user_id + "','" + problem_id + "','" + time + "');";
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static void getPlatformTimePerWeek(Connection connection, int i) throws SQLException {
        ResultSet results;
        String query;
        Statement stmt;

        String week_start = weekDays[i-1], week_end = weekDays[i];

        query = "SELECT course_user.course_user_id as course_user_id, course_user.final_grade as grade, sum(CASE WHEN sessions.start_time > '2014-10-28' and sessions.start_time < '2014-11-04' THEN sessions.duration else 0 end) as week_1"+
                " FROM course_user " +
                "INNER JOIN sessions " +
                "ON course_user.course_user_id=sessions.course_user_id " +
                "where course_user.final_grade >= '0.6' " +
                "group BY course_user.course_user_id;";

        stmt = connection.createStatement();
        results = stmt.executeQuery(query);
        int j =0;
        while(results.next()) {
            System.out.println(j++);
            System.out.println(results.getString(1));
        }

        stmt.close();

        //return results;
    }

};