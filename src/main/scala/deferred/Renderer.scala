package deferred

import deferred.Implicits._
import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLTexture
import subspace.{Matrix4x4, Quaternion, Vector3}

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

  val sunDepthProgram = new ProgramF[(Float32Array)]("sunDepth") {
    override def setUniforms(uniforms: (Float32Array)): Unit = {
      val sunMatrixLocation = gl.getUniformLocation(program, "sunMatrix")

      gl.enable(DEPTH_TEST)
      gl.enable(CULL_FACE)
      //      gl.cullFace(FRONT)
      //      gl.polygonOffset(1, 1)
      //      gl.enable(POLYGON_OFFSET_FILL)
      val (sunMatrix) = uniforms
      gl.uniformMatrix4fv(sunMatrixLocation, false, sunMatrix)
    }

    //    override def unset(): Unit = gl.disable(POLYGON_OFFSET_FILL)
  }

  val normalDepthProgram = new ProgramF[(Float32Array)]("normal") {
    override def setUniforms(uniforms: (Float32Array)): Unit = {
      val pvMatrixLocation = gl.getUniformLocation(program, "pvMatrix")

      gl.enable(DEPTH_TEST)
      gl.enable(CULL_FACE)
      gl.cullFace(BACK)
      val (pvMatrix) = uniforms
      gl.uniformMatrix4fv(pvMatrixLocation, false, pvMatrix)
    }
  }

  val lambertProgram = new ProgramF[(Boolean, WebGLTexture, Float32Array, Float32Array, Vector3)]("lambert") {
    override def setUniforms(uniforms: (Boolean, WebGLTexture, Float32Array, Float32Array, Vector3)): Unit = {
      val noShadowLocation = gl.getUniformLocation(program, "noShadow")
      val sunDepthLocation = gl.getUniformLocation(program, "sunDepth")
      val pvMatrixLocation = gl.getUniformLocation(program, "pvMatrix")
      val sunMatrixLocation = gl.getUniformLocation(program, "sunMatrix")
      val sunDirectionLocation = gl.getUniformLocation(program, "sunDirection")

      gl.enable(DEPTH_TEST)
      gl.enable(CULL_FACE)
      gl.cullFace(BACK)
      val (noShadow, sunDepthTexture, pvMatrix, sunMatrix, sunDirection) = uniforms
      gl.uniform1i(noShadowLocation, if (noShadow) 1 else 0)
      gl.activeTexture(TEXTURE0)
      gl.bindTexture(TEXTURE_2D, sunDepthTexture)
      gl.uniform1i(sunDepthLocation, 0)
      gl.uniformMatrix4fv(pvMatrixLocation, false, pvMatrix)
      gl.uniformMatrix4fv(sunMatrixLocation, false, sunMatrix)
      gl.uniform3fv(sunDirectionLocation, sunDirection.allocateBuffer)
    }
  }

  val sobelProgram = new ProgramF[(WebGLTexture)]("tex2d3x3", "sobel") {
    override def setUniforms(uniforms: (WebGLTexture)): Unit = {
      val textureLocation = gl.getUniformLocation(program, "texture")
      val wf = gl.getUniformLocation(program, "imageWidthFactor")
      val hf = gl.getUniformLocation(program, "imageHeightFactor")

      val (texture) = uniforms
      gl.disable(DEPTH_TEST)
      gl.activeTexture(TEXTURE0)
      gl.bindTexture(TEXTURE_2D, texture)
      gl.uniform1i(textureLocation, 0)
      gl.uniform1f(wf, 1.0 / w)
      gl.uniform1f(hf, 1.0 / h)
    }
  }

  val blurredHProgram = new ProgramF[(WebGLTexture)]("blurredH1", "blurred") {
    override def setUniforms(uniforms: (WebGLTexture)): Unit = {
      val textureLocation = gl.getUniformLocation(program, "texture")

      val (texture) = uniforms
      gl.disable(DEPTH_TEST)
      gl.activeTexture(TEXTURE0)
      gl.bindTexture(TEXTURE_2D, texture)
      gl.uniform1i(textureLocation, 0)
    }
  }

  val blurredVProgram = new ProgramF[(WebGLTexture)]("blurredV1", "blurred") {
    override def setUniforms(uniforms: (WebGLTexture)): Unit = {
      val textureLocation = gl.getUniformLocation(program, "texture")

      val (texture) = uniforms
      gl.disable(DEPTH_TEST)
      gl.activeTexture(TEXTURE0)
      gl.bindTexture(TEXTURE_2D, texture)
      gl.uniform1i(textureLocation, 0)
    }
  }

  val composeProgram = new ProgramF[(WebGLTexture, WebGLTexture)]("tex2d", "compose") {
    override def setUniforms(uniforms: (WebGLTexture, WebGLTexture)): Unit = {
      val normalTextureLocation = gl.getUniformLocation(program, "normalTexture")
      val depthTextureLocation = gl.getUniformLocation(program, "depthTexture")

      val (normalTexture, depthTexture) = uniforms
      gl.disable(DEPTH_TEST)
      gl.activeTexture(TEXTURE0)
      gl.bindTexture(TEXTURE_2D, normalTexture)
      gl.uniform1i(normalTextureLocation, 0)
      gl.activeTexture(TEXTURE1)
      gl.bindTexture(TEXTURE_2D, depthTexture)
      gl.uniform1i(depthTextureLocation, 1)
    }
  }

  var mesh = new ObjVBO("test.obj")
  val w = 1024
  val h = 1024
  val fbo = new FrameBuffer(w, h)
  val normalStage = fbo.addStage(normalDepthProgram, mesh)
  val sunDepthStage = fbo.addStage(sunDepthProgram, mesh, true)
  val lambertStage = fbo.addStage(lambertProgram, mesh)
  val textBlt = new TexBlt()
  val depthStage = fbo.addStage(sobelProgram, textBlt.square)
  val blurredHStage = fbo.addStage(blurredHProgram, textBlt.square)
  val blurredVStage = fbo.addStage(blurredVProgram, textBlt.square)
  val compositeStage = fbo.addStage(composeProgram, textBlt.square)

  def render(width: Int, height: Int, newActions: Set[String], allActions: Set[String]): Unit = {
    if (allActions.contains("up")) a -= 0.1f
    if (allActions.contains("down")) a += 0.1f
    if (allActions.contains("left")) b += 0.1f
    if (allActions.contains("right")) b -= 0.1f

    if (allActions.contains("up2")) c -= 0.1f
    if (allActions.contains("down2")) c += 0.1f
    if (allActions.contains("left2")) d += 0.1f
    if (allActions.contains("right2")) d -= 0.1f

    val noShadow = allActions.contains("shadow")

    val eyeRot = Quaternion.forAxisAngle(Vector3(1, 0, 0), a) * Quaternion.forAxisAngle(Vector3(0, 0, 1), b)
    val sunRot = Quaternion.forAxisAngle(Vector3(1, 0, 0), c) * Quaternion.forAxisAngle(Vector3(0, 0, 1), d)

    val aspect = w.toFloat / h
    val projectionMatrix = Matrix4x4.forPerspective(math.Pi.toFloat / 2, aspect, .1f, 10)
    val viewMatrix = Matrix4x4.forTranslation(Vector3(0f, 0f, -5f)) * Matrix4x4.forRotation(eyeRot)
    val pvMatrix = (projectionMatrix * viewMatrix).allocateBuffer

    val sunViewMatrix = Matrix4x4.forRotation(sunRot)
    val k = 10
    val sunMatrix = (Matrix4x4.forOrtho(-k * aspect, k * aspect, -k, k, -k, k) * sunViewMatrix).allocateBuffer

    val sunDepthTex = sunDepthStage.draw((sunMatrix))
    val lambertTex = lambertStage.draw((noShadow, sunDepthTex,
      pvMatrix, sunMatrix,
      Vector3(sunViewMatrix.c0r2, sunViewMatrix.c1r2, sunViewMatrix.c2r2)))
//    val normalTex = normalStage.draw((pvMatrix))
//    val depthTex = depthStage.draw((normalTex))
//    val blurredHTex = blurredHStage.draw((depthTex))
//    val blurredTex = blurredVStage.draw((blurredHTex))
//    val compositeTex = compositeStage.draw((lambertTex, blurredTex))

    val ws = w / 2
    val hs = h / 2
    textBlt.blt(sunDepthTex, width - ws, height - hs, ws, hs)
    textBlt.blt(lambertTex, 0, 0, w, h)
    //    textBlt.blt(blurredTex, width - ws, 0, ws, hs)
    //    textBlt.blt(compositeTex, 0, 0, ws, hs)
  }
}
