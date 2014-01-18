/*
 *
 *  * Copyright 2014 John D. Ament and authors forked from.
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

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.plugins.server.netty.RequestDispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.weld.context.bound.BoundRequestContext;

import javax.enterprise.inject.spi.CDI;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * An extension of the standard RequestDispatcher that also sets up a RequestContext for the duration of the call.
 *
 * @author johndament
 */
public class CDIRequestDispatcher extends RequestDispatcher
{

    public CDIRequestDispatcher(SynchronousDispatcher dispatcher, ResteasyProviderFactory providerFactory,
                                SecurityDomain domain) {
        super(dispatcher,providerFactory,domain);
    }

    public void service(HttpRequest request, HttpResponse response, boolean handleNotFound) throws IOException
    {
        BoundRequestContext requestContext = CDI.current().select(BoundRequestContext.class).get();
        Map<String,Object> requestMap = new HashMap<String,Object>();
        requestContext.associate(requestMap);
        requestContext.activate();
        try {
            super.service(request,response,handleNotFound);
        }
        finally {
            requestContext.invalidate();
            requestContext.deactivate();
            requestContext.dissociate(requestMap);
        }
    }
}
