

import static org.jboss.seam.ScopeType.APPLICATION;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.cache.CacheProvider;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Naming;

/**
 * Implementation of CacheProvider backed by Infinispan Cache from existing
 * JNDI.
 * 
 * @author Marek Novotny
 */
@Name("org.jboss.seam.cache.cacheProvider")
@Scope(APPLICATION)
@BypassInterceptors
@Install(value = false, classDependencies = "org.infinispan.Cache")
@AutoCreate
public class InfinispanJNDICacheProvider<K, V> extends
		CacheProvider<Cache<K, V>> {

	private org.infinispan.Cache<String, Object> cache;

	private CacheContainer cacheContainer;

	private static final LogProvider log = Logging
			.getLogProvider(InfinispanJNDICacheProvider.class);

   @Override
   public Cache getDelegate()
   {
      return cacheContainer.getCache();
   }

	@Override
	public void put(String region, String key, Object object) {
		cache.put(key, object);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public Object get(String region, String key) {
		return cache.get(key);
	}

	@Override
	public void remove(String region, String key) {
		cache.remove(key);
	}

	@Create
	public void create() {
	  log.info("Trying to get Infinispan cache from JNDI");
	  InitialContext context = null;
	  try
	  {
		 context = Naming.getInitialContext();
		 log.info("Got context");
		 log.info("JNDI to lookup " + getJndi());
	     cacheContainer = (CacheContainer) context.lookup(getJndi());
	     cache = cacheContainer.getCache();
	  }
	  catch (NamingException ne)
	  {
		  log.error(ne);
		  throw new IllegalArgumentException("Error while getting Infinispan cache");
	  }
	}

	@Destroy
	public void destroy() {
		log.info("Leaving Infinispan cache");
		cacheContainer = null;
		cache = null;
	}

}
