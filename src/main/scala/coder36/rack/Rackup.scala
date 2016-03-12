package coder36.rack

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}

import scala.collection.JavaConversions._
import scala.collection.mutable.Map



class RackServer extends LazyLogging {

  val port : Int = 8080
  var path : String = ""

  val apps = scala.collection.mutable.Map[String,RackServlet]()

  def map(path: String) : RackServer = {
    this.path = path
    this
  }

  def onto(rack: Rack) = {
    apps(path) = new RackServlet(rack)
  }

  def start() : RackServer = {
    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setContextPath("/")
    val server = new Server(port)
    server.setHandler(context)
    apps.foreach( app => context.addServlet(new ServletHolder(app._2), app._1) )
    println(s"\u001B[34mRack started... listening for HTTP on /0.0.0.0:${port}")
    server.start
    this
  }

}

class RackServlet(val rack: Rack)  extends HttpServlet {

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    val res = new BaseRack( rack, request, response ).call(Map())
    val status = res._1
    val headers = res._2
    val body = res._3
    response.setStatus(status)
    headers.foreach( (h) => response.addHeader(h._1, h._2))
    response.getWriter().write(body)
  }
}

class BaseRack(val rack: Rack, request: HttpServletRequest, response: HttpServletResponse ) extends Rack {
  def call(env: Map[String, Any]): (Int, Map[String, String], String) = {
    val env = Map[String,Any]()
    env("REQUEST") = request
    env("RESPONSE") = response
    env("REQUEST_METHOD") = request.getMethod
    env("REQUEST_PATH") = request.getRequestURI
    env("QUERY_STRING") = request.getQueryString
    env("BODY") = body(request)
    val hdrs = headers(request)
    env("HEADERS") = hdrs
    env("CONTENT_TYPE") = hdrs.getOrElse("Content-Type", "")
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


