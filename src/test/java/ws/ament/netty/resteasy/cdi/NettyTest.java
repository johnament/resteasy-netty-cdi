/*
 *
 *  * Copyright 2013 John D. Ament
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  * implied.
 *  *
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package ws.ament.netty.resteasy.cdi;

import org.apache.log4j.BasicConfigurator;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.weld.environment.se.Weld;
import org.junit.Assert;
import org.junit.Test;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: johndament
 * Date: 1/18/14
 * Time: 1:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class NettyTest {
    @Test
    public void testInit() throws InterruptedException {
        /**
         * Some TODOs:
         *
         * 1. Add an extension that observes ProcessAnnotatedType, keeps in a class, and then add a getter to get the list of these that are annotated @Path
         * 2. Override CDIRequestDispatcher to start a request context, and then stop when done.
         *
         */
        BasicConfigurator.configure();
        Weld weld = new Weld();
        weld.initialize();
        LoadPathsExtension paths = CDI.current().select(LoadPathsExtension.class).get();
        CDINettyJaxrsServer netty = new CDINettyJaxrsServer();
        ResteasyDeployment rd = new ResteasyDeployment();
        rd.setActualResourceClasses(paths.getResources());
        rd.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
        netty.setDeployment(rd);
        netty.setPort(8087);
        netty.setRootResourcePath("");
        netty.setSecurityDomain(null);
        netty.start();
        Client c = ClientBuilder.newClient();
        String result = c.target("http://localhost:8087").path("/").request("text/plain").accept("text/plain").get(String.class);
        Assert.assertEquals("pong", result);
    }
}
