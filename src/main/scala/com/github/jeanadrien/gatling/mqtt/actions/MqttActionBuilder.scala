package com.github.jeanadrien.gatling.mqtt.actions

import com.github.jeanadrien.gatling.mqtt.protocol.{MqttComponents, MqttProtocol}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext

/**
  *
  */
abstract class MqttActionBuilder extends ActionBuilder {

    def mqttComponents(ctx: ScenarioContext) : MqttComponents =
        ctx.protocolComponentsRegistry.components(MqttProtocol.MqttProtocolKey)

}
