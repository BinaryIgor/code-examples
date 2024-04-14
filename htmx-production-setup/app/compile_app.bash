#!/bin/bash

mvn_classpath=$(mvn dependency:build-classpath)
echo $mvn_classpath
echo

find ./src/ -type f -name "*.java" > sources.txt

javac -cp "$mvn_classpath" -d ./out/ @sources.txt
#  src/main/java/com/binaryigor/htmxproductionsetup/*.java \
#  src/main/java/com/binaryigor/htmxproductionsetup/shared/*.java \
#  src/main/java/com/binaryigor/htmxproductionsetup/auth/*.java