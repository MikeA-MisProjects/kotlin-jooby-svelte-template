# Jooby/Kotlin + Svelte/Bootstrap app template

A relatively lightweight modern app template using Kotlin/JVM.
For people asking me at conferences what would I recommend from technical perspective.

* Server API using Jooby
    * Postgres is used for DB (runnable using docker-compose)
    * Liquibase migrates the DB
    * Server unit tests use Junit5/Mockk
    * Repository integration tests run in in-memory H2 DB to avoid any dependencies
* UI is built with Svelte + Snowpack with TypeScript support
    * UI tests use Jest and TypeScript
* E2E tests use Selenide to drive the actual browser backed by H2 DB

## Noteworthy features

* Efficient Docker usage (cached layers in order of less frequent changes), Gradle downloads dependencies once
* Builds/tests run in Docker in several stages, test results available after build in [Jenkins](Jenkinsfile)
* [Internationalization](i18n) (both client-side and server-side)
* Supports static server-side rendered pages using [Pebble templates](ui/static)
* Selenide/Selenium tests work inside of Docker
* Automatic TypeScript types from Kotlin classes in [UI API](ui/api/types.ts) 
  (configurable in [Gradle build script](build.gradle.kts))

No frameworks needed for this:

* 30-line [dependency injection](src/app/AutoCreatingServiceRegistry.kt)
* 100-line [JDBC extensions](src/db/JdbcExtensions.kt) instead of ORM and [transaction management](src/db/ThreadLocalTransaction.kt)  
* Simple client-side [router](ui/routing/Router.ts) used in [root component](ui/App.svelte)

Testing:

* 3 layers of testing: UI (client-side components), server-side (unit + repositories) and E2E using Selenide to drive an actual browser.
* Repository tests rollback to avoid recreation of the DB each time
* E2E tests test login once and then use [fake login](src/auth/FakeLoginForTestingController.kt) to get to needed places quickly

## Running in Docker

`docker-compose up --build`

or to just start the DB:
`docker-compose up -d db`

This will bind to `127.0.0.1:55432` by default

# Development

After clone:

```
npm install
```

Then:

```
npm run watch
# or just `npm run build`
./gradlew run
```

To run tests:

* `npm test` - for UI components
* `./gradlew test` - for API
* `./gradlew e2eTest` - for in-browser End-to-End tests

## Running from IDE

Some [IntelliJ IDEA config](.idea) is committed to share code style, run configurations, etc with the team.

* Open the directory as project
* Click "Import gradle project"
* `npm run watch` will run automatically to compile changing UI assets on the fly
* Install "Svelte" plugin for working with UI components
* Choose "LauncherKt" run configuration to start the server (Jooby/Netty)

## Deployment

* [Jenkinsfile](Jenkinsfile) would deploy the app using `docker-compose`
* In addition, deployment to [Heroku](https://heroku.com) is supported using the same Docker container
* Env-specific configuration is provided using env vars (docker-compose.yml files or Heroku), according to [12-factor apps](https://12factor.net)
* All env vars are optional, so that everything would run out of the box in development

## Adding icons

Uses Feather icon set available at https://feather.netlify.com/.

To add an icon:

1. Download any icon from that repository as svg. For custom ones, use existing ones as a basis for consistency.
2. Add the icon to `public/img/icons`.
3. Generate the sprites using the run configuration in IDEA or `npm run gen-icon-sprite`
