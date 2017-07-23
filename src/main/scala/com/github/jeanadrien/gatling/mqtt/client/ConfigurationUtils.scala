package com.github.jeanadrien.gatling.mqtt.client

import io.gatling.commons.validation.{Success, Validation}
import io.gatling.core.session.{Expression, Session}

/**
  *
  */
object ConfigurationUtils {

    def realize[T, V](maybeExpr : Option[Expression[T]])(fn : (V, Option[T]) => V ) : (V, Session) => Validation[V] = { (v : V, s : Session) =>
        maybeExpr.map { expression =>
            expression(s).map(res => fn(v, Some(res)))
        } getOrElse Success(v)
    }
}
