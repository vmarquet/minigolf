#!/bin/bash

# colors for terminal output
NORMAL="\e[0;39m"
BLUE="\e[1;34m"

# we create a build directory to avoid melting .java and .class files
if [[ ! -d build ]]; then
	mkdir build
fi

# compilation
COMPIL="javac -cp .:./lib/* Minigolf.java -d build"  # compilation command
echo -e "${BLUE}Compilation: $COMPIL ${NORMAL}"  # terminal message
eval "$COMPIL"  # we launch the compilation

# we check if the compilation was successful, and if yes, we launch the program
if [[ $? -eq 0 ]]; then
	EXEC="java -cp .:./build:./lib/* Minigolf"  # execution command
	echo -e "${BLUE}Execution: $EXEC ${NORMAL}"  # terminal message
	eval "$EXEC"  # we execute the program
fi

exit 0
