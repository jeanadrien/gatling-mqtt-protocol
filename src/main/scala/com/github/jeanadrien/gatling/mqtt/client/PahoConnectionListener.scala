package com.github.jeanadrien.gatling.mqtt.client

import akka.actor.ActorRef
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttCallback, MqttMessage}

/**
  *
  */
class PahoConnectionListener(actor : ActorRef) extends MqttCallback with LazyLogging {
    override def deliveryComplete(token : IMqttDeliveryToken) : Unit = {
        // nop
   }

    override def messageArrived(
        topic : String, message : MqttMessage
    ) : Unit = {
        val payload = message.getPayload
        logger.trace(s"Paho listener receives: topic=${topic}, body length=${payload.length}")

        actor ! MqttCommands.OnPublish(topic, payload)
    }

    override def connectionLost(cause : Throwable) : Unit = {
        logger.debug(s"Client has been disconnected.")
        // support this for reconnection
    }
}
