import com.github.jeanadrien.gatling.mqtt.Predef._
import io.gatling.core.Predef._

import scala.concurrent.duration._
import scala.util.Random

/**
  *
  */
class MqttRoundtripMeasurementExample extends Simulation {

    val mqttConf = mqtt.host("tcp://localhost:1883")

    def randomPayload : Array[Byte] = {
        val res = new Array[Byte](35)
        Random.nextBytes(res)
        res
    }

    val scn = scenario("MQTT Random byte array test")
        .exec(connect)
        .exec(subscribe("myTopic"))
        .during(30 seconds) {
            pace(1 second).exec(publishAndWait("myTopic", ByteArrayBody(_ => randomPayload)).timeout(60 seconds))
        }
        .exec(waitForMessages().timeout(30 seconds))

    setUp(
        scn.inject(rampUsers(10) over (10 seconds)))
        .protocols(mqttConf)
}
