package coder36.rack
import scala.collection.mutable.Map

trait Rack {
  def call(env: Map[String,Any]) : (Int, Map[String,String], String )
}


