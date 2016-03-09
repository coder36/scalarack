package coder36.rack

import coder36.rack.rackTypes.Handler
import org.fusesource.scalate._
import scala.collection.mutable.{Map => mMap}
import scala.util.matching.Regex

package object rackTypes {
  type Handler = (Context) => String
}

case class Context(env: Map[String,Any],
                   var status: Int = 200,
                   val respHeaders : mMap[String,String] = mMap( "Content-Type" -> "text/html"),
                   val params : mMap[String,String] = mMap() )

class Sinatra(val engine: TemplateEngine = new TemplateEngine) extends Rack {

  var mathodHandlers = Map[String, mMap[String,Handler ]](
    "GET" -> mMap[String, Handler](),
    "POST" -> mMap[String,Handler ](),
    "PUT" -> mMap[String, Handler ]()
  )

  def call(env: Map[String,Any]) : (Int, Map[String,String], String ) =  {
    val c = Context(env = env)

    getHandler(c, env ) match {
      case Some(handler) => (c.status, c.respHeaders.toMap, handler(c))
      case _ => (404, Map(), "Not found")
    }
  }

  def get(path: String )(fn: Handler ) = {
    mathodHandlers("GET")(path) = fn (_)
  }

  def post(path: String )(fn: Handler ) = {
    mathodHandlers("POST")(path) = fn (_)
  }

  def ssp(name:String, params: Map[String,Any] = Map[String,Any]()) : String = {
    engine.layout(s"views/${name}.ssp", params)
  }

  def getHandler(context: Context, env: Map[String,Any]) : Option[Handler] = {
    val path = env("REQUEST_PATH").toString
    val method = env("REQUEST_METHOD").toString

    mathodHandlers(method).foreach( (handler) => {
      val handler_path = handler._1
      val tokens = handler_path.split("/").filter( _.nonEmpty ).filter(_.startsWith(":"))
      val regex = tokens.foldLeft(handler_path)( (t,p) => t.replace(p,"(\\w+)" ))
      val pathRegex = new Regex("^" + regex + "$", tokens:_*)
      val res = pathRegex.findFirstMatchIn(path)
      if ( !res.isEmpty ) {
        tokens.foreach( t => context.params(t.substring(1)) = res.get.group(t) )
        return Some(handler._2)
      }
    })
    None
  }

}

