package deferred

import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLTexture

class FrameBuffer(val w: Int, val h: Int)(implicit val gl: raw.WebGLRenderingContext) {

  case class Texture(val texture: WebGLTexture, val mipMap: Boolean) {
    def prepare = {
      gl.bindFramebuffer(FRAMEBUFFER, fb)
      gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, texture, 0)
      // todo: make depth optional
      gl.framebufferRenderbuffer(FRAMEBUFFER, DEPTH_ATTACHMENT, RENDERBUFFER, depth)
      // todo: changeable color?
      gl.clearColor(0.0, 0.0, 0.0, 0.0)
      gl.clear(COLOR_BUFFER_BIT | DEPTH_BUFFER_BIT)
      gl.viewport(0, 0, w, h)
      gl.bindTexture(TEXTURE_2D, texture)
      if (mipMap)
        gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR_MIPMAP_LINEAR)
    }
  }

  val fb = gl.createFramebuffer()
  val depth = gl.createRenderbuffer()
  gl.bindRenderbuffer(RENDERBUFFER, depth)
  gl.renderbufferStorage(RENDERBUFFER, DEPTH_COMPONENT16, w, h)

  def createTexture(mipMap: Boolean = false) = {
    val tex = gl.createTexture()
    gl.bindTexture(TEXTURE_2D, tex)
    gl.texImage2D(TEXTURE_2D, 0, RGBA, w, h, 0, RGBA, UNSIGNED_BYTE, null)

    if (mipMap)
      gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR_MIPMAP_LINEAR)
    else
      gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)

    gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR)
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)

    if (mipMap)
      gl.generateMipmap(TEXTURE_2D)

    Texture(tex, mipMap)
  }
}
