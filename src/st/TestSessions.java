package st;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ioana on 4/18/2016.
 */
public class TestSessions {

    private static List<Session> sessions;

    public static void main(String[] args) throws IOException {
        sessions = new ArrayList<>();

        //readSessions("data\\ri\\2014\\sessions.csv");
        //readSessions("data\\st\\2015\\sessions.csv");
        //readSessions("data\\ri\\2016\\sessions.csv");
        readSessions("data\\st\\2016\\sessions.csv");
        System.out.println("Sessions: " + sessions.size());

        //readForumSessions("data\\ri\\2014\\forum_sessions.csv");
        //readForumSessions("data\\st\\2015\\forum_sessions.csv");
        //readForumSessions("data\\ri\\2016\\forum_sessions.csv");
        readForumSessions("data\\st\\2016\\forum_sessions.csv");
        //readQuizSessions("data\\ri\\2014\\quiz_sessions.csv");
        //readQuizSessions("data\\st\\2015\\quiz_sessions.csv");
        //readQuizSessions("data\\ri\\2016\\quiz_sessions.csv");
        readQuizSessions("data\\st\\2016\\quiz_sessions.csv");

    }

    private static void readForumSessions(String filename) throws IOException{
        //session_id, course_user_id, start_time, duration
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine, session_attr;
        //3 - id, 4 - start, 5 - end
        int mobile = 0;
        boolean found;

        while ((nextLine = csvReader.readNext()) != null) {
            session_attr = nextLine[0].split("_");
            found = false;

            for (Session s: sessions) {
                if(s.includes(session_attr[3], session_attr[4], session_attr[5])) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                //System.out.println(session_attr[3] + ": " + session_attr[4] + " - " + session_attr[5]);
                mobile++;
            }
        }

        System.out.println("Unmatched forum sessions: " + mobile);

        csvReader.close();

    }

    private static void readQuizSessions(String filename) throws IOException{
        //session_id, course_user_id, start_time, duration
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine, session_attr;
        //4 - id, 5 - start, 6 - end
        int mobile = 0;
        boolean found;

        while ((nextLine = csvReader.readNext()) != null) {
            session_attr = nextLine[0].split("_");
            found = false;

            for (Session s: sessions) {
                if(s.includes(session_attr[4], session_attr[5], session_attr[6])) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                //System.out.println(session_attr[4] + ": " + session_attr[5] + " - " + session_attr[6]);
                mobile++;
            }
        }

        System.out.println("Unmatched quiz sessions: " + mobile);

        csvReader.close();

    }

    private static void readSessions(String filename) throws IOException {
        //session_id, course_user_id, start_time, duration
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String[] nextLine, session_attr;

        while ((nextLine = csvReader.readNext()) != null) {
            session_attr = nextLine[0].split("_");
            sessions.add(new Session(session_attr[2], session_attr[3], session_attr[4]));
        }

        csvReader.close();

    }
}
