package server;

import common.InvalidSessionException;
import common.User;
import server.model.UserModel;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Session {
	private static HashMap<Long, Integer> sessionIdToUid = new HashMap<>();
	private static HashMap<Integer, Long> uidToSessionId = new HashMap<>();
	private static Random rand = new Random();
	private static ReadWriteLock lock = new ReentrantReadWriteLock();

	static {
		createSession(UserModel.SERVER_USER);
	}

	/**
	 * Create a new session for this user, and remove old session.
	 *
	 * @param user
	 */
	public static void createSession(User user) {
		lock.writeLock().lock();
		try {
			long sessionId = rand.nextLong();
			// Session collision detection
			Integer temp = sessionIdToUid.get(sessionId);
			while (temp != null && temp != user.uid) {
				sessionId = rand.nextLong();
				temp = sessionIdToUid.get(sessionId);
			}
			sessionIdToUid.put(sessionId, user.uid);
			if (uidToSessionId.containsKey(user.uid))
				sessionIdToUid.remove(uidToSessionId.get(user.uid));
			uidToSessionId.put(user.uid, sessionId);
			user.sessionId = sessionId;
		} finally {
			lock.writeLock().unlock();
		}
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
			Integer uid = sessionIdToUid.get(user.sessionId);
			if (uid == null || uid.intValue() != user.uid) {
				System.out.println(String.format("Warning: Invalid session detected from user %s.", user.userName));
				throw new InvalidSessionException("The current session is expired. Please re-login.");
			}
		} finally {
			lock.readLock().unlock();
		}
		return;
	}

	/**
	 * Destroy the session for this user.
	 *
	 * @param user
	 */
	public static void destroySession(User user) {
		lock.writeLock().lock();
		try {
			sessionIdToUid.remove(user.sessionId);
			uidToSessionId.remove(user.uid);
		} finally {
			lock.writeLock().unlock();
		}
		return;
	}
}
