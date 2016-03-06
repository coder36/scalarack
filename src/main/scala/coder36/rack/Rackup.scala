package coder36.rack

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}



class RackServer(port: Int = 8080) {

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
    apps.foreach( app => context.addServlet(new ServletHolder(app._2), app._1) );
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
    response.setStatus(res._1)
    response.getWriter().write(res._3)
  }

}

