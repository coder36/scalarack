import coder36.rack._
import com.typesafe.scalalogging.LazyLogging
import coder36.rack.Middleware._

object App {

  def main(args: Array[String]) {


    object SinatraAppServer1 extends Sinatra {
      get("/") ((c: Context) => {
        """
          <html>
            <body>
              <h1>App1</h1>
              <form action="/hiddenredirect" method="POST">
                <input type="hidden" name="test" value="something"/>
                <button>Sign in</button>
              </form>
            </body>
          </html>
        """
      })

      post("/hiddenredirect") ((c: Context) => {
        """
          <html>
            <body>
              <h1>App1</h1>
              <h1>hidden redirect to App2</h1>
              <form id="myform" action="http://localhost:8081/">
              </form>
              <script>
              setTimeout( function() {
                document.getElementById("myform").submit()
              },1000 )
              </script>
            </body>
          </html>
        """
      })

      get("/redirect") ((c: Context) => {
        c.status = 302
        c.respHeaders("Location") = "http://localhost:8081"
        ""
      })

      get("/hello/:name") ((c: Context) => {
        s"""
            <html>
             <body>
               <h1>Hello ${c.params("name")}</h1>
             </body>
           </html>

        """
      })


    }


    object SinatraAppServer2 extends Sinatra {
      get("/")((c: Context) => {
        """
          <html>
            <body>
              <h1>App 2</h1>
            </body>
          </html>
        """
      })
    }



    object server1 extends RackServer {
      override val port = 8080
      this map "/*" onto SinatraAppServer1
    }
    server1 start


    object server2 extends RackServer {
      override val port = 8081
      this map "/*" onto SinatraAppServer2
    }
    server2 start

  }

}


