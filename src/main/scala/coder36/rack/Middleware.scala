package coder36.rack
import com.typesafe.scalalogging.LazyLogging
import scala.collection.mutable.Map


object Middleware {

  case class RequestLogger(val rack: Rack ) extends Rack with LazyLogging {
    def call(env: Map[String,Any]) : (Int, Map[String,String], String ) =  {
      val res = rack.call(env)
      val s = new StringBuilder
      env.map { case(k,v) => s.append( k + "=" + v + " || " ) }
      logger.info(s.toString())
      res
    }
  }


  case class FormParser(val rack: Rack ) extends Rack with LazyLogging {
    def call(env: Map[String,Any]) : (Int, Map[String,String], String ) =  {

      val formParams = Map[String,String]()
      env("FORM_PARAMS") = formParams

      if ( env("CONTENT_TYPE").toString == "application/x-www-form-urlencoded" ) {
        val body = env("BODY").toString

        val params = body.split("&").foldLeft(formParams)((acc, v) => {
          val a = v.split("=")
          acc += a(0) -> a(1)
        })
      }
      rack.call(env)
    }
  }



}
