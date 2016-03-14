package coder36.rack

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import com.typesafe.scalalogging.LazyLogging
import scala.collection.mutable.Map
import scala.collection.JavaConversions._


object Middleware {

  case class RequestLogger(val rack: Rack ) extends Rack with LazyLogging {
    def call(env: Map[Symbol,Any]) : (Int, Map[String,String], String ) =  {
      val res = rack.call(env)
      val s = new StringBuilder
      env.map { case(k,v) => s.append( k + "=" + v + " || " ) }
      logger.info(s.toString())
      res
    }
  }


  case class FormParser(val rack: Rack ) extends Rack with LazyLogging {
    def call(env: Map[Symbol,Any]) : (Int, Map[String,String], String ) =  {

      val formParams = Map[String,String]()
      env('form_params) = formParams

      if ( env('content_type).toString == "application/x-www-form-urlencoded" ) {
        val body = env('body).toString

        val params = body.split("&").foldLeft(formParams)((acc, v) => {
          val a = v.split("=")
          acc += java.net.URLDecoder.decode(a(0), "UTF-8") -> java.net.URLDecoder.decode(a(1), "UTF-8")
        })
      }
      rack.call(env)
    }
  }

  case class NotFoundRack() extends Rack {
    def call(env: Map[Symbol, Any]): (Int, Map[String, String], String) = {
      (404, Map(), "Not found")
    }
  }

  case class RackCallProxy(fn: (Map[Symbol,Any]) => (Int, Map[String, String], String) ) extends Rack {
    def call(env: Map[Symbol, Any]): (Int, Map[String, String], String) = {
      fn(env)
    }
  }

  case class BaseRack(val rack: Rack, request: HttpServletRequest, response: HttpServletResponse ) extends Rack {
    def call(env: Map[Symbol, Any]): (Int, Map[String, String], String) = {
      env('request) = request
      env('response) = response
      env('request_method) = request.getMethod
      env('request_path) = request.getRequestURI
      env('query_string) = request.getQueryString
      env('body) = body(request)
      val hdrs = headers(request)
      env('headers) = hdrs
      env('content_type) = hdrs.getOrElse("Content-Type", "")
      val basePath = env('base_path).toString
      env('path) = request.getRequestURI.replaceFirst(basePath, "/").replaceAll("//", "/")
      rack.call(env)
    }

    def body(request: HttpServletRequest) : String = {

      val reader = request.getReader
      var body = ""
      Stream.continually(reader.readLine()).takeWhile( _ ne null) foreach { line =>
        body += line
      }
      body
    }

    def headers( request: HttpServletRequest) : Map[String,String] = {
      val hdrs = Map[String,String]()
      request.getHeaderNames.foreach( name => hdrs(name) = request.getHeader(name))
      hdrs
    }
  }



}
