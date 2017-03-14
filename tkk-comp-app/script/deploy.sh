#!/bin/sh

#
# Template for specific my_deploy.sh for a given composite applicaiton.

#
# Pre-requisites: expects the following env variables to have been set:
#
#   COMPOSITE_DEPLOYER_HOME  - The root of the composite deployer installation.
#   CLOUD_USER               - The cloud provider user. Not needed for stub cloud provider.
#   CLOUD_PASSWORD           - The cloud provider password. Not needed for stub cloud provider.
#   ARTIFACT_STORE_USER      - The artifact store user. Not needed for file-based artifact store.
#   ARTIFACT_STORE_PASSWORD  - The artifact store password. Not needed for file-based artifact store.
#

if [ -z "$COMPOSITE_DEPLOYER_HOME" ]; then
    echo "required env var COMPOSITE_DEPLOYER_HOME not set - aborting"
    exit -1
fi

# Include the generic blue-green deploy function.
. "$COMPOSITE_DEPLOYER_HOME"/bin/generic-deploy.sh

name="ci-comp-app"   # Change to the name of your composite app.

deploy_usage() {
    echo "usage: $0 -e env -b branch -v version -l deployment_configs_root -s space"
    exit -1
}

while [ $# -gt 0 ]; do
    case "$1" in
        -e) env=$2; shift; shift;;
        -b) branch=$2; shift; shift;;
        -v) version=$2; shift; shift;;
        -l) root=$2; shift; shift;;
        -s) space=$2; shift; shift;;
        *) deploy_usage; break;;
    esac
done

[ -n "$env" ] || abort "required parameter env (-e) missing"
[ -n "$branch" ] || abort "required parameter branch (-b) missing"
[ -n "$version" ] || abort "required parameter version (-v) missing"
[ -n "$env" ] || abort "required parameter env (-e) missing"
[ -n "$space" ] || abort "required parameter space (-s) missing"
[ -n "$root" ] || abort "required parameter locator [root of composites] (-l) missing"

[ -d "$root" ] || abort "composite app locator $root is not a directory"

#
# Override the blue-green deploy hooks.
#
pre_start() { 
    echo "=========================================="
    echo "========= custom pre_start =============="; 
    echo "=========================================="
}

pre_publish() { 
    echo "=========================================="
    echo "========= custom pre_publish ============="; 
    echo "=========================================="
}

pre_commit() { 
    echo "=========================================="
    echo "========= custom pre_commit =============="; 
    echo "=========================================="
}

echo "deploying composite app $name for env $env to space $space from $root"

#
# Run the deployer.
#
blue_green \
    --name $name \
    --env $env \
    --branch $branch \
    --version $version \
    --locator $root \
    --space $space 

