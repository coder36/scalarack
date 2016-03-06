package coder36.rack
import org.fusesource.scalate._
import scala.collection.mutable.{Map => mMap}

case class Context(env: Map[String,Any],
                   var status: Int = 200,
                   val respHeaders : mMap[String,String] = mMap( "Content-Type" -> "text/html") )

class Sinatra(val engine: TemplateEngine = new TemplateEngine) extends Rack {

  var handlers = mMap[String,(Context) => String ]()

  def call(env: Map[String,Any]) : (Int, Map[String,String], String ) =  {
    val c = Context(env = env)
    val body  = handlers(env("REQUEST_PATH").toString)(c)
    (c.status, c.respHeaders.toMap, body)
  }

  def get(path: String )(fn: (Context) => String ) = {
    handlers(path) = fn (_)
  }

  def ssp(name:String, params: Map[String,Any] = Map[String,Any]()) : String = {
    engine.layout(s"views/${name}.ssp", params)
  }

}

