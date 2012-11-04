import java.io.PrintStream;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import java.io.*;
import java.util.Random;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

public class CaveMan extends MIDlet implements CommandListener {
	private static Display display;
	private static StopWatchDisplay stopWatchDisplay;
	private static Command exitCommand;

	private static Thread thread;
	private static long currentTime;
	private static Font defaultFont;
	private static Random rnd;
	private static final int SizeX = 120;
	private static final int SizeY = 120;
	private static final int SLeft = 0;
	private static final int STop = 0;
	private static final int TimeOut = 100;
	private static final int GS_TITLE = 0;
	private static final int GS_GAME = 1;
	private static final int GS_OVER = 2;
	private static final int GC_FIRST = 1;
	private static Image offImage;
	private static Graphics graOff;
	private static int mouseX;
	private static int mouseY;
	private static boolean mousePushed;
	private static boolean mouseClicked;
	private static boolean keyPushed;
	private static int GameState;
	private static int GameCount;
	private static boolean flag1;
	private static int HiScore;
	private static int Score;
	private static int map[][];
	private static int my;
	private static int mh;
	private static int mv;
	private static int oy;
	private static int y;
	private static int vy;
	private static int screenWidth;
	private static int screenHeight;
	private static int marginTop;
	private static int marginLeft;
	private RecordStore recordStore;

	public CaveMan() {
		display = Display.getDisplay(this);
		exitCommand = new Command("Exit", 4, 2);
		stopWatchDisplay = new StopWatchDisplay();
		stopWatchDisplay.startThread();
		stopWatchDisplay.addCommand(exitCommand);
		stopWatchDisplay.setCommandListener(this);
		display.setCurrent(stopWatchDisplay);
	}

	public void commandAction(Command command, Displayable displayable) {
		if (displayable == stopWatchDisplay && command == exitCommand) {
			exit();
		}
	}

	public void exit() {
		destroyApp(true);
		notifyDestroyed();
	}

	public void startApp() {
		System.out.println("in startApp()");
	}

	public void pauseApp() {
		System.out.println("in pause()");
	}

	public void destroyApp(boolean flag) {
		display = null;
		stopWatchDisplay = null;
		exitCommand = null;
	}

	private class StopWatchDisplay extends Canvas implements Runnable {
		public StopWatchDisplay() {
			recordStore = null;
			rnd = new Random();
			defaultFont = Font.getFont(64, 1, 16);
			map = new int[4][32];
			screenWidth = 120;
			screenHeight = 120;
			marginTop = (getHeight() - screenHeight) / 2;
			marginLeft = (getWidth() - screenWidth) / 2;
			offImage = Image.createImage(screenWidth, screenHeight);
			graOff = offImage.getGraphics();
			setGameState(0);
			Score = 0;
			HiScore = 0;
			HiScore = OpenScore();
		}

		public void run() {
			long beforeProcessTime;
			do {
				try {
					beforeProcessTime = System.currentTimeMillis();
					repaint();
					serviceRepaints();
					currentTime = System.currentTimeMillis();
					long itijiSleepTime = 100L - (System.currentTimeMillis() - beforeProcessTime);
					if (itijiSleepTime < 0) {
						Thread.currentThread();
						Thread.yield();
					} else {
						thread.sleep(itijiSleepTime);
					}
				} catch (Exception e) {
					System.out.println(e.toString());
				}
			} while (true);
		}

		public void restartThread() {
			if (thread != null) {
				stopThread();
			}
			startThread();
		}

		public void startThread() {
			System.out.println("thread started");
			thread = new Thread(this);
			thread.start();
		}

		public void stopThread() {
			System.out.println("thread stopped");
			if (thread != null) {
				thread = null;
			}
		}

