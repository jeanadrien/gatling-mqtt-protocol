package com.github.jeanadrien.gatling.mqtt

import java.nio.charset.StandardCharsets

import com.github.jeanadrien.gatling.mqtt.actions._
import com.github.jeanadrien.gatling.mqtt.protocol.MqttProtocolBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session._

/**
  *
  */
object Predef {

    def mqtt(implicit configuration : GatlingConfiguration) = MqttProtocolBuilder(configuration)

    def connect = ConnectActionBuilder()

    def subscribe(topic : Expression[String]) = SubscribeActionBuilder(topic)

    def publish[T <% MqttPayload](
        topic : Expression[String], payload : Expression[T]
    ) = PublishActionBuilder(topic, payload.map(_.toByteArray))

    def publishAndWait[T <% MqttPayload](
        topic : Expression[String], payload : Expression[T]
    ) = PublishAndWaitActionBuilder(topic, payload.map(_.toByteArray))

    def waitForMessages = WaitForMessagesActionBuilder

    def payload(in : Expression[String]) : Expression[Array[Byte]] =
        in.map(_.getBytes(StandardCharsets.UTF_8))

    trait MqttPayload {
        def toByteArray : Array[Byte]
    }

    implicit class StringMqttPayload(s : String) extends MqttPayload {
        override def toByteArray = s.getBytes
    }

    implicit def byteArrayPayload(b : Array[Byte]) : MqttPayload = new MqttPayload {
        override def toByteArray = b
    }
}
