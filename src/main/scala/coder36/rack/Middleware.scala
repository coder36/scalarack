package coder36.rack
import com.typesafe.scalalogging.LazyLogging


object Middleware {

  case class RequestLogger(val rack: Rack ) extends Rack with LazyLogging {
    def call(env: Map[String,Any]) : (Int, Map[String,String], String ) =  {
      logger.info(s"${env("REQUEST_METHOD")} ${env("REQUEST_PATH")}")
      rack.call(env)
    }
  }


  case class FormParser(val rack: Rack ) extends Rack with LazyLogging {
    def call(env: Map[String,Any]) : (Int, Map[String,String], String ) =  {
      val body = env("body").toString


      rack.call(env)
    }
  }



}
