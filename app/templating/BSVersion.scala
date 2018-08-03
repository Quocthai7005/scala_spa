package templating

object BSVersion {
  final val code = "1.1.2-P26-B4"
  final val library = "1.1"
  final val play = "Play 2.6"
  final val play_code = "2.6"
  final val bootstrap = "Bootstrap 4"
  final val bootstrap_code = "4"

  final val repositoryBase = "master/play26-bootstrap4/module"

  final val repository = "https://github.com/adrianhurt/play-bootstrap"
  def repositoryPath(path: String) = s"$repository/$path"
  def repositoryFile(file: String) = s"$repository/blob/$repositoryBase/$file"
  def repositoryFolder(folder: String) = s"$repository/tree/$repositoryBase/$folder"

  final val msgsName = "msgsProv"
  final val msgsClass = "MessagesProvider"
  final val msgsArg = s"$msgsName: $msgsClass"
}
