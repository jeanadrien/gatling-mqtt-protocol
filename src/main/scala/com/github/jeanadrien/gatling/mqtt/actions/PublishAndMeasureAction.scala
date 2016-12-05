package com.github.jeanadrien.gatling.mqtt.actions

import java.nio.charset.StandardCharsets

import akka.actor.ActorRef
import akka.pattern.AskTimeoutException
import MessageListenerActor.WaitForMessage
import akka.util.Timeout
import com.github.jeanadrien.gatling.mqtt.protocol.MqttComponents
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.ClockSingleton._
import io.gatling.core.CoreComponents
import io.gatling.core.action.Action
import io.gatling.core.session._
import org.fusesource.mqtt.client.{CallbackConnection, QoS}

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

/**
  *
  */
class PublishAndMeasureAction(
    mqttComponents: MqttComponents,
    coreComponents: CoreComponents,
    topic: Expression[String],
    payload: Expression[Array[Byte]],
    payloadFeedback: Array[Byte] => Array[Byte] => Boolean,
    qos: QoS,
    retain: Boolean,
    timeout : FiniteDuration,
    val next  : Action
) extends MqttAction(mqttComponents, coreComponents) {

    import MessageListenerActor._
    import akka.pattern.ask
    import mqttComponents.system.dispatcher

    override val name = genName("mqttPublishAndMeasure")

    override def execute(session: Session): Unit = recover(session)(for {
        connection <- session("connection").validate[CallbackConnection]
        listener <- session("listener").validate[ActorRef]
        connectionId <- session("connectionId").validate[String]
        resolvedTopic <- topic(session)
        resolvedPayload <- payload(session)
    } yield {
        val requestStartDate = nowMillis

        val requestName = "publishAndMeasure"

        logger.debug(s"${connectionId} : Execute ${requestName}:${resolvedTopic} Payload: ${resolvedPayload}")

        val payloadCheck = payloadFeedback(resolvedPayload)

        implicit val messageTimeout = Timeout(timeout)

        listener ? WaitForMessage(resolvedTopic, payloadCheck) onComplete { result =>
            val latencyTimings = timings(requestStartDate)

            statsEngine.logResponse(
                session,
                requestName,
                latencyTimings,
                if (result.isSuccess) OK else KO,
                None,
                result match {
                    case Success(_) => None
                    case Failure(t) if t.isInstanceOf[AskTimeoutException] =>
                        logger.warn(s"${connectionId}: Wait for PUBLISH back from mqtt timed out on ${resolvedTopic}")
                        Some("Wait for PUBLISH timed out")
                    case Failure(t) =>
                        logger.warn(s"${connectionId}: Failed to receive PUBLISH back from mqtt on ${resolvedTopic}: ${t}")
                        Some(t.getMessage)
                }
            )
        }

        connection.publish(resolvedTopic, resolvedPayload, qos, retain, Callback
            .onSuccess[Void] { _ =>
            val publishTimings = timings(requestStartDate)

            statsEngine.logResponse(
                session,
                "publish",
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
                "publish",
                publishTimings,
                KO,
                None,
                Some(th.getMessage)
            )

        })
    })
}