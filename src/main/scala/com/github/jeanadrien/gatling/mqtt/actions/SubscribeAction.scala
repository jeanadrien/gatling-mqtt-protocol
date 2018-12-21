package com.github.jeanadrien.gatling.mqtt.actions

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.github.jeanadrien.gatling.mqtt.client.MqttCommands
import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.github.jeanadrien.gatling.mqtt.protocol.MqttComponents
import io.gatling.commons.stats._
import io.gatling.commons.util.ClockSingleton._
import io.gatling.core.CoreComponents
import io.gatling.core.action.Action
import io.gatling.core.session._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  *
  */
class SubscribeAction(
    mqttComponents : MqttComponents,
    coreComponents : CoreComponents,
    topic          : Expression[String],
    qos            : MqttQoS,
    val next       : Action
) extends MqttAction(mqttComponents, coreComponents) {

    import mqttComponents.system.dispatcher

    override val name = genName("mqttSubscribe")

    override def execute(session : Session) : Unit = recover(session)(for {
        connection <- session("engine").validate[ActorRef]
        connectionId <- session("connectionId").validate[String]
        resolvedTopic <- topic(session)
    } yield {
        implicit val timeout = Timeout(1 minute) // TODO check how to configure this

        val requestStartDate = nowMillis

        val requestName = "subscribe"

        logger.debug(s"${connectionId}: Execute ${requestName}:${resolvedTopic}")
        (connection ? MqttCommands.Subscribe((resolvedTopic -> qos) :: Nil)).mapTo[MqttCommands].onComplete {
            case Success(MqttCommands.SubscribeAck) =>
                val subscribeTimings = timings(requestStartDate)

                statsEngine.logResponse(
                    session,
                    requestName,
                    subscribeTimings,
                    OK,
                    None,
                    None // Some(new String(value)) // FIXME see equiv in Fuse client
                )

                next ! session
            case Failure(th) =>
                val subscribeTimings = timings(requestStartDate)
                logger.warn(s"${connectionId}: Failed to SUBSCRIBE on ${resolvedTopic}: ${th}")

                statsEngine.logResponse(
                    session,
                    requestName,
                    subscribeTimings,
                    KO,
                    None,
                    Some(th.getMessage)
                )

                next ! session
        }
    })

}
