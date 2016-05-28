package analysis;

/**
 * Created by Ioana on 5/27/2016.
 */
public class Utils {

    public static int getWeek(String course, String startTime) {
        switch (course) {
            case "st": return ST_getWeek(startTime);
            case "ri": return RI_getWeek(startTime);
            case "dw": return DW_getWeek(startTime);
        }

        return 0;
    }

    private static int ST_getWeek(String startTime) {
        if (startTime.compareTo("2016-04-12") >= 0 && startTime.compareTo("2016-04-19") < 0)
            return 1;
        if (startTime.compareTo("2016-04-19") >= 0 && startTime.compareTo("2016-04-26") < 0)
            return 2;
        if (startTime.compareTo("2016-04-26") >= 0 && startTime.compareTo("2016-05-03") < 0)
            return 3;
        if (startTime.compareTo("2016-05-03") >= 0 && startTime.compareTo("2016-05-10") < 0)
            return 4;
        if (startTime.compareTo("2016-05-10") >= 0 && startTime.compareTo("2016-05-17") < 0)
            return 5;
        if (startTime.compareTo("2016-05-17") >= 0 && startTime.compareTo("2016-05-24") < 0)
            return 6;
        if (startTime.compareTo("2016-05-24") >= 0 && startTime.compareTo("2016-05-31") < 0)
            return 7;
        if (startTime.compareTo("2016-05-31") >= 0 && startTime.compareTo("2016-06-07") < 0)
            return 8;
        if (startTime.compareTo("2016-06-07") >= 0 && startTime.compareTo("2016-06-14") < 0)
            return 9;
        if (startTime.compareTo("2016-06-14") >= 0 && startTime.compareTo("2016-06-20") < 0)
            return 10;
        return 99;
    }

    private static int RI_getWeek(String startTime) {
        if (startTime.compareTo("2016-04-11") >= 0 && startTime.compareTo("2016-04-18") < 0)
            return 1;
        if (startTime.compareTo("2016-04-18") >= 0 && startTime.compareTo("2016-04-25") < 0)
            return 2;
        if (startTime.compareTo("2016-04-25") >= 0 && startTime.compareTo("2016-05-02") < 0)
            return 3;
        if (startTime.compareTo("2016-05-02") >= 0 && startTime.compareTo("2016-05-09") < 0)
            return 4;
        if (startTime.compareTo("2016-05-09") >= 0 && startTime.compareTo("2016-05-16") < 0)
            return 5;
        if (startTime.compareTo("2016-05-16") >= 0 && startTime.compareTo("2016-05-23") < 0)
            return 6;
        if (startTime.compareTo("2016-05-23") >= 0 && startTime.compareTo("2016-05-30") < 0)
            return 7;
        if (startTime.compareTo("2016-05-30") >= 0 && startTime.compareTo("2016-06-06") < 0)
            return 8;
        if (startTime.compareTo("2016-06-06") >= 0 && startTime.compareTo("2016-06-15") < 0)
            return 9;
        return 99;
    }

    private static int DW_getWeek(String startTime) {
        if(startTime.compareTo("2016-01-12")>0&&startTime.compareTo("2016-01-19")<0)
                return 1;
        if(startTime.compareTo("2016-01-19")>0&&startTime.compareTo("2016-01-26")<0)
                return 2;
        if(startTime.compareTo("2016-01-26")>0&&startTime.compareTo("2016-02-02")<0)
                return 3;
        if(startTime.compareTo("2016-02-02")>0&&startTime.compareTo("2016-02-09")<0)
                return 4;
        if(startTime.compareTo("2016-02-09")>0&&startTime.compareTo("2016-02-16")<0)
                return 5;
        if(startTime.compareTo("2016-02-16")>0&&startTime.compareTo("2016-02-23")<0)
                return 6;
        if(startTime.compareTo("2016-02-23")>0&&startTime.compareTo("2016-03-01")<0)
                return 7;
        if(startTime.compareTo("2016-03-01")>0&&startTime.compareTo("2016-03-08")<0)
                return 8;
        if(startTime.compareTo("2016-03-08")>0&&startTime.compareTo("2016-03-15")<0)
                return 9;
        if(startTime.compareTo("2016-03-15")>0&&startTime.compareTo("2016-03-22")<0)
                return 10;
        if(startTime.compareTo("2016-03-22")>0&&startTime.compareTo("2016-03-29")<0)
                return 11;
        return 99;
    }

    public static int getMaximumGradedAssignments(String course) {
        switch (course) {
            case "dw": return 25;
            case "st": return 35;
            case "ri": return 76;
        }
        return 0;
    }

    public static int getMaximumNonGradedAssignments(String course) {
        switch (course) {
            case "dw": return 63;
            case "st": return 261;
            case "ri": return 0;
        }
        return 0;
    }

    public static int getMaximumVideos(String course) {
        switch (course) {
            case "dw": return 58;
            case "st": return 81;
            case "ri": return 53;
        }
        return 0;
    }

}
