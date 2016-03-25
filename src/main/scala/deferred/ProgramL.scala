package deferred

import org.scalajs.dom._
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.XMLHttpRequest


abstract class ProgramF[U](vertName: String, fragName: String)(implicit val gl: raw.WebGLRenderingContext) extends Program[U] {
  def this(name: String)(implicit gl: raw.WebGLRenderingContext) = this(name, name)

  var inner: ProgramL[U] = null
  var vert: String = null
  var frag: String = null

  def program = inner.program

  load(vertName + ".vert", { v => vert = v; check })
  load(fragName + ".frag", { v => frag = v; check })

  def check = if (vert != null && frag != null) inner = new ProgramL[U](vert, frag) {
    override def setUniforms(uniforms: U): Unit = ProgramF.this.setUniforms(uniforms)
  }

  override def unset(): Unit = {}

  def draw(drawBuffer: DrawBuffer, uniforms: U): Unit = if (inner != null) inner.draw(drawBuffer, uniforms)

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

abstract class ProgramL[U](vertex: String, fragment: String)(implicit val gl: raw.WebGLRenderingContext) extends Program[U] {
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

  override def unset(): Unit = {}

  def draw(drawBuffer: DrawBuffer, uniforms: U): Unit = {
    gl.useProgram(program)
    setUniforms(uniforms)
    drawBuffer.draw(program)
    unset()
  }
}
