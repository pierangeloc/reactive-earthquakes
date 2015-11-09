package stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._


trait StreamingFacilities {

  implicit val actorSystem = ActorSystem("Playground")
  implicit val flowMaterializer = ActorMaterializer()
  implicit val timeout = Timeout(3 seconds)
  implicit val executionContext = actorSystem.dispatcher



  /**
   * Sink that just logs
   * @tparam T
   * @return
   */
  def loggerSink[T]: Sink[T, Future[Int]] = Sink.fold(0) { (index, elem) => println(s"$index-th element: $elem"); index + 1}

}
