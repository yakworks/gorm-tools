# check for build/shipkit and clone if not there, this should come first
SHIPKIT_DIR = build/shipkit
$(shell [ ! -e $(SHIPKIT_DIR) ] && git clone -b v1.0.11 https://github.com/yakworks/shipkit.git $(SHIPKIT_DIR) >/dev/null 2>&1)
# build.sh should be set so it create the env through it.
build.sh := ./build.sh
# Shipkit.make first, which does all the lifting to create makefile.env for the BUILD_VARS
include $(SHIPKIT_DIR)/Shipkit.make
include $(SHIPKIT_DIR)/makefiles/spring-common.make
include $(SHIPKIT_DIR)/makefiles/ship-gh-pages.make
# DB = true # set this to true to turn on the DB environment options

## ci deploy, main target to call from circle
ship-it::
	make vault-decrypt
	make ci-credentials
	make ship-release
	echo $@ success

ci-credentials: config-bot-git-user kubectl-config dockerhub-login
	echo $@ success

.PHONY: ship-release

ifdef RELEASABLE_BRANCH

ship-release: build ship-libs ship-docker kube-deploy

	# this should happen last and in its own make as it will increment the version number which is used in scripts above
    # TODO it seems a bit backwards though and the scripts above should be modified
	make ship-version
	echo $@ success

kube-deploy: kube-create-ns kube-clean
	$(kube_tools) kubeApplyTpl $(APP_DIR)/src/deploy/app-configmap.tpl.yml
	$(kube_tools) kubeApplyTpl $(APP_DIR)/src/deploy/app-deploy.tpl.yml
	echo $@ success

else

ship-release:
	echo "$@ not on a RELEASABLE_BRANCH, nothing to do"

endif # end RELEASABLE_BRANCH

# the "dockmark-build" target depends on this. depend on the docmark-copy-readme to move readme to index
docmark-build-prep: docmark-copy-readme


# -- here below is for testing and debugging ---

## alias for `docker-dockmark up` to server the docs
docmark-start:
	make docker-dockmark up

# gradle restify:bootRun
# start:
# 	$(gw) restify:bootRun

PORT ?= 8080
## sanity checks api with curl -i -G http://localhost:8081/api/rally/org, pass PORT=8081 for other than default 8080
api-sanity-check:
	curl -i -G http://localhost:$(PORT)/api/rally/org/1

api-sanity-check-deployed:
	curl -i -G https://$(APP_KUBE_INGRESS_URL)/api/rally/org/1

# -- helpers --
## shows gorm-tools:dependencies --configuration compile
show-dependencies:
	# ./gradlew gorm-tools:dependencies --configuration compileClasspath
	./gradlew restify:dependencies --configuration runtime

run-benchmarks:
	$(gw) benchmarks:assemble
	cd examples/benchmarks
	java -server -Xmx3048m -XX:MaxMetaspaceSize=256m -jar \
	  -DmultiplyData=3 -Dgpars.poolsize=4 build/libs/benchmarks.war
	# -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap

