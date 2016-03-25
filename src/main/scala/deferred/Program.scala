package deferred

import org.scalajs.dom._
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLProgram, XMLHttpRequest}

trait Program {
  def getAttribLocation(name: String)(implicit gl: raw.WebGLRenderingContext): Int = gl.getAttribLocation(program, name)

  def program: WebGLProgram

  def unset(): Unit = {}
}

class LiteralProgram(vertex: String, fragment: String)(implicit val gl: raw.WebGLRenderingContext) extends Program {
  val vShader = gl.createShader(VERTEX_SHADER)
  gl.shaderSource(vShader, vertex)
  gl.compileShader(vShader)

  val fShader = gl.createShader(FRAGMENT_SHADER)
  gl.shaderSource(fShader, fragment)
  gl.compileShader(fShader)

  val program = gl.createProgram()

  gl.attachShader(program, vShader)
  gl.attachShader(program, fShader)
  gl.linkProgram(program)
}

class FileProgram(vertName: String, fragName: String)(implicit val gl: raw.WebGLRenderingContext) extends Program {
  def this(name: String)(implicit gl: raw.WebGLRenderingContext) = this(name, name)

  var inner: LiteralProgram = null
  var vert: String = null
  var frag: String = null

  def program = if (inner == null) null else inner.program

  load(vertName + ".vert", { v => vert = v; check })
  load(fragName + ".frag", { v => frag = v; check })

  def check = if (vert != null && frag != null) inner = new LiteralProgram(vert, frag)

  private def load(name: String, callback: String => Unit) = {
    val xhr = new XMLHttpRequest()
    xhr.onload = { (e: Event) =>
      if (xhr.status == 200) {
        callback(xhr.responseText)
      }
    }
    xhr.open("GET",
      name
    )
    xhr.send("")
  }
}
