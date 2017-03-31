### Practical Financial Engineering with InsightEdge

#This is not a working demo yet

This repository contains working code for an [InsightEdge](http://insightedge.io) application that uses NYSE market data to continuously calculate the [Security Characteristic Line](https://en.wikipedia.org/wiki/Security_characteristic_line) for 41 stocks.

![Demo Architecture](docs/images/demo-flow.png)

####Requirements

* Java 1.8 (OSX 1.8.0_60-b27 used)
* Kafka 2.11-0.10.2.0
* SBT 0.13.13
* XAP 12.0.1 (premium build 16611 used)
* InsightEdge 1.0.0 (community used)
* Development license (optional)

####Demo Steps

1. Initial setup
2. Install, Start Data Grid
3. Install, Start Kafka 
4. Install, Start InsightEdge
5. Build, Deploy Processing Unit

6. Run Demo Setup Apps

7. Start Kafka Feed
8. Submit Spark Jobs
9. View Results

#####Initial Setup

This demo recipe is semi-automated. You can copy-paste most of the commands directly into terminal assuming you follow this initial setup section. Note that these commands were tested on a Mac.

Create a working directory.
 
```bash
$ export WORKING_DIRECTORY=/full/path/to/your/working/directory/without/trailing/slash
$ mkdir -p ${WORKING_DIRECTORY}
$ cp /path/to/xap-license.txt ${WORKING_DIRECTORY}
```  

#####Install, Start Data Grid

* Download XAP from [this link](???)
* Copy `xap-license.txt` into `XAP_HOME`.

```bash
# Download XAP...
$ cd ${WORKING_DIRECTORY}
$ unzip ~/Downloads/gigaspaces-xap-premium-12.0.1-ga-b16611.zip
$ export XAP_HOME="${WORKING_DIRECTORY}/gigaspaces-xap-premium-12.0.1-ga-b16611"
$ cd ${XAP_HOME}
$ mv ../xap-license.txt .
$ export XAP_LOOKUP_LOCATORS=localhost ; ./gs-agent.sh gsa.gsc 4
# in another term session...
$ . ${XAP_HOME}/bin/gs-ui.sh
```

#####Install, Start Kafka

Follow [these quickstart instructions](http://kafka.apache.org/quickstart). In this document, we will refer to the unzipped directory as `KAFKA_HOME`. 

Steps 1-2 are sufficient. Steps 3-5 are useful for verifying that the queue is operational...
```bash
# from the kafka install directory...
export KAFKA_HOME=$(pwd)
```

#####Install, Start InsightEdge

* Download from [http://insightedge.io](http://insightedge.io)
```bash
$ cd ${WORKING_DIRECTORY}
$ unzip ~/Downloads/gigaspaces-insightedge-1.0.0-community.zip
$ export IE_HOME="${WORKING_DIRECTORY}/gigaspaces-insightedge-1.0.0-community"
$ cd ${IE_HOME}
$ ./sbin/insightedge.sh --mode demo 
```
(Side note: There will now be two LUS running...)

#####Build, Deploy Processing Unit

```bash

$ cd ${WORKING_DIRECTORY}
$ git clone https://github.com/InsightEdge/financial-engineering
$ cd financial-engineering
$ export FE_SRC_HOME=$(pwd)
$ sbt assembly
$ find . -type f -name "*.jar"

./core/target/scala-2.10/core.jar
./demoSetup/target/scala-2.10/setup.jar
./processingUnit/target/scala-2.10/demoPU.jar
./sparkJobs/target/scala-2.10/sparkjobs.jar
./target/scala-2.10/financial-engineering-assembly-1.0.0.jar
./web/target/scala-2.10/web-assembly-1.0.0.jar

$ cd ${XAP_HOME} 
$ export XAP_LOOKUP_LOCATORS=localhost ; ./bin/gs.sh deploy ${FE_SRC_HOME}/processingUnit/target/scala-2.10/demoPU.jar

```

######Run Demo Setup Apps
#TODO

######Start Kafka Feed
# TODO

######View Results
# TODO

######Submit Spark Jobs


```bash
./bin/insightedge-submit --class org.insightedge.examples.financialengineering.jobs.Ingest \
   --master spark://127.0.0.1:7077 /tmp/financial-engineering.jar
./bin/insightedge-submit --class org.insightedge.examples.financialengineering.jobs.Ingest ^ 
   --master spark://127.0.0.1:7077 C:\\TEMP\financial-engineering.jar
./bin/insightedge-submit --class org.insightedge.examples.financialengineering.jobs.CalcIndividualReturns \
   --master spark://127.0.0.1:7077 /tmp/financial-engineering.jar
```

## Troubleshooting

If you have any trouble running the demo, please contact us at any of the following:
- Slack channel using [invitation](http://insightedge-slack.herokuapp.com/)
- StackOverflow [insightedge tag](http://stackoverflow.com/questions/tagged/insightedge)
- contact form at [InsightEdge main page](http://insightedge.io/)
- or [email message](mailto:hello@insightedge.io)