# deploy_conf
# Composite apps for testing composite Deployer
Because we tend to use the same Cloud Foundry Org and Space for testing multiple functions
of the deployer we need to separate the app names to avoid conflicts. We have two major use cases
at this time for the Composite Deployer:
### Promote a composite app from one environment to another
For this purpose we use ci-comp-app to test promotion from DEV to QA
### Promote a single app into DEV
For this purpose we use ci-promote-single to test promotion of a single app into DEV
# deploy_conf_master1
