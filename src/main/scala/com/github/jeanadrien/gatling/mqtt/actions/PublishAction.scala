package com.github.jeanadrien.gatling.mqtt.actions

import com.github.jeanadrien.gatling.mqtt.protocol.MqttComponents
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.ClockSingleton._
import io.gatling.core.CoreComponents
import io.gatling.core.action.Action
import io.gatling.core.session._
import org.fusesource.mqtt.client.{CallbackConnection, QoS}

/**
  *
  */
class PublishAction(
    mqttComponents : MqttComponents,
    coreComponents : CoreComponents,
    topic          : Expression[String],
    payload : Expression[Array[Byte]],
    qos            : QoS,
    retain : Boolean,
    val next       : Action
) extends MqttAction(mqttComponents, coreComponents) {

    override val name = genName("mqttPublish")

    override def execute(session : Session) : Unit = recover(session)(for {
        connection <- session("connection").validate[CallbackConnection]
        connectionId <- session("connectionId").validate[String]
        resolvedTopic <- topic(session)
        resolvedPayload <- payload(session)
    } yield {
        val requestStartDate = nowMillis

        val requestName = "publish"

        logger.debug(s"${connectionId}: Execute ${requestName}:${resolvedTopic} Payload: ${resolvedPayload}")

        connection.publish(resolvedTopic, resolvedPayload, qos, retain, Callback.onSuccess[Void] { _ =>
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
        } onFailure { th =>
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
        })
    })
}
