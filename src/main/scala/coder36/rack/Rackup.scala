package coder36.rack

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import com.typesafe.scalalogging.LazyLogging


class RackServer(port: Int = 8080) extends LazyLogging {

  var path : String = ""

  val apps = scala.collection.mutable.Map[String,RackServlet]()

  def server : RackServer = {
    this
  }

  def map(path: String) : RackServer = {
    this.path = path
    this
  }

  def onto(rack: Rack) = {
    apps(path) = new RackServlet(rack)
  }

  def start() = {
    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setContextPath("/")
    val server = new Server(port)
    server.setHandler(context)
    apps.foreach( app => {
      context.addServlet(new ServletHolder(app._2), app._1)
    } );
    println(s"\u001B[34mRack started... listening for HTTP on /0.0.0.0:${port}")
    server.start
  }

}



class RackServlet(val rack: Rack)  extends HttpServlet {

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    val env = scala.collection.mutable.Map[String,String]()
    env("REQUEST_METHOD") = request.getMethod
    env("REQUEST_PATH") = request.getRequestURI
    env("QUERY_STRING") = request.getQueryString

    val res = rack.call( env.toMap  )
    val status = res._1
    val headers = res._2
    val body = res._3
    response.setStatus(status)
    headers.foreach( (h) => response.addHeader(h._1, h._2))
    response.getWriter().write(body)

  }

}

