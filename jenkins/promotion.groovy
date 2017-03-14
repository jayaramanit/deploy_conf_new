promotions {
  promotion("Development") {
    icon("star-red")
    conditions {
      manual('')
    }
    actions {
      downstreamParameterized {
        trigger("deploy-application","SUCCESS",false,["buildStepFailure": "FAILURE","failure":"FAILURE","unstable":"UNSTABLE"]) {
          predefinedProp("ENVIRONMENT","dev.microservice.com")
          predefinedProp("APPLICATION_NAME", "\${PROMOTED_JOB_FULL_NAME}")
          predefinedProp("BUILD_ID","\${PROMOTED_NUMBER}")
        }
      }
    }
  }
  promotion("QA") {
    icon("star-yellow")
    conditions {
      manual('')
      upstream("Development")
    }
    actions {
      downstreamParameterized {
        trigger("deploy-application","SUCCESS",false,["buildStepFailure": "FAILURE","failure":"FAILURE","unstable":"UNSTABLE"]) {
          predefinedProp("ENVIRONMENT","qa.microservice.com")
          predefinedProp("APPLICATION_NAME", "\${PROMOTED_JOB_FULL_NAME}")
          predefinedProp("BUILD_ID","\${PROMOTED_NUMBER}")
        }
      }
    }
  } 
  promotion("Production") {
    icon("star-green")
    conditions {
      manual('prod_admin')
      upstream("QA")
    }
    actions {
      downstreamParameterized {
        trigger("deploy-application","SUCCESS",false,["buildStepFailure": "FAILURE","failure":"FAILURE","unstable":"UNSTABLE"]) {
          predefinedProp("ENVIRONMENT","prod.microservice.com")
          predefinedProp("APPLICATION_NAME", "\${PROMOTED_JOB_FULL_NAME}")
          predefinedProp("BUILD_ID","\${PROMOTED_NUMBER}")
        }
      }
    }
  }
}
