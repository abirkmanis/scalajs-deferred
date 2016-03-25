package deferred

import org.scalajs.dom.raw.XMLHttpRequest
import org.scalajs.dom.{Event, raw}

import scala.collection.mutable
import scala.scalajs.js.RegExp

object ObjReader {
  val vertexPattern = RegExp("^v\\s+([\\d|\\.|\\+|\\-|e|E]+)\\s+([\\d|\\.|\\+|\\-|e|E]+)\\s+([\\d|\\.|\\+|\\-|e|E]+)")
  val normalPattern = RegExp("^vn\\s+([\\d|\\.|\\+|\\-|e|E]+)\\s+([\\d|\\.|\\+|\\-|e|E]+)\\s+([\\d|\\.|\\+|\\-|e|E]+)")
  //  val facePattern1 = RegExp("^f\\s+(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)(?:\\s+(-?\\d+))?")
  val facePattern = RegExp("^f\\s+(\\d+)\\/\\/(\\d+)\\s+(\\d+)\\/\\/(\\d+)\\s+(\\d+)\\/\\/(\\d+)(?:\\s+(\\d+)\\/\\/(\\d+))?")

  def load(name: String, callback: VBO => Unit)(implicit gl: raw.WebGLRenderingContext) = {
    val xhr = new XMLHttpRequest()

    xhr.onload = { (e: Event) =>
      if (xhr.status == 200) {
        val lines = xhr.responseText.split("\n")
        val positions = mutable.Buffer[Float]()
        val normals = mutable.Buffer[Float]()
        val attributes = mutable.Buffer[Float]()
        def addTri(ix: Seq[Int]) = {
          for (i <- 0 to 2) {
            for (j <- 0 to 2) {
              attributes += positions(ix(i * 2) * 3 + j)
            }
            for (j <- 0 to 2) {
              attributes += normals(ix(i * 2 + 1) * 3 + j)
            }
          }
        }
        lines.foreach { l =>
          val mt = vertexPattern.exec(l)
          if (mt != null) {
            val coords = (1 to 3).map { i => mt(i).get.toFloat }
            positions ++= coords
          }
          else {
            val mn = normalPattern.exec(l)
            if (mn != null) {
              val coords = (1 to 3).map { i => mn(i).get.toFloat }
              normals ++= coords
            }
            else {
              val mf = facePattern.exec(l)
              if (mf != null) {
                val ix = (1 to 8).flatMap { i => mf(i).toOption }.map(s => s.toInt - 1)
                if (ix.size == 6)
                  addTri(ix)
                else if (ix.size == 8) {
                  addTri(ix.slice(0, 6))
                  addTri(ix.slice(0, 2) ++ ix.slice(4, 8))
                }
              }
            }
          }
        }

        import Implicits._
        val vbo = new VBO(attributes, Seq("position" -> 3, "normal" -> 3))
        println(s"${positions.size / 3} positions, ${normals.size / 3} normals, ${attributes.size} floats, ${attributes.size / 6} vertices")
        // todo: provide indices
        callback(vbo)
      }
    }
    xhr.open("GET",
      name
    )
    xhr.send("")
  }
}
