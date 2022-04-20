<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Welcome to Grails</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  <h1 id="index"><g:include action="index" /> </h1>
  <h1 id="overridenExternalTemplatesPath"><g:include controller="foo" action="overridenExternalTemplatesPath" /> </h1>
  <h1 id="fooPlugin-override"><g:include controller="fooPlugin" action="override" /> </h1>
  <h1 id="fooPlugin-index"><g:include controller="fooPlugin"  /> </h1>
</body>
</html>
