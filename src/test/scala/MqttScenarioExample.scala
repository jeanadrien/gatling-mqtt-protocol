import com.github.jeanadrien.gatling.mqtt.Predef._
import io.gatling.core.Predef._
import scala.concurrent.duration._

/**
  *
  */
class MqttScenarioExample extends Simulation {

    val mqttConf = mqtt.host("tcp://localhost:1883")

    val scn = scenario("MQTT Test")
        .exec(connect)
        .exec(subscribe("myTopic"))
        .during(5 minutes) {
            pace(1 second).exec(publish("myTopic", "myPayload"))
        }

    setUp(
        scn.inject(rampUsers(50) over (2 minutes)))
        .protocols(mqttConf)
}
