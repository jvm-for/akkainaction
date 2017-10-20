package com.damoshow.swagger.service

import javax.ws.rs.Path

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives
import akka.util.Timeout
import akka.pattern._
import com.damoshow.swagger.DefaultJsonFormats
import com.damoshow.swagger.actor.AddActor.{AddRequest, AddResponse}
import io.swagger.annotations._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * 
  */
@Api(value = "/add", produces = "application/json")
@Path("/add")
class AddService(addActor: ActorRef)(implicit ec: ExecutionContext) extends Directives with DefaultJsonFormats {

  implicit val timeout = Timeout(2.seconds)

  implicit val requestFormat = jsonFormat1(AddRequest)

  implicit val responseFormat = jsonFormat1(AddResponse)

  val route = add

  @ApiOperation(value = "Add integers", nickname = "addIntegers", httpMethod = "POST", response = classOf[AddResponse])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "\"numbers\" to sum", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Interal server error")
  ))
  def add =
    path("add") {
      post {
        entity(as[AddRequest]) { request =>
          complete { (addActor ? request).mapTo[AddResponse] }
        }
      }
    }
}
