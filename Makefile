# check for build/shipkit and clone if not there, this should come first
SHIPKIT_DIR = build/shipkit
$(shell [ ! -e $(SHIPKIT_DIR) ] && git clone -b v2.0.11 https://github.com/yakworks/shipkit.git $(SHIPKIT_DIR) >/dev/null 2>&1)
# Shipkit.make first, which does all the lifting to create makefile.env for the BUILD_VARS
include $(SHIPKIT_DIR)/Shipkit.make
include $(SHIPKIT_DIR)/makefiles/circle.make
include $(SHIPKIT_DIR)/makefiles/vault.make
include $(SHIPKIT_DIR)/makefiles/spring-common.make
include $(SHIPKIT_DIR)/makefiles/ship-gh-pages.make
# DB = true # set this to true to turn on the DB environment options
gw ?= ./gradlew

# open api targets
include ./api-docs/Makefile

## Run spotlessApply and normal check
check: lint
	# $(gradlew) spotlessApply
	# $(gradlew) check --max-workers=3
	$(gradlew) check

clean.all:
	rm -rf .build-cache/
	$(MAKE) clean

# should run vault.decrypt before this,
# sets up github, kubernetes and docker login
# sets up github, kubernetes and docker login, commented out # kubectl.config dockerhub.login
ship.authorize: git.config-bot-user
	$(logr.done)

## publish the java jar lib to repo.9ci for snapshot and to both for prod Sonatype Maven Central
publish:
	if [ "$(dry_run)" ]; then
		echo "🌮 dry_run ->  $(gradlew) publish"
	else
		if [ "$(IS_SNAPSHOT)" ]; then
			$(gradlew) publishJavaLibraryPublicationToMavenRepository
			$(logr.done) "- published SNAPSHOT to repo.9ci.com - libraries with version $(VERSION)$(VERSION_SUFFIX)"
		else
			${gradlew} rally-domain:verifyNoSnapshots
			$(gradlew) publishJavaLibraryPublicationToMavenRepository
			$(logr.done) "- published to repo.9ci.com - libraries with version $(VERSION)"

			$(logr) "Starting publishing to Sonatype Maven Central"
			$(gradlew) publishToSonatype closeAndReleaseSonatypeStagingRepository
			$(logr.done) "- published to Maven Central - libraries with version $(VERSION)"
		fi
	fi

## publish snapsot to repo.9ci
publish.snapshot:
	if [ "$(IS_SNAPSHOT)" ]; then
		$(gradlew) publishJavaLibraryPublicationToMavenRepository
		$(logr.done) "- libs with version $(VERSION)$(VERSION_SUFFIX) published to snapshot repo"
	fi

## alias to publish.snapshot
snapshot.publish: publish.snapshot

## Build snapshot and publishes to your local maven.
snapshot:
	# snapshot task comes from the yakworks shipkit plugin.
	$(gradlew) snapshot
	$(logr.done) "- libs with version $(VERSION)$(VERSION_SUFFIX) published to local ~/.m2 maven"


ifdef PUBLISHABLE_BRANCH_OR_DRY_RUN

# removed  ship.docker kube.deploy for now
 ship.release: build publish
	$(logr.done)

 ship.docker: docker.app-build docker.app-push
	$(logr.done) "docker built and pushed"

 kube.deploy: kube.create-ns kube.clean
	$(kube_tools) apply_tpl $(APP_KUBE_SRC)/app-configmap.tpl.yml
	$(kube_tools) apply_tpl $(APP_KUBE_SRC)/app-deploy.tpl.yml
	$(logr.done)

else

 ship.release:
	$(logr.done) "not on a PUBLISHABLE_BRANCH, nothing to do"

endif # end PUBLISHABLE_BRANCH_OR_DRY_RUN

# ---- Docmark -------

# the "dockmark-build" target depends on this. depend on the docmark-copy-readme to move readme to index
docmark.build-prep: docmark.copy-readme

# --- Testing and misc, here below is for testing and debugging ----

PORT ?= 8080
# sanity checks api with curl -i -G http://localhost:8081/api/rally/org,
# pass PORT=8081 for other than default 8080, only works when security is turned off
api-sanity-check:
	curl -i -G http://localhost:$(PORT)/api/rally/org/1

api-check-login:
	curl -H "Content-Type: application/json" -X POST -d '{"username":"admin","password":"!@#Foo"}' \
		http://localhost:8080/api/login

# gets token from curl and used that for sanity check
test.api-check-with-token:
	curl_call="curl --silent -H 'Content-Type: application/json' -X POST \
		-d '{\"username\":\"admin\",\"password\":\"!@#Foo\"}' \
		http://localhost:$(PORT)/api/login"
	resp=`eval "$$curl_call"`
	echo -e "login response: $$resp \n"
	token=`echo $$resp | awk -F'"' '/access_token/{ print $$(NF-1) }'`
	curl_call="curl -G -H 'Authorization: Bearer $$token' http://localhost:$(PORT)/api/rally/org/1"
	echo -e "$$curl_call \n"
	eval $$curl_call
	echo -e "\n$@ success"

# -- helpers --

ifdef IS_SNAPSHOT
# publish snapsot to repo.9ci
 publish.snapshot.repo:
	./gradlew publishJavaLibraryPublicationToMavenRepository
endif

## shows gorm-tools:dependencies --configuration runtime
gradle.dependencies:
	 $(gw) gorm-tools:dependencies # --configuration compileClasspath
	# ./gradlew rally-api:dependencies --configuration compileClasspath
	#./gradlew rally-security:dependencies --configuration compileClasspath

rally-api.dependencies:
	./gradlew rally-api:dependencies --configuration compileClasspath
	#./gradlew rally-security:dependencies --configuration compileClasspath

## shows spring dependencyManagement, modify to show other projects https://bit.ly/3CpcIls
spring.dependencyManagement:
	$(gw) gorm-tools:dependencyManagement

## shows spring dependencyManagement, modify to show other projects https://bit.ly/3CpcIls
gradle.buildEnvironment:
	$(gw) buildEnvironment
	$(gw) gorm-tools:buildEnvironment

## runs the benchmark tests
test.benchmarks:
	$(gw) benchmarks:assemble
	cd examples/benchmarks
	java -server -Xmx3g -XX:MaxMetaspaceSize=256m \
		-DmultiplyData=3 -Dgorm.tools.async.poolSize=4 -Djava.awt.headless=true \
        -Dgrails.env=prod -jar build/libs/benchmarks.jar

#		-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
#		-XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly \
#		-XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=70 \
#		-XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark \
#		-XX:SurvivorRatio=8 \

# start the restify example jar
start.rally-api: # start.db
	${gradlew} restify:assemble
	cd examples/restify
	java -server -Xmx2g -jar build/libs/restify.jar


# clones the api-docs branch or this project where we will publish/push
# oapi.docs-clone:
#	mkdir -p $(BUILD_DIR) && rm -rf "$(API_DOCS_BUILD_DIR)"
#	git clone $(GITHUB_URL) $(API_DOCS_BUILD_DIR) -b $(API_DOCS_BRANCH) --single-branch --depth 1
#	$(logr.done)

## run to get into builder shell
# make oapi.generate-apy-yaml
# make oapi.shell
# cd api-docs
# npm install   # ONLY ONCE PER DOCKER! If you have a docker from last time, don't reinstall npm
# npm run start
BIN_BASH=/bin/bash
DOCKER_SHELL=yakworks/bullseye:dev

# for testing set up .env or export both GITHUB_TOKEN and the base64 enocded GPG_KEY from lastpass.
docker.circle.shell:
	docker volume create gradle_cache
	docker run -it --rm \
	-e GITHUB_TOKEN \
	-e GPG_KEY \
	-v gradle_cache:/root/.gradle \
	-v "$$PWD":/root/project \
	$(DOCKER_SHELL) $(BIN_BASH)
	#	-v ~/.gradle_docker:/root/.gradle \

test.token.txt:
	TOKEN=`http POST admin:123@localhost:8080/api/token.txt -b`
	echo "$$TOKEN"
	http localhost:8080/api -A bearer -a "$$TOKEN"

test.token:
	RESP=`http -b POST admin:123@localhost:8080/api/token`
	# use awk to parse out the access_token
	TOKEN=`echo $$RESP | awk -F'"' -v RS="," '/access_token/{ print $$4 }'`
	echo "$$TOKEN"
	http localhost:8080/api -A bearer -a "$$TOKEN"

test.token.cookie:
	RESP=`http -b POST admin:123@localhost:8080/api/token`
	# use awk to parse out the access_token
	TOKEN=`echo $$RESP | awk -F'"' -v RS="," '/access_token/{ print $$4 }'`
	echo "$$TOKEN"
	http localhost:8080/api Cookie:jwt="$$TOKEN"

test.hello-post:
	RESP=`http -b POST localhost:8080/api/login username=admin password=123`
	echo $$RESP
	TOKEN=`echo $$RESP | awk -F'"' -v RS="," '/access_token/{ print $$4 }'`
	echo $$TOKEN
	http localhost:8080 -A bearer -a "$$TOKEN"
