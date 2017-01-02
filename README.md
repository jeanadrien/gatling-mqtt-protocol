# Gatling-MQTT-Protocol

_Gatling-MQTT-Protocol_ is an unofficial plugin for the [Gatling](http://gatling.io) load testing framework which enables 
to run performance tests using the [MQTT](http://mqtt.org/) pub/sub protocol.

It is based on _Fusesource_ MQTT Java [client](https://github.com/fusesource/mqtt-client)

## Installation

Locally, build and add the plugin into the `lib` directory of your Gatling installation

1. Checkout the code, and build the plugin using [sbt](http://www.scala-sbt.org/)

```bash
$ sbt assembly
```

2. Copy the _jar_ built into your Gatling library directory

```bash
$ cp target/scala-2.11/gatling-mqtt-protocol-assembly-{VERSION}.jar ${GATLING_HOME}/lib`
```

It is also possible to use the plugin on [Flood.io](https://flood.io/). 
Please refer to the ad-hoc [documentation](https://help.flood.io/docs/custom-libraries-on-grid-nodes) on how to
add custom library to Flood IO.

## Documentation

### Quickstart

1. Import the following packages into your Simulation file

```scala
import com.github.jeanadrien.gatling.mqtt.Predef._
import io.gatling.core.Predef._
import scala.concurrent.duration._
```

2. Configure your MQTT Clients.

```scala
val mqttConf = mqtt.host("tcp://localhost:1883")
```

3. Describe your _Scenario_ in your simulation file.

```scala
val scn = scenario("MQTT Test")
            .exec(connect)
            .exec(subscribe("myTopic"))
            .during(20 minutes) {
                pace(1 second).exec(publish("myTopic", "myPayload"))
            }
setUp(
    scn.inject(rampUsers(5000) over (10 minutes)))
    .protocols(mqttConf)
```

The above _Scenario_ will connects up to 5000 MQTT clients to your localhost MQTT server at a rate of 500 clients per minute.
Each connected client will _SUBSCRIBE_ to `myTopic` and then perform one _PUBLISH_ on that same `myTopic` topic each
second. Therefore, the resulting performance test ramps up from 0 to 300k _PUBLISH_ rpm. 

### MQTT Protocol configuraion

These options are configurable using _method chaining_ on the `mqtt` object. They are wrappers on to the _Fusesource MQTT-Client_ 
options: https://github.com/fusesource/mqtt-client#controlling-mqtt-options.

* `host(host: Expression[String])` :
* `clientId(clientId: Expression[String])` : Default is a random String, and it's probably a good choice in the context
 of load tests, since client ID must be unique.
* `cleanSession(cleanSession: Boolean)` : Default is true
* `keepAlive(keepAlive: Short)` : Interval in seconds between client _PING_ messages. Default is 30
* `userName(userName: Expression[String])` :
* `password(password: Expression[String])` :
* `willTopic(willTopic: Expression[String])` :
* `willMessage(willMessage: Expression[String])` :
* `willQos(willQos: QoS)` :
* `willRetain(willRetain: Boolean)` :
* `version(version: Expression[String])` : Default is 3.1
* `connectAttemptsMax(connectAttemptsMax: Long)` :
* `reconnectAttemptsMax(reconnectAttemptsMax: Long)` :
* `reconnectDelay(reconnectDelay: Long)` :
* `reconnectDelayMax(reconnectDelayMax: Long)` :
* `reconnectBackOffMultiplier(reconnectBackOffMultiplier: Double)` :
* `receiveBufferSize(receiveBufferSize: Int)` :
* `sendBufferSize(sendBufferSize: Int)` :
* `trafficClass(trafficClass: Int)` :
* `maxReadRate(maxReadRate: Int)` :
* `maxWriteRate(maxWriteRate: Int)` :

### Scenario actions

_Gatling-MQTT-Protocol_ plugin provides the following scenario actions:

* `connect` : _CONNECT_ MQTT command. Must run at the beginning of the scenario.
* `subscribe(topic : Expression[String])` : _SUBSCRIBE_ MQTT command. Client subscribes to one topic.
* `publish(topic : Expression[String], payload : Expression[Array[Byte]])` : _PUBLISH_ MQTT command. Client publishes
the given payload on the given topic.

Additionally to this `publish` (and forget) action. The plugin provides two additional publish actions which expects
an echo from the MQTT server. This allows to measure the round-trip performances of the MQTT server.
Note that the client __must__ subscribe to the topic first, otherwise the actions will fail with a _timeout_.

* `publishAndWait(topic : Expression[String], payload : Expression[Array[Byte]])` : _PUBLISH_ MQTT command. The action
does not call the next chained action until the client listener receives the payload. 
* `publishAndMeasure(topic : Expression[String], payload : Expression[Array[Byte]])` : _PUBLISH_ MQTT command. Unlike
the _andWait_ version, the next chained action is called immediately after the PUBLISH command. But the plugin waits
for the update notification from the server and measures the duration of the round-trip.

* Last, the plugin provides a `waitForMessages` action which does nothing but waits until all the pending `publishAndMeasure`
receives their notifications.

#### Connect options

The `connect` action provides the following options using _method chaining_. They are a subset of the protocol
configuration options described here above and they allow to have different client settings using e.g. a gatling _feeder_. 

* `clientId(clientId: Expression[String])` :
* `cleanSession(cleanSession: Boolean)` :
* `userName(userName: Expression[String])` :
* `password(password: Expression[String])` :
* `willTopic(willTopic: Expression[String])` :
* `willMessage(willMessage: Expression[String])` :
* `willQos(willQos: QoS)` :
* `willRetain(willRetain: Boolean)` :

#### Subscribe options

The `subscribe` action provides the following MQTT options using _method chaining_ :

* `qosAtMostOnce` : Ask for a _QoS_ of 0 (at most once) for the subscribed topic. 
* `qosAtLeastOnce` : Ask for a _QoS_ of 1 (at least once) for the subscribed topic.
* `qosExactlyOnce` : Ask for a _QoS_ of 2 (exactly once) for the subscribed topic.


#### Publish, publishAndWait, publishAndMeasure options

The `publish`, `publishAndWait` and `publishAndMeasure` actions provide the following options using _method chaining_

* `qosAtMostOnce` : Publish with a _QoS_ of 0 (at most once).
* `qosAtLeastOnce` : Publish with a _QoS_ of 1 (at least once).
* `qosExactlyOnce` : Publish with a _QoS_ of 2 (exactly once).
* `retain(newRetain : Boolean)` : Set the retain flag of the _PUBLISH_ command. Default: false.

Additionally `publishAndWait` and `publishAndMeasure` provides useful options to define how to _validate_ the
feedback notification receivied from the server.

* `payloadFeedback(fn : Array[Byte] => Array[Byte] => Boolean)` : Define the comparison function to use when notifications
are received on the subscribed topic. The default function compares each byte of the payload.
* `timeout(duration : FiniteDuration)` : Timeout before failure.

#### WaitForMessages options

* `timeout(duration : FiniteDuration)` : Timeout duration.

### Note about metrics

The time measured for the different actions is:

* `connect` : Time to get connected CONACK. Note that the connection is automatically retried, and a single _connect_
action an potentially lead to several _KO_ requests in the statistics.
* `subscribe` : Time to get the SUBACK message back from the server
* `publish` : Time to perform the full PUBLISH negotiation, it depends of the selected QoS. 
* `publishAndWait` : Time to perform the publish and receive the notification on the topic. Upper bound is the configured _timeout_
* `publishAndMeasure` : Time to perform the publish and receive the notification on the topic. Upper bound is the configured _timeout_

## Examples

You can find Simulation examples in the [test directory](test/scala)

## Compatibility

Here is the _Gatling-MQTT-Protocol_ vs. _Gatling_ version compatibility table.
Older versions of Gatling are not supported.

* [v1.0] is built with Gatling sources _v2.2.3_. 

## Acknowledgments

_Gatling-MQTT-Protocol_ is a rewrite of [Gatling-MQTT](https://github.com/mnogu/gatling-mqtt) plugin which provides a mqtt connect+publish
stress test also based on _Fusesource_ MQTT Client. 

This extended plugin increases the flexibility in the MQTT scenario. It enables MQTT actions and thus it allows measurement 
 of server performance at a command level. It also provides tools to customize 
 the behaviour of the MQTT clients during the performance tests. 

The protocol configuration DSL is widely compliant with by _Gatling-MQTT_

* Thanks to [@verakruhliakova](https://github.com/verakruhliakova) who wrote the initial version for Gatling 2.1.
* Thanks to [EVRYTHNG](https://evrythng.com/) for the use cases and the testing.

## License

Apache License, Version 2.0