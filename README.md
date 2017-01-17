### Practical Financial Engineering with InsightEdge

#This is not a working demo yet

This repository contains working code for an [InsightEdge](http://insightedge.io) application that uses NYSE market data to continuously calculate the [Security Characteristic Line](https://en.wikipedia.org/wiki/Security_characteristic_line) for 41 stocks.

![Demo Architecture](docs/images/demo-flow.png)

***XXX*** A detailed description of the application is given in [this blogpost](#todo).

#### Requirements    ***XXX*** link TOC
* Java 1.8
* Scala 2.10
* Kafka 2.10-0.8.2.2
* InsightEdge 1.1.0-SNAPSHOT
* Either Maven 3.1 OR SBT 0.13.9+
* Development license (optional)

#### Demo Recipe

1. Setup InsightEdge
2. Setup Kafka
3. Build
4. Deploy
5. Track Ticker Symbols
6. Start Spark Jobs
7. Start Kafka Stream
8. View Results

##### Setup InsightEdge

1. Download from [http://insightedge.io](http://insightedge.io)
2. Unzip. In this document, we will refer to the unzipped directory as `IE_HOME`.
3. (optional) Copy `ie-license.txt` into `IE_HOME`. 

##### Setup Kafka

Follow [these quickstart instructions](http://kafka.apache.org/quickstart). In this document, we will refer to the unzipped directory as `KAFKA_HOME`. 

Instead of - or in addition to - creating a topic named `test`, we will create a topic for each stock in the dataset, using its NYSE ticker symbol for the topic name.

The list of symbols is in [this file](src/main/resources/NYSE-ticker-symbols.txt), which can be read by [this script](src/main/bash/add-topics.sh) to create the topics.

##### Build

InsightEdge jars are not published to Maven Central Repository yet. To install artifacts to your local Maven repository, make sure you have Maven installed and then run the following from InsightEdge directory:
```bash
# Linux:
./sbin/insightedge-maven.sh

# Windows:
sbin\insightedge-maven.cmd
```

This project has both SBT and Maven build files. You can build it as follows:

```bash
# Maven
mvn clean test package

# SBT
sbt clean test assembly
```

##### Deploy

Prior to executing example application, you have to start the Data Grid and deploy an empty space on it. You can do it using `demo` mode:
```bash
# Linux:
./sbin/insightedge.sh --mode demo

# Windows:
sbin\insightedge.cmd --mode demo
```

Deployed resources:
  
* Spark master at `spark://127.0.0.1:7077` and Spark slave
* Data Grid manager and two `1GB` containers
    - `insightedge-space` Space is deployed 
    - lookup locator is `127.0.0.1:4174`
    - lookup group is `insightedge`

##### Track ticker symbols

To simulate trading action, we read data at an a appropriate rate from csv files that have the ticker symbol for the respective security in the filename.
 
To tell the system which symbols to 'follow', we write those ticker symbols to the Data Grid. 

This section describes how to set up such data on your system.

1. Create a symbol file - or edit [this one]()

2. Change [Settings.scala](), in particular, you might want to change:

[tickerSymbolsFilename]() - a file containing ticker symbols of interest (will be added to the system if not already present)
[tickerSymbolLimit]() - read only the first number of lines form the symbol file
[marketDataFileDirectory]() - location where market data csv files are stored

*You need to recompile after changing these Settings.*

3. Run AddTickerSymbolsFromFile 

4. Place market data files into the `marketDataFileDirectory`:

Each one should be named `[ticker symbol].csv`, where `ticker symbol` is the one you added in step 3.

##### Start Spark Jobs

##### Start Kafka Stream

###### Running from command line

You can build the project and submit examples as Spark applications with the next command:
```bash
# Linux:
./bin/insightedge-submit --class {main class name} --master {Spark master URL} \
    {insightedge-examples.jar location} \
    {Spark master URL} {space name} {lookup group} {lookup locator}

# Windows:
bin\insightedge-submit --class {main class name} --master {Spark master URL} ^
    {insightedge-examples.jar location} ^
    {Spark master URL} {space name} {lookup group} {lookup locator}
```

For example, `SaveRDD` can be submitted with the next syntax:
```bash
# Linux:
./bin/insightedge-submit --class org.insightedge.examples.basic.SaveRdd --master spark://127.0.0.1:7077 \
    ./quickstart/scala/insightedge-examples.jar \
    spark://127.0.0.1:7077 insightedge-space insightedge 127.0.0.1:4174

# Windows:
bin\insightedge-submit --class org.insightedge.examples.basic.SaveRdd --master spark://127.0.0.1:7077 ^
    quickstart\scala\insightedge-examples.jar ^
    spark://127.0.0.1:7077 insightedge-space insightedge 127.0.0.1:4174
```

If you are running local cluster with default settings (see [Running Examples](#running-examples)), you can omit arguments:
```bash
# Linux:
./bin/insightedge-submit --class {main class name} --master {Spark master URL} \
    {insightedge-examples.jar location}

# Windows:
bin\insightedge-submit --class {main class name} --master {Spark master URL} ^
    {insightedge-examples.jar location}
```

## Troubleshooting

If you have any trouble running the example applications, please, contact us with:
- Slack channel using [invitation](http://insightedge-slack.herokuapp.com/)
- StackOverflow [insightedge tag](http://stackoverflow.com/questions/tagged/insightedge)
- contact form at [InsightEdge main page](http://insightedge.io/)
- or [email message](mailto:hello@insightedge.io)