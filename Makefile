build.sh := ./build.sh
# DB = true # set this to true to turn on the DB environment options
shResults := $(shell $(build.sh)) # call build.sh first without args which will git clone scripts to build/bin
# core include, creates the makefile.env for the BUILD_VARS that evrything else depends on
include ./build/bin/Makefile-core.make # core includes
# --- variables ---
BOT_USER ?= 9cibot@9ci.com
VAULT_PROJECT ?= https://github.com/9ci/vault.git

# --- helper makefiles ---
include $(BUILD_BIN)/makefiles/spring-common.make

.PHONY: publish-release
# runs the full release publish, empty targets so make doesn't blow up when not a RELEASABLE_BRANCH
publish-release:

.PHONY: publish-release
## kubectl apply tpl.yml files to deploy to rancher/kubernetes
kube-deploy:

ifdef RELEASABLE_BRANCH

  publish-release: publish-lib
	@if [ ! "$(IS_SNAPSHOT)" ]; then \
		echo "not a snapshot ... doing version bump, changelog and tag push"; \
		$(MAKE) release-tag; \
	fi;

  kube-deploy: kube-create-ns
	@$(kube_tools) kubeApplyTpl $(APP_DIR)/src/deploy/app-configmap.tpl.yml
	@$(kube_tools) kubeApplyTpl $(APP_DIR)/src/deploy/app-deploy.tpl.yml

endif # end RELEASABLE_BRANCH

# used to test that yaml tpl is generated properly
kube-check-yaml:
	@$(kube_tools) process_tpl $(APP_DIR)/src/deploy/app-configmap.tpl.yml
	@$(kube_tools) process_tpl $(APP_DIR)/src/deploy/app-deploy.tpl.yml


# the "dockmark-build" target depends on this. depend on the docmark-copy-readme to move readme to index
docmark-build-prep: docmark-copy-readme

## alias for `docker-dockmark up` to server the docs
docmark-start:
	make docker-dockmark up

# gradle restify:bootRun
# start:
# 	$(gw) restify:bootRun

PORT ?= 8080
## sanity checks api with curl -i -G http://localhost:8081/api/rally/org, pass PORT=8081 for other than default 8080
curl-sanity-check:
	curl -i -G http://localhost:$(PORT)/api/rally/org/1

curl-sanity-check-deployed:
	curl -i -G https://$(APP_KUBE_INGRESS_URL)/api/rally/org/1

# -- helpers --
## shows gorm-tools:dependencies --configuration compile
show-dependencies:
	# ./gradlew gorm-tools:dependencies --configuration compileClasspath
	./gradlew restify:dependencies --configuration runtime

run-benchmarks:
	@ $(gw) benchmarks:assemble
	@ cd examples/benchmarks; \
	java -server -Xmx3048m -XX:MaxMetaspaceSize=256m -jar \
	  -DmultiplyData=3 -Dgpars.poolsize=4 build/libs/benchmarks.war
	@ # -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap

