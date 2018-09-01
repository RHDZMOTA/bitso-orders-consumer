# Bitso Trades Consumer

This is a scala application that consumes real-time orders from the
[bitso exchange](https://bitso.com/?l=en). 

Bitso exposes a [web-socket API](https://bitso.com/api_info/?shell#websocket-api) 
for live data retrieval. In this application we consume the trades 
of the following coin pairs:
* btc/mxn
* eth/mxn
* xrp/mxn
* ltc/mxn
* bch/mxn
* tusd/mxn

## Dependencies

You'll need **sbt** (scala build tool) to run/compile this project.
We use the **Akka Toolkit** (e.g., streams, http, actors) to generate
a websocket connection with Bitso and create a stream for 
decoding the data. We use **Circe** for decoding the json text 
messages into Scala objects and **Alpakka** for connecting to 
the database (sink).

* [Scala 2.12.x](https://www.scala-lang.org/)
* [SBT 1.1.6](https://www.scala-sbt.org/)
* [Akka Toolkit](https://akka.io/)
* [Alpakka](https://developer.lightbend.com/docs/alpakka/current/)
* [Circe](https://circe.github.io/circe/)

## Usage
Consider editing the `application.conf` file according to your needs. 

Instructions:
1. Clone this repository and enter to the project dir: 
    * `git clone https://github.com/rhdzmota/bitso-orders-consumer.git`
    * `cd bitso-trades-consumer`
2. Edit the configuration file if needed.
    * `vim src/main/resources/application.conf`
3. Run the application:
    * Using SBT: `sbt run`
    * Using java:
        * Create a fat-jar: `sbt assembly`
        * Run using `java -jar`
        
Note that the resulting `jar` is on the `target/scala-*` directory.
To execute the application with java use something similar to: 
```bash
java -jar target/scala-2.12/bitso-orders-consumer-assembly-1.0.0.jar
```  

## Databases

Currently there's only support for Cassandra Database. Although 
support for any other database should be straightforward (see the
`com.rhdzmota.service.database` package). 

### Cassandra

Open the Cassandra CLI with `cqlsh` on a terminal 
and create the keyspace and table: 

* Create the keyspace:
    * `CREATE KEYSPACE bitso WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1};`
* Create the table: 
```text
CREATE TABLE bitso.orders (
  received timestamp,
  arrival int,
  book text,
  sequence bigint,
  id text,
  milliseconds bigint,
  rate double,
  maker_action text,
  status text,
  amount double,
  value double,
  PRIMARY KEY (received, arrival, book, sequence, id, milliseconds)
)
```

## Authors and contribution
Feel free to contribute by opening an issue or sending a PR.

Authors:
* [Rodrigo Hernández Mota](https://www.linkedin.com/in/rhdzmota/)

## License

See the `LICENSE.md` file. 
