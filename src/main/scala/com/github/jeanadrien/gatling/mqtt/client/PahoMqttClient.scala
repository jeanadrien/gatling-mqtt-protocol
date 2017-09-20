package com.github.jeanadrien.gatling.mqtt.client
import akka.actor.ActorRef
import com.github.jeanadrien.gatling.mqtt.client.MqttCommands.{ConnectAck, PublishAck, SubscribeAck}
import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.{MqttConnectOptions, MqttMessage, MqttClient => PahoClient}

/**
  *
  */
class PahoMqttClient(config : MqttClientConfiguration) extends MqttClient {

    private val persistence = new MemoryPersistence();

    private def qosIntValue(qos: MqttQoS) : Int = qos match {
        case MqttQoS.AtMostOnce => 0
        case MqttQoS.AtLeastOnce => 1
        case MqttQoS.ExactlyOnce => 2
    }

    val broker = config.host
    val clientId = config.clientId.getOrElse(PahoClient.generateClientId())
    val pahoClient = new PahoClient(broker, clientId, persistence)

    val connOpts = new MqttConnectOptions
    connOpts.setCleanSession(config.cleanSession)
    // connOpts.setConnectionTimeout() // TODO
    connOpts.setKeepAliveInterval(config.keepAlive)
    config.version match {
        case Some("3.1") => connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1)
        case Some("3.1.1") => connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1)
        case _ => // nop
    }
    config.password.map(_.toCharArray).foreach(connOpts.setPassword _)
    config.username.foreach(connOpts.setUserName _)
    // connOpts.setServerURIs() ??
    // connOpts.setSocketFactory() ??
    config.will.foreach { will =>
        connOpts.setWill(
            will.topic,
            will.message.getBytes,
            qosIntValue(will.qos),
            will.willRetain
        )
    }

    // setup listener
    val listener = new PahoConnectionListener(self)
    pahoClient.setCallback(listener)

    // FIXME: Throttling
    // FIXME: Reconnect Part
    // FIXME: Socketconfig

    // FIXME: Support close

    //
//    System.out.println("Connected");
//    System.out.println("Publishing message: "+content);
//    MqttMessage message = new MqttMessage(content.getBytes());
//    message.setQos(qos);
//    sampleClient.publish(topic, message);
//    System.out.println("Message published");
//    sampleClient.disconnect();
//    System.out.println("Disconnected");
//    System.exit(0);
    override protected def connect(replyTo: ActorRef): Unit = {
        pahoClient.connect(connOpts);
        replyTo ! ConnectAck
    }

    override protected def subscribe(topics: List[(String, MqttQoS)], replyTo: ActorRef): Unit = {
        pahoClient.subscribe(topics.map(_._1).toArray, topics.map(_._2).map(qosIntValue).toArray)
        replyTo ! SubscribeAck
    }

    override protected def publish(
        topic   : String, payload : Array[Byte],
        mqttQoS : MqttQoS, retain : Boolean,
        replyTo : ActorRef
    ) : Unit = {
        val message = new MqttMessage(payload)
        message.setQos(qosIntValue(mqttQoS))
        message.setRetained(retain)
        pahoClient.publish(topic, message)
        replyTo ! PublishAck
    }
}
