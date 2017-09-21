package com.github.jeanadrien.gatling.mqtt.protocol

import akka.actor.{ActorRef, ActorSystem}
import com.github.jeanadrien.gatling.mqtt.client.{FuseSourceMqttClient, MqttClient}
import com.typesafe.scalalogging.StrictLogging
import io.gatling.commons.validation.Validation
import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session._
import org.fusesource.mqtt.client.CallbackConnection

/**
  *
  */
case class MqttComponents(
    mqttProtocol : MqttProtocol, system : ActorSystem
) extends ProtocolComponents with StrictLogging {

    def mqttEngine(session : Session, connectionSettings : ConnectionSettings) : Validation[ActorRef] = {
        logger.debug("MqttComponents: new mqttEngine")
        mqttProtocol.configureMqtt(session).map { config =>
            // inject the selected engine
            val mqttClient = system.actorOf(MqttClient.clientInjection(config))
            mqttClient
        }
    }

    override def onStart : Option[(Session) => Session] = Some(s => {
        logger.debug("MqttComponents: onStart");
        s
    })

    override def onExit : Option[(Session) => Unit] = Some(s => {
        logger.debug("MqttComponents: onExit");
        s("engine").asOption[ActorRef].foreach { mqtt =>
            system.stop(mqtt)
        }
    })
}
