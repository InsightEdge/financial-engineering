### Practical Financial Engineering with InsightEdge

#This is not a working demo yet

This repository contains working code for an [InsightEdge](http://insightedge.io) application that uses NYSE market data to continuously calculate the [Security Characteristic Line](https://en.wikipedia.org/wiki/Security_characteristic_line) for 41 stocks.

![Demo Architecture](docs/images/demo-flow.png)



####Requirements    ***XXX*** link TOC
* Java 1.8
* Scala 2.10
* Kafka 2.10-0.8.2.2
* InsightEdge 1.0.0
* Either Maven 3.1 OR SBT 0.13.9+
* XAP >= 12.0
* Development license (optional)

####Demo Recipe

1. Install InsightEdge
2. Install Kafka
3. Install Data Grid
4. Build
5. Setup Kafka Data
6. Start Kafka Feed
7. Start Data Grid
8. Deploy Processing Unit
9. Start InsightEdge
10. Submit Spark Jobs
11. Deploy Space
12. Demo setup
13. Run demo
14. View Results

#####Install InsightEdge

* Download from [http://insightedge.io](http://insightedge.io)
* Unzip. In this document, we will refer to the unzipped directory as `IE_HOME`.
* (optional) Copy `ie-license.txt` into `IE_HOME`. 

#####Install Kafka

Follow [these quickstart instructions](http://kafka.apache.org/quickstart). In this document, we will refer to the unzipped directory as `KAFKA_HOME`. 

Instead of - or in addition to - creating a topic named `test`, we will create a topic for each stock in the dataset, using its NYSE ticker symbol for the topic name.

The list of symbols is in [this file](src/main/resources/NYSE-ticker-symbols.txt), which can be read by [this script](src/main/bash/add-topics.sh) to create the topics.

#####Install Data Grid

* Download XAP from [this link](???)
* unzip. The location of the unzipped directory will be referred to as `XAP_HOME` in this document.

#####Build

The commands at the end of this section creates a jar file containing each of the following items.

They produce the following:
   
######Applications

* **Feed**
An application that writes tick data to the Kafka queue (discussed below).
* **AddTickerSymbolsFromFile**
Reads stock symbols from a "ticker symbol file". For each symbol, a record is created in the Data Grid that controls how many of each type of Spark Jobs is created for that symbol.  
* **PopulateTBills**
Generates TBill records in the Data Grid (simulates 3-mo US Treasury yields).
* **ResetTickerSymbolThreadCounts**
Convenience utility to update all TickerSymbols' thread counts to zero.

######Spark Jobs

* **Ingest** 
Reads tick data of a Kafka queue and writes it as a MarketTick into the Data Grid. 
* **CalcIndividualReturns** 
Calculates CAGR for each MarketTick, as is arrives from Kafka. These are calculated against the month-ago data and represented as `Investment`s. Finally, an `InvestmentReturn` is written to the DataGrid.
* **CalcMarketReturns** 
Calculates CAGR for the entire market by averaging across `IndividualReturn`s. (This would usually be provided as part of a real-world market feed for a broad index like the S&P500.) 

######Data Grid Application

The jar itself will be deployed as a Processing Unit on the Data Grid.

######Build instructions

InsightEdge jars are not yet staged on a public repository, but the jars can be deployed directly from the InsightEdge installation:

```bash
FROM IE_HOME

# Linux:
./sbin/insightedge-maven.sh

# Windows:
sbin\insightedge-maven.cmd
```

This project has SBT and Maven build files. They produce the same output.

```bash
# Maven
mvn clean test package

# SBT
sbt clean test assembly

TODO: fix the damn build

cp target/financial-engineering.jar /tmp # so that the rest of the commands in this document work without modification...
```

#####Setup Kafka Data
     
Code for this demo provides a Feed application that loads historical market data into the Data Grid. We are not allowed to share the original data file that we purchased from [QuantQuote](http://quantquote.com).
     
* Work-arounds      
    * Buy data from [QuantQuote](http://quantquote.com) and unzip your order into /tmp/marketdata.
    * Write a Kafka feeder that reads from another datasource.
    * Convert existing data into the format described below.
    * Generate data, using the format described below.
       
* Format      
    * Base directory for data is at `/tmp/marketdata` (this directory can be changed in [Settings.scala](src/main/scala/org/insightedge/examples/financialengineering/Settings.scala))
    * Sub-directories named `allstocks_[date string]` exist for each date that has data. [date string] takes the form YYYYMMDD.
    * Each sub-directory contains csv files called `table_[symbol].csv`, where [symbol] is a lowercase symbol in the "ticker symbol file" (more on how it's used later).
    * Each file has data in the following form:
     
`date | time | open | high | low | close | volume | splits | earnings | dividends`
     
`date` has the same form as "date string", above. `time` is an integer representing a clock time: e.g. 800 => 8:00 a.m., 1204 => 12:04 p.m.


#####Demo setup

######Track ticker symbols

To simulate trading action, we read data from csv files for a given ticker symbol.
 
To tell the system which symbols to 'follow', we write those ticker symbols to the Data Grid. 

This section describes how to set up such data on your system.

* Create a symbol file - or edit [this one](setup/src/main/resources/cap-symbols.txt)
* Change [Settings.scala](src/main/scala/org/insightedge/examples/financialengineering/Settings.scala), in particular, you might want to change:
[tickerSymbolsFilename](src/main/scala/org/insightedge/examples/financialengineering/Settings.scala#L48) - a file containing ticker symbols of interest (will be added to the system if not already present)
[tickerSymbolLimit](src/main/scala/org/insightedge/examples/financialengineering/Settings.scala#L47) - read only the first number of lines form the symbol file (track only this many Stock symbols)
[feedDataDirectory](src/main/scala/org/insightedge/examples/financialengineering/Settings.scala#L41) - location where market data csv files are stored
*You need to recompile if you change these Settings.*
* Run **AddTickerSymbolsFromFile** 

```bash
java -cp /tmp/financial-engineering.jar org.insightedge.examples.financialengineering.applications.AddTickerSymbolsFromFile
```
######Start Kafka Feed

As referenced above, see: [Quickstart Instructions](http://kafka.apache.org/quickstart)

######Start Data Grid

```bash 
   cd $XAP_HOME
   ./bin/gs-agent.sh gsa.lus 1 gsa.global.lus 0 gsa.gsm 1 gsa.global.gsm 1 gsa.gsc 4
   # then, in another shell session
   ./bin/gs-webui.sh
```

######Deploy Processing Unit

* Go to: http://localhost:8099
* Deploy /tmp/financial-engineering.jar as a ProcessingUnit with partitioned, 2 paritions,1 backup 

######Start InsightEdge

```bash
# Linux:
./sbin/insightedge.sh --mode master --master localhost --locator localhost:7102

# Windows:
sbin\insightedge.cmd --mode master --master localhost --locator localhost:7102
```
**_Note_** `localhost` should be replaced with the host of the Spark Master after `--master` (if different from localhost) and the host of the Lookup Service after `--locator` (if different from localhost)

Deployed resources:
  
* Spark master at `spark://{value passed to --master}:7077` and Spark slave
* Data Grid 
    
**_Note_** We rely upon advanced Data Grid features, and therefore choose to point at a running Lookup Service for the XAP Data Grid installed above.

######Submit Spark Jobs

* **_Ingest_** Retrieves stock tick data from Kafka and writes `TickData` to the Data Grid.

```bash
./bin/insightedge-submit --class org.insightedge.examples.financialengineering.jobs.Ingest \
   --master spark://127.0.0.1:7077 /tmp/financial-engineering.jar
```

```
bin\insightedge-submit --class org.insightedge.examples.financialengineering.jobs.Ingest ^ 
   --master spark://127.0.0.1:7077 C:\\TEMP\financial-engineering.jar
```

* **_CalcIndividualReturns_** For each `TickData` created by `Ingest`, calculates [CAGR](https://en.wikipedia.org/wiki/Compound_annual_growth_rate) versus the Stock's month-ago `TickData`. An `InvestmentReturn` is written to the Data Grid for every `TickData`.

```bash
./bin/insightedge-submit --class org.insightedge.examples.financialengineering.jobs.CalcIndividualReturns \
   --master spark://127.0.0.1:7077 /tmp/financial-engineering.jar
```

```
bin\insightedge-submit --class org.insightedge.examples.financialengineering.jobs.CalcIndividualReturns ^ 
   --master spark://127.0.0.1:7077 C:\\TEMP\financial-engineering.jar
```

* **_CalcMarketReturns_** For each `InvestmentReturn` created by `CalcIndividualReturns`, calculates average [CAGR](https://en.wikipedia.org/wiki/Compound_annual_growth_rate) for all new `InvestmentReturn`s.

```bash
./bin/insightedge-submit --class org.insightedge.examples.financialengineering.jobs.CalcMarketReturns \
   --master spark://127.0.0.1:7077 /tmp/financial-engineering.jar
```
```
bin\insightedge-submit --class org.insightedge.examples.financialengineering.jobs.CalcMarketReturns ^ 
   --master spark://127.0.0.1:7077 C:\\TEMP\financial-engineering.jar
```

## Troubleshooting

If you have any trouble running the demo, please contact us at any of the following:
- Slack channel using [invitation](http://insightedge-slack.herokuapp.com/)
- StackOverflow [insightedge tag](http://stackoverflow.com/questions/tagged/insightedge)
- contact form at [InsightEdge main page](http://insightedge.io/)
- or [email message](mailto:hello@insightedge.io)