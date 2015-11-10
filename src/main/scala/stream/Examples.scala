package stream

import akka.stream.scaladsl._

import scala.concurrent.Future

object StreamRange extends App with StreamingFacilities {
  val src: Source[Int, Unit] = Source(1 to 100)
  val sink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)
  val runnableGraph: RunnableGraph[Future[Int]] = src.toMat(sink)(Keep.right)
  val materialized: Future[Int] = runnableGraph.run()
}






object StreamRangeConcise extends App with StreamingFacilities {

  val materialized: Future[Int] = Source(1 to 100).runWith(Sink.fold(0)(_ + _))
}








object ExplicitFlow extends App with StreamingFacilities {
  val sourceInt: Source[Int, Unit] = Source(1 to 100)
  val flow: Flow[Int, String, Unit] = Flow[Int].map(_ + "-th")
  val sink: Sink[String, Future[Unit]] = Sink.foreach(println(_))
  (sourceInt via flow to sink).run()


}







object FastSource extends App with StreamingFacilities {
  val sourceInt: Source[Int, Unit] = Source(() => Iterator.from(0))
  sourceInt.to(loggerSink).run()
}