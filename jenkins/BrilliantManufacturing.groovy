/*
 Create the Brilliant Manufacturing pipelines
*/

String baseFolder = 'BrilliantManufacturing'

// Create the root folder
folder("$baseFolder") {
  displayName("$baseFolder")
  description("$baseFolder")
}

def slurper = new ConfigSlurper()
// fix classloader problem using ConfigSlurper in job dsl
slurper.classLoader = this.class.classLoader
def config = slurper.parse(readFileFromWorkspace('jenkins/BrilliantManufacturing.dsl'))

// create job for every microservice
config.microservices.each { name, data ->
  createBuildJob("$baseFolder", name, data)
  createITestJob("$baseFolder", name, data)
  createETestJob("$baseFolder", name, data)
  createDeployJob("$baseFolder", name, data)
}

def microservicesByGroup = config.microservices.groupBy { name, data -> data.group } 

// create nested build pipeline view
nestedView("$baseFolder/Pipeline") { 
   description('Shows the service build pipelines')
   columns {
      status()
      weather()
   }
   views {
      microservicesByGroup.each { group, services ->
         nestedView("$baseFolder/${group}") {
            description('Shows the service build pipelines')
            columns {
               status()
               weather()
            }
            views {
               def innerNestedView = delegate
               services.each { service, data ->
                  innerNestedView.buildPipelineView("pipeline-${service}") {
                    selectedJob("$baseFolder/${service}-build")
                    triggerOnlyLatestJob(true)
    	              alwaysAllowManualTrigger(true)
                    showPipelineParameters(true)
                    showPipelineParametersInHeaders(true)
                    showPipelineDefinitionHeader(true)
    	              startsWithParameters(true)
                  }
               }
            }
         }
      }
   }
}


def createBuildJob(base, name,data) {
  // Create the folder
  folder("${base}/${name}") {
    displayName("${name}")
    description("${name}")
  }

  freeStyleJob("${base}/${name}/${name}-build") {
    description("CI build for the application: $name.") 
    displayName("${name}-build")

    scm {
      git {
        remote {
          url("$repoName" + data.repo)
        }
        branch(data.branch)
      }
    }

    triggers {
      cron('@hourly')
      githubPush()
    }

    // Delete workspace before build start
    wrappers {
      preBuildCleanup()
    }
  
    steps {
      shell '''
        rm -rf ~/.m2/repository/com/ge
        chmod +x build.sh
        ./build.sh ${BUILD_NUMBER}
      '''.stripIndent().trim()
      }

    publishers {
      archiveJunit('target/surefire-reports/*.xml')
      downstream ("${name}-Itest", 'SUCCESS')
    }
  }
}


def createITestJob(base, name,data) {
  freeStyleJob("${base}/${name}/${name}-Itest") {
    description("CI test for the application: $name.") 
    displayName("${name}-Itest")

    scm {
      git {
        remote {
          url("$repoName" + data.repo)
        }
        branch(data.branch)
      }
    }
  
    steps {
      shell '''
        echo Starting itest.sh script
        chmod a+x *.sh
        ./itest.sh
      '''.stripIndent().trim()
      }

    publishers {
      downstream("${name}-deploy", 'SUCCESS')
    }
  }
}

def createETestJob(base, name, data) {
  freeStyleJob("${base}/${name}/${name}-Etest") {
    description("E2E test for the application: $name.") 
    displayName("${name}-Etest")

    scm {
      git {
        remote {
          url("$repoName" + data.repo)
        }
        branch(data.branch)
      }
    }
  
    steps {
      shell '''
        echo Starting etest.sh script
        chmod a+x *.sh
        ./etest.sh
      '''.stripIndent().trim()
      }
  }
}

def createDeployJob(base, name, data) {
  freeStyleJob("${base}/${name}/${name}-deploy") {
    description("Deploy the artifacts from ${name}")
    displayName("${name}-deploy")

    parameters {
      stringParam('FROM_ENV', '', "Deploy from")
      stringParam("COMPOSITE_APP_NAME", "bm", "")
      stringParam("ENVIRONMENT", "DEV", "")
      stringParam("VC_BRANCH", "master", "")
      stringParam("UPDATE_APP_NAME", "hello-service", "")
      stringParam("UPDATE_BUILD_ID", '${BUILD_NUMBER}', "")
      choiceParam('TOEnv', ['Dev', 'QA', 'Demo', 'INT', 'PreProd', 'Prod'])
    }

    scm {
      git {
        remote {
          url("$repoName" + data.repo)
        }
        branch(data.branch)
      }
    }

    steps {
      shell """
        echo Starting the ${name} deployment to
        chmod a+x *.sh
        ./deploy.sh
      """.stripIndent().trim()
    }
  }
}
