package com.github.jeanadrien.gatling.mqtt.client

/**
  *
  */
object MqttQoS extends Enumeration {

    type MqttQoS = Value

    val AtMostOnce = Value // 0
    val AtLeastOnce = Value // 1
    val ExactlyOnce = Value // 2

}
