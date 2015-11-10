package stream

import akka.pattern._
import akka.stream.OverflowStrategy
import akka.stream.io.{Framing, InputStreamSource}
import akka.stream.scaladsl._
import akka.util.ByteString
import argonaut.Argonaut._
import argonaut._

import scala.concurrent.Future
import scala.concurrent.duration._


object EarthquakeParser extends StreamingFacilities {

  implicit val dispatcher = actorSystem.dispatcher

  val earthquakesDump = "all_month_asc.geojson"

  case class EarthquakeEvent(time: Long, lat: Double, long: Double, elevation: Double, magnitude: Double, place: String)

  /** decoder **/
  implicit def EarthquakeEventDecodeJson: DecodeJson[EarthquakeEvent] = DecodeJson (
    c => for {
      long <- (c --\ "geometry" --\ "coordinates" =\ 0).as[Double]
      lat <- (c --\ "geometry" --\ "coordinates" =\ 1).as[Double]
      elev <- (c --\ "geometry" --\ "coordinates" =\ 2).as[Double]
      magnitude <- (c --\ "properties" --\ "mag").as[Double]
      time <- (c --\ "properties" --\ "time").as[Long]
      place <- (c --\ "properties" --\ "place").as[String]
    } yield EarthquakeEvent(time, lat, long, elev, magnitude, place)
  )

  /** encoder **/
  implicit def EarthquakeEventEncodeJson: EncodeJson[EarthquakeEvent] = jencode6L(
    (e: EarthquakeEvent) =>
      (e.time, e.lat, e.long, e.elevation, e.magnitude, e.place))("time", "lat", "long", "elevation", "magnitude", "place")

  /**
   * This source emits strings as they come from any text file
   */
  def strings(resource: String): Source[String, Future[Long]] = {
    println(s"getting events from $resource")

    val inputStream = getClass.getClassLoader.getResourceAsStream(resource)

    InputStreamSource(() => inputStream)
      .via(Framing.delimiter(ByteString(",\n"), Int.MaxValue))
      .map(bytestring => bytestring.decodeString("UTF-8"))
  }

  /**
   * map String to decoded EarthquakeEvent
   * @param s
   */
  def jsonToEvent(s: String): Option[EarthquakeEvent] = {
    s.decodeOption[EarthquakeEvent]
  }

  /** Source of events extracted from json, when parsable **/
  def earthquakeEvents(s: String): Source[EarthquakeEvent, Future[Long]] = strings(s).map(jsonToEvent).filter(_.isDefined).map(_.get)

  /** Source of adjacent events from json **/
  def adjacentEvents(s: String): Source[(EarthquakeEvent, EarthquakeEvent), Future[Long]] = earthquakeEvents(s).via(adjacentElementsExtractor[EarthquakeEvent])

  // 1 h --> 10 s
  val scaleFactor = 360L
  def replayedEvents(s: String): Source[EarthquakeEvent, Future[Long]] = adjacentEvents(s).buffer(1, OverflowStrategy.backpressure).mapAsync[EarthquakeEvent](1) {
                                            case (event1: EarthquakeEvent, event2: EarthquakeEvent) => {
                                              val waitingTime = (event2.time - event1.time) / scaleFactor
                                              println(s"waiting $waitingTime [ms]")
                                              after( waitingTime.milliseconds , actorSystem.scheduler)(Future.successful(event2))
                                            }
                                          }

  def eventToJson(e: EarthquakeEvent): String = e.asJson.spaces2

  def adjacentElementsExtractor[T] = Flow() { implicit builder =>
    import FlowGraph.Implicits._

    val broadcast = builder.add(Broadcast[T](2))
    val zip = builder.add(Zip[T, T]())

    broadcast.out(0) ~> zip.in0
    broadcast.out(1).drop(1) ~> zip.in1

    (broadcast.in, zip.out)
  }

}







object AdjacentExtractor extends App with StreamingFacilities {
  import EarthquakeParser._

  Source(List(1,2,3,4))
    .via(adjacentElementsExtractor)
    .to(loggerSink).run()

}






object FastEvents extends App with StreamingFacilities {
  import EarthquakeParser._
  strings(earthquakesDump).to(loggerSink).run()
}







object SlowEvents extends App with StreamingFacilities {
  import EarthquakeParser._
  strings(earthquakesDump)
  .mapAsync(4)(s => after (1000 millisecond, actorSystem.scheduler)(Future.successful(s)))
  .to(loggerSink).run()

}






