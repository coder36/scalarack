package coder36.rack

import javax.servlet.http.HttpServletResponse

import org.fusesource.scalate.TemplateEngine

import scala.collection.mutable.Map

case class Assets(rack: Rack = Middleware.NotFoundRack(), root: String = "public") extends Rack {
  val engine: TemplateEngine = new TemplateEngine

  def call(env: Map[Symbol,Any]) : (Int, Map[String,String], String ) =  {
    val path = s"/$root${env('path).toString}"
    val is = getClass.getResourceAsStream(path)
    val resp = env('response).asInstanceOf[HttpServletResponse]
    if( is == null ) return rack.call(env)

    val bytes = new Array[Byte](1024) //1024 bytes - Buffer size
    Iterator
      .continually (is.read(bytes))
      .takeWhile (-1 !=)
      .foreach (read=>resp.getOutputStream.write(bytes,0,read))

    (200, Map(), null)
  }

}
