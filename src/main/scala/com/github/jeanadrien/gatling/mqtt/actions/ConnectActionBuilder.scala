package com.github.jeanadrien.gatling.mqtt.actions

import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.github.jeanadrien.gatling.mqtt.protocol.ConnectionSettings
import com.softwaremill.quicklens._
import io.gatling.core.action.Action
import io.gatling.core.session._
import io.gatling.core.structure.ScenarioContext
import org.fusesource.mqtt.client.QoS

/**
  *
  */
case class ConnectActionBuilder(
    connectionSettings: ConnectionSettings = ConnectionSettings(
        clientId = None, // default : Random
        cleanSession = None, // default : true
        userName = None,
        password = None,
        willTopic = None,
        willMessage = None,
        willQos = None,
        willRetain = None
    )
) extends MqttActionBuilder {

    def clientId(clientId: Expression[String]) = this.modify(_.connectionSettings.clientId).setTo(Some(clientId))

    def cleanSession(cleanSession: Boolean) = this.modify(_.connectionSettings.cleanSession).setTo(Some(cleanSession))

    def userName(userName: Expression[String]) = this.modify(_.connectionSettings.userName).setTo(Some(userName))

    def password(password: Expression[String]) = this.modify(_.connectionSettings.password).setTo(Some(password))

    def willTopic(willTopic: Expression[String]) = this.modify(_.connectionSettings.willTopic).setTo(Some(willTopic))

    def willMessage(willMessage: Expression[String]) = this.modify(_.connectionSettings.willMessage)
        .setTo(Some(willMessage))

    def willQos(willQos: MqttQoS) = this.modify(_.connectionSettings.willQos).setTo(Some(willQos))

    def willRetain(willRetain: Boolean) = this.modify(_.connectionSettings.willRetain).setTo(Some(willRetain))

    override def build(
        ctx: ScenarioContext, next: Action
    ): Action = {
        new ConnectAction(
            mqttComponents(ctx),
            ctx.coreComponents,
            connectionSettings,
            next
        )
    }

}
