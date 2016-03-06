package coder36.rack

trait Rack {
  def call(env: Map[String,String]) : (Int, Map[String,String], String )
}


