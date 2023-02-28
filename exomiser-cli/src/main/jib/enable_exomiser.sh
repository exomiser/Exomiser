#!/bin/bash

# Assumes `java` is on PATH in the base image.
alias exomiser="exec java $JAVA_OPTS -cp $( cat /app/jib-classpath-file ) $( cat /app/jib-main-class-file )"
