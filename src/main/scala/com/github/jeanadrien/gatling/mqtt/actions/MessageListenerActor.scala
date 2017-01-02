package com.github.jeanadrien.gatling.mqtt.actions

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.StrictLogging

/**
  *
  */
class MessageListenerActor(connectionId : String) extends Actor with StrictLogging {
    import MessageListenerActor._

    import scala.collection.mutable.{ArrayBuffer, HashMap}

    private val pending = HashMap[String, ArrayBuffer[(Array[Byte] => Boolean, ActorRef)]]()

    private var waitingForClose : Option[ActorRef] = None

    def numPending: Int = pending.values.foldLeft(0)((acc, xs) => acc + xs.length)

    private def addPending(topic: String, payloadValidation: Array[Byte] => Boolean, sender: ActorRef): Unit = {
        pending.getOrElseUpdate(topic, ArrayBuffer[(Array[Byte] => Boolean, ActorRef)]()).+=:(payloadValidation, sender)
    }

    private def removePending(topic: String, payloadValidation: Array[Byte] => Boolean): Unit = {
        pending.get(topic).foreach { buff =>
            logger.debug(s"removePending: Topic ${topic}: buff size before filtering: ${buff.length}")
            buff.filterNot(_._1 == payloadValidation) match {
                case Seq() =>
                    logger.debug(s"removePending: Topic ${topic} is now empty")
                    pending.remove(topic)
                case xs =>
                    logger.debug(s"removePending: Topic ${topic} now has ${xs.length} items")
                    pending.put(topic, xs)
            }
        }
        areWeDone()
    }

    private def areWeDone(): Unit = {
        if (waitingForClose.isDefined && pending.isEmpty) {
            waitingForClose.foreach(_ ! None)
            waitingForClose = None
        }
    }

    override def receive = {
        case WaitForMessage(topic, payloadValidation) =>
            if (waitingForClose.isDefined) {
                sender() ! akka.actor.Status.Failure(new Exception("We are waiting for last messages"))
            } else {
                addPending(topic, payloadValidation, sender())
            }
        case CancelWaitForMessage(topic, payloadValidation) =>
            removePending(topic, payloadValidation)
        case MqttReceive(topic, payload) =>
            pending.get(topic).foreach(_.partition { case(predicate, _) =>
                predicate(payload)
            } match {
                case (Seq(), xs) =>
                    ()
                case (xs1, Seq()) =>
                    pending.remove(topic)
                    xs1.foreach { case (_, ref) =>
                        ref ! None
                    }
                    areWeDone()
                case (xs1, xs2) =>
                    pending.put(topic, xs2)
                    xs1.foreach { case (_, ref) =>
                        ref ! None
                    }
            })
        case WaitForAllReceived =>
            if (waitingForClose.isDefined) {
                sender() ! akka.actor.Status.Failure(new Exception("We are already waiting for last messages"))
            } else {
                logger.debug(s"${connectionId} : waitForAllReceived: ${numPending}")
                waitingForClose = Some(sender())
                areWeDone()
            }

    }

}

object MessageListenerActor {
    // actor messages
    case class WaitForMessage(topic : String, payloadValidation : Array[Byte] => Boolean)
    case class CancelWaitForMessage(topic : String, payloadValidation : Array[Byte] => Boolean)
    case class MqttReceive(topic : String, payload : Array[Byte])
    case object WaitForAllReceived

    def props(connectionId: String): Props = Props(new MessageListenerActor(connectionId))
}
