package http

import java.nio.file.Paths

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.io.SynchronousFileSource
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.{Failure, Success}


object FileServer extends App with ServerBox {

  val path = "/home/pierangeloc/Documents/projects/scala/reactive-earthquakes/src/main/resources/all_month_asc.geojson"

  def rowsStream: Source[ByteString, Future[Long]] = SynchronousFileSource(Paths.get(path).toFile)

  val handler: HttpRequest => HttpResponse = {
    case req @ HttpRequest(HttpMethods.GET, Uri.Path("/earthquakes"), _, entity, _) =>
      //absorb request
      entity.dataBytes.runWith(Sink.ignore)
      //return chunked response
      HttpResponse(entity = HttpEntity.Chunked.fromData(MediaTypes.`text/plain`, rowsStream))

    case req ⇒
      req.entity.dataBytes.runWith(Sink.ignore)
      HttpResponse(404, entity = "Not found!")
  }

  Http().bindAndHandleSync(interface = "localhost", port = 8081, handler = handler).onComplete {
    case Success(binding) => println(s"Serving chunked events on http://${binding.localAddress.getHostName}:${binding.localAddress.getPort}/earthquakes")
    case Failure(cause) => println(s"Failed running server, ${cause.getMessage}")
  }




}
