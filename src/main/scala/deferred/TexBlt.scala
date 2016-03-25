package deferred

import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLTexture

class TexBlt(implicit val gl: raw.WebGLRenderingContext) {

  import Implicits._

  val square = new VAO(Seq(
    -1f, -1f,
    1f, -1f,
    1f, 1f,
    -1f, 1f
  ), Seq(("position", 2)), Seq(0, 1, 2, 0, 2, 3))

  val program2dTex = new ProgramF[(WebGLTexture)]("tex2d", "blt") {
    override def setUniforms(uniforms: (WebGLTexture)): Unit = {
      val textureLocation = gl.getUniformLocation(program, "texture")

      val (tex) = uniforms
      gl.disable(DEPTH_TEST)
      gl.activeTexture(TEXTURE0)
      gl.bindTexture(TEXTURE_2D, tex)
      gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
      gl.uniform1i(textureLocation, 0)
    }
  }

  def blt(tex: WebGLTexture, x: Int, y: Int, w: Int, h: Int) = {
    gl.bindFramebuffer(FRAMEBUFFER, null)
    gl.viewport(x, y, w, h)
    program2dTex.draw(square, (tex))
  }
}
