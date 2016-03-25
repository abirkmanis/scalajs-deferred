package deferred

import java.nio.{ShortBuffer, ByteBuffer, ByteOrder, FloatBuffer}

import scala.scalajs.js.typedarray.{Float32Array, Int16Array, TypedArrayBufferOps}

object Implicits {
  implicit def toTypedArray(buffer: FloatBuffer): Float32Array = TypedArrayBufferOps.floatBufferOps(buffer).typedArray()

  implicit def toTypedArray1(seq: Seq[Float]): Float32Array = {
    val sizeInBytes = seq.size << 2
    val b = ByteBuffer.allocateDirect(sizeInBytes).order(ByteOrder.nativeOrder).asFloatBuffer()
    seq.foreach(b.put)
    toTypedArray(b)
  }

  implicit def toTypedArray(buffer: ShortBuffer): Int16Array = TypedArrayBufferOps.shortBufferOps(buffer).typedArray()

  implicit def toTypedArray2(seq: Seq[Int]): Int16Array = {
    val sizeInBytes = seq.size << 1
    val b = ByteBuffer.allocateDirect(sizeInBytes).order(ByteOrder.nativeOrder).asShortBuffer()
    seq.foreach { i => b.put(i.toShort) }
    toTypedArray(b)
  }
}

class Matrix4() {
  val array = new Float32Array(16)
  array(0) = 1
  array(5) = 1
  array(10) = 1
  array(15) = 1

  def this(fov: Float, aspect: Float, near: Float, far: Float) = {
    this
    val top = near * Math.tan(Math.PI / 180 * fov / 2).toFloat
    val bottom = -top
    val right = top * aspect
    val left = -right
    array(0) = 2 * near / (right - left)
    array(5) = 2 * near / (top - bottom)
    array(10) = -(far + near) / (far - near)
    array(11) = -1
    array(14) = -2 * (far * near) / (far - near)
    array(15) = 0
  }

  def this(sx: Float, sy: Float) = {
    this
    array(0) = sx
    array(5) = sy
  }

  def this(dx: Float, dy: Float, dz: Float) = {
    this
    array(12) = dx
    array(13) = dy
    array(14) = dz
  }

  def asArray = array

  def mul(o: Matrix4) = {
    val m = new Matrix4()
    for (i <- 0 to 3)
      for (j <- 0 to 3)
        m.array(i + j * 4) = (0 to 3).map(k => array(i + k * 4) * o.array(k + j * 4)).sum
    m
  }
}
