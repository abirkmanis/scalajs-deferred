package deferred

import deferred.Implicits._
import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLRenderingContext._
import subspace.{Matrix4x4, Quaternion, Vector3}

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class Renderer(implicit val gl: raw.WebGLRenderingContext) {
  var a = 0f
  var b = 0f

  var c = 0f
  var d = 0f

  def save(): String = {
    s"$a;$b;$c;$d"
  }

  def load(state: String) = {
    val a :: b :: c :: d :: Nil = state.split(";").map(s => s.toFloat).toList
    this.a = a
    this.b = b
    this.c = c
    this.d = d
  }

  var mesh = new ObjVBO("test.obj")
  val w = 1024
  val h = 1024
  val fbo = new FrameBuffer(w, h)

  val sunDepthProgram = new FileProgram("sunDepth") {
    val output = fbo.createTexture(true)

    def draw(sunMatrix: Float32Array): Unit = {
      output.prepare()
      mesh.draw(this, () => {
        val sunMatrixLocation = gl.getUniformLocation(program, "sunMatrix")

        gl.enable(DEPTH_TEST)
        gl.enable(CULL_FACE)
        //      gl.cullFace(FRONT)
        //      gl.polygonOffset(1, 1)
        //      gl.enable(POLYGON_OFFSET_FILL)
        gl.uniformMatrix4fv(sunMatrixLocation, false, sunMatrix)
      })
      output.genMipMap()
    }

    //    override def unset(): Unit = gl.disable(POLYGON_OFFSET_FILL)
  }

  val anisotropic = gl.getExtension("EXT_texture_filter_anisotropic")
  if (anisotropic != null)
    println("Max anisotropy: " + gl.getParameter(anisotropic.asInstanceOf[js.Dynamic].MAX_TEXTURE_MAX_ANISOTROPY_EXT.asInstanceOf[Int]))
  else
    println("Anisotropic filtering is not supported")

  def setAnisotropic(a: Int) = {
    if (anisotropic != null)
      gl.texParameterf(TEXTURE_2D, anisotropic.asInstanceOf[js.Dynamic].TEXTURE_MAX_ANISOTROPY_EXT.asInstanceOf[Int], a)
  }

  val rainbow = RainbowMipMap.createTexture(1024, 0)

  val lambertProgram = new FileProgram("lambert") {
    val output = fbo.createTexture()

    def draw(special: Boolean, pvMatrix: Float32Array, sunMatrix: Float32Array, sunDirection: Vector3): Unit = {
      output.prepare()
      mesh.draw(this, () => {
        val specialLocation = gl.getUniformLocation(program, "special")
        val sunDepthLocation = gl.getUniformLocation(program, "sunDepth")
        val rainbowLocation = gl.getUniformLocation(program, "rainbow")
        val pvMatrixLocation = gl.getUniformLocation(program, "pvMatrix")
        val sunMatrixLocation = gl.getUniformLocation(program, "sunMatrix")
        val sunDirectionLocation = gl.getUniformLocation(program, "sunDirection")

        gl.enable(DEPTH_TEST)
        gl.enable(CULL_FACE)
        gl.cullFace(BACK)
        gl.uniform1i(specialLocation, if (special) 1 else 0)
        gl.activeTexture(TEXTURE0)
        gl.bindTexture(TEXTURE_2D, sunDepthProgram.output.texture)
        gl.uniform1i(sunDepthLocation, 0)
        setAnisotropic(16)
        gl.activeTexture(TEXTURE1)
        gl.bindTexture(TEXTURE_2D, rainbow)
        gl.uniform1i(rainbowLocation, 1)
        setAnisotropic(16)
        gl.uniformMatrix4fv(pvMatrixLocation, false, pvMatrix)
        gl.uniformMatrix4fv(sunMatrixLocation, false, sunMatrix)
        gl.uniform3fv(sunDirectionLocation, sunDirection.allocateBuffer)
      })
    }
  }

  //  val normalDepthProgram = new ProgramF("normal") {
  //    var pvMatrix: Float32Array = null
  //
  //    override def setUniforms = {
  //      val pvMatrixLocation = gl.getUniformLocation(program, "pvMatrix")
  //
  //      gl.enable(DEPTH_TEST)
  //      gl.enable(CULL_FACE)
  //      gl.cullFace(BACK)
  //      gl.uniformMatrix4fv(pvMatrixLocation, false, pvMatrix)
  //    }
  //  }
  //
  //  val sobelProgram = new ProgramF("tex2d3x3", "sobel") {
  //    var texture: WebGLTexture = null
  //
  //    override def setUniforms = {
  //      val textureLocation = gl.getUniformLocation(program, "texture")
  //      val wf = gl.getUniformLocation(program, "imageWidthFactor")
  //      val hf = gl.getUniformLocation(program, "imageHeightFactor")
  //
  //      gl.disable(DEPTH_TEST)
  //      gl.activeTexture(TEXTURE0)
  //      gl.bindTexture(TEXTURE_2D, texture)
  //      gl.uniform1i(textureLocation, 0)
  //      gl.uniform1f(wf, 1.0 / w)
  //      gl.uniform1f(hf, 1.0 / h)
  //    }
  //  }
  //
  //  val blurredHProgram = new ProgramF("blurredH1", "blurred") {
  //    var texture: WebGLTexture = null
  //
  //    override def setUniforms = {
  //      val textureLocation = gl.getUniformLocation(program, "texture")
  //
  //      gl.disable(DEPTH_TEST)
  //      gl.activeTexture(TEXTURE0)
  //      gl.bindTexture(TEXTURE_2D, texture)
  //      gl.uniform1i(textureLocation, 0)
  //    }
  //  }
  //
  //  val blurredVProgram = new ProgramF("blurredV1", "blurred") {
  //    var texture: WebGLTexture = null
  //
  //    override def setUniforms = {
  //      val textureLocation = gl.getUniformLocation(program, "texture")
  //
  //      gl.disable(DEPTH_TEST)
  //      gl.activeTexture(TEXTURE0)
  //      gl.bindTexture(TEXTURE_2D, texture)
  //      gl.uniform1i(textureLocation, 0)
  //    }
  //  }
  //
  //  val composeProgram = new ProgramF("tex2d", "compose") {
  //    var normalTexture: WebGLTexture = null
  //    var depthTexture: WebGLTexture = null
  //
  //    override def setUniforms = {
  //      val normalTextureLocation = gl.getUniformLocation(program, "normalTexture")
  //      val depthTextureLocation = gl.getUniformLocation(program, "depthTexture")
  //
  //      gl.disable(DEPTH_TEST)
  //      gl.activeTexture(TEXTURE0)
  //      gl.bindTexture(TEXTURE_2D, normalTexture)
  //      gl.uniform1i(normalTextureLocation, 0)
  //      gl.activeTexture(TEXTURE1)
  //      gl.bindTexture(TEXTURE_2D, depthTexture)
  //      gl.uniform1i(depthTextureLocation, 1)
  //    }
  //  }

  val textBlt = new TexBlt()

  def render(width: Int, height: Int, newActions: Set[String], allActions: Set[String]): Unit = {
    if (allActions.contains("up")) a -= 0.1f
    if (allActions.contains("down")) a += 0.1f
    if (allActions.contains("left")) b += 0.1f
    if (allActions.contains("right")) b -= 0.1f

    if (allActions.contains("up2")) c -= 0.1f
    if (allActions.contains("down2")) c += 0.1f
    if (allActions.contains("left2")) d += 0.1f
    if (allActions.contains("right2")) d -= 0.1f

    val special = allActions.contains("special")

    val eyeRot = Quaternion.forAxisAngle(Vector3(1, 0, 0), a) * Quaternion.forAxisAngle(Vector3(0, 0, 1), b)
    val sunRot = Quaternion.forAxisAngle(Vector3(1, 0, 0), c) * Quaternion.forAxisAngle(Vector3(0, 0, 1), d)

    val aspect = w.toFloat / h
    val projectionMatrix = Matrix4x4.forPerspective(math.Pi.toFloat / 2, aspect, .1f, 10)
    val viewMatrix = Matrix4x4.forTranslation(Vector3(0f, 0f, -5f)) * Matrix4x4.forRotation(eyeRot)
    val pvMatrix = (projectionMatrix * viewMatrix).allocateBuffer

    val sunViewMatrix = Matrix4x4.forRotation(sunRot)
    val k = 10
    val sunMatrix = (Matrix4x4.forOrtho(-k * aspect, k * aspect, -k, k, -k, k) * sunViewMatrix).allocateBuffer

    sunDepthProgram.draw(sunMatrix)
    lambertProgram.draw(special, pvMatrix, sunMatrix, Vector3(sunViewMatrix.c0r2, sunViewMatrix.c1r2, sunViewMatrix.c2r2))

    //    val normalTex = normalStage.draw((pvMatrix))
    //    val depthTex = depthStage.draw((normalTex))
    //    val blurredHTex = blurredHStage.draw((depthTex))
    //    val blurredTex = blurredVStage.draw((blurredHTex))
    //    val compositeTex = compositeStage.draw((lambertTex, blurredTex))

    val ws = w / 2
    val hs = h / 2
    textBlt.blt(sunDepthProgram.output.texture, width - ws, height - hs, ws, hs, 1)
    textBlt.blt(lambertProgram.output.texture, 0, 0, w, h)
    //    textBlt.blt(blurredTex, width - ws, 0, ws, hs)
    //    textBlt.blt(compositeTex, 0, 0, ws, hs)
  }
}
