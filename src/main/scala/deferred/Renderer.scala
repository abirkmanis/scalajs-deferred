package deferred

import com.github.jpbetz.subspace.{Matrix4x4, Vector3}
import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._

import scala.scalajs.js
import scala.scalajs.js.typedarray.{Float32Array, Int16Array, Int32Array}

class Renderer(implicit val gl: raw.WebGLRenderingContext) {
  val positions = List(
    -1f, -1f, 0.5f,
    1f, -1f, 0.5f,
    1f, 1f, 0.5f,
    -1f, 1f, 0.5f
  )
  val values = new Float32Array(positions.size)
  positions.zipWithIndex.foreach { case (e, i) => values(i) = e }

  val vbo = gl.createBuffer()
  gl.bindBuffer(ARRAY_BUFFER, vbo)
  gl.bufferData(ARRAY_BUFFER, values, STATIC_DRAW)

  val ind = List(
    0, 1, 2, 0, 2, 3
  )
  val indices = new Int16Array(ind.size)
  ind.zipWithIndex.foreach { case (e, i) => indices(i) = e.toShort }

  val ibo = gl.createBuffer()
  gl.bindBuffer(ELEMENT_ARRAY_BUFFER, ibo)
  gl.bufferData(ELEMENT_ARRAY_BUFFER, indices, STATIC_DRAW)

  def compile(vertSource: String, fragSource: String) = {
    val vShader = gl.createShader(VERTEX_SHADER)
    gl.shaderSource(vShader, vertSource)
    gl.compileShader(vShader)

    val fShader = gl.createShader(FRAGMENT_SHADER)
    gl.shaderSource(fShader, fragSource)
    gl.compileShader(fShader)

    val program = gl.createProgram()
    gl.attachShader(program, vShader)
    gl.attachShader(program, fShader)
    gl.linkProgram(program)

    program
  }


  val program1 = compile(
    "uniform mat4 pvMatrix;" +
      "attribute vec3 position;" +
      "attribute vec3 normal;" +
      "varying vec3 vNormal;" +
      "void main(){" +
      "gl_Position = pvMatrix * vec4(position, 1);" +
      "vNormal = normal;" +
      "}",
    "precision highp float;" +
      "varying vec3 vNormal;" +
      "void main(){" +
      "gl_FragColor = vec4(vNormal,1);" +
      "}")

  val pvMatrixLocation = gl.getUniformLocation(program1, "pvMatrix")
  val positionLocation = gl.getAttribLocation(program1, "position")
  val normalLocation = gl.getAttribLocation(program1, "normal")

  var obj2 = (null.asInstanceOf[WebGLBuffer], 0)
  ObjReader.load("test.obj", { case (v, n) => obj2 = (v, n) })

  def scene(width: Int, height: Int): Unit = {
    gl.useProgram(program1)
    gl.clearColor(0.0, 0.0, 1.0, 1.0)
    gl.clear(COLOR_BUFFER_BIT | DEPTH_BUFFER_BIT)
    gl.enable(DEPTH_TEST)
    gl.viewport(0, 0, width, height)
    val projectionMatrix = Matrix4x4.forPerspective(math.Pi.toFloat / 2, width.toFloat / height, .1f, 10)
    val viewMatrix = Matrix4x4.forTranslation(Vector3(0f, 0f, -3f))
    val pvMatrix = projectionMatrix * viewMatrix
    import Implicits._

    gl.uniformMatrix4fv(pvMatrixLocation, false, pvMatrix.allocateBuffer)

    if (obj2._1 != null) {
      gl.bindBuffer(ARRAY_BUFFER, obj2._1)
      gl.vertexAttribPointer(positionLocation, 3, FLOAT, false, 6 * 4, 0)
      gl.enableVertexAttribArray(positionLocation)
      gl.vertexAttribPointer(normalLocation, 3, FLOAT, false, 6 * 4, 3 * 4)
      gl.enableVertexAttribArray(normalLocation)

      gl.drawArrays(TRIANGLES, 0, obj2._2)
      gl.disableVertexAttribArray(positionLocation)
      gl.disableVertexAttribArray(normalLocation)
    }
  }

