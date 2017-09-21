package com.github.jeanadrien.gatling.mqtt

import com.typesafe.config.ConfigFactory

/**
  *
  */
trait Settings {

    val settings = new {
        val mqtt = new {
            val client = Settings.config.getString("mqtt.client")
        }
    }
}

object Settings {
    val config = ConfigFactory.load()
}
