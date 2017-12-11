package com.github.jeanadrien.gatling.mqtt.client

import akka.actor.{Actor, ActorRef, Props}
import com.github.jeanadrien.gatling.mqtt.Settings
import com.github.jeanadrien.gatling.mqtt.client.MqttClient.FeedbackFunction
import com.github.jeanadrien.gatling.mqtt.client.MqttCommands._
import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.typesafe.scalalogging.LazyLogging

/**
  *
  */
abstract class MqttClient(gatlingMqttId : String) extends Actor with LazyLogging {

    // FIXME: Timeout and feebackListener polution
    private var feedbackListener : Map[String, List[(FeedbackFunction, ActorRef)]] = Map()

    private var waitForMessagesReceivedListeners : List[ActorRef] = Nil

    protected def connect(replyTo : ActorRef) : Unit

    protected def subscribe(topics : List[(String, MqttQoS)], replyTo : ActorRef) : Unit

    protected def publish(topic : String, payload : Array[Byte], mqttQoS : MqttQoS, retain : Boolean, replyTo : ActorRef) : Unit

    protected def close() : Unit

    private def addFeedbackListener(topic : String, listener : (FeedbackFunction, ActorRef)): Unit = {
        feedbackListener = feedbackListener.get(topic) match {
            case Some(list : List[(FeedbackFunction, ActorRef)]) =>
                feedbackListener + (topic -> (listener::list))
            case None =>
                feedbackListener + (topic -> (listener::Nil))
        }
    }

    protected def onPublish(topic : String, payload : Array[Byte]): Unit = {
        logger.debug(s"Client ${gatlingMqttId} received (topic: $topic, payload size: ${payload.size}")
        feedbackListener = feedbackListener.get(topic) match {
            case Some(listeners) =>
                val (matching, nonMatching) = listeners.partition { case(fn, _) =>
                    fn(payload)
                }
                // fire matching listeners
                matching.foreach { case(_, replyTo) =>
                    replyTo ! FeedbackReceived
                }
                nonMatching match {
                    case Nil =>
                        feedbackListener - topic
                    case remainingListeners =>
                        feedbackListener + (topic -> remainingListeners)
                }
            case None =>
                feedbackListener
        }
    }

    private def publishAndWait(
        topic : String,
        payload : Array[Byte],
        payloadFeedback : FeedbackFunction,
        qos : MqttQoS,
        retain : Boolean,
        replyTo : ActorRef
    ) = {
        addFeedbackListener(topic, (payloadFeedback, replyTo))

        self ! MqttCommands.Publish(
            topic = topic,
            payload = payload,
            mqttQoS = qos,
            retain = retain
        )
    }

    private def waitForMessages(replyTo : ActorRef): Unit = {
        if (feedbackListener.isEmpty) {
            replyTo ! WaitForMessagesDone
        } else {
            waitForMessagesReceivedListeners = replyTo::waitForMessagesReceivedListeners
        }
    }

    private def fireAllWaitForMessageListeners = {
        waitForMessagesReceivedListeners.foreach(_ ! WaitForMessagesDone)
        waitForMessagesReceivedListeners = Nil
    }

    override def postStop() = {
        super.postStop()
        close()
    }

    override def receive : Receive = {
        case Connect =>
            connect(sender())
        case Subscribe(topics) =>
            subscribe(topics, sender())
        case Publish(topic, payload, mqttQoS, retain) =>
            publish(topic, payload, mqttQoS, retain, sender())
        case PublishAndWait(topic, payload, payloadFeedback, mqttQoS, retain) =>
            publishAndWait(topic, payload, payloadFeedback, mqttQoS, retain, sender())
        case OnPublish(topic, payload) =>
            onPublish(topic, payload)
            if (feedbackListener.isEmpty) {
                fireAllWaitForMessageListeners
            }
        case WaitForMessages =>
            waitForMessages(sender())
        case _ =>
            // nop
    }
}

object MqttClient extends Settings with LazyLogging {

    type FeedbackFunction = Array[Byte] => Boolean

    
    var clientInjection : (MqttClientConfiguration, String) => Props = { (configuration, gatlingClientId) =>
        val clientClass = settings.mqtt.client
        logger.info(s"Use MqttClient '$clientClass'")
        Props(Class.forName(clientClass), configuration, gatlingClientId)
    }
}