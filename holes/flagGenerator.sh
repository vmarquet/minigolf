#!/bin/bash

# the goal of this script is to automatically generate the lines used in the hole.txt files
# which describe the coordinates of the flag and it's stake
# for that, you only need the position of the base of the flag and this script build it
# to use this script on Linux, enter in command line: ./flagGenerator.sh
# you can send the script's response directly to the file: ./flagGenerator.sh >> hole.txt

read -p "Enter the abscissa of the base of the flag: " x
read -p "Enter the ordonate of the base of the flag: " y

n1=$[ $y + 9 ]
n2=$[ $x + 1 ]
n3=$[ $y + 8 ]
n4=$[ $y + 7 ]

echo "$x,$y $x,$n1 $x,$y flagStake RED # mât du drapeau"
echo "$x,$n1 $n2,$n3 $x,$n4 flag RED # drapeau"


exit 0