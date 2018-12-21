package com.github.jeanadrien.gatling.mqtt.actions

import com.github.jeanadrien.gatling.mqtt.client.MqttQoS
import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.softwaremill.quicklens._
import io.gatling.core.action.Action
import io.gatling.core.session._
import io.gatling.core.structure.ScenarioContext
import org.fusesource.mqtt.client.QoS

/**
  *
  */
case class SubscribeActionBuilder(
    topic : Expression[String],
    qos   : MqttQoS = MqttQoS.AtMostOnce
) extends MqttActionBuilder {

    def qos(newQos : MqttQoS) : SubscribeActionBuilder = this.modify(_.qos).setTo(newQos)

    def qosAtMostOnce = qos(MqttQoS.AtMostOnce)

    def qosAtLeastOnce = qos(MqttQoS.AtLeastOnce)

    def qosExactlyOnce = qos(MqttQoS.ExactlyOnce)

    override def build(
        ctx : ScenarioContext, next : Action
    ) : Action = {
        new SubscribeAction(
            mqttComponents(ctx),
            ctx.coreComponents,
            topic,
            qos,
            next
        )
    }

}

