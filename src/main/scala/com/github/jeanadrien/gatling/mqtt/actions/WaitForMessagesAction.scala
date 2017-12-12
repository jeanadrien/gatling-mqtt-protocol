package com.github.jeanadrien.gatling.mqtt.actions

import akka.actor.ActorRef
import akka.pattern.AskTimeoutException
import akka.util.Timeout
import com.github.jeanadrien.gatling.mqtt.client.MqttCommands
import com.github.jeanadrien.gatling.mqtt.protocol.MqttComponents
import io.gatling.core.CoreComponents
import io.gatling.core.action.Action
import io.gatling.core.session.Session

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

/**
  *
  */
class WaitForMessagesAction(
    mqttComponents : MqttComponents,
    coreComponents : CoreComponents,
    timeout        : FiniteDuration,
    val next       : Action
) extends MqttAction(mqttComponents, coreComponents) {

    import akka.pattern.ask
    import mqttComponents.system.dispatcher

    override val name = genName("mqttWaitForMessage")

    override def execute(session : Session) : Unit = recover(session)(for {
        connection <- session("engine").validate[ActorRef]
        connectionId <- session("connectionId").validate[String]
    } yield {
        implicit val messageTimeout = Timeout(timeout)
        logger.debug(s"Wait for message action started... Timeout: ${timeout}")
        (connection ? MqttCommands.WaitForMessages).mapTo[MqttCommands] onComplete {
            case Success(MqttCommands.WaitForMessagesDone) =>
                logger.info(s"${connectionId} : Done waitForMessage.")
                next ! session
            case Failure(t) if t.isInstanceOf[AskTimeoutException] =>
                logger.warn("Wait for remaining messages timed out")
                next ! session.markAsFailed
            case Failure(t) =>
                logger.warn("Wait for remaining messages error:", t)
                next ! session.markAsFailed
        }
    })
}
