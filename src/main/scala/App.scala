import coder36.rack._
import com.typesafe.scalalogging.LazyLogging
import coder36.rack.Middleware._

object App {

  def main(args: Array[String]) {

    object HelloWorld extends Rack {
      def call(env: Map[String,Any]) : (Int, Map[String,String], String ) =  {
        (200, Map(), "Hello world")
      }
    }

    object MySinatraApp extends Sinatra {
      get("/notfound") ((c: Context) => {
        c.status = 404
        "Not Found"
      })

      get("/welcome") ((c: Context) => {
        ssp("welcome", Map("name" -> "Mark"))
      })
    }

    object Server extends RackServer {
      val port = 8080
      server map "/helloworld" onto RequestLogger(HelloWorld)
      server map "/*" onto MySinatraApp
    }
    Server.start

  }

}


