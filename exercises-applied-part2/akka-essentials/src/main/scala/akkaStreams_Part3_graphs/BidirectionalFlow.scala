package akkaStreams_Part3_graphs

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, BidiShape, ClosedShape, Graph}

object BidirectionalFlow extends App{

  implicit val system = ActorSystem("BidirectionalFlows")
  implicit val materializer = ActorMaterializer

  /*
  example: crytography
   */
  def encrypt(n: Int)(string: String) = string.map(c => (c + n).toChar)
  def decrypt(n: Int)(string: String) = string.map(c => (c - n).toChar)

  println(encrypt(3)("Akka"))
  println(decrypt(3)("Dnnd"))

  //bidirectional Flow
  val bidiCryptoStaticGraph = GraphDSL.create() {
    implicit builder =>
      import GraphDSL.Implicits._

      val encryptionFlow = builder.add(Flow[String].map(element => encrypt(3)(element)))
      val decryptionFlow = builder.add(Flow[String].map(element => decrypt(3)(element)))

      //BidiShape(encryptionFlow.in, encryptionFlow.out, decryptionFlow.in, decryptionFlow.out)
      //below is shortcut for wrting above line
      BidiShape.fromFlows(encryptionFlow, decryptionFlow)
  }
  val unencryptedStrings = List("akka","is", "awesome", "testing", "bidirectional", "flows")
  val unencryptedSource = Source(unencryptedStrings)

  val encryptedSource = Source(unencryptedStrings.map(encrypt(3)))

  val cryptoBidiGraph = RunnableGraph.fromGraph(
    GraphDSL.create() {
      implicit builder =>
        import GraphDSL.Implicits._

        val unencryptedSourceShape = builder.add(unencryptedSource)
        val encryptedSourceShape = builder.add(encryptedSource)
        val bidi = builder.add(bidiCryptoStaticGraph)
        val encryptedSink = builder.add(Sink.foreach[String](string => println(s"Encrypted: $string")))
        val decryptedSink = builder.add(Sink.foreach[String](string => println(s"Decrypted: $string")))

        unencryptedSourceShape ~> bidi.in1 ; bidi.out1 ~> encryptedSink
        //encryptedSourceShape ~> bidi.in2 ; bidi.out2 ~> decryptedSink
        //or
        decryptedSink <~ bidi.out2 ; bidi.in2 <~ encryptedSourceShape

        ClosedShape
        /*
        we use bidirectional flow wehn we need
        encrypting/decrypting
        encoding/decoding
        serializing/deserializing
         */
    }
  )
}
