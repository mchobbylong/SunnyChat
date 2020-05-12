package server;

import common.InvalidSessionException;
import common.User;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Session {
	private static HashMap<Long, Integer> sessionIdToUid = new HashMap<>();
	private static HashMap<Integer, Long> uidToSessionId = new HashMap<>();
	private static Random rand = new Random();
	private static ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * Create a new session for this user, and remove old session.
	 *
	 * @param user
	 */
	public static void createSession(User user) {
		long sessionId = rand.nextLong();
		lock.writeLock().lock();
		sessionIdToUid.put(sessionId, user.uid);
		if (uidToSessionId.containsKey(user.uid)) {
			sessionIdToUid.remove(uidToSessionId.get(user.uid));
		}
		uidToSessionId.put(user.uid, sessionId);
		lock.writeLock().unlock();
		user.sessionId = sessionId;
	}

	/**
	 * Validate the session for this user.
	 * 
	 * @param user
	 * @throws InvalidSessionException
	 */
	public static void validateSession(User user) throws InvalidSessionException {
		lock.readLock().lock();
		try {
			if (sessionIdToUid.containsKey(user.sessionId)) {
				throw new InvalidSessionException("The current session is expired. Please re-login.");
			}
		} finally {
			lock.readLock().unlock();
		}
	}
}
