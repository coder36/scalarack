package coder36.rack
import org.fusesource.scalate._

case class Context(env: Map[String,String], var status: Int = 200) {


}

class Sinatra(val engine: TemplateEngine = new TemplateEngine) extends Rack {

  var handlers = scala.collection.mutable.Map[String,(Context) => String ]()

  def call(env: Map[String,String]) : (Int, Map[String,String], String ) =  {
    val c = Context(env = env)
    val body  = handlers(env("REQUEST_PATH"))(c)
    (c.status, Map(), body)
  }

  def get(path: String )(fn: (Context) => String ) = {
    handlers(path) = fn (_)
  }

  def ssp(name:String, params: Map[String,Any] = Map[String,Any]()) : String = {
    engine.layout(s"${name}.ssp", params)
  }

}

