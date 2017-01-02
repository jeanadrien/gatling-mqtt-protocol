package com.github.jeanadrien.gatling.mqtt.actions

import akka.actor.ActorRef
import com.typesafe.scalalogging.StrictLogging
import org.fusesource.hawtbuf.{Buffer, UTF8Buffer}
import org.fusesource.mqtt.client.Listener

/**
  *
  */
class ConnectionListener(val connectionId : String, actor : ActorRef) extends Listener with StrictLogging {

    import MessageListenerActor._

    override def onPublish(
        topic : UTF8Buffer, body : Buffer,
        ack : Runnable
    ) : Unit = {
        val topicStr = topic.toString()
        val bodyStr = body.toByteArray()

        logger.trace(s"Listener ${actor} receives: topic=${topicStr}, body=${bodyStr}")

        actor ! MqttReceive(topicStr, bodyStr)
        ack.run()
    }

    override def onConnected() : Unit = {
        logger.debug(s"${connectionId}: client is now connected.")
    }

    override def onFailure(value : Throwable) : Unit = {
        logger.error(s"${connectionId}: Listener: onFailure: ${value.getMessage}")
    }

    override def onDisconnected() : Unit = {
        logger.debug(s"${connectionId}: has been disconnected.")
    }
}


