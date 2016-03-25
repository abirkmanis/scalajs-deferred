package deferred

import org.scalajs.dom._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.UIEvent

import scala.collection.mutable
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
    //    val ext = gl.getExtension("WEBGL_debug_renderer_info")
    //    if (ext != null) {
    //      console.log(gl.getParameter(ext.asInstanceOf[js.Dynamic].UNMASKED_VENDOR_WEBGL.asInstanceOf[Int]))
    //      console.log(gl.getParameter(ext.asInstanceOf[js.Dynamic].UNMASKED_RENDERER_WEBGL.asInstanceOf[Int]))
    //    }
    //    console.log(gl.getContextAttributes())
    console.log(gl.getSupportedExtensions())

    def resize(): Unit = {
      canvas.width = document.body.clientWidth
      canvas.height = document.body.clientHeight
    }
    window.onresize = { e: UIEvent => resize }

    resize()

    val actions = new Actions {
      val activeCommands = new mutable.HashSet[String]()
      val handledCommands = new mutable.HashSet[String]()

      {
        val codesToCommands = Map(KeyCode.W -> "up", KeyCode.A -> "left", KeyCode.S -> "down", KeyCode.D -> "right",
          KeyCode.Up -> "up2", KeyCode.Left -> "left2", KeyCode.Down -> "down2", KeyCode.Right -> "right2", KeyCode.Space -> "special")
        document.onkeydown = { e: KeyboardEvent =>
          //          console.log(e.keyCode)
          val command = codesToCommands.getOrElse(e.keyCode, "")
          activeCommands += command
        }
        document.onkeyup = { e: KeyboardEvent =>
          val command = codesToCommands.getOrElse(e.keyCode, "")
          activeCommands -= command
          handledCommands -= command
        }
      }

      def consumeCommands: Set[String] = {
        val hotCommands = activeCommands -- handledCommands
        handledCommands ++= hotCommands
        hotCommands.toSet
      }
    }

    val renderer = new Renderer()
    val cookie = window.localStorage.getItem("test")
    window.onbeforeunload = { e: BeforeUnloadEvent => window.localStorage.setItem("test", renderer.save()) }
    if (cookie != null)
      renderer.load(cookie)

    def requestAnimation(): Unit = {
      renderer.render(canvas.width, canvas.height, actions.consumeCommands, actions.handledCommands.toSet)
      document.defaultView.requestAnimationFrame { d: Double => requestAnimation() }
    }
    requestAnimation()
  }
}
