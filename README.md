This code has been contributed upstream to Resteasy: https://github.com/resteasy/Resteasy/tree/master/jaxrs/server-adapters/resteasy-netty4-cdi


# Integrating Weld + Netty + RestEasy

Why would I do something like this?  I recently had a need to run a simple socket server (rather than a true application server) for the first time in a few years.  I wanted to still use some of my enterprisey tools, I needed it to be a basic REST server, but run at very small amounts of memory.  I've also been a bit unhappy with my application server of choice, mostly because of how my team wants to build things and deploy things.

Looking at how it worked, RestEasy already supported Netty, and it already supported CDI (Weld).  I figured that was a good start.  It only worked to an extent.  First, I found an issue in the CdiInjectorFactory that was causing a NullPointerException when not run in a container.  Booooo.  So I created a custom CdiInjectorFactory, and luckily basic things like ApplicationScoped resources worked fine.  However, I also wanted `@RequestScoped` resources to work, and to simplify how resources were found (when running in standalone mode, there's no scanning of paths).

So, I wrote a CDI extension to look for annotated types, and if they were annotated `@Path` I added them to a list of resources, then inject the extension to where I boot up the Netty server.

I had to start mucking around with internals too.  The `NettyJaxrsServer` did a lot of start up logic, but it was hard coded to use a specific `RequestDispatcher`.  I knew from looking at this code before if I wanted to start a `RequestScope` from an incoming request, I needed to do it here.  I ended up extending the base `RequestDispatcher` for `CDIRequestDispatcher` that starts the context and then passes off the rest to its parent.  Then I kill the `RequestContext` at the end.
