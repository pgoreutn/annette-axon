package annette.security.user
import com.lightbend.rp.servicediscovery.lagom.scaladsl.LagomServiceLocatorComponents
import org.scalatestplus.play.{BaseOneAppPerTest, FakeApplicationFactory, PlaySpec}
import play.api.{Application, ApplicationLoader, Environment}

import scala.concurrent.ExecutionContext.Implicits.global

class UserServiceSpec extends PlaySpec with BaseOneAppPerTest with FakeApplicationFactory {

  var webGateway: loader.WebGateway = _

  def fakeApplication(): Application = {
    // Default `mode` is `Mode.Test`
    val environment = Environment.simple()
    val context = ApplicationLoader.createContext(environment)
    webGateway = new loader.WebGateway(context) with LagomServiceLocatorComponents {}
    webGateway.application
  };

  "UserService" should {
    lazy val service = webGateway.userService

    val token = ""

    "find all users" in {
      service.findUsers(token, UserQuery())

    }
  }

}
