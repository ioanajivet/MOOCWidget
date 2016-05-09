package ri;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ioana on 4/25/2016.
 */
public class SessionCreation {

    static HashMap<String, UserForSessionCreation> users = new HashMap<>();

    public static void createArtificalSessions() throws IOException {
        readUsers(users, "data\\ri\\2014\\RI2014_graduates.csv");


    }

    //read users, quiz, forum, video
    private static void readUsers(HashMap<String, UserForSessionCreation> group, String filename) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filename));
        String [] nextLine;

        csvReader.readNext();

        while ((nextLine = csvReader.readNext()) != null)
            group.put(nextLine[0], new UserForSessionCreation(nextLine[0]));

        csvReader.close();

        System.out.println("Users read: " + group.size());
    }
}

class UserForSessionCreation {
    private String id;

    private List<Session> sessions;

    private List<Session> quizSessions;
    private List<Session> forumSessions;
    private List<Session> videoSessions;

    public UserForSessionCreation (String id) {
        this.id = id;
        quizSessions = new ArrayList<>();
        forumSessions = new ArrayList<>();
        videoSessions = new ArrayList<>();
    }

    public String getId() {
        return id;
    }
    public void addQuizSession(Session s) {
        quizSessions.add(s);
    }

    public List<Session> getQuizSessions () {
        return quizSessions;
    }

    public void setQuizSessions(List<Session> quizSessions) {
        this.quizSessions = quizSessions;
    }

    public void addForumSession(Session s) {
        forumSessions.add(s);
    }

    public List<Session> getForumSessions () {
        return forumSessions;
    }

    public void setForumSessions(List<Session> forumSessions) {
        this.forumSessions = forumSessions;
    }

    public void addVideoSession(Session s) {
        videoSessions.add(s);
    }

    public List<Session> getVideoSessions () {
        return videoSessions;
    }

    public void setVideoSessions(List<Session> videoSessions) {
        this.videoSessions = videoSessions;
    }


}

