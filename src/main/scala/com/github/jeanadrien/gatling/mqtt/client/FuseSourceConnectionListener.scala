package com.github.jeanadrien.gatling.mqtt.client

import akka.actor.ActorRef
import com.typesafe.scalalogging.StrictLogging
import org.fusesource.hawtbuf.{Buffer, UTF8Buffer}
import org.fusesource.mqtt.client.Listener

/**
  *
  */
class FuseSourceConnectionListener(actor : ActorRef) extends Listener with StrictLogging {

    override def onPublish(
        topic : UTF8Buffer, body : Buffer,
        ack   : Runnable
    ) : Unit = {
        val topicStr = topic.toString()
        val bodyStr = body.toByteArray()

        logger.trace(s"Listener receives: topic=${topicStr}, body=${bodyStr}")

        actor ! MqttCommands.OnPublish(topicStr, bodyStr)
        ack.run()
    }

    override def onConnected() : Unit = {
        logger.debug(s"Client is now connected.")
    }

    override def onFailure(value : Throwable) : Unit = {
        logger.error(s"Listener: onFailure: ${value.getMessage}")
    }

    override def onDisconnected() : Unit = {
        logger.debug(s"Client has been disconnected.")
    }
}
