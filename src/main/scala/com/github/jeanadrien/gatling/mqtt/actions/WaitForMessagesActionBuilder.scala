package com.github.jeanadrien.gatling.mqtt.actions

import com.softwaremill.quicklens._
import io.gatling.core.action.Action
import io.gatling.core.structure.ScenarioContext

import scala.concurrent.duration._

/**
  *
  */
case class WaitForMessagesActionBuilder(
    timeout : FiniteDuration = 60 seconds
) extends MqttActionBuilder {

    def timeout(duration : FiniteDuration) : WaitForMessagesActionBuilder = this.modify(_.timeout).setTo(duration)

    override def build(
        ctx : ScenarioContext, next : Action
    ) : Action = {
        new WaitForMessagesAction(
            mqttComponents(ctx),
            ctx.coreComponents,
            timeout,
            next
        )
    }

}