# MOOCWidget

== Usage == 
LearningTracker -command [params]

Commands:
-computethreshold course_code nr_of_weeks path [cutOffpercent]
      -- course_code - a two letter code to identify the course
      -- nr_of_weeks - the length of the course in weeks
      -- path - the path to the folder that contains the "data" folder with the csv files
      -- cutOffPercent - used for removing the outliers in aggregating the threshold
                       - optional; default is set to 5%
** as input in the folder "data"
      -- graduates.csv
** as output in the specified path:
      -- a folder "metrics" that contains the metrics for all graduates
      -- a folder "thresholds" that contains the maximums, the thresholds and the scaled thresholds for all course weeks

-computemetrics course_code week path
      -- course_code - a two letter code to identify the course
      -- week - the week which the metrics are computed for
      -- path - the path of the folder that contains the "week[i]" folders

** as input in the folder with the specified path
      -- test.csv - a list with the ids of all learners taking part in the experiment 
      -- folder "thresholds" with the thresholds calculated offline
      
** as output in the folder "week[i]"
      -- "metrics" folder with the metrics and the scaled metrics
      -- "scripts" folder with the scripts for all the test learners


== Input file structure ==
* short_user_id = edX identifier (numeric)
* any tiemstamp is in the format: yyyy-mm-dd hh:mm:ss

1. sessions.csv
      - session_id, short_user_id,	start_timestamp,	end_timestamp,	duration
2. forum_sessions.csv
      - session_id,	short_user_id,	start_timestamp,	end_timestamp,	duration
3. quiz_sessions.csv
      - session_id,	short_user_id,	start_timestamp,	end_timestamp,	duration
4. observations.csv
      - observation_id,	short_user_id,	video_id, start_timestamp, end_timestamp, duration
5. submissions.csv
      - submission_id,	short_user_id,	problem_id, timestamp
      - timestamp is in the format: 2015-02-04T13:13:24.658757+00:00
6. problems.csv
      - problem_id, published_week
      - in the original data, the published_week is week-1 (don't know why...?)
      - it should include only the graded assignments/the ones that are to be tracked
5. resources.csv
      - resource_id, resource_type,	published_week
