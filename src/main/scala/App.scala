import coder36.rack._



object App {

  def main(args: Array[String]) {

    object Hello extends Rack {
      def call(env: Map[String,String]) : (Int, Map[String,String], String ) =  {
        (200, Map(), "Hello world")
      }
    }

    object EnvInfo extends Rack {
      def call(env: Map[String,String]) : (Int, Map[String,String], String ) =  {
        var body = ""
        env.foreach( (e) => body += e._1 + "=" + e._2 + "\n" ) 
        (200, Map(), body)
      }
    }


    object MySinatraApp extends Sinatra {


      get("/hello") ((c: Context) => {
        c.status = 404
        "hello from sinatra"
      })

      get("/mark") ((c: Context) => {
        ssp("index", Map(("name", "Mark")))
      })

    }

    object Server extends RackServer {
      val port = 8080
      server map "/env" onto EnvInfo
      server map "/*" onto MySinatraApp
    }
    Server.start

  }

}


