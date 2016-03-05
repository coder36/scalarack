# ScalaRack

Rack for Scala.

# Example
```
object App {

  def main(args: Array[String]) {

    object HelloWorld extends Rack {
      def call(env: Map[String,String]) : (Int, Map[String,String], String ) =  {
        (200, Map(), "Hello world")
      }
    }

    Rackup map "/" to HelloWorld
    Rackup start
  }

}


```



## Credit
Based on the amazingly simple ruby web framework [Rack](https://github.com/rack)
