package deferred

import org.scalajs.dom._
import org.scalajs.dom.raw.WebGLRenderingContext._

import scala.scalajs.js.typedarray.{Float32Array, Int16Array}

case class Attribute(name: String, offset: Int, size: Int)

trait DrawBuffer {
  def draw(program: Program, setUniforms: () => Unit): Unit
}

class VBO(values: Float32Array, sizes: Seq[(String, Int)])(implicit val gl: raw.WebGLRenderingContext) extends DrawBuffer {
  val buffer = gl.createBuffer()
  gl.bindBuffer(ARRAY_BUFFER, buffer)
  gl.bufferData(ARRAY_BUFFER, values, STATIC_DRAW)
  val sizesValues = sizes.map { case (_, s) => s }
  val stride = sizesValues.sum
  val count = values.length / stride
  val offsets = sizesValues.scanLeft(0) { case (o, s) => o + s }
  val attributes = sizes.zip(offsets).map { case ((n, s), o) => Attribute(n, o, s) }

  def draw(program: Program, setUniforms: () => Unit): Unit = {
    if (program.program == null) return
    gl.useProgram(program.program)
    setUniforms()
    // todo: optimize
    gl.bindBuffer(ARRAY_BUFFER, buffer)
    attributes.foreach { a =>
      val location = program.getAttribLocation(a.name)
      if (location >= 0) {
        gl.vertexAttribPointer(location, a.size, FLOAT, false, stride * 4, a.offset * 4)
        gl.enableVertexAttribArray(location)
      }
    }
    drawCall
    attributes.foreach { a =>
      val location = program.getAttribLocation(a.name)
      if (location >= 0)
        gl.disableVertexAttribArray(location)
    }
    program.unset()
  }

  def drawCall = gl.drawArrays(TRIANGLES, 0, count)
}

class VAO(values: Float32Array, sizes: Seq[(String, Int)], indices: Int16Array)(implicit override val gl: raw.WebGLRenderingContext) extends VBO(values, sizes)(gl) {
  val ibo = gl.createBuffer()
  gl.bindBuffer(ELEMENT_ARRAY_BUFFER, ibo)
  gl.bufferData(ELEMENT_ARRAY_BUFFER, indices, STATIC_DRAW)
  val length = indices.length

  override def drawCall: Unit = {
    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, ibo)
    gl.drawElements(TRIANGLES, length, UNSIGNED_SHORT, 0)
  }
}

class ObjVBO(name: String)(implicit val gl: raw.WebGLRenderingContext) extends DrawBuffer {
  var inner = null.asInstanceOf[VBO]
  ObjReader.load(name, { v => inner = v })

  override def draw(program: Program, setUniforms: () => Unit): Unit = {
    if (inner != null) inner.draw(program, setUniforms: () => Unit)
  }
}