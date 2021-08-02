# check for build/shipkit and clone if not there, this should come first
SHIPKIT_DIR = build/shipkit
$(shell [ ! -e $(SHIPKIT_DIR) ] && git clone -b v1.0.20 https://github.com/yakworks/shipkit.git $(SHIPKIT_DIR) >/dev/null 2>&1)
# Shipkit.make first, which does all the lifting to create makefile.env for the BUILD_VARS
include $(SHIPKIT_DIR)/Shipkit.make
include $(SHIPKIT_DIR)/makefiles/spring-common.make
include $(SHIPKIT_DIR)/makefiles/ship-gh-pages.make
# DB = true # set this to true to turn on the DB environment options

## ci deploy, main target to call from circle
ship-it::
	make secrets.decrypt-vault
	make ci-credentials
	make ship.release
	$(log.done)

ci-credentials: git.config-bot-user kubectl.config dockerhub.login
	$(log.done)

ifdef RELEASABLE_BRANCH

 ship.release: build ship.libs ship.docker kube.deploy
	# this should happen last and in its own make as it will increment the version number which is used in scripts above
    # TODO it seems a bit backwards though and the scripts above should be modified
	make ship.version
	$(log.done)

 kube.deploy: kube.create-ns kube.clean
	$(kube_tools) kubeApplyTpl $(APP_DIR)/src/deploy/app-configmap.tpl.yml
	$(kube_tools) kubeApplyTpl $(APP_DIR)/src/deploy/app-deploy.tpl.yml
	$(log.done)

else

 ship.release:
	$(log.done) "not on a RELEASABLE_BRANCH, nothing to do""

endif # end RELEASABLE_BRANCH

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
	curl -H "Content-Type: application/json" -X POST -d '{"username":"admin","password":"Br1ck#ouse"}' \
		http://localhost:8080/api/login

## gets token from curl and used that for sanity check
api-check-with-token:
	curl_call="curl --silent -H 'Content-Type: application/json' -X POST \
		-d '{\"username\":\"admin\",\"password\":\"Br1ck#ouse\"}' \
		http://localhost:$(PORT)/api/login"
	resp=`eval "$$curl_call"`
	echo -e "login response: $$resp \n"
	token=`echo $$resp | awk -F'"' '/access_token/{ print $$(NF-1) }'`
	curl_call="curl -G -H 'Authorization: Bearer $$token' http://localhost:$(PORT)/api/rally/org/1"
	echo -e "$$curl_call \n"
	eval $$curl_call
	echo -e "\n$@ success"

## login to the deployed restify app, see notes for `api-check-login`
api-check-deployed-login:
	curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST \
	  -d '{"username":"admin","password":"123Foo"}' https://$(APP_KUBE_INGRESS_URL)/api/login

## use TOKEN after api-check-deployed-login ex: `make api-check-deployed-with-token TOKEN=asdfasdfasdf`
api-check-deployed-with-token:
	curl -i -G -H "Authorization: Bearer $(TOKEN)" https://$(APP_KUBE_INGRESS_URL)/api/rally/org/1

# -- helpers --
## shows gorm-tools:dependencies --configuration runtime
gradle.dependencies:
	# ./gradlew gorm-tools:dependencies --configuration compileClasspath
	./gradlew restify:dependencies --configuration runtime

run-benchmarks:
	$(gw) benchmarks:assemble
	cd examples/benchmarks
	java -server -Xmx3048m -XX:MaxMetaspaceSize=256m -jar \
	  -DmultiplyData=3 -Dgpars.poolsize=4 build/libs/benchmarks.war
	# -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap
