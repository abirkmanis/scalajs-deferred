package deferred

import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLTexture

class TexBlt(implicit val gl: raw.WebGLRenderingContext) {

  import Implicits._

  val useLOD = gl.getExtension("EXT_shader_texture_lod") != null
  val program2dTex = new FileProgram("tex2d", if (useLOD) "bltLOD" else "blt") {
    val square = new VAO(Seq(
      -1f, -1f,
      1f, -1f,
      1f, 1f,
      -1f, 1f
    ), Seq(("position", 2)), Seq(0, 1, 2, 0, 2, 3))

    def draw(tex: WebGLTexture, lod: Float) = {
      square.draw(this, () => {
        val textureLocation = gl.getUniformLocation(program, "texture")

        gl.disable(DEPTH_TEST)
        gl.activeTexture(TEXTURE0)
        gl.bindTexture(TEXTURE_2D, tex)
        gl.uniform1i(textureLocation, 0)
        if (useLOD)
          gl.uniform1f(gl.getUniformLocation(program, "lod"), lod)
      })
    }
  }

  def blt(tex: WebGLTexture, x: Int, y: Int, w: Int, h: Int, lod: Float = 0) = {
    gl.bindFramebuffer(FRAMEBUFFER, null)
    gl.viewport(x, y, w, h)
    program2dTex.draw(tex, lod)
  }
}
