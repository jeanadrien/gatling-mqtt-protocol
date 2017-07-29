package com.github.jeanadrien.gatling.mqtt.actions

import com.github.jeanadrien.gatling.mqtt.client.MqttQoS
import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.softwaremill.quicklens._
import io.gatling.core.action.Action
import io.gatling.core.session._
import io.gatling.core.structure.ScenarioContext

/**
  *
  */
case class PublishActionBuilder(
    topic : Expression[String],
    payload : Expression[Array[Byte]],
    qos : MqttQoS = MqttQoS.AtMostOnce,
    retain : Boolean = false
) extends MqttActionBuilder {

    def qos(newQos : MqttQoS) : PublishActionBuilder = this.modify(_.qos).setTo(newQos)

    def qosAtMostOnce = qos(MqttQoS.AtMostOnce)

    def qosAtLeastOnce = qos(MqttQoS.AtLeastOnce)

    def qosExactlyOnce = qos(MqttQoS.ExactlyOnce)

    def retain(newRetain : Boolean) : PublishActionBuilder = this.modify(_.retain).setTo(newRetain)

    override def build(
        ctx : ScenarioContext, next : Action
    ) : Action = {
        new PublishAction(
            mqttComponents(ctx),
            ctx.coreComponents,
            topic,
            payload,
            qos,
            retain,
            next
        )
    }

}