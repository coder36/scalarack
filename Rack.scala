package coder36.rack
import javax.servlet.http.HttpServlet

trait Rack {
  def call(env: Map[String,String]) : (Int, Map[String,String], String )
}

class Rackup {
  var path = "/"
  val apps = scala.collection.mutable.Map[String,RackServlet]()
  def map(path: String) : Rackup = {
    this.path = path
    this
  }

  def to(rack: Rack) = {
    apps(path) = new RackServlet(rack)
  }

  def start() = {
    val ws = new WebServer(apps.toMap)
    ws.start
  }

}

object Rackup extends Rackup 

class RackServlet(val rack: Rack)  extends HttpServlet {

  import javax.servlet.http.HttpServletRequest
  import javax.servlet.http.HttpServletResponse

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    val env = scala.collection.mutable.Map[String,String]()
    env("REQUEST_METHOD") = request.getMethod
    env("REQUEST_PATH") = request.getRequestURI
    env("QUERY_STRING") = request.getQueryString

    val res = rack.call( env.toMap  )
    response.setStatus(res._1)
    response.getWriter.write(res._3)
  }

}

class WebServer(val apps: Map[String,RackServlet], val port: Int = 8080, val contextPath: String = "/") {

  import org.eclipse.jetty.server.Server
  import org.eclipse.jetty.servlet.ServletContextHandler
  import org.eclipse.jetty.servlet.ServletHolder

  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
  context.setContextPath(contextPath)
  val server = new Server(port)
  server.setHandler(context)
  apps.foreach( app => context.addServlet(new ServletHolder(app._2), app._1) );

  def start() = server.start
  def stop() = server.stop

}
