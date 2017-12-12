package com.github.jeanadrien.gatling.mqtt.actions

import com.github.jeanadrien.gatling.mqtt.client.{FuseSourceConnectionListener, MqttCommands}
import com.github.jeanadrien.gatling.mqtt.protocol.{ConnectionSettings, MqttComponents}
import io.gatling.commons.stats._
import io.gatling.commons.util.ClockSingleton._
import io.gatling.core.CoreComponents
import io.gatling.core.Predef._
import io.gatling.core.action.Action
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  *
  */
class ConnectAction(
    mqttComponents : MqttComponents,
    coreComponents : CoreComponents,
    connectionSettings : ConnectionSettings,
    val next : Action
) extends MqttAction(mqttComponents, coreComponents) {

    import mqttComponents.system.dispatcher

    override val name = genName("mqttConnect")

    override def execute(session : Session) : Unit = recover(session) {
        val connectionId = genName("mqttClient")
        mqttComponents.mqttEngine(session, connectionSettings, connectionId).flatMap { mqtt =>

            val requestName = "connect"
            logger.debug(s"${connectionId}: Execute ${requestName}")

            // connect
            implicit val timeout = Timeout(1 minute) // TODO check how to configure this
            val requestStartDate = nowMillis
            (mqtt ? MqttCommands.Connect).mapTo[MqttCommands].onComplete {
                case Success(MqttCommands.ConnectAck) =>
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
                        set("engine", mqtt).
                        set("connectionId", connectionId)
                case Failure(th) =>
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
                    next ! session.markAsFailed
            }
        }
    }

}
