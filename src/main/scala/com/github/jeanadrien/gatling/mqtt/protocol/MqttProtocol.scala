package com.github.jeanadrien.gatling.mqtt.protocol

import akka.actor.ActorSystem
import com.github.jeanadrien.gatling.mqtt.client.MqttClientConfiguration
import com.typesafe.scalalogging.StrictLogging
import io.gatling.commons.validation.{Success, Validation}
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}
import io.gatling.core.session._
import org.fusesource.mqtt.client.MQTT
import com.github.jeanadrien.gatling.mqtt.client.ConfigurationUtils._


/**
  *
  */
object MqttProtocol extends StrictLogging {

    val MqttProtocolKey = new ProtocolKey {

        type Protocol = MqttProtocol
        type Components = MqttComponents

        def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[MqttProtocol]
            .asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

        def defaultProtocolValue(configuration: GatlingConfiguration): MqttProtocol = MqttProtocol(configuration)

        def newComponents(system: ActorSystem, coreComponents: CoreComponents): MqttProtocol => MqttComponents = {

            mqttProtocol => {
                val mqttComponents = MqttComponents(
                    mqttProtocol,
                    system
                )

                mqttComponents
            }
        }
    }

    def apply(configuration: GatlingConfiguration): MqttProtocol =
        MqttProtocol(
            host = None,
            defaultConnectionSettings = ConnectionSettings(
                clientId = None, // default : Random
                cleanSession = None, // default : true
                userName = None,
                password = None,
                willTopic = None,
                willMessage = None,
                willQos = None,
                willRetain = None
            ),
            optionPart = MqttProtocolOptionPart(
                keepAlive = None, // default ??
                version = None // default 3.1
            ),
            reconnectPart = MqttProtocolReconnectPart(
                connectAttemptsMax = None,
                reconnectAttemptsMax = None,
                reconnectDelay = None,
                reconnectDelayMax = None,
                reconnectBackOffMultiplier = None
            ),
            socketPart = MqttProtocolSocketPart(
                receiveBufferSize = None,
                sendBufferSize = None,
                trafficClass = None
            ),
            throttlingPart = MqttProtocolThrottlingPart(
                maxReadRate = None,
                maxWriteRate = None
            )
        )
}

case class MqttProtocol(
    host                     : Option[Expression[String]],
    defaultConnectionSettings: ConnectionSettings,
    optionPart               : MqttProtocolOptionPart,
    reconnectPart            : MqttProtocolReconnectPart,
    socketPart               : MqttProtocolSocketPart,
    throttlingPart           : MqttProtocolThrottlingPart
) extends Protocol {

    private[protocol] def configureMqtt(session: Session): Validation[MqttClientConfiguration] = {
        Success(
            MqttClientConfiguration(
                reconnectConfig = reconnectPart,
                socketConfig = socketPart,
                throttlingConfig = throttlingPart
            )
        ).flatMap { config : MqttClientConfiguration =>
            host.map {
                _ (session).map { dh => config.copy(host = dh) }
            } getOrElse (Success(config))
        }.
            flatMap(defaultConnectionSettings.configureMqtt(session)).
            flatMap(optionPart.configureMqtt(session))
    }
}

// Mqtt protocol parts

case class MqttProtocolOptionPart(
    keepAlive: Option[Int], // default is 30 (seconds)
    version  : Option[Expression[String]]
) {

    def assign[T, V](maybeValue : Option[T])(fn : (V, T) => V) : V => Validation[V] = { current =>
        Success(maybeValue.map(value => fn(current, value)).getOrElse(current))
    }

    private[protocol] def configureMqtt(session: Session)(mqtt: MqttClientConfiguration): Validation[MqttClientConfiguration] = {
        assign[Int, MqttClientConfiguration](keepAlive){ (config, value) =>
            config.copy(keepAlive = value)
        }(mqtt) flatMap { mqtt =>
            realize[String, MqttClientConfiguration](version){(config, value) => config.copy(version = value)}(mqtt, session)
        }
//        keepAlive.foreach(mqtt.setKeepAlive)
//        version.map { vv =>
//            vv(session).map { v =>
//                mqtt.setVersion(v)
//                mqtt
//            }
//        } getOrElse (Success(mqtt))
    }
}

case class MqttProtocolReconnectPart(
    connectAttemptsMax        : Option[Long] = None,
    reconnectAttemptsMax      : Option[Long] = None,
    reconnectDelay            : Option[Long] = None,
    reconnectDelayMax         : Option[Long] = None,
    reconnectBackOffMultiplier: Option[Double]  = None
) {
//    private[protocol] def configureMqtt(session: Session)(config: MqttClientConfiguration): Validation[MqttClientConfiguration] = {
//        connectAttemptsMax.foreach(mqtt.setConnectAttemptsMax)
//        reconnectAttemptsMax.foreach(mqtt.setReconnectAttemptsMax)
//        reconnectDelay.foreach(mqtt.setReconnectDelay)
//        reconnectDelayMax.foreach(mqtt.setReconnectDelayMax)
//        reconnectBackOffMultiplier.foreach(mqtt.setReconnectBackOffMultiplier)
//        Success(mqtt)
//    }
}

case class MqttProtocolSocketPart(
    receiveBufferSize: Option[Int] = None,
    sendBufferSize   : Option[Int] = None,
    trafficClass     : Option[Int] = None
) {
//    private[protocol] def configureMqtt(session: Session)(mqtt: MQTT): Validation[MQTT] = {
//        receiveBufferSize.foreach(mqtt.setReceiveBufferSize)
//        sendBufferSize.foreach(mqtt.setSendBufferSize)
//        Success(mqtt)
//    }
}

case class MqttProtocolThrottlingPart(
    maxReadRate : Option[Int] = None,
    maxWriteRate: Option[Int] = None
) {
//    private[protocol] def configureMqtt(session: Session)(mqtt: MQTT): Validation[MQTT] = {
//        maxReadRate.foreach(mqtt.setMaxReadRate)
//        maxWriteRate.foreach(mqtt.setMaxWriteRate)
//        Success(mqtt)
//    }
}