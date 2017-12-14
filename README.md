# Spare Cortex

Spare Cortex is a wiki and personal information manager mashed up into flexible
tool for orgnanizing your life and your projects, for keeping track of ideas
and things to do.

It is a cloud application, currently written to run on Google App Engine,
though the ties to that platform are not substantial.

It has an AJAX client, creating using the Google Web Toolkit, and the backend
is written in Scala and uses the Objectify library to access Google App
Engine's datastore.

# More Info

You can use it at [http://www.sparecortex.com/](http://www.sparecortex.com/).

You can discuss it and get in touch with its author at the
[Spare Cortex Google Group](http://groups.google.com/group/spare-cortex).

# Building

We build with Maven. For testing:

```
mvn appengine:devserver
```

Running superdevmode (in parallel with above command):

```
mvn gwt:run-codeserver
```

For deployment:

```
mvn appengine:update
```
