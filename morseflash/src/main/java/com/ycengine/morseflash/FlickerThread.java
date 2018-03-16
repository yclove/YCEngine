package com.ycengine.morseflash;

public class FlickerThread implements Runnable {

	private boolean flickerRun;
	private int speed = 0;

	@Override
	public void run() {
		while (flickerRun) {
			try {
                MainActivity.turnOn();
				Thread.sleep(speed);
                MainActivity.turnOff();
				Thread.sleep(speed);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void setFlickerRun() {
		this.flickerRun = true;
	}

	public void setFlickerStop() {
		this.flickerRun = false;
	}

	public void setFlickerSpeed(int fspeed) {
		this.speed = fspeed;
	}

}
