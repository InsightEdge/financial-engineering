#!/bin/bash

source ./kafka-settings.sh

#readonly symbols_file=/tmp/41_symbols.csv
readonly symbols_file=../resources/NYSE-41.txt

for line in $(cat ${symbols_file}); do
    # echo "$line" | sed 's/\,/ /;s/\.IDX//' | awk '{ print $1 }' | xargs echo "${kafka_home}/bin/kafka-topics.sh --create --zookeeper ${zookeeper_host}:${zookeeper_port} --replication-factor ${replication-factor} --partitions ${partitions} --topic"
    echo "$line" | sed 's/\,/ /;s/\.IDX//' | awk '{ print $1 }' | xargs echo "${kafka_home}/bin/kafka-topics.sh --delete --zookeeper ${zookeeper_host}:${zookeeper_port} --if-exists --topic"
done
