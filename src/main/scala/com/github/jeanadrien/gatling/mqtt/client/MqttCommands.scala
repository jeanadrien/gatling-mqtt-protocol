package com.github.jeanadrien.gatling.mqtt.client

import akka.actor.ActorRef
import com.github.jeanadrien.gatling.mqtt.client.MqttClient.FeedbackFunction
import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS

/**
  * FIXME: Rename all of this mqtt actions ?
  */
sealed trait MqttCommands

object MqttCommands {

    case object Connect extends MqttCommands

    case object ConnectAck extends MqttCommands

    case class Subscribe(topics : List[(String, MqttQoS)]) extends MqttCommands

    case object SubscribeAck extends MqttCommands

    case class Publish(topic : String, payload : Array[Byte], mqttQoS : MqttQoS, retain : Boolean) extends MqttCommands

    case object PublishAck extends MqttCommands

    case class OnPublish(topic : String, payload : Array[Byte]) extends MqttCommands

    case class PublishAndWait(
        topic : String, payload : Array[Byte], payloadFeedback : FeedbackFunction, qos : MqttQoS, retain : Boolean
    ) extends MqttCommands

    case class PublishAckRegisterFeedback(
        topic : String, payloadFeedback : FeedbackFunction, listener : ActorRef
    ) extends MqttCommands

    case object FeedbackReceived extends MqttCommands

    case object WaitForMessages extends MqttCommands

    case object WaitForMessagesDone extends MqttCommands

}
