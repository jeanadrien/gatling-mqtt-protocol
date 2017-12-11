package com.github.jeanadrien.gatling.mqtt.client
import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.actor.Status.Failure
import com.github.jeanadrien.gatling.mqtt.client.MqttCommands._
import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.github.jeanadrien.gatling.mqtt.protocol.MqttProtocol
import org.fusesource.mqtt.client.{CallbackConnection, MQTT, QoS, Topic}

/**
  *
  */
class FuseSourceMqttClient(config : MqttClientConfiguration, gatlingMqttId : String) extends MqttClient(gatlingMqttId) {

    implicit def qosToQos(mqttQoS : MqttQoS) : QoS = mqttQoS match {
        case MqttQoS.AtLeastOnce => QoS.AT_LEAST_ONCE
        case MqttQoS.AtMostOnce => QoS.AT_MOST_ONCE
        case MqttQoS.ExactlyOnce => QoS.EXACTLY_ONCE
    }

    val engine = new MQTT()

    engine.setHost(config.host)
    engine.setCleanSession(config.cleanSession)
    config.clientId.foreach(engine.setClientId)
    config.username.foreach(engine.setUserName)
    config.password.foreach(engine.setPassword)
    config.will.foreach { will =>
        engine.setWillTopic(will.topic)
        engine.setWillMessage(will.message)
        engine.setWillQos(qosToQos(will.qos))
        engine.setWillRetain(will.willRetain)
    }
    engine.setKeepAlive(config.keepAlive.toShort)
    config.version.foreach(engine.setVersion)

    // configure reconnect part
    config.reconnectConfig.connectAttemptsMax.foreach(engine.setConnectAttemptsMax)
    config.reconnectConfig.reconnectAttemptsMax.foreach(engine.setReconnectAttemptsMax)
    config.reconnectConfig.reconnectDelay.foreach(engine.setReconnectDelay)
    config.reconnectConfig.reconnectDelayMax.foreach(engine.setReconnectDelayMax)
    config.reconnectConfig.reconnectBackOffMultiplier.foreach(engine.setReconnectBackOffMultiplier)

    // configure socket part
    config.socketConfig.receiveBufferSize.foreach(engine.setReceiveBufferSize)
    config.socketConfig.sendBufferSize.foreach(engine.setSendBufferSize)

    // configure throttling part
    config.throttlingConfig.maxReadRate.foreach(engine.setMaxReadRate)
    config.throttlingConfig.maxWriteRate.foreach(engine.setMaxWriteRate)

    // === mqtt commands =================================================== //

    private var openConnection : Option[CallbackConnection] = None

    override protected def connect(replyTo : ActorRef) : Unit = {
        val connection = engine.callbackConnection()
        val listener = new FuseSourceConnectionListener(self)
        connection.listener(listener)

        connection.connect(Callback.onSuccess[Void] { _ =>
            openConnection = Some(connection)
            replyTo ! ConnectAck
        } onFailure { th =>
            replyTo ! Failure(th)
        })
    }

    override protected def subscribe(topics : List[(String, MqttQoS)], replyTo : ActorRef) : Unit = openConnection match {
        case Some(connection) =>
            val topicsList : List[Topic] = topics.map { case (t, mqttQoS) => new Topic(t, mqttQoS) }
            connection.subscribe(topicsList.toArray, Callback.onSuccess { value : Array[Byte] =>
                // FIXME : What to do with this 'value' ?
                replyTo ! SubscribeAck
            } onFailure { th =>
                replyTo ! Failure(th)
            })
        case None =>
            replyTo ! Failure(new IllegalStateException("Cannot subscribe: mqtt connection is not open"))
    }

    override protected def publish(topic : String, payload : Array[Byte], mqttQoS : MqttQoS, retain : Boolean, replyTo : ActorRef) : Unit = openConnection match {
        case Some(connection) =>
            connection.publish(topic, payload, mqttQoS, retain, Callback.onSuccess[Void] { _ =>
                replyTo ! PublishAck
            } onFailure { th =>
                replyTo ! Failure(th)
            })
        case None =>
            replyTo ! Failure(new IllegalStateException("Cannot publish: mqtt connection is not open"))
    }

    override protected def close() = openConnection match {
        case Some(connection) =>
            connection.disconnect(Callback.onSuccess[Void] { _ =>
                logger.debug("MQTT client disconnected.")
            } onFailure { th =>
                logger.warn("Failed to close MQTT connection.")
            })
        case None =>
            // nop
    }
}
