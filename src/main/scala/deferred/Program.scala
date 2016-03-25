package deferred

trait Program[U] {
  def setUniforms(uniforms: U)
  def unset()

  def draw(drawBuffer: DrawBuffer, uniforms: U)
}
