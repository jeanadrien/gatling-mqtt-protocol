package com.github.jeanadrien.gatling.mqtt.actions

import com.typesafe.scalalogging.StrictLogging

/**
  *
  */
object Callback extends StrictLogging {

    class HalfCallback[T](val onSuccessFn : T => Unit) {

        def onFailure(onFailureFn : Throwable => Unit) = new org.fusesource.mqtt.client.Callback[T] {

            override def onSuccess(void : T) : Unit = onSuccessFn(void)

            override def onFailure(value : Throwable) : Unit = onFailureFn(value)
        }
    }

    def onSuccess[T](onSuccessFn : T => Unit) = new HalfCallback[T](onSuccessFn)

}
