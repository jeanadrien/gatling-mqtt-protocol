package com.github.jeanadrien.gatling.mqtt.protocol

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.scalalogging.StrictLogging
import io.gatling.commons.validation.{Success, Validation}
import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session._
import org.fusesource.mqtt.client.{CallbackConnection, MQTT}

/**
  *
  */
case class MqttComponents(
    mqttProtocol : MqttProtocol, system : ActorSystem
) extends ProtocolComponents with StrictLogging {

    def mqttEngine(session : Session, connectionSettings : ConnectionSettings) : Validation[MQTT] = {
        logger.debug("MqttComponents: new mqttEngine");
        Success(new MQTT()).flatMap(mqttProtocol.configureMqtt(session))
            .flatMap(connectionSettings.configureMqtt(session))
    }

    override def onStart : Option[(Session) => Session] = Some(s => {
        logger.debug("MqttComponents: onStart");
        s
    })

    override def onExit : Option[(Session) => Unit] = Some(s => {
        logger.debug("MqttComponents: onExit");
        s("connection").asOption[CallbackConnection].foreach(_.disconnect(null))
        s("listener").asOption[ActorRef].foreach(system stop _)
    })
}
