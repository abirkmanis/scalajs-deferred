package deferred

import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLRenderingContext, WebGLTexture}

import scala.scalajs.js.typedarray.Uint8Array

object RainbowMipMap {
  def createTexture(size: Int, c: Int)(implicit gl: WebGLRenderingContext): WebGLTexture = {
    val tex = gl.createTexture()
    gl.bindTexture(TEXTURE_2D, tex)
    gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, NEAREST_MIPMAP_LINEAR)
    gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, NEAREST)

    val d = 1 << c
    val w = size
    val h = size
    var ws = w
    var hs = h
    var l = 0
    while (ws >= 1 && hs >= 1) {
      println(ws + "x" + hs)
      val pixels = new Uint8Array(ws * hs * 4)
      for (i <- 0 until ws)
        for (j <- 0 until hs) {
          val b = (i + j * ws) * 4
          val x = (i * size / w / d) % 2
          val y = (j * size / h / d) % 2
          val bright = if (x + y == 1) 1 else 0
          pixels(b) = ((if (l % 2 == 0) 255 else 0) * bright).toByte
          pixels(b + 1) = ((if (l / 2 % 2 == 0) 255 else 0) * bright).toByte
          pixels(b + 2) = ((if (l / 4 % 2 == 0) 255 else 0) * bright).toByte
          pixels(b + 3) = 255.toByte
        }
      gl.texImage2D(TEXTURE_2D, l, RGBA, ws, hs, 0, RGBA, UNSIGNED_BYTE, pixels)
      ws >>= 1
      hs >>= 1
      l += 1
    }

    //    gl.texParameteri(TEXTURE_2D, TEXTURE_BASE_LEVEL, 0);
    //    gl.texParameteri(TEXTURE_2D, TEXTURE_MAX_LEVEL, d-1);

    tex
  }
}