		public void paint(Graphics g) {
			if (GameState == 0) {
				g.setColor(128, 128, 255);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
			OnPeriod();
			g.drawImage(offImage, marginLeft, marginTop, 0x10 | 4);
		}

		void setGameState(int i) {
			GameState = i;
			GameCount = 0;
		}

		public int OpenScore() {
			try {
				recordStore = RecordStore.openRecordStore("CaveScore", true);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			return load();
		}

		public int load() {
			boolean flag = false;
			boolean flag2 = false;
			Object obj = null;
			if (recordStore == null) {
				return 0;
			}
			try {
				int i;
				if (recordStore.getNumRecords() == 0) {
					i = 0;
					ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
					DataOutputStream dataoutputstream = new DataOutputStream(
							bytearrayoutputstream);
					try {
						dataoutputstream.writeInt(i);
					} catch (Exception e) {
						System.out.println(e.toString());
					}
					byte abyte1[] = bytearrayoutputstream.toByteArray();
					recordStore.addRecord(abyte1, 0, abyte1.length);
				} else {
					byte abyte0[] = recordStore.getRecord(1);
					if (abyte0 != null) {
						ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(
								abyte0);
						DataInputStream datainputstream = new DataInputStream(
								bytearrayinputstream);
						i = datainputstream.readInt();
					} else {
						i = 0;
					}
				}
				return i;
			} catch (Exception e) {
				System.out.println(e.toString());
				return 0;
			}
		}

		public void save() {
			if (recordStore == null) {
				return;
			}
			ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
			DataOutputStream dataoutputstream = new DataOutputStream(
					bytearrayoutputstream);
			try {
				dataoutputstream.writeInt(HiScore);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			byte abyte0[] = bytearrayoutputstream.toByteArray();
			try {
				if (recordStore.getNumRecords() == 0) {
					recordStore.addRecord(abyte0, 0, abyte0.length);
				} else {
					recordStore.setRecord(1, abyte0, 0, abyte0.length);
				}
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}

		private void OnPeriod() {
			GameCount++;
			int[][] localMap = CaveMan.this.map;
			switch (GameState) {
			default:
				break;

			case 0: // '\0'
				if (GameCount == 1) {
					flag1 = false;
					if (HiScore < Score) {
						HiScore = Score;
					}
				}
				graOff.setColor(128, 128, 255);
				graOff.fillRect(0, 0, 120, 120);
				graOff.setColor(0, Math.abs(rnd.nextInt() % 64), 128);
				int i = 20 + GameCount % 20;
				graOff.fillArc(60 - i, 59 - i, i * 2, i * 2, 0, 360);
				graOff.setColor(0, 0, 0);
				graOff.drawString("SFCave", 15, 20, 20);
				graOff.setColor(255, 255, 255);
				graOff.drawString("SFCave", 10, 25, 20);
				graOff.setColor(255, 0, 0);
				graOff.drawString("DOWN key:UP", 10, 50, 20);
				graOff.drawString("DOWN key to start!", 10, 70, 20);
				graOff.setColor(255, 255, 255);
				graOff.drawString("Score   : " + Score, 10, 90, 20);
				graOff.drawString("HiScore : " + HiScore, 10, 105, 20);
				if (!flag1 && !mousePushed) {
					flag1 = true;
					mouseClicked = false;
				}
				if (flag1 && mouseClicked) {
					setGameState(1);
				}
				break;

			case 1: // '\001'
				if (GameCount == 1) {
					Score = 0;
					my = 10;
					mh = 100;
					mv = 0;
					y = oy = 50;
					vy = -5;
					for (int j = 29; j >= 0; j--) {
						int l = Math.abs(j % 16 - 8) * 16;
						graOff.setColor(128 - l, 255, 128 - l);
						graOff.fillRect(j * 4, 0, 4, 120);
						graOff.setColor(l, 0, 0);
						graOff.fillRect(j * 4, my, 4, mh);
						localMap[0][j] = my;
						localMap[1][j] = my + mh;
						localMap[2][j] = -1;
					}
				}
				Score += 3;
				if (mousePushed || keyPushed) {
					vy--;
				} else {
					vy++;
				}
				if (vy < -8) {
					vy = -8;
				} else if (vy > 8) {
					vy = 8;
				}
				y = y + vy;
				if (GameCount % 10 == 0) {
					mh--;
				}
				if (Math.abs(rnd.nextInt() % 10) < 1) {
					mv = Math.abs(rnd.nextInt() % 10) - 5;
				}
				my = my + mv;
				if (my < 1) {
					my = 1;
					mv = Math.abs(mv);
				}
				if (my > 118 - mh) {
					my = 118 - mh;
					mv = -Math.abs(mv);
				}

				graOff.drawImage(offImage, -4, 0, 20);

				// ���Ȃ�Ӗ����������B�B�B
				for (int k = 28; k >= 0; k--) {
					for (int i1 = 3; i1 >= 0; i1--) {
						localMap[3 - i1][28 - k] = localMap[3 - i1][28 - k + 1];
					}
				}
				graOff.setColor(128, 128, 255);
				graOff.drawLine(30, oy - 1, 34, y - 1);
				graOff.drawLine(30, oy, 34, y);
				graOff.drawLine(30, oy + 1, 34, y + 1);
				int j1 = Math.abs(GameCount % 16 - 8) * 16;
				graOff.setColor(128 - j1, 255, 128 - j1);
				graOff.fillRect(116, 0, 4, 120);
				graOff.setColor(j1, 0, 0);
				graOff.fillRect(116, my, 4, mh);
				localMap[0][29] = my;
				localMap[1][29] = my + mh;
				if (GameCount % 10 == 0) {
					int k1 = Math.abs(rnd.nextInt() % (mh - 16)) + my;
					graOff.setColor(0, 255, 128);
					graOff.fillRect(116, k1, 4, 16);
					localMap[2][29] = k1;
				} else {
					localMap[2][29] = -1;
				}
				oy = y;
				if (y < localMap[0][8] || localMap[1][8] < y
						|| localMap[2][8] != -1 && localMap[2][8] < y
						&& y < localMap[2][8] + 16) {
					setGameState(2);
				}
				break;

			case 2: // '\002'
				if (GameCount == 1) {
					flag1 = false;
				}
				if (GameCount < 20) {
					graOff.setColor(255, 0, 0);
					int l1 = GameCount * 2;
					graOff.drawArc(32 - l1, y - l1, l1 * 2, l1 * 2, 0, 360);
				}
				if (GameCount == 20) {
					graOff.setColor(0, 0, 255);
					graOff.drawString("GameOver", 7, 50, 20);
					graOff.setColor(255, 128, 0);
					graOff.drawString("Score : " + Score, 13, 80, 20);
					if (HiScore < Score) {
						graOff.setColor(255, 128, 0);
						graOff.drawString("HiScore!!", 13, 100, 20);
						HiScore = Score;
						save();
					}
				}
				if (GameCount == 100) {
					flag1 = true;
					mouseClicked = true;
				}
				if (GameCount <= 20) {
					break;
				}
				if (!flag1 && !mousePushed) {
					flag1 = true;
					mouseClicked = false;
				}
				if (flag1 && mouseClicked) {
					setGameState(0);
				}
				break;
			}
		}

		protected void keyReleased(int i) {
			int j = getGameAction(i);
			switch (j) {
			case 6: // '\006'
				mousePushed = false;
				break;

			case 8: // '\b'
				mousePushed = false;
				break;
			}
			repaint();
		}

		protected void keyPressed(int i) {
			int j = getGameAction(i);
			switch (j) {
			case 6: // '\006'
				mousePushed = true;
				mouseClicked = true;
				// System.out.println("key down");
				break;

			case 8: // '\b'
				mousePushed = true;
				mouseClicked = true;
				break;
			}
			if (i != 50 && i != 52 && i != 53 && i != 49) {
				if (i != 51) {
					;
				}
			}
			repaint();
		}
	}

}
