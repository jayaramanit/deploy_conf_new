/*
 Create APM Deployer jobs
*/
// Create DevOps folder
String baseFolder = 'APM'
String repoName = 'https://2e33fad869d74a756617f3d19bc602585ebb38bc:x-oauth-basic@github.build.ge.com/APM/'
String artifactFolder = 'https://devcloud.swcoe.ge.com/artifactory/APM'

def folderlists = ["Alerts", "Avid", "ADA", "Cases", "Common", "SmartSignal", "Tub"]
def joblists = ["Deploy", "Deploy-Single-App-Dev", "Promote-Env-to-Env", "Remove"]
def envlists = ["DEV"]
def branchlists = ["master"]

// Create baseFolder
/*
folder(baseFolder) {
  description 'Create the base folder.'
}
*/
// Create the rest of folders

folderlists.each {
  folder("${baseFolder}/$it") {
    description ""
  }

  // Create the jobs under each folder
  createDeployJob("${baseFolder}", "$it", joblists[0], envlists[0], branchlists[0])
  createDeploySAJob("${baseFolder}", "$it", joblists[1], envlists[0], branchlists[0])
  createPromJob("${baseFolder}", "$it", joblists[2], envlists[0])
  createRemoveJob("${baseFolder}", "$it", joblists[3])
}

def createDeployJob(basePath, folder, jobname, env, branch) {
  job("${basePath}/${folder}/${jobname}") {
    description("Deploying job")
    displayName("${jobname}")
    parameters {
      stringParam('COMPOSITE_APP_NAME', "${folder}", '')
      stringParam('TO_ENV', "${env}", "Deploy to ${env}")
      stringParam('VC_BRANCH', "${branch}", "using ${branch} branch")
    }

    triggers {
      githubPush()
    }

    wipeOutWorkspace = true

    steps {
      shell '''
export JAVA_HOME=${JAVA_HOME_ORACLEJDK8}
export PATH=${JAVA_HOME}/bin:$PATH
export http_proxy=http://sjc1intproxy01.crd.ge.com:8080
export https_proxy=$http_proxy

mkdir -p deployer

 wget --quiet --user 502397503 --password AP9TiZGWALUcfngpkZkbdtREWsL https://devcloud.swcoe.ge.com/artifactory/UQWKB/deployer/release/composite-deployer-install.zip -P deployer

 unzip -q deployer/composite-deployer-install.zip -d .

 echo $(pwd)
 export COMPOSITE_DEPLOYER_HOME="${WORKSPACE}"

 export GIT_REPO=github.build.ge.com/hyperion-ic/deploy_conf.git

 cd "${WORKSPACE}"
 python ./bin/deployCompositeApp.py --workspace "${WORKSPACE}" --compAppName $COMPOSITE_APP_NAME \\
              --env $TO_ENV
              --gitToken $GIT_TOKEN --gitRepo $GIT_REPO --gitBranch $VC_BRANCH \\
              --aUsername $ARTIFACT_STORE_USER --aPassword $ARTIFACT_STORE_PASSWORD  \\
              --cfUsername $CLOUD_USER --cfPassword $CLOUD_PASSWORD \\
              --retries 1 --verbose  -f # --usePythonDeployer 
             
echo "exit code: $?"
      '''
    }

    publishers {
      mailer('someone@ge.com', true, true)
    }
 }
}

def createDeploySAJob(basePath, folder, jobname, env, branch) {
  job("${basePath}/${folder}/${jobname}") {
    description("Deploying Single App job to ${env}")
    displayName("${jobname}")
    parameters {
      stringParam('COMPOSITE_APP_NAME', "${folder}", '')
      stringParam('TO_ENV', "${env}", "Deploy to ${env}")
      stringParam('VC_BRANCH', "${branch}", "using ${branch} branch")
      stringParam('UPDATE_APP_NAME', "", "")
      stringParam('UPDATE_BUILD_ID', "", "")
    }

    triggers {
      githubPush()
    }

    wipeOutWorkspace = true

    steps {
      shell '''
        export JAVA_HOME=${JAVA_HOME_ORACLEJDK8}
        export PATH=${JAVA_HOME}/bin:$PATH
        export http_proxy=http://sjc1intproxy01.crd.ge.com:8080
        export https_proxy=$http_proxy
        export HTTP_PROXY=$http_proxy
        export HTTPS_PROXY=$http_proxy
        export no_proxy=devcloud.sw.ge.com,openge.ge.com,github.sw.ge.com,localhost,127.0.0.1,api.grc-apps.svc.ice.ge.com,login.grc-apps.svc.ice.ge.com,loggregator.grc-apps.svc.ice.ge.com,uaa.grc-apps.svc.ice.ge.com,console.grc-apps.svc.ice.ge.com,192.168.50.4,xip.io

        export COMPOSITE_DEPLOYER_HOME="${WORKSPACE}"

        export GIT_REPO=github.build.ge.com/APM/deploy_conf.git

        mkdir -p deployer

        wget --quiet --user 502397503 --password AP9TiZGWALUcfngpkZkbdtREWsL https://devcloud.swcoe.ge.com/artifactory/UQWKB/deployer/release/composite-deployer-install.zip -P deployer
        wget https://api.system.aws-usw02-pr.ice.predix.io/info
        #wget https://api.grc-apps.svc.ice.ge.com/info
        unzip -q deployer/composite-deployer-install.zip -d .

        echo $(pwd)


        cd "${WORKSPACE}"
        python ./bin/deployCompositeApp.py --workspace "${WORKSPACE}" --compAppName $COMPOSITE_APP_NAME \\
             --appToUpdate $UPDATE_APP_NAME --newBuildNum $UPDATE_BUILD_ID \\
             --env $TO_ENV  \\
             --gitToken $GIT_TOKEN --gitRepo $GIT_REPO --gitBranch $VC_BRANCH \\
             --aUsername $ARTIFACT_STORE_USER --aPassword $ARTIFACT_STORE_PASSWORD  \\
             --cfUsername $CLOUD_USER --cfPassword $CLOUD_PASSWORD \\
             --retries 1 --verbose  #-f  #--usePythonDeployer

        echo "exit code: $?"
      '''.stripIndent().trim()
    }

    publishers {
      mailer('someone@ge.com', true, true)
    }
  }
}

def createPromJob(basePath, folder, jobname, env) {
  job("${basePath}/${folder}/${jobname}") {
    description("Promoting job to ${env}")
    displayName("${jobname}")
    parameters {
      stringParam('BUILD_NUMBER', "", '')
      stringParam('TO_ENV', "${env}", "Deploy to ${env}")
    }
  /*
  publishers {
    buildPipelineTrigger('build11', 'build2') {
      parameters {
        predefinedProp('BUILD_NUMBER', '$BUILD_NUMBER')
      }
    }
  */
  }
}

def createRemoveJob(basePath, folder, jobname) {
  job("${basePath}/${folder}/${jobname}") {
    description("Deleting jobs")
    displayName("${jobname}")
    /*
    publishers {
      buildPipelineTrigger('build11', 'build2') {
        parameters {
          predefinedProp('BUILD_NUMBER', '$BUILD_NUMBER')
        }
      }
    */
  }
}