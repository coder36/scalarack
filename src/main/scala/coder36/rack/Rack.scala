package coder36.rack

trait Rack {
  def call(env: Map[String,Any]) : (Int, Map[String,String], String )
}


