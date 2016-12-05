package com.github.jeanadrien.gatling.mqtt.actions

/**
  *
  */
object PayloadComparison {

    val sameBytesContent : Array[Byte] => Array[Byte] => Boolean = (a => b => a.deep == b.deep)

}
