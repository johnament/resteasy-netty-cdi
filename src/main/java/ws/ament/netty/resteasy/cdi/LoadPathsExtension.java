/*
 *
 *  * Copyright 2014 John D. Ament
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

import org.apache.log4j.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This CDI extension handles the reading of resources annotated {@see Path} so that at runtime you can get
 * the needed resources.
 *
 */
public class LoadPathsExtension implements Extension {
    private static final Logger logger = Logger.getLogger(LoadPathsExtension.class);
    private final List<Class> resources = new ArrayList<Class>();
    public void checkForPath(@Observes ProcessAnnotatedType<?> pat) {
        if(pat.getAnnotatedType().isAnnotationPresent(Path.class)) {
            logger.info("Discovered resource "+pat.getAnnotatedType().getJavaClass());
            resources.add(pat.getAnnotatedType().getJavaClass());
        }
    }

    public List<Class> getResources() {
        return resources;
    }
}
