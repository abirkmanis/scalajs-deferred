package deferred

import org.scalajs.dom._
import org.scalajs.dom.raw.UIEvent

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object Main extends JSApp {
  @JSExport
  override def main(): Unit = {
    val canvas = document.createElement("canvas").asInstanceOf[html.Canvas]
    document.body.appendChild(canvas)
    implicit val gl: raw.WebGLRenderingContext = canvas.getContext("webgl").asInstanceOf[raw.WebGLRenderingContext]
//    console.log(gl.getParameter(VERSION))
//    console.log(gl.getParameter(SHADING_LANGUAGE_VERSION))
//    console.log(gl.getParameter(VENDOR))
    val ext = gl.getExtension("WEBGL_debug_renderer_info")
    if (ext!=null) {
      console.log(gl.getParameter(ext.asInstanceOf[js.Dynamic].UNMASKED_VENDOR_WEBGL.asInstanceOf[Int]))
      console.log(gl.getParameter(ext.asInstanceOf[js.Dynamic].UNMASKED_RENDERER_WEBGL.asInstanceOf[Int]))
    }
//    console.log(gl.getContextAttributes())
    console.log(gl.getSupportedExtensions())

    def resize(): Unit = {
      canvas.width = document.body.clientWidth
      canvas.height = document.body.clientHeight
    }
    document.defaultView.onresize = { e: UIEvent => resize }

    resize()

    val renderer = new Renderer()

    def requestAnimation(): Unit = {
      renderer.render(canvas.width, canvas.height)
      document.defaultView.requestAnimationFrame { d: Double => requestAnimation() }
    }
    requestAnimation()
  }
}
