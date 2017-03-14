cf login -a https://api.system.asv-pr.ice.predix.io --skip-ssl-validation -u $CLOUD_USER -p $CLOUD_PASSWORD -o predix-devops-hc -s training
cf map-route ci-comp-app-1-training run.asv-pr.ice.predix.io -n t123
