package com.frostphyr.kin;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntryKeyer {
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private Robot robot;
	
	private List<Entry> entries;
	private Callback callback;
	private int initialDelay;
	private int keyDelay;
	private boolean reminderDelay;
	
	public EntryKeyer(List<Entry> entries, int initialDelay, int keyDelay, boolean reminderDelay, Callback callback) throws AWTException {
		this.entries = entries;
		this.initialDelay = initialDelay;
		this.keyDelay = keyDelay;
		this.reminderDelay = reminderDelay;
		this.callback = callback;
		
		robot = new Robot();
	}
	
	public void start(final int index) {
		executor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(initialDelay);
					for (int i = index; i < entries.size(); i++) {
						checkReminder();
						Entry entry = entries.get(i);
						callback.onNextEntry(entry, i);
						typeString(entry.getDivision());
						keyType(KeyEvent.VK_ENTER);
						typeString(entry.getCategory());
						keyType(KeyEvent.VK_ENTER);
						typeString(entry.getPercent());
						keyType(KeyEvent.VK_ENTER);
						typeString(entry.getPercent());
						keyType(KeyEvent.VK_F12);
						keyType(KeyEvent.VK_F12);
					}
					callback.onFinish();
				} catch (InterruptedException e) {
				}
			}
			
		});
	}
	
	public void shutdown() {
		executor.shutdownNow();
	}
	
	private void checkReminder() throws InterruptedException {
		if (reminderDelay) {
			Date date = new Date();
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(date);
			int minute = calendar.get(Calendar.MINUTE);
			if ((minute >= 14 && minute <= 15) || (minute >= 29 && minute <= 30) || (minute >= 44 && minute <= 45) || (minute == 59 || minute == 0)) {
				Thread.sleep(1000 * 60 * 2);
				robot.keyPress(KeyEvent.VK_CONTROL);
				Thread.sleep(keyDelay);
				keyType(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_CONTROL);
				Thread.sleep(keyDelay);
			}
		}
	}
	
	private void keyType(int keycode) throws InterruptedException {
		robot.keyPress(keycode);
		robot.keyRelease(keycode);
		Thread.sleep(keyDelay);
	}
	
	private void typeString(String s) throws InterruptedException {
		for (int i = 0; i < s.length(); i++) {
			int keycode = getKeycode(s.charAt(i));
			if (keycode != -1) {
				keyType(keycode);
			}
		}
	}
	
	private int getKeycode(char c) {
		switch (c) {
			case '0':
				return KeyEvent.VK_0;
			case '1':
				return KeyEvent.VK_1;
			case '2':
				return KeyEvent.VK_2;
			case '3':
				return KeyEvent.VK_3;
			case '4':
				return KeyEvent.VK_4;
			case '5':
				return KeyEvent.VK_5;
			case '6':
				return KeyEvent.VK_6;
			case '7':
				return KeyEvent.VK_7;
			case '8':
				return KeyEvent.VK_8;
			case '9':
				return KeyEvent.VK_9;
		}
		return -1;
	}
	
	public static interface Callback {
		
		void onNextEntry(Entry entry, int index);
		
		void onFinish();
		
	}

}
