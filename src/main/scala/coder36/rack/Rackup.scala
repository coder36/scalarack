package coder36.rack

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}

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
    apps(path) = new RackServlet(rack, path)
  }

  def start() : RackServer = {
    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setContextPath("/")
    val server = new Server(port)
    server.setHandler(context)
    apps.foreach( app => context.addServlet(new ServletHolder(app._2), (app._1 + "/*").replaceAll("//", "/")) )
    println(s"\u001B[34mRack started... listening for HTTP on /0.0.0.0:${port}")
    server.start
    this
  }

}

class RackServlet(val rack: Rack, val path: String)  extends HttpServlet {

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    val res = Middleware.BaseRack( rack, request, response ).call(Map('base_path -> path))
    val status = res._1
    val headers = res._2
    val body = res._3
    response.setStatus(status)
    headers.foreach( (h) => response.addHeader(h._1, h._2))
    if( body != null ) response.getWriter().write(body)
  }
}




