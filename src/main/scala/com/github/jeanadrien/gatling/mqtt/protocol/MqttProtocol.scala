package com.github.jeanadrien.gatling.mqtt.protocol

import akka.actor.ActorSystem
import com.typesafe.scalalogging.StrictLogging
import io.gatling.commons.validation.{Success, Validation}
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}
import io.gatling.core.session._
import org.fusesource.mqtt.client.{MQTT}

/**
  *
  */
object MqttProtocol extends StrictLogging {

    val MqttProtocolKey = new ProtocolKey {

        type Protocol = MqttProtocol
        type Components = MqttComponents

        def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[MqttProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

        def defaultProtocolValue(configuration: GatlingConfiguration): MqttProtocol = MqttProtocol(configuration)

        def newComponents(system: ActorSystem, coreComponents: CoreComponents): MqttProtocol => MqttComponents = {

            mqttProdocol => {
                val mqttComponents = MqttComponents (
                    mqttProdocol,
                    system
                )

                mqttComponents
            }
        }
    }

    def apply(configuration: GatlingConfiguration) : MqttProtocol =
        MqttProtocol(
            host = None,
            defaultConnectionSettings = ConnectionSettings(
                clientId = None, // default : Random
                cleanSession = None, // default : true
                userName = None,
                password = None,
                willTopic = None,
                willMessage = None,
                willQos = None,
                willRetain = None
            ),
            optionPart = MqttProtocolOptionPart(
                keepAlive = None, // default ??
                version = None // default 3.1
            ),
            reconnectPart = MqttProtocolReconnectPart(
                connectAttemptsMax = None,
                reconnectAttemptsMax = None,
                reconnectDelay = None,
                reconnectDelayMax = None,
                reconnectBackOffMultiplier = None
            ),
            socketPart = MqttProtocolSocketPart(
                receiveBufferSize = None,
                sendBufferSize = None,
                trafficClass = None
            ),
            throttlingPart = MqttProtocolThrottlingPart(
                maxReadRate = None,
                maxWriteRate = None
            )
        )
}

case class MqttProtocol(
    host: Option[Expression[String]],
    defaultConnectionSettings : ConnectionSettings,
    optionPart: MqttProtocolOptionPart,
    reconnectPart: MqttProtocolReconnectPart,
    socketPart: MqttProtocolSocketPart,
    throttlingPart: MqttProtocolThrottlingPart
) extends Protocol {

    private[protocol] def configureMqtt(session: Session)(mqttInstance : MQTT) : Validation[MQTT] = {
        Success(mqttInstance).flatMap { mqtt =>
            host.map { _(session).map { dh => mqtt.setHost(dh); mqtt }} getOrElse( Success(mqtt) )
        }.
            flatMap(defaultConnectionSettings.configureMqtt(session)).
            flatMap(optionPart.configureMqtt(session)).
            flatMap(reconnectPart.configureMqtt(session)).
            flatMap(socketPart.configureMqtt(session)).
            flatMap(throttlingPart.configureMqtt(session))
    }
}

// Mqtt protocol parts

case class MqttProtocolOptionPart(
    keepAlive: Option[Short], // default is 30 (seconds)
    version: Option[Expression[String]]
) {
    private[protocol] def configureMqtt(session: Session)(mqtt: MQTT) : Validation[MQTT] = {
        keepAlive.foreach(mqtt.setKeepAlive)
        version.map { vv =>
            vv(session).map { v=>
                mqtt.setVersion(v)
                mqtt
            }
        } getOrElse(Success(mqtt))
    }
}

case class MqttProtocolReconnectPart(
    connectAttemptsMax: Option[Long],
    reconnectAttemptsMax: Option[Long],
    reconnectDelay: Option[Long],
    reconnectDelayMax: Option[Long],
    reconnectBackOffMultiplier: Option[Double]
) {
    private[protocol] def configureMqtt(session: Session)(mqtt: MQTT) : Validation[MQTT] = {
        connectAttemptsMax.foreach(mqtt.setConnectAttemptsMax)
        reconnectAttemptsMax.foreach(mqtt.setReconnectAttemptsMax)
        reconnectDelay.foreach(mqtt.setReconnectDelay)
        reconnectDelayMax.foreach(mqtt.setReconnectDelayMax)
        reconnectBackOffMultiplier.foreach(mqtt.setReconnectBackOffMultiplier)
        Success(mqtt)
    }
}

case class MqttProtocolSocketPart(
    receiveBufferSize: Option[Int],
    sendBufferSize: Option[Int],
    trafficClass: Option[Int]
) {
    private[protocol] def configureMqtt(session: Session)(mqtt : MQTT) : Validation[MQTT] = {
        receiveBufferSize.foreach(mqtt.setReceiveBufferSize)
        sendBufferSize.foreach(mqtt.setSendBufferSize)
        Success(mqtt)
    }
}

case class MqttProtocolThrottlingPart(
    maxReadRate: Option[Int],
    maxWriteRate: Option[Int]
) {
    private[protocol] def configureMqtt(session : Session)(mqtt : MQTT) : Validation[MQTT] = {
        maxReadRate.foreach(mqtt.setMaxReadRate)
        maxWriteRate.foreach(mqtt.setMaxWriteRate)
        Success(mqtt)
    }
}