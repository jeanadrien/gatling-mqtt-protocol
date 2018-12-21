package com.github.jeanadrien.gatling.mqtt.actions

import com.github.jeanadrien.gatling.mqtt.client.MqttQoS
import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.softwaremill.quicklens._
import io.gatling.core.action.Action
import io.gatling.core.session._
import io.gatling.core.structure.ScenarioContext
import org.fusesource.mqtt.client.QoS

import scala.concurrent.duration._

/**
  *
  */
case class PublishAndWaitActionBuilder(
    topic : Expression[String],
    payload         : Expression[Array[Byte]],
    payloadFeedback : Array[Byte] => Array[Byte] => Boolean = PayloadComparison.sameBytesContent,
    qos             : MqttQoS = MqttQoS.AtMostOnce,
    retain          : Boolean = false,
    timeout         : FiniteDuration = 60 seconds
) extends MqttActionBuilder {

    def qos(newQos : MqttQoS) : PublishAndWaitActionBuilder = this.modify(_.qos).setTo(newQos)

    def qosAtMostOnce = qos(MqttQoS.AtMostOnce)

    def qosAtLeastOnce = qos(MqttQoS.AtLeastOnce)

    def qosExactlyOnce = qos(MqttQoS.ExactlyOnce)

    def retain(newRetain : Boolean) : PublishAndWaitActionBuilder = this.modify(_.retain).setTo(newRetain)

    def payloadFeedback(fn : Array[Byte] => Array[Byte] => Boolean) : PublishAndWaitActionBuilder = this
        .modify(_.payloadFeedback).setTo(fn)

    def timeout(duration : FiniteDuration) : PublishAndWaitActionBuilder = this.modify(_.timeout).setTo(duration)

    override def build(
        ctx : ScenarioContext, next : Action
    ) : Action = {
        new PublishAndWaitAction(
            mqttComponents(ctx),
            ctx.coreComponents,
            topic,
            payload,
            payloadFeedback,
            qos,
            retain,
            timeout,
            next
        )
    }

}
