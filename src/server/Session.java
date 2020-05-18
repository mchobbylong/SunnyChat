package server;

import common.InvalidSessionException;
import common.User;
import server.model.UserModel;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Session {
	private static HashMap<Long, User> sessionIdToUser = new HashMap<>();
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
			User temp = sessionIdToUser.get(sessionId);
			while (temp != null && !temp.equals(user)) {
				sessionId = rand.nextLong();
				temp = sessionIdToUser.get(sessionId);
			}
			sessionIdToUser.put(sessionId, user);
			if (uidToSessionId.containsKey(user.uid))
				sessionIdToUser.remove(uidToSessionId.get(user.uid));
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
			User storedUser = sessionIdToUser.get(user.sessionId);
			if (storedUser == null || !storedUser.equals(user)) {
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
			sessionIdToUser.remove(user.sessionId);
			uidToSessionId.remove(user.uid);
		} finally {
			lock.writeLock().unlock();
		}
		return;
	}
}
