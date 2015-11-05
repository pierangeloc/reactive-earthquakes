package http

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import stream.StreamingFacilities

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.{Failure, Success}

/**
 * Box to run arbitrary Akka-http server, in terms of flow.
 * Any route can be implicitly converted to a req-res flow
 */
trait ServerBox extends StreamingFacilities {

  def runServer(route: Flow[HttpRequest, HttpResponse, Any])(appName: String, port: Int): Unit = {
    val binding: Future[ServerBinding] = Http().bindAndHandle(route, "0.0.0.0", port)
    binding.onComplete {
      case Success(b) => println(s"Server ready, serving '$appName' on ${b.localAddress.getHostName}:${b.localAddress.getPort}")
      case Failure(e) => println(s"Error serving '$appName', reason: ${e.getMessage}")
    }

    println(s"Enter to stop '$appName' application")
    StdIn.readLine()
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }





}
