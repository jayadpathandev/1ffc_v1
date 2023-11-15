/**
 * 
 */
package com.sorrisotech.svcs.accountstatus.cache;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UserStatusCache is a cache of UserStatusCacheItem(s) that 
 * minimizes the frequency with which we poke the status feed
 * for a user's accounts in order to get their status information.
 * The item in the cache contains a map with status for all the
 * accounts the user has access to within the scope of a single
 * payment group.
 * <p>
 * The cache itself is static, but it is policed by accessing all
 * items that have "timed out" i.e. haven't been accessed within
 * the timeout interval and removing those items from the cache.
 * 
 * @author 	John A. Kowalonek
 * @since  	26-Sep-2023
 * @version 26-Sep-2023
 */
public final class UserStatusCache {
	
	private static final Logger LOG = LoggerFactory.getLogger(UserStatusCache.class);

	// -- this object will be accessed from several threads so a concurrent hash map is in order --
	private static ConcurrentHashMap<String, UserStatusCacheItem> m_Cache = new ConcurrentHashMap<String, UserStatusCacheItem>();

	// -- tracks last time we cleaned the cache --
	private static Instant m_LastTimeCleaned = Instant.now();
	// -- used to allow a single cleaner to be operating ... don't waste CPU --
	private static AtomicBoolean bCleaning = new AtomicBoolean(false); 
	
	final private static long m_Timeout = 5*60*1000; // time is in milliseconds so this is 5 minutes
	final private static long m_CleaningInterval = 2*60*100; // time is in milliseconds so this is 2 minutes

	/**
	 * Removes old items from the cache if we haven't done it in awhile.
	 * We don't want to be cycling through the cache every time its touched,
	 * but instead once since the last time it was cleaned.
	 */
	private static void removeOldItems() {
			
		// -- add cleaning interval milliseconds to last cleaned... if its greater than
		//	the current time then we're still good... if not, then time to dump this object --
		Instant currentTime = Instant.now();
		if (m_LastTimeCleaned.plusMillis(m_CleaningInterval).compareTo(currentTime) > 0)
			return;
		else {
			// -- ok... its basically timed out... let's see if there's a cleaner running
			// 		if its not running (i.e. bCleaning == false, then set it to true, 
			//		otherwise its already running so just return.
			if (!bCleaning.compareAndSet(false, true))
				return;
			LOG.debug("UserStatusCache:removeOldItems -- cache cleaning started");
			// -- as the cleaner... update the time, clean, then update the time again
			//		to avoid extra unnecessary cleaning --
			m_LastTimeCleaned = Instant.now();
			for (ConcurrentHashMap.Entry<String, UserStatusCacheItem> entry : m_Cache.entrySet() ) {
				// -- check each entry to see if its timed out... if it has, 
				//		remove it from the HashMap --
				if (!entry.getValue().isBeingUsed(m_Timeout)) {
					m_Cache.remove(entry.getKey());
				}
			}
			m_LastTimeCleaned = Instant.now();		// update time		
			LOG.debug("UserStatusCache:removeOldItems -- cache cleaning ended");
			bCleaning.set(false);					// done cleaning
		}
	}
		
	/**
	 * Returns the status information for a given UserId and StatusGroup. First it
	 * looks in the cache, if its not there, it creates the item and adds in into 
	 * the cache... 
	 * <p>
	 * <b>Note:</b> Also checks and cleans up the cache if we're looking at it after a
	 * cleaning interval has passed.
	 * 
	 * @param	UserId		-- Id for this user
	 * @return	IUserstatusCacheItem interface to the status object in memory.
	 */
	public static IUserStatusCacheItem getItem(String UserId) {
		
		// -- Build the key to be used in storage and retrieval
		String sKey = UserId;
		
		removeOldItems();	// -- clean up the cache if it needs cleaning
		
		// -- grab the interface to the item if its there --
		IUserStatusCacheItem retValue = m_Cache.get(sKey);
		int numAccounts = 0;
		
		if (null == retValue) {
			// -- we have a cached miss, create an item and insert it --
			LOG.debug("UserStatusCache:getItem -- cache miss, creating new item");
			UserStatusCacheItem item = new UserStatusCacheItem();
			try {
				// -- set contents queries the database and sets the results in the class --
				numAccounts = item.setContents(UserId);
				// -- add this item to the cache and return it for use. Note that 
				//		if there's already an item there (which could be from multiple
				//		concurrent calls) the put replaces the existing time --
				m_Cache.put(sKey, item);
				retValue = item;
				LOG.debug("UserStatusCache:getItem -- new item added to cache for user id {}, number of accounts {}", UserId, numAccounts );				
			} catch (AccountStatusException e) {
				LOG.error("UserStatusCache:getItem -- exception creating new item", e);				
				retValue = null;
			}
		} else {
			LOG.debug("UserStatusCache:getItem -- cache hit, all good");
		}
		return retValue;
	}
}
