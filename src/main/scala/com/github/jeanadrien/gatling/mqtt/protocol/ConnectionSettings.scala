package com.github.jeanadrien.gatling.mqtt.protocol

import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.github.jeanadrien.gatling.mqtt.client.{MqttClientConfiguration, Will}
import io.gatling.commons.validation.{Failure, Success, Validation}
import io.gatling.core.session.{Session, _}
import com.github.jeanadrien.gatling.mqtt.client.ConfigurationUtils._

/**
  *
  */
case class ConnectionSettings(
    clientId : Option[Expression[String]],
    cleanSession : Option[Boolean],
    userName : Option[Expression[String]],
    password : Option[Expression[String]],
    willTopic : Option[Expression[String]],
    willMessage : Option[Expression[String]],
    willQos : Option[MqttQoS],
    willRetain : Option[Boolean]
) {

    private def configureWill(session : Session)(mqtt1 : MqttClientConfiguration) : Validation[MqttClientConfiguration] = (willTopic, willMessage) match {
        case (Some(wte), Some(wme)) =>
            for {
                wt <- wte(session)
                wm <- wme(session)
            } yield {
                var will = Will(
                    topic = wt,
                    message = wm
                )
                will = willQos.map(qos => will.copy(qos = qos)).getOrElse(will)
                will = willRetain.map(r => will.copy(willRetain = r)).getOrElse(will)
                mqtt1.copy(will = Some(will))
            }
        case (None, None) =>
            Success(mqtt1)
        case _ =>
            Failure("Both will topic and message must be defined")
    }

    private[protocol] def configureMqtt(session : Session)(mqtt1 : MqttClientConfiguration) : Validation[MqttClientConfiguration] = {
        var mqtt = mqtt1
        mqtt = cleanSession.map(cs => mqtt.copy(cleanSession = cs)).getOrElse(mqtt)

        Success(mqtt).flatMap { mqtt =>
            realize[String, MqttClientConfiguration](clientId){ (config, value) => config.copy(clientId = value) }(mqtt, session)
        } flatMap { mqtt =>
            realize[String, MqttClientConfiguration](userName){ (config, value) => config.copy(username = value) }(mqtt, session)
        } flatMap { mqtt =>
            realize[String, MqttClientConfiguration](password){(config, value) => config.copy(password = value)}(mqtt, session)
        } flatMap {
            configureWill(session)
        }
    }

}