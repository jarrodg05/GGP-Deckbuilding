#!/bin/bash

MATCH_ID="match"
GAMEFILE="testdata/games/games_gdl/connectFour/connectFour.kif"
START_CLOCK=5
PLAY_CLOCK=5
GDL_VERSION=1
PLAYER_NAME_1="random"
ROLE_ID_1=1
PLAYER_NAME_2="mcs"
ROLE_ID_2=2
OUTPUT_DIR="matches/"
STYLESHEET="../../stylesheets/2player_normal_form/2player_normal_form.xsl"
NUMTESTS=10

for ((i=0 ; i < NUMTESTS ; i++));
do
    match_id_temp="${MATCH_ID}_${i}"
    java -jar gamecontroller-cli.jar $match_id_temp $GAMEFILE $START_CLOCK $PLAY_CLOCK $GDL_VERSION -$PLAYER_NAME_1 $ROLE_ID_1 -$PLAYER_NAME_2 $ROLE_ID_2 -printxml $OUTPUT_DIR $STYLESHEET
done

python tests/testAgent/parseXML.py $OUTPUT_DIR "${MATCH_ID}_" $NUMTESTS

$SHELL