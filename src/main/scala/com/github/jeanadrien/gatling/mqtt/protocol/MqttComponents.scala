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
            // TODO inject the selected engine
            system.actorOf(MqttClient.pahoClient(config))
//            system.actorOf(MqttClient.fuseClient(config))
        }
    }

    override def onStart : Option[(Session) => Session] = Some(s => {
        logger.debug("MqttComponents: onStart");
        s
    })

    override def onExit : Option[(Session) => Unit] = Some(s => {
        logger.debug("MqttComponents: onExit");
        s("connection").asOption[CallbackConnection].foreach(_.disconnect(null))
        s("listener").asOption[ActorRef].foreach(system stop _)
        // TODO step the engine actor
    })
}
