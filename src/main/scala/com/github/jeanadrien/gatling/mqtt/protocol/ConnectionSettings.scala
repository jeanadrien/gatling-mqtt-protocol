package com.github.jeanadrien.gatling.mqtt.protocol

import io.gatling.commons.validation.{Success, Validation}
import io.gatling.core.session.{Session, _}
import org.fusesource.mqtt.client.{MQTT, QoS}

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
    willQos : Option[QoS],
    willRetain : Option[Boolean]
) {

    private[protocol] def configureMqtt(session : Session)(mqtt1 : MQTT) : Validation[MQTT] = {
        willQos.foreach(mqtt1.setWillQos)
        willRetain.foreach(mqtt1.setWillRetain)
        cleanSession.foreach(mqtt1.setCleanSession)

        Success(mqtt1).flatMap { mqtt =>
            clientId.map {
                _ (session).map { cid => mqtt.setClientId(cid); mqtt }
            } getOrElse (Success(mqtt))
        } flatMap { mqtt =>
            userName.map {
                _ (session).map { un => mqtt.setUserName(un); mqtt }
            } getOrElse (Success(mqtt))
        } flatMap { mqtt =>
            password.map {
                _ (session).map { pswd => mqtt.setPassword(pswd); mqtt }
            } getOrElse (Success(mqtt))
        } flatMap { mqtt =>
            willTopic.map {
                _ (session).map { wto => mqtt.setWillTopic(wto); mqtt }
            } getOrElse (Success(mqtt))
        } flatMap { mqtt =>
            willMessage.map {
                _ (session).map { wme => mqtt.setWillMessage(wme); mqtt }
            } getOrElse (Success(mqtt))
        }
    }

}