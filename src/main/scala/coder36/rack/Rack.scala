package coder36.rack

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import scala.collection.mutable.Map

trait Rack {
  def call(env: Map[Symbol,Any]) : (Int, Map[String,String], String )
}




