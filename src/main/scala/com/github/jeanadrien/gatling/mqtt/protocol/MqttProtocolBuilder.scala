package com.github.jeanadrien.gatling.mqtt.protocol

import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.softwaremill.quicklens._
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session._

/**
  *
  */
case class MqttProtocolBuilder(mqttProtocol : MqttProtocol) {

    def host(host : Expression[String]) = this.modify(_.mqttProtocol.host).setTo(Some(host))

    def clientId(clientId : Expression[String]) = this.modify(_.mqttProtocol.defaultConnectionSettings.clientId)
        .setTo(Some(clientId))

    def cleanSession(cleanSession : Boolean) = this.modify(_.mqttProtocol.defaultConnectionSettings.cleanSession)
        .setTo(Some(cleanSession))

    def keepAlive(keepAlive : Short) = this.modify(_.mqttProtocol.optionPart.keepAlive).setTo(Some(keepAlive))

    def userName(userName : Expression[String]) = this.modify(_.mqttProtocol.defaultConnectionSettings.userName)
        .setTo(Some(userName))

    def password(password : Expression[String]) = this.modify(_.mqttProtocol.defaultConnectionSettings.password)
        .setTo(Some(password))

    def willTopic(willTopic : Expression[String]) = this.modify(_.mqttProtocol.defaultConnectionSettings.willTopic)
        .setTo(Some(willTopic))

    def willMessage(willMessage : Expression[String]) = this
        .modify(_.mqttProtocol.defaultConnectionSettings.willMessage)
        .setTo(Some(willMessage))

    def willQos(willQos : MqttQoS) = this.modify(_.mqttProtocol.defaultConnectionSettings.willQos).setTo(Some(willQos))

    def willRetain(willRetain : Boolean) = this.modify(_.mqttProtocol.defaultConnectionSettings.willRetain)
        .setTo(Some(willRetain))

    def version(version : Expression[String]) = this.modify(_.mqttProtocol.optionPart.version).setTo(Some(version))

    def connectAttemptsMax(connectAttemptsMax : Long) = this.modify(_.mqttProtocol.reconnectPart.connectAttemptsMax)
        .setTo(Some(connectAttemptsMax))

    def reconnectAttemptsMax(reconnectAttemptsMax : Long) = this
        .modify(_.mqttProtocol.reconnectPart.reconnectAttemptsMax).setTo(Some(reconnectAttemptsMax))

    def reconnectDelay(reconnectDelay : Long) = this.modify(_.mqttProtocol.reconnectPart.reconnectDelay)
        .setTo(Some(reconnectDelay))

    def reconnectDelayMax(reconnectDelayMax : Long) = this.modify(_.mqttProtocol.reconnectPart.reconnectDelayMax)
        .setTo(Some(reconnectDelayMax))

    def reconnectBackOffMultiplier(reconnectBackOffMultiplier : Double) =
        this.modify(_.mqttProtocol.reconnectPart.reconnectBackOffMultiplier).setTo(Some(reconnectBackOffMultiplier))

    def receiveBufferSize(receiveBufferSize : Int) = this.modify(_.mqttProtocol.socketPart.receiveBufferSize)
        .setTo(Some(receiveBufferSize))

    def sendBufferSize(sendBufferSize : Int) = this.modify(_.mqttProtocol.socketPart.sendBufferSize)
        .setTo(Some(sendBufferSize))

    def trafficClass(trafficClass : Int) = this.modify(_.mqttProtocol.socketPart.trafficClass).setTo(Some(trafficClass))

    def maxReadRate(maxReadRate : Int) = this.modify(_.mqttProtocol.throttlingPart.maxReadRate).setTo(Some(maxReadRate))

    def maxWriteRate(maxWriteRate : Int) = this.modify(_.mqttProtocol.throttlingPart.maxWriteRate)
        .setTo(Some(maxWriteRate))

    def build = mqttProtocol
}

object MqttProtocolBuilder {

    implicit def toMqttProtocol(builder : MqttProtocolBuilder) : MqttProtocol = builder.build

    def apply(configuration : GatlingConfiguration) : MqttProtocolBuilder =
        MqttProtocolBuilder(MqttProtocol(configuration))
}
