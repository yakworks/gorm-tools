
api_docs_dir=api-docs
SRC_OAPI_DIR=$(api_docs_dir)/dist/openapi
#SRC_OAPI := $(shell find $(SRC_OAPI_DIR) -type f 2>/dev/null)
BUILD_API=$(api_docs_dir)/build/api.yaml

## API, list help for oapi.* targets
help.oapi:
	$(MAKE) help HELP_REGEX="^oapi.*"

## API, list help for oapi.* targets
oapi.help: help.oapi

# this puts combines into the build/api.yml which is then referenced in eleventy/_data/env.js and used to build site
# builds, bundles ( combines yml from dist into build) and lints the api.yml.
oapi.build-bundle: npm.install $(BUILD_API)
	$(logr.done)

$(BUILD_API): $(shell find $(SRC_OAPI_DIR) -type f 2>/dev/null)
	cd $(api_docs_dir)
	npx openapi bundle -o build/api.yaml
	npx openapi lint build/api.yaml
	$(logr.done)

## starts the eleventy server for the api docs, assumes the oapi.generate-api-yaml has been run to generate api.yml
oapi.start: oapi.build-bundle oapi.build-sass
	cd $(api_docs_dir)
	npx eleventy --serve --port=4567

#STYLE_FILES=$(wildcard ./$(api_docs_dir)/eleventy/slate/css/*.scss)
STYLE_FILES=$(wildcard ./$(api_docs_dir)/eleventy/slate/css/*.scss)
CSS_DIR=eleventy/slate/css
API_CSS_DIR=$(api_docs_dir)/$(CSS_DIR)

oapi.build-sass: $(API_CSS_DIR)/print.css $(API_CSS_DIR)/screen.css

$(API_CSS_DIR)/%.css: $(API_CSS_DIR)/%.scss
	cd $(api_docs_dir)
	npx sass --update --style compressed --no-source-map $(CSS_DIR):$(CSS_DIR)
	# npx sass --update --style compressed --no-source-map ./eleventy/slate/css:./eleventy/slate/css
	$(logr.done)

npm.install: $(api_docs_dir)/node_modules

$(api_docs_dir)/node_modules: $(api_docs_dir)/package.json
	cd $(api_docs_dir)
	npm install
	touch node_modules

# builds the eleventy docs with npm
oapi.docs-build: oapi.build-bundle oapi.build-sass
	cd api-docs
	npx eleventy
	$(logr.done)

oapi.docs-publish:
	cp -r api-docs/build/_site/. build/api-docs
	cp api-docs/build/api.yaml build/api-docs
	$(MAKE) oapi.docs-push
	$(logr.done)

# clones the api-docs branch or this project where we will publish/push
oapi.docs-clone:
	mkdir -p $(BUILD_DIR) && rm -rf "$(API_DOCS_BUILD_DIR)"
	git clone $(GITHUB_URL) $(API_DOCS_BUILD_DIR) -b $(API_DOCS_BRANCH) --single-branch --depth 1
	$(logr.done)

# pushes the changes to the api-docs branch, we will trigger go-rndc later that pull from this brnach
oapi.docs-push:
	git -C $(API_DOCS_BUILD_DIR) add -A .
	# or true so doesnt blow error if no changes
	git -C $(API_DOCS_BUILD_DIR) commit -a -m "CI API docs published [skip ci]" || true
	git -C $(API_DOCS_BUILD_DIR) push -q $(GITHUB_URL) $(API_DOCS_BRANCH) || true
	$(logr.done)

# this runs the test that generates the api.yml from the domains.
oapi.generate-api-yaml:
	# HACK, rm test-results to force a run if only editing yaml, NOT WORKING
	# rm -rf examples/rally-api/build/test-results
	# Clean so it forces a rerun
	${gradlew} rally-api:clean
	${gradlew} rally-api:integrationTest --tests *OpenapiGeneratorSpec*

## generates api yaml with grails test and runs eleventy server for api docs
oapi.generate-start: oapi.generate-api-yaml oapi.start

# generates and builds the openapi, adds to rcm
oapi.generate-build: oapi.generate-api-yaml oapi.build-bundle oapi.copy-to-rally-api

# Coppies the final single file bundle to rcm-api
oapi.copy-to-rally-api:
	cp api-docs/build/api.yaml examples/rally-api/src/main/resources/oapi.yaml

# run to get into builder shell
# make oapi.generate-apy-yaml
# make oapi.shell
# cd api-docs
# npm install   # ONLY ONCE PER DOCKER! If you have a docker from last time, don't reinstall npm
# npm run start
# Now you're in a docker shell for running oapi
oapi.shell:
	docker run --name oapi-shell -it --rm \
	  -v `pwd`:/project:delegated  \
	  -p 4567:4567 \
	  yakworks/builder-debian:base /bin/bash

# run this if making changes to openapi files. only need to clean dist if changing domains.
oapi.clean-build:
	rm -rf $(api_docs_dir)/build

oapi.clean:
	rm -rf $(api_docs_dir)/build
	rm -rf $(api_docs_dir)/dist

clean.node_modules:
	rm -rf $(api_docs_dir)/node_modules


# OLD STUFF here
# pushes the changes to the api-docs branch, the way the docs works is it pulls from repo on docker startup and builds
#oapi.docs-push:
#	git -C $(API_DOCS_BUILD_DIR) add -A .
#	# or true so doesnt blow error if no changes
#	git -C $(API_DOCS_BUILD_DIR) commit -a -m "CI API docs published [skip ci]" || true
#	git -C $(API_DOCS_BUILD_DIR) push -q $(GITHUB_URL) $(API_DOCS_BRANCH) || true
#	$(logr.done)
