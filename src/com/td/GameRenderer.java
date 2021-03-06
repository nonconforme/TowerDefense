package com.td;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.particles.ParticleSystem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by IntelliJ IDEA. User: melling Date: May 31, 2010 Time: 4:09:04 PM
 */

public class GameRenderer implements GLSurfaceView.Renderer {

	private static final String TAG = "GameRenderer";
	private Context context;

	private List<Square> enemyUnits;
	private List<Circle> circleUnits;

	private List<WayPoint> wayPoints;

	// Game Time vars
	private long gameTime;
	private long gameStartTime;
	private long normalizedGameTime;
	// private Circle circle;
	Path2 path;
	private Square2 square2;
	private Square2 square3;
	private Rect1 rect1;
	Square3 backgroundSquare;
	private ParticleSystem particleSystem;

	/*
	 * @param context - Our app context
	 */

	public GameRenderer(Context context) {
		this.context = context;

		// square = new Square(context);
		// square = new Square();
		initGameTime();

	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glClearColor(0, 0, 0, 0);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

		backgroundSquare = new Square3();

		path = new Path2();
		square2 = new Square2();
		square2.xc = 150;
		square2.yc = 150;

		square3 = new Square2();
		square3.xc = 350;
		square3.yc = 350;

		rect1 = new Rect1();
		rect1.xc = 200;
		rect1.yc = 400;
		Log.i("patSystem added","ParticleSystem added1");
	
		particleSystem = new ParticleSystem();
		Log.i("patSystem added","ParticleSystem added");

		wayPoints = new ArrayList<WayPoint>();
		enemyUnits = new ArrayList<Square>();
		circleUnits = new ArrayList<Circle>();

		loadWayPoints();
		loadEnemyUnits();

	}

	public void onDrawFrame(GL10 gl) {
		Log.i("drawframe","drawframe");
		float _red = 0.5f;
		float _green = 0.5f;
		float _blue = 1f;

		
		long currentTime = System.currentTimeMillis();
		normalizedGameTime = (currentTime - gameStartTime) / 1000; // Time in
																	// seconds

		boolean move = false;
		if ((currentTime - gameTime) > 50) {
			// Log.i(TAG, "MOVE IT - GameTime: " + gameTime + ", " +
			// (currentTime - gameTime));
			move = true;
			gameTime = currentTime;
		} else {
			// Log.i(TAG, "DON'T MOVE - GameTime: " + gameTime + ", " +
			// (currentTime - gameTime));

		}

		gl.glClearColor(_red, _green, _blue, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glClearColor(0, 0, 0, 1.0f);
		

		backgroundSquare.draw(gl, true, gameTime);
		path.draw(gl);
		particleSystem.update();
		particleSystem.draw(gl);
		for (Circle cir : circleUnits) {
			Log.i("onDrawCircle", "onDrawCircle");
			cir.draw(gl, move, gameTime);

		}

		for (Square e : enemyUnits) {
			// Log.i("onDraw", "" + i);
			// square.draw(gl);
			e.draw(gl, move, normalizedGameTime);

		}
		square2.draw(gl, move, normalizedGameTime);
		square3.draw(gl, move, normalizedGameTime);
		rect1.draw(gl, move, normalizedGameTime);


		// gl.glPopMatrix();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// avoid division by zero
		if (height == 0)
			height = 1;
		// draw on the entire screen
		gl.glViewport(0, 0, width, height);
		// setup projection matrix
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glDisable(GL10.GL_CULL_FACE);

		gl.glLoadIdentity();

		Log.i("SpriteRenderer", width + "x" + height);
		GLU.gluOrtho2D(gl, 0, 480, 0, 800);
		// GLU.gluOrtho2D(gl, 0, 480, 0, 320);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		// gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glLoadIdentity();

	}

	/*
    *
    *
    */

	public void initGameTime() {
		// this.context = context;
		gameTime = System.currentTimeMillis();
		Log.i(TAG, "GameStart: " + gameTime);
		gameStartTime = gameTime;
		normalizedGameTime = gameTime - gameStartTime;
	}

	/*
     *
     * 
     */

	void loadEnemyUnits() {
		String[] fields = new String[10];
		int nCol;
		int levelStartY = 10;
		int i;
		try {

			InputStream is = context.getAssets().open("level1EnemyUnits.txt");

			Scanner scanner = new Scanner(is);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (!line.startsWith("#")) { // Skip
					Scanner lineScanner = new Scanner(line);
					lineScanner.useDelimiter("\\|");
					nCol = 0;
					fields[nCol] = lineScanner.next();

					nCol++;
					while (lineScanner.hasNext()) { // At this point we know we
													// want the line

						fields[nCol] = lineScanner.next();
						// Log.i("=========== FOO", fields[nCol] + ",");
						nCol++;
					}

					i = 1;
					if (fields[0].startsWith("Square")) {
						int startTime = Integer.parseInt(fields[i]);
						i++;
						String colors = fields[i];
						/*
						 * i++; String startAngle = fields[i]; i++; String
						 * turnAngle = fields[i];
						 */

						String[] rgbStr = colors.split(",");

						float red = Float.parseFloat(rgbStr[0]);
						float green = Float.parseFloat(rgbStr[1]);
						float blue = Float.parseFloat(rgbStr[2]);

						Square square = new Square();
						square.redColor = red;
						square.greenColor = green;
						square.blueColor = blue;
						square.startTime = startTime;

						square.xc = 400;
						square.yc = levelStartY;
						levelStartY += 15;
						square.angle = 75;
						square.setWayPoints(wayPoints);
						square.initOrigin();
						enemyUnits.add(square);
					} else if (fields[0].startsWith("Circle")) {

						Circle circle = new Circle(200, levelStartY, 1, 8, 30);

						circle.setWayPoints(wayPoints);
						circle.initOrigin();
						circleUnits.add(circle);

						levelStartY += 15;

					} else if (fields[0].startsWith("Triangle")) {
						Circle circle = new Circle(200, levelStartY, 1, 8, 30);

						circle.setWayPoints(wayPoints);
						circle.setSides(3);
						circle.initOrigin();
						circleUnits.add(circle);

						levelStartY += 15;

					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
    *
    */

	void loadWayPoints() {
		String[] fields = new String[10];
		int nCol;
		int i;
		try {

			InputStream is = context.getAssets().open("level1WayPoints.txt");

			Scanner scanner = new Scanner(is);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (!line.startsWith("#")) { // Skip
					Scanner lineScanner = new Scanner(line);
					lineScanner.useDelimiter("\\|");
					nCol = 0;
					fields[nCol] = lineScanner.next();

					nCol++;
					while (lineScanner.hasNext()) { // At this point we know we
													// want the line

						fields[nCol] = lineScanner.next();
						// Log.i("=========== FOO", fields[nCol] + ",");
						nCol++;
					}

					i = 0;
					int seqNo = Integer.parseInt(fields[i]);
					i++;
					int x = Integer.parseInt(fields[i]);
					i++;
					int y = Integer.parseInt(fields[i]);
					i++;
					int dx = Integer.parseInt(fields[i]);
					i++;
					int dy = Integer.parseInt(fields[i]);
					// i++;

					WayPoint wp = new WayPoint(seqNo, x, y, dx, dy);

					wayPoints.add(wp);
				}
			}

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
