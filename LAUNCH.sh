#!/bin/bash
# if this script does not start, on Linux do: chmod +x launch.sh

# if we want to compile always before launch, uncomment the following line:
# javac -cp ./lib/*:. Minigolf.java

# if the Minigolf.class file does not exists, we must compile:
if [ -f Minigolf.class ]; then
  javac -cp ./lib/*:. Minigolf.java
fi

# launch:
java -cp ./lib/*:. Minigolf

exit 0
