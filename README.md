# MOOCWidget

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
6. problems.csv
      - problem_id,	published_week
5. resources.csv
      - resource_id, resource_type,	published_week
