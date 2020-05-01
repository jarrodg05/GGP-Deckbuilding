# GGP-GameController

## Preface

This project has been developed as part of the Master of Professional Engineering (software) research project at the University of Western Australia.

Building primarily on the research done by Michael Thielscher to further research on General Game Playing (GGP) in incomplete information games.

The base code to run the basic agents (legal, random) came from combining code from the following resources:<br>
* https://github.com/arnaudsj/ggpserver
* https://sourceforge.net/projects/ggpserver/files/gamecontroller/

## How to Build (Original instructions from )

To build GameController run:

$ ant -f my-build.xml

This will create three jar files:
- gamecontroller.jar - used by ggpserver
- gamecontroller-cli.jar - the command line version
- gamecontroller-gui.jar - a version with a simple gui

You can run the CLI and GUI versions of gamecontroller with
$ java -jar gamecontroller-???.jar

To use the stylesheets in the resources/ directory you have
to run GameController with the parameter "-printxml OUTPUTDIR XSLT"
where OUTPUTDIR is a directory in which GameController writes xml files,
one for each state of the match and XSLT is a relative or absolute URL
to some xslt stylesheet that is referenced from the state xml files.
It's a bit tricky to get the directories right, because image references
in the xslt have to be relative to the state xml. The xsl files in the 
resources directory assume that OUTPUTDIR is in the same directory as the
stylesheets directory.<br>
E.g., the following directory structure and parameter should work:

./matches/  # a directory for the xml files<br>
./stylesheets/ # the stylesheets from resources directory

$ java -jar gamecontroller-cli.jar SomeMatchID bidding-tictactoe.gdl 120 30 \
	-remote 2 MyPlayer localhost 4000 \
	-printxml matches/ ../../stylesheets/bidding_tictactoe/bidding_tictactoe.xsl

This should generate the following files:

./matches/SomeMatchID/step_*.xml<br>
./matches/SomeMatchID/finalstate.xml

Just open any of the xml files in a web browser that supports xslt (the stylesheets
are known to work with Firefox 3).<br>
If you open local files you might have to change settings in Firefox, because the URL
to the stylesheets contains "..". Type "about:config" in the address bar and change
the following setting:<br>
security.fileuri.strict_origin_policy=false

## How to run the players

### GUI

1. Run the following command
    ```$xslt
    $ java -jar gamecontroller-gui.jar
    ```
2. Select a valid gdl game and select the version
3. Choose the players (only works for local players)
4. View the results

### CLI

This method allows the running of multiple games through scripting and outputs the results in xml format for automatic parsing.

Run the following command:

```
java -jar gamecontroller-cli.jar MATCH_ID GAMEFILE START_CLOCK PLAY_CLOCK GDL_VERSION -PLAYER_NAME_1 ROLE_ID_1 -PLAYER_NAME_2 ROLE_ID_2 -printxml OUTPUT_DIR/ ../../stylesheets/2player_normal_form/2player_normal_form.xsl
```

Where:

* MATCH_ID - Unique ID for each match
* GAMEFILE - A valid GDL or GDL-II game (ending in .kif or .gdl)
* START_CLOCK - The length in seconds before the game begins
* PLAY_CLOCK - The length in seconds given to each player on every turn
* GDL_VERSION - An integer representing the version of GDL the GAMEFILE uses (GDL -> 1, GDL-II -> 2)
* -PLAYER_NAME - A valid name given to the player implemented (instructions below)
* ROLE_ID - An integer representing the role of the player (>=1)
* -printxml OUTPUT_DIR/ ../../stylesheets/2player_normal_form/2player_normal_form.xsl - Outputs the results of the game to OUTPUT_DIR for a generic two player normal form game (use when the GAMEFILE doesn't match any other stylesheet)

## How to add an agent

### Add an agent for use through the GUI app

#### Required Files:

* XXXXPlayer.java
* XXXXPlayerInfo.java
* players\PlayerInfo.java
* players\PlayerFactory.java
* gui\PlayerType.java
* gui\PlayerTableModel.java
* gui\JPlayerTable.java

#### Method:

1. Create a new XXXXPlayer.java class from an existing LegalPlayer.java class and change the move selection as required

2. Create a new XXXXPlayerInfo.java class from an existing LegalPlayerInfo.java class and rename it

3. Add the line:
    ```public static final String TYPE_XXXX = "xxxx";```
     to the top of the PlayerInfo.java class

4. Add a createXXXPlayer function to PlayerFactory.java<br>
    e.g.
    ```$xslt
    public static <TermType extends TermInterface, StateType extends StateInterface<TermType, ? extends StateType>>
        Player<TermType, StateType> createXXXXPlayer(XXXXPlayerInfo info) {
            return newXXXX Player<TermType, StateType>(info.getName(), info.getGdlVersion());
        }
    ```
				
5. Extend the else-if logic in the createPlayer function in PlayerFactory.java
    e.g.
    ```$xslt
    else if(info instanceof XXXXPlayerInfo){
    	return PlayerFactory. <TermType, StateType> createXXXXPlayer((XXXXPlayerInfo)info);
    }
    ```
				
6. Add XXXX to the PlayerType.java enum

7. Extend the else-if logic in the getPlayerInfo function in the PlayerTableModel.java class
    e.g.
	```$xslt
    else if(type.equals(PlayerType.XXXX)){
		return new XXXXPlayerInfo(row,GDLVersion.v1);
	}
    ```
8. Add PlayerType.XXXX to the PlayerTypeCellEditor constructor in the PlayerTypeCellEditor private class in the JPlayerTable.java class

### Add an agent for use through the CLI app

#### Required Files:

* XXXXPlayer.java
* XXXXPlayerInfo.java
* players\PlayerInfo.java
* players\PlayerFactory.java
* cli\AbstractGameControllerCLIRunner.java

#### Method:

1. Create a new XXXXPlayer.java class from an existing LegalPlayer.java class and change the move selection as required

2. Create a new XXXXPlayerInfo.java class from an existing LegalPlayerInfo.java class and rename it

3. Add the line:
    ```public static final String TYPE_XXXX = "xxxx";```
     to the top of the PlayerInfo.java class

4. Add a createXXXPlayer function to PlayerFactory.java<br>
    e.g.
    ```$xslt
    public static <TermType extends TermInterface, StateType extends StateInterface<TermType, ? extends StateType>>
        Player<TermType, StateType> createXXXXPlayer(XXXXPlayerInfo info) {
            return newXXXX Player<TermType, StateType>(info.getName(), info.getGdlVersion());
        }
    ```
				
5. Extend the else-if logic in the createPlayer function in PlayerFactory.java
    e.g.
    ```$xslt
    else if(info instanceof XXXXPlayerInfo){
    	return PlayerFactory. <TermType, StateType> createXXXXPlayer((XXXXPlayerInfo)info);
    }
    ```
				
6. Extend the else-if logic in the parsePlayerArguments funciton in AbstractGameControllerCLIRunner.java
    e.g.
	```$xslt
    else if(argv[index].equals("-xxxx")){
        ++index;
        if(argv.length>=index+1){
            int roleindex=getIntArg(argv[index], "roleindex"); ++index;
            if(roleindex<1){
                System.err.println("roleindex out of bounds");
                printUsage();
                System.exit(-1);
            }
            player=new XXXXPlayerInfo(roleindex-1, getGdlVersion());
        }else{
            missingArgumentsExit(argv[index-1]);
        }
    }
    ```