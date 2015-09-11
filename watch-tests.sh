#!/bin/sh

./run-tests.sh
while inotifywait -e close_write out/tests.js;
do
  ./run-tests.sh;
done
