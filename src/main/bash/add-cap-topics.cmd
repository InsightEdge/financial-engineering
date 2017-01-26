for %%t in (AA AAPL AIG AXP BA BAC C CAT CXCO CVX DD DIS GE GM GS GT HD HON HPQ IBM INTC IP JNJ JPM KO MCD MDLZ MMM MO MRK MSFT NKE PFE PG T TRV UNH UTX V VZ WMT XOM) do %KAFKA_HOME%\bin\windows\kafka-topics.bat --create --zookeeper localhost --if-not-exists --replication-factor 1 --partitions 1 --topic %%t

