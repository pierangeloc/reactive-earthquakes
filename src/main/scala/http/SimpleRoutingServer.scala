package http

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import stream.StreamingFacilities
import akka.http.scaladsl.server.Directives._


object SimpleRoutingServer extends App with StreamingFacilities {

  val route: Route =
    get {
      path("hello") {
        complete {
          "Welcome!"
        }
      } ~
        path("buongiorno") {
          complete {
            "Benvenuto!"
          }
        } ~
        path("fail") {
          failWith(new Exception())
        }
    }

  Http().bindAndHandle(route, "localhost", 8081)



}
