package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.AbstractListModel;

import common.ChatMessage;
import common.ChatRoom;

public class ChatRoomListModel extends AbstractListModel<ChatRoom> {
	private static final long serialVersionUID = 1L;

	private HashMap<Integer, ChatRoom> rooms = new HashMap<>();
	private ArrayList<Integer> roomIndex = new ArrayList<>();
	private ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private Lock rLock = rwLock.readLock();
	private Lock wLock = rwLock.writeLock();

	@Override
	public int getSize() {
		rLock.lock();
		try {
			return roomIndex.size();
		} finally {
			rLock.unlock();
		}
	}

	@Override
	public ChatRoom getElementAt(int index) {
		rLock.lock();
		try {
			return rooms.get(roomIndex.get(index));
		} finally {
			rLock.unlock();
		}
	}

	public void addChatRoom(ChatRoom room) {
		wLock.lock();
		boolean sorted = false;
		int size = roomIndex.size();
		try {
			rooms.put(room.cid, room);
			roomIndex.add(room.cid);
			if (size > 0 && roomIndex.get(size - 1) < room.cid) {
				Collections.sort(roomIndex);
				sorted = true;
			}
		} finally {
			wLock.unlock();
		}
		if (sorted)
			fireContentsChanged(this, 0, size - 1);
		fireIntervalAdded(this, size, size);
	}

	public void addMessage(ChatMessage message, Integer selectedChatRoomID) {
		rLock.lock();
		try {
			ChatRoom room = rooms.get(message.cid);
			if (room == null)
				return;
			room.addMessage(message);
			if (selectedChatRoomID == message.cid)
				room.unreadCount = 0;
			int index = roomIndex.indexOf(message.cid);
			if (index > -1)
				fireContentsChanged(this, index, index);
		} finally {
			rLock.unlock();
		}
	}

	public void clearUnreadCount(Integer cid) {
		rLock.lock();
		try {
			ChatRoom room = rooms.get(cid);
			if (room == null)
				return;
			room.unreadCount = 0;
			int index = roomIndex.indexOf(cid);
			if (index > -1)
				fireContentsChanged(this, index, index);
		} finally {
			rLock.unlock();
		}
	}
}