  private val ext = gl.getExtension("WEBGL_draw_buffers").asInstanceOf[js.Dynamic]
  val program2 = compile(
    "attribute vec2 position;" +
      "varying vec2 vTexCoord;" +
      "void main(){" +
      "gl_Position = vec4(position, .9999, 1);" +
      "vTexCoord = position * 0.5 + 0.5;" +
      "}",
    "#extension GL_EXT_draw_buffers : require\n" +
      "precision highp float;" +
      "uniform sampler2D texture;" +
      "varying vec2 vTexCoord;" +
      "void main(){" +
      "gl_FragData[0] = texture2D(texture,vTexCoord);" +
      "gl_FragData[1] = vec4(vTexCoord,0,1);" +
      "}")

  val textureLocation = gl.getUniformLocation(program2, "texture")
  val position2Location = gl.getAttribLocation(program2, "position")

  val fb = gl.createFramebuffer()
  val w = 256
  val h = 256
  gl.activeTexture(TEXTURE0)
  val tex0 = gl.createTexture()
  gl.bindTexture(TEXTURE_2D, tex0)
  gl.texImage2D(TEXTURE_2D, 0, RGBA, w, h, 0, RGBA, UNSIGNED_BYTE, null)
  gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
  val ca0 = ext.COLOR_ATTACHMENT0_WEBGL.asInstanceOf[Int]
  val ca1 = ext.COLOR_ATTACHMENT1_WEBGL.asInstanceOf[Int]
  val tex1 = gl.createTexture()
  gl.bindTexture(TEXTURE_2D, tex1)
  gl.texImage2D(TEXTURE_2D, 0, RGBA, w, h, 0, RGBA, UNSIGNED_BYTE, null)
  gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
  gl.getExtension("WEBGL_depth_texture")
  val tex2 = gl.createTexture()
  gl.bindTexture(TEXTURE_2D, tex2)
  gl.texImage2D(TEXTURE_2D, 0, DEPTH_COMPONENT, w, h, 0, DEPTH_COMPONENT, UNSIGNED_SHORT, null)
  gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)

  def render(width: Int, height: Int): Unit = {
    //    gl.enable(CULL_FACE)

    gl.bindFramebuffer(FRAMEBUFFER, fb)
    gl.framebufferTexture2D(FRAMEBUFFER, ca0, TEXTURE_2D, tex0, 0)
    gl.framebufferTexture2D(FRAMEBUFFER, ca1, TEXTURE_2D, tex1, 0)
    gl.framebufferTexture2D(FRAMEBUFFER, DEPTH_ATTACHMENT, TEXTURE_2D, tex2, 0)
    val buffers = new Int32Array(2)
    buffers(0) = ext.COLOR_ATTACHMENT0_WEBGL.asInstanceOf[Int]
    buffers(1) = ext.COLOR_ATTACHMENT1_WEBGL.asInstanceOf[Int]
    ext.drawBuffersWEBGL(buffers)
    scene(w, h)

    gl.bindFramebuffer(FRAMEBUFFER, null)
    scene(width, height)

    gl.useProgram(program2)
    gl.bindBuffer(ARRAY_BUFFER, vbo)
    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, ibo)
    gl.disable(DEPTH_TEST)
    gl.viewport(0, height - h, w, h)
    gl.bindTexture(TEXTURE_2D, tex0)
    gl.uniform1i(textureLocation, 0)
    gl.vertexAttribPointer(position2Location, 3, FLOAT, false, 0, 0)
    gl.enableVertexAttribArray(position2Location)
    gl.drawElements(TRIANGLES, 6, UNSIGNED_SHORT, 0)
    gl.disableVertexAttribArray(position2Location)

    gl.useProgram(program2)
    gl.disable(DEPTH_TEST)
    gl.viewport(width - w, height - h, w, h)
    gl.bindTexture(TEXTURE_2D, tex2)
    gl.uniform1i(textureLocation, 0)
    gl.vertexAttribPointer(position2Location, 3, FLOAT, false, 0, 0)
    gl.enableVertexAttribArray(position2Location)
    gl.drawElements(TRIANGLES, 6, UNSIGNED_SHORT, 0)
    gl.disableVertexAttribArray(position2Location)
  }
}
