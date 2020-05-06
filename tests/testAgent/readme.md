# parseXML Manual

The purpose of this program is to parse the XML output by the tests and converting it into a .csv format for better analytics

Runs using python 3.X

# How To Run
    >>>python parseXML.py

# Expected input
    match
        match-id
        sight-of
        role
        role
        player
        player
        timestamp
        startclock
        history
            step
                step-number
                move
                move
        scores
            reward
            reward
        state
            fact
                prop-f
                arg

# Output Format

The output is a .csv file containing the following columns

* match_id
* timestamp
* startclock
* sight_of
* num_steps
* role_1
* player_1
* player_1_score
* role_2
* player_2
* player_2_score

Example:

| match_id | timestamp | startclock | sight_of | num_steps | role_1 | player_1 | player_1_score | role_2 | player_2 | player_2_score |
| :----: | :----: | :----: | :----: | :----: | :----: | :----: | :----: | :----: | :----: | :----: |
| match_0 | 1588752755208 | 5 | RANDOM | 29 | red | RANDOM | 100 | black | RANDOM | 0 |


