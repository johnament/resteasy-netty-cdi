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

import org.apache.log4j.Logger;
import org.jboss.resteasy.cdi.CdiConstructorInjector;
import org.jboss.resteasy.cdi.CdiPropertyInjector;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.core.InjectorFactoryImpl;
import org.jboss.resteasy.core.ValueInjector;
import org.jboss.resteasy.spi.*;
import org.jboss.resteasy.spi.metadata.Parameter;
import org.jboss.resteasy.spi.metadata.ResourceClass;
import org.jboss.resteasy.spi.metadata.ResourceConstructor;
import org.jboss.resteasy.spi.metadata.ResourceLocator;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * This class is forked from the RestEasy Cdi InjectorFactory, directly supporting only CDI 1.1 utility class.
 */
public class CdiInjectorFactory implements InjectorFactory
{
    private static final Logger log = Logger.getLogger(CdiInjectorFactory.class);
    private BeanManager manager;
    private InjectorFactory delegate = new InjectorFactoryImpl();
    private ResteasyCdiExtension extension;
    private Map<Class<?>, Type> sessionBeanInterface;

    public CdiInjectorFactory()
    {
        this.manager = lookupBeanManager();
        this.extension = lookupResteasyCdiExtension();
        sessionBeanInterface = extension.getSessionBeanInterface();
    }

    @Override
    public ValueInjector createParameterExtractor(Parameter parameter, ResteasyProviderFactory providerFactory)
    {
        return delegate.createParameterExtractor(parameter, providerFactory);
    }

    @Override
    public MethodInjector createMethodInjector(ResourceLocator method, ResteasyProviderFactory factory)
    {
        return delegate.createMethodInjector(method, factory);
    }

    @Override
    public PropertyInjector createPropertyInjector(ResourceClass resourceClass, ResteasyProviderFactory providerFactory)
    {
        return new CdiPropertyInjector(delegate.createPropertyInjector(resourceClass, providerFactory), resourceClass.getClazz(), sessionBeanInterface, manager);
    }

    @Override
    public ConstructorInjector createConstructor(ResourceConstructor constructor, ResteasyProviderFactory providerFactory)
    {
        Class<?> clazz = constructor.getConstructor().getDeclaringClass();

        ConstructorInjector injector = cdiConstructor(clazz);
        if (injector != null) return injector;

        log.debug("No CDI beans found for "+clazz+". Using default ConstructorInjector.");
        return delegate.createConstructor(constructor, providerFactory);
    }

    @Override
    public ConstructorInjector createConstructor(Constructor constructor, ResteasyProviderFactory factory)
    {
        Class<?> clazz = constructor.getDeclaringClass();

        ConstructorInjector injector = cdiConstructor(clazz);
        if (injector != null) return injector;

        log.debug("No CDI beans found for "+clazz+". Using default ConstructorInjector.");
        return delegate.createConstructor(constructor, factory);
    }



    protected ConstructorInjector cdiConstructor(Class<?> clazz)
    {
        if (!manager.getBeans(clazz).isEmpty())
        {
            log.debug("Using CdiConstructorInjector for class "+ clazz);
            return new CdiConstructorInjector(clazz, manager);
        }

        if (sessionBeanInterface.containsKey(clazz))
        {
            Type intfc = sessionBeanInterface.get(clazz);
            log.debug("Using "+intfc+" for lookup of Session Bean "+clazz+".");
            return new CdiConstructorInjector(intfc, manager);
        }

        return null;
    }

    public PropertyInjector createPropertyInjector(Class resourceClass, ResteasyProviderFactory factory)
    {
        return new CdiPropertyInjector(delegate.createPropertyInjector(resourceClass, factory), resourceClass, sessionBeanInterface, manager);
    }

    public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget, Class type, Type genericType, Annotation[] annotations, ResteasyProviderFactory factory)
    {
        return delegate.createParameterExtractor(injectTargetClass, injectTarget, type, genericType, annotations, factory);
    }

    public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget, Class type,
                                                  Type genericType, Annotation[] annotations, boolean useDefault, ResteasyProviderFactory factory)
    {
        return delegate.createParameterExtractor(injectTargetClass, injectTarget, type, genericType, annotations, useDefault, factory);
    }

    /**
     * Do a lookup for BeanManager instance. JNDI and ServletContext is searched.
     *
     * @return BeanManager instance
     */
    protected BeanManager lookupBeanManager()
    {
        BeanManager beanManager = null;

        beanManager = lookupBeanManagerCDIUtil();
        if(beanManager != null)
        {
            log.debug("Found BeanManager via CDI Util");
            return beanManager;
        }

        throw new RuntimeException("Unable to lookup BeanManager.");
    }

    public static BeanManager lookupBeanManagerCDIUtil()
    {
        BeanManager bm = CDI.current().getBeanManager();
        return bm;
    }

    /**
     * Lookup ResteasyCdiExtension instance that was instantiated during CDI bootstrap
     *
     * @return ResteasyCdiExtension instance
     */
    private ResteasyCdiExtension lookupResteasyCdiExtension()
    {
        return CDI.current().select(ResteasyCdiExtension.class).get();
    }
}