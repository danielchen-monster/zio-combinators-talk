import java.util.{NoSuchElementException, Optional}

import reactor.core.publisher.{Flux, Mono}
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import zio.duration.Duration
import zio.{App, _}

import scala.concurrent.{ExecutionContext, Future}

object ExampleSuceeed extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = ZIO.succeed(ExitCode.failure)
}

object ExampleEffect extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = ZIO.effect(Optional.empty().get()).catchSome {
    case e: NoSuchElementException => ZIO.unit
  }.exitCode
}

object ExampleForever extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    console.putStrLn("test").forever.exitCode
  }
}


object ExampleRepeat extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    console.putStrLn("test").repeat(Schedule.recurs(10)).exitCode
  }
}

object ExampleTimed extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val prog = console.putStrLn("Sleeping") *> clock.sleep(Duration.fromMillis(5 * 1000))

    prog.timed.flatMap(a => console.putStrLn(a._1.toString)).exitCode
  }
}

object ExampleRetry extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val prog = for {
      _ <- console.putStrLn("Choosing bool")
      random <- ZIO.service[random.Random.Service]
      bool <- random.nextBoolean
      a <- ZIO.fail(new RuntimeException("boom")).when(!bool).unit
    } yield a

    prog.retry(Schedule.recurs(3)).exitCode
  }

}

object ExampleEventually extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val prog = for {
      _ <- console.putStrLn("Choosing bool")
      random <- ZIO.service[random.Random.Service]
      bool <- random.nextBoolean
      a <- ZIO.fail(new RuntimeException("boom")).when(!bool).unit
    } yield a

    prog.eventually.exitCode
  }

}

object ExampleUnless extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    console.putStrLn("test").unless(true).exitCode
  }

}


object ExampleMono extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val mono = Mono.just("test")
    ZIO.fromCompletionStage(mono.toFuture).map { str =>
      println(str)
      ExitCode.success
    }.exitCode
  }
}

object ExampleMonoFail extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val monoFail: Mono[String] = Mono.empty()
    ZIO.fromCompletionStage(monoFail.toFuture).map { str =>
      println(str)
      ExitCode.success
    }.exitCode
  }
}


object ExampleFlux extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    import scala.jdk.CollectionConverters._
    import zio.interop.reactivestreams._

    val flux = Flux.fromIterable(List(1, 2, 3).asJava)


    publisherToStream(flux) // due to implicits conflict
      .toStream()
      .runCollect
      .map(_.sum)
      .map(ExitCode.apply).foldCauseM(
      cause => console.putStrLnErr(cause.prettyPrint) as ExitCode.failure,
      a => ZIO.succeed(a))


  }
}

object ExampleManaged extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    ZManaged.fromAutoCloseable[Any, Throwable, DynamoDbAsyncClient](
      ZIO.effect(DynamoDbAsyncClient.create())
    ).use(client => {
      console.putStrLn(client.serviceName())
    }).exitCode
  }
}

object ExampleFuture extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val x: ExecutionContext => Future[String] = Future("success")(_)

    ZIO.fromFuture(x).flatMap(a => console.putStrLn(a)).exitCode

  }
}