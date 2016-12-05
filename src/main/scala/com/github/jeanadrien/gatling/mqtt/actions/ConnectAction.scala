package com.github.jeanadrien.gatling.mqtt.actions

import com.github.jeanadrien.gatling.mqtt.protocol.{ConnectionSettings, MqttComponents}
import io.gatling.commons.stats._
import io.gatling.core.CoreComponents
import io.gatling.core.Predef._
import io.gatling.commons.util.ClockSingleton._
import io.gatling.core.action.Action

/**
  *
  */
class ConnectAction(
    mqttComponents : MqttComponents,
    coreComponents : CoreComponents,
    connectionSettings : ConnectionSettings,
    val next: Action
) extends MqttAction(mqttComponents, coreComponents) {

    override val name = genName("mqttConnect")

    override def execute(session: Session): Unit = recover(session) {
        mqttComponents.mqttEngine(session, connectionSettings).flatMap { mqtt =>
            val connectionId = genName("mqttConnection")

            val requestName = "connect"
            logger.debug(s"${connectionId}: Execute ${requestName}")

            val messageListener = mqttComponents.system.actorOf(MessageListenerActor.props(connectionId), "ml-"+connectionId)

            // connect
            val requestStartDate = nowMillis
            val connection = mqtt.callbackConnection()

            val listener = new ConnectionListener(connectionId, messageListener)
            connection.listener(listener)

            connection.connect(Callback.onSuccess[Void] { _ =>
                val connectTiming = timings(requestStartDate)

                statsEngine.logResponse(
                    session,
                    requestName,
                    connectTiming,
                    OK,
                    None,
                    None
                )

                next ! session.
                    set("connection", connection).
                    set("connectionId", connectionId).
                    set("listener", messageListener)
            } onFailure { th =>

                val connectTiming = timings(requestStartDate)
                logger.warn(s"${connectionId}: Failed to connect to MQTT: ${th}")
                statsEngine.logResponse(
                    session,
                    requestName,
                    connectTiming,
                    KO,
                    None,
                    Some(th.getMessage)
                )
            })
        }
    }

}
