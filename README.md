# ScalaRack

Rack is a fundamental unit of web.  Its the base upon which all web apps are built.

This is Rack:

```
trait Rack {
  def call(env: Map[String,Any]) : (Int, Map[String,String], String )
}
```

To create a rack application, you implement the `call` method, which returns `status, headers, body`
```
object HelloWorld extends Rack {
  def call(env: Map[String,Any]) : (Int, Map[String,String], String ) =  {
    (200, Map(), "Hello world")
  }
}
```

# Rackup

To run Rack apps, you need a RackServer:

```
object Server extends RackServer {
  server map "/helloworld" onto HelloWorld
}
Server.start
```

Navigate to http://localhost:8080/helloworld

# Rack Middleware

Now for the clever part... Rack apps can be chained together.  The following
bit of middleware will add a Access-Control-Allow-Origin header to the response:

```
case class CrossOriginSupport(val rack: Rack) extends Rack {
  def call(env: Map[String,Any]) : (Int, Map[String,String], String ) =  {
    val res = rack.call(env)
    (res._1, res._2 + "Access-Control-Allow-Origin" -> "http://*" , res._2)
  }
}

The RackServer mapping becomes:

server map "/helloworld" onto CrossOriginSupport(HelloWorld)

```

# Assets

Assets is a basic web server.
```
server map "/" onto Assets(root="public")
```
By default it serves up content from the resources/public folder.

# Sinatra

Sinatra is a lightweight web framework developed on top of Rack.  It also has a built in
static asset server (based on `Asset`).

## Example

```
object MyApp extends Sinatra {

  get("/hello") ((c: Context) => {
    "Hello World"
  })
}

object Server extends RackServer {
  server map "/" onto MyApp
}
Server.start



```

## Scala Server Page support

Sinatra supports [SSP](https://scalate.github.io/scalate/documentation/ssp-reference.html).  SSP is
a templating language.

```
get("/hello/:name") ((c: Context) => {
  ssp("welcome", Map("name" -> c.params("name")) )
})
```

`ssp(...)` looks in the `resources/views` for `welcome.ssp` and renders it:

```
<%@ val name: String %>
<html>
    <body>
        <h1> Hi <%=name%>, welcome to Sinatra</h1>
    </body>
</html>
```


## Context

### HTTP Status code
`c.status = 404`  (The default response status is 200)

### Response Headers
`c.respHeaders("Content-Type") = "application/json"`



# Running the demo

```
sbt run

[info] Set current project to scalarack (in build file:/Users/mark/dev/scalarack/)
[info] Compiling 1 Scala source to /Users/mark/dev/scalarack/target/scala-2.11/classes...
[info] Running App
Rack started... listening for HTTP on /0.0.0.0:8080
```

[http://localhost:8080/helloworld](http://localhost:8080/helloworld)
[http://localhost:8080/notfound](http://localhost:8080/notfound)
[http://localhost:8080/welcome](http://localhost:8080/welcome)


## License

Licensed under the [MIT license](https://raw.githubusercontent.com/coder36/scalarack/master/LICENSE).
