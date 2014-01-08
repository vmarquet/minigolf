#!/bin/bash

# the goal of this script is to generate rectangles for the hole.txt file
# you can add directly the result to the hole.txt file: ./rectangleGenerator.sh >> hole.txt
# but you will need to change the XX in groundXX to a unique number
# or use rand to generate random numbers

# it generate a rectangle with horizontal and vertical lines from four numbers:

read -p "Enter the ordonate of the higher horizontal line of the rectangle: " yh
read -p "Enter the ordonate of the lower horizontal line of the rectangle: " yl
read -p "Enter the abscissea of the left vertical line of the rectangle: " xl
read -p "Enter the abscissea of the right vertical line of the rectangle: " xr

# we start with the left-down point of the rectangle
# then we list the points in counter-clockwise order

echo "$xl,$yl $xr,$yl $xr,$yh $xl,$yh groundXX GREEN # "

exit 0
