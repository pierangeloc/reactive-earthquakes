package http

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{Message, TextMessage, UpgradeToWebsocket}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Outlet
import akka.stream.scaladsl._
import stream.{EarthquakeParser, StreamingFacilities}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Serve the earthquake stream to each websocket, separately, with time dictated by server.
 * Client can pass in the ws request the time multiplier, which determines how fast we want events to be replayed
 */
object EarthquakeServer extends App with ServerBox {

  val eventsUrl = "http://localhost:8081/earthquakes"

  val route: Route =
    pathPrefix("world") {
      get {
        getFromResourceDirectory("webapp")
      }
    } ~
    path("earthquakes") {
      onSuccess(EarthquakeParser.remoteStrings(eventsUrl)) {
        import EarthquakeParser._
        source => {
          val replayedEventsAsWsJson: Source[Message, Any] = replayedEvents(source)
                                                              .map(EarthquakeParser.eventToJson)
                                                              .map(event => TextMessage.Strict(event.toString))

          val eventFlow: Flow[Message, Message, Unit] = Flow.wrap(Sink.ignore, replayedEventsAsWsJson)(Keep.none)

          handleWebsocketMessages(eventFlow)
        }

      }
    }


  runServer(route)("Earthquake Server", 8080)


}
