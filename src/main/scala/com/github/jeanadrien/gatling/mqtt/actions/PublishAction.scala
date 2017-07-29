package com.github.jeanadrien.gatling.mqtt.actions

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.github.jeanadrien.gatling.mqtt.client.MqttCommands
import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.github.jeanadrien.gatling.mqtt.protocol.MqttComponents
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.ClockSingleton._
import io.gatling.core.CoreComponents
import io.gatling.core.action.Action
import io.gatling.core.session._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  *
  */
class PublishAction(
    mqttComponents : MqttComponents,
    coreComponents : CoreComponents,
    topic          : Expression[String],
    payload : Expression[Array[Byte]],
    qos            : MqttQoS,
    retain : Boolean,
    val next       : Action
) extends MqttAction(mqttComponents, coreComponents) {

    import mqttComponents.system.dispatcher

    override val name = genName("mqttPublish")

    override def execute(session : Session) : Unit = recover(session)(for {
        connection <- session("engine").validate[ActorRef]
        connectionId <- session("connectionId").validate[String]
        resolvedTopic <- topic(session)
        resolvedPayload <- payload(session)
    } yield {
        implicit val timeout = Timeout(1 minute) // TODO check how to configure this

        val requestStartDate = nowMillis

        val requestName = "publish"

        logger.debug(s"${connectionId}: Execute ${requestName}:${resolvedTopic} Payload: ${resolvedPayload}")

        (connection ? MqttCommands.Publish(
            resolvedTopic, resolvedPayload, qos, retain
        )).mapTo[MqttCommands].onComplete {
            case Success(MqttCommands.PublishAck) =>
                val publishTimings = timings(requestStartDate)

                statsEngine.logResponse(
                    session,
                    requestName,
                    publishTimings,
                    OK,
                    None,
                    None
                )

                next ! session
            case Failure(th) =>
                val publishTimings = timings(requestStartDate)
                logger.warn(s"${connectionId}: Failed to publish on ${resolvedTopic}: ${th}")
                statsEngine.logResponse(
                    session,
                    requestName,
                    publishTimings,
                    KO,
                    None,
                    Some(th.getMessage)
                )

                next ! session
        }
    })
}
