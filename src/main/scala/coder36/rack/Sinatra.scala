package coder36.rack

import coder36.rack.Middleware.FormParser
import coder36.rack.rackTypes.Handler
import org.fusesource.scalate._
import scala.collection.mutable.Map
import scala.util.matching.Regex



package object rackTypes {
  type Handler = (Context) => String
}

case class Context(env: Map[Symbol,Any],
                   var status: Int = 200,
                   val respHeaders : Map[String,String] = Map( "Content-Type" -> "text/html"),
                   val params : Map[String,String] = Map() )


class Sinatra( assets: Assets = Assets() ) extends Rack {

  val engine: TemplateEngine = new TemplateEngine

  var methodHandlers = Map[Symbol, Map[String,Handler]](
    'GET -> Map[String, Handler](),
    'POST -> Map[String,Handler](),
    'PUT -> Map[String, Handler]()
  )

  def call(env: Map[Symbol,Any]) : (Int, Map[String,String], String ) =  {
    FormParser( Middleware.RackCallProxy(_call _) ).call(env)
  }

  def _call(env: Map[Symbol,Any]) : (Int, Map[String,String], String ) =  {
    val c = Context(env = env)
    val formParams = env('form_params).asInstanceOf[Map[String,String]]
    formParams.foreach( m => c.params(m._1) = m._2)
    getHandler(c, env ) match {
      case Some(handler) => (c.status, c.respHeaders, handler(c))
      case _ => assets.call(env)
    }
  }

  def get(path: String )(fn: Handler ) = methodHandlers('GET)(path) = fn (_)
  def post(path: String )(fn: Handler ) = methodHandlers('POST)(path) = fn (_)
  def put(path: String )(fn: Handler ) = methodHandlers('PUT)(path) = fn (_)

  def ssp(name:String, params: Map[String,Any] = Map[String,Any]()) : String = {
    engine.layout(s"views/${name}.ssp", params.toMap)
  }

  def getHandler(context: Context, env: Map[Symbol,Any]) : Option[Handler] = {
    val path = env('path).toString
    val method = Symbol(env('request_method).toString)

    methodHandlers(method).foreach((handler) => {
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

