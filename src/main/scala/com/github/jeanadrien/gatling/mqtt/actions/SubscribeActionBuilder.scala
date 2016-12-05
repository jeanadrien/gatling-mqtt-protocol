package com.github.jeanadrien.gatling.mqtt.actions

import io.gatling.core.action.Action
import io.gatling.core.session._
import io.gatling.core.structure.ScenarioContext
import org.fusesource.mqtt.client.QoS

import com.softwaremill.quicklens._

/**
  *
  */
case class SubscribeActionBuilder(
    topic : Expression[String],
    qos : QoS = QoS.AT_MOST_ONCE
) extends MqttActionBuilder {

    def qos(newQos : QoS) : SubscribeActionBuilder = this.modify(_.qos).setTo(newQos)

    def qosAtMostOnce = qos(QoS.AT_MOST_ONCE)
    def qosAtLeastOnce = qos(QoS.AT_LEAST_ONCE)
    def qosExactlyOnce = qos(QoS.EXACTLY_ONCE)

    override def build(
        ctx: ScenarioContext, next: Action
    ): Action = {
        new SubscribeAction(
            mqttComponents(ctx),
            ctx.coreComponents,
            topic,
            qos,
            next
        )
    }

}

