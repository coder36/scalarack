import coder36.rack.Rack
import coder36.rack.Rackup

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

    Rackup map "/*" to EnvInfo
    Rackup start
  }

}


