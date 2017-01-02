package com.github.jeanadrien.gatling.mqtt.actions

import com.github.jeanadrien.gatling.mqtt.protocol.MqttComponents
import org.fusesource.mqtt.client.{CallbackConnection, QoS, Topic}
import io.gatling.commons.util.ClockSingleton._
import io.gatling.commons.stats._
import io.gatling.core.CoreComponents
import io.gatling.core.action.Action
import io.gatling.core.session._

/**
  *
  */
class SubscribeAction(
    mqttComponents : MqttComponents,
    coreComponents : CoreComponents,
    topic: Expression[String],
    qos: QoS,
    val next : Action
) extends MqttAction(mqttComponents, coreComponents) {

    override val name = genName("mqttSubscribe")

    override def execute(session: Session): Unit = recover(session) (for {
        connection <- session("connection").validate[CallbackConnection]
        connectionId <- session("connectionId").validate[String]
        resolvedTopic <- topic(session)
    } yield {
        val requestStartDate = nowMillis

        val requestName = "subscribe"

        logger.debug(s"${connectionId}: Execute ${requestName}:${resolvedTopic}")

        val topics : Array[Topic] = Array(resolvedTopic).map((t : String) => new Topic(t, qos))
        connection.subscribe(topics, Callback.onSuccess { value : Array[Byte] =>
            val subscribeTimings = timings(requestStartDate)

            statsEngine.logResponse(
                session,
                requestName,
                subscribeTimings,
                OK,
                None,
                Some(new String(value))
            )

            next ! session
        } onFailure { th =>
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
        })

    })

}
