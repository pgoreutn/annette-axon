package annette.shared.exceptions

class AnnetteThrowable(throwable: Throwable)
    extends AnnetteException("core.throwable",
                             Map("message" -> throwable.getMessage, "stackTrace" -> throwable.getStackTrace.map(_.toString).mkString("\n")))
