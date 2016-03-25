package deferred

trait Actions {
  def consumeCommands: Set[String]
}
