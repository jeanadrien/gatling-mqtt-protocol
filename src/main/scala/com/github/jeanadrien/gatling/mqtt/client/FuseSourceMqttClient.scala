package com.github.jeanadrien.gatling.mqtt.client
import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.github.jeanadrien.gatling.mqtt.protocol.MqttProtocol
import org.fusesource.mqtt.client.{MQTT, QoS}

/**
  *
  */
class FuseSourceMqttClient(config : MqttClientConfiguration) extends MqttClient {

    def qosToQos(mqttQoS : MqttQoS) : QoS = mqttQoS match {
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

}
