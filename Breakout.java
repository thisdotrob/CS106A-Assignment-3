/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 * 
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

	/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

	/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

	/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 40;
	private static final int PADDLE_HEIGHT = 10;

	/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 40;
	
	/** Y position of the paddle */
	private static final int PADDLE_Y = HEIGHT - PADDLE_Y_OFFSET - PADDLE_HEIGHT;

	/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

	/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

	/** Separation between bricks */
	private static final int BRICK_SEP = 4;

	/** Width of a brick */
	private static final double BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / (double)NBRICKS_PER_ROW;

	/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

	/** Radius and diameter of the ball in pixels */
	private static final int BALL_RADIUS = 10;
	private static final int BALL_DIAMETER = 2 * BALL_RADIUS;

	/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

	/** Number of turns */
	private static final int NTURNS = 3;
	
	/** Delay between ball moves */
	private static final int DELAY = 10;
	
	/** Number of collision points on the paddle required */
	private static final int PADDLE_COLLISION_POINTS = 2 + (PADDLE_WIDTH / BALL_DIAMETER);

	/* Method: run */
	/** Sets up the game, prompts the user to start and then runs the game until the player either wins or loses */
	public void run() {
		setupGame();
		promptUserToStart();
		while(!gameFinished) {
			for (int i = 0; i < NTURNS; i++) {
				nTurnsLeft--;
				runGame();
			}
		}
	}
	
	/* Method: setupGame */
	/** Sets up the game environment */
	private void setupGame(){
		addBricks();
		addPaddle();
		addScoreLabel();
		addMouseListeners();
	}
	
	/* Method: runGame */
	/** Runs and finishes the game. Precondition = setupGame must be called first. */
	private void runGame(){
		addBall();
		while( ball.getY() < HEIGHT ) {
			moveBall();
			checkForBounce();
			pause(DELAY);
			checkForDoubleSpeed();
			isGameWon();
		}
		if(nTurnsLeft > 0) {
			pauseAndUseCredit();
			paddleHits = 0;
		}
		else {
			gameOver();
			gameFinished = true;
		}
	}
	
	/* Method: isGameWon */
	/** Checks to see if the game has been won (i.e. no bricks left) and removes the ball if so. */
	private boolean isGameWon() {
		if(numBricksLeft == 0) {
			gameWon();
			remove(ball);
			gameFinished = true;
			return true;
		}
		else return false;
	}
	
	/* Method: checkForDoubleSpeed */
	/** Doubles the ball's X velocity on the 7th paddle hit. */
	private void checkForDoubleSpeed() {
		if(paddleHits == 7) {
			paddleHits++;
			ballVelocityX *= 2;
		}		
	}

	/* Method: addScoreLabel */
	/** Displays the score. */
	private void addScoreLabel() {
		scoreLabel.setFont("SansSerif-10");
		scoreLabel.setColor(Color.black);
		scoreLabel.setLocation((WIDTH - scoreLabel.getWidth())/2, HEIGHT - 2 * scoreLabel.getAscent());
		add(scoreLabel);
	}
	
	/* Method: gameOver */
	/** Displays the game over message */
	private void gameOver() {
		GLabel gameOver = new GLabel("GAME OVER. insert coins");
		gameOver.setFont("SansSerif-20");
		gameOver.setColor(Color.black);
		gameOver.setLocation((WIDTH - gameOver.getWidth())/2,(HEIGHT - gameOver.getAscent()) / 2);
		add(gameOver);
	}
	
	/* Method: promptUserToStart */
	/** Displays message prompting user to start. */
	private void promptUserToStart() {
		GLabel startPrompt = new GLabel("Are you ready to play? Click to start.");
		startPrompt.setFont("SansSerif-20");
		startPrompt.setColor(Color.green);
		startPrompt.setLocation((WIDTH - startPrompt.getWidth())/2,(HEIGHT - startPrompt.getAscent()) / 2);
		add(startPrompt);
		waitForClick();
		remove(startPrompt);
	}
	
	/* Method: endGameWin */
	/** Displays congratulatory message when no bricks left. */
	private void gameWon() {
		GLabel congrats = new GLabel("Congratulations, you have won!");
		congrats.setFont("SansSerif-20");
		congrats.setColor(Color.blue);
		congrats.setLocation((WIDTH - congrats.getWidth())/2,(HEIGHT - congrats.getAscent()) / 2);
		add(congrats);		
	}
	
	/* Method: pauseAndUseCredit */
	/** Prompt the user to use a credit and restart play if the ball has slipped past the paddle */
	private void pauseAndUseCredit() {
		GLabel commiserations = new GLabel("Bad Luck! " + nTurnsLeft + " credits left, click to use credit");
		commiserations.setFont("SansSerif-18");
		commiserations.setColor(Color.red);
		commiserations.setLocation((WIDTH - commiserations.getWidth())/2,(HEIGHT - commiserations.getAscent()) / 2);
		add(commiserations);
		waitForClick();
		remove(commiserations);		
	}
	
	/* Method: mouseMoved */
	/** Moves the paddle, centred on the mouse location (provided the mouse is within the game board). */
	public void mouseMoved(MouseEvent e){
		if ( e.getX() < WIDTH - ( PADDLE_WIDTH / 2 ) && e.getX() > PADDLE_WIDTH / 2 ) {
			paddle.setLocation(e.getX() - ( PADDLE_WIDTH / 2 ), PADDLE_Y );
		}
	}
	
	/* Method: setupBricks */
	/** Sets up the bricks. */
	private void addBricks(){
		for (int i = 0; i < NBRICK_ROWS; i++) {
			int y = BRICK_Y_OFFSET + ( i * ( BRICK_HEIGHT + BRICK_SEP ));
			Color color = getRowColor(i);
			addRowOfBricks(y,color);
		}		
	}
	
	/* Method: addRowOfBricks */
	/** Adds a row of bricks of the specified colour, at the specified y coordinate. */
	private void addRowOfBricks(int y, Color color) {
		for (int i = 0; i < NBRICKS_PER_ROW ; i++) {
			double x = ( BRICK_WIDTH + BRICK_SEP ) * i ;
			addBrick(x,y,color);
		}
	}
	
	/* Method: addBrick */
	/** Adds a brick of the specified colour, at the specified x, y coordinates. */
	private void addBrick(double x, int y, Color color) {
		GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT) ;
		brick.setFilled(true) ;
		brick.setFillColor(color) ;
		brick.setColor(color);
		add(brick);
	}
	
	/* Method: getRowColor */
	/** Returns the color of the next row, based on the row number. */
	private Color getRowColor(int rowNum) {
		int lastDigitOfRowNum = getLastDigit(rowNum);
		Color rowColor;
		switch(lastDigitOfRowNum) {
			case 0:		rowColor = Color.RED;		break;
			case 1: 	rowColor = Color.RED;		break;
			case 2: 	rowColor = Color.ORANGE;	break;
			case 3: 	rowColor = Color.ORANGE;	break;
			case 4: 	rowColor = Color.YELLOW;	break;
			case 5: 	rowColor = Color.YELLOW;	break;
			case 6: 	rowColor = Color.GREEN;		break;
			case 7: 	rowColor = Color.GREEN;		break;
			case 8: 	rowColor = Color.CYAN;		break;
			case 9: 	rowColor = Color.CYAN;		break;
			default:	rowColor = Color.white;		break;
		}
		return rowColor;
	}
	
	/* Method: increaseScore */
	/** Increases the score by an amount according to the colour of the brick which has been hit. */
	private void increaseScore(GObject object) {
		Color color = object.getColor();
		if (color == Color.RED) {
			score += 11;
		}
		else if (color == Color.ORANGE){
			score += 7;
		}
		else if (color == Color.YELLOW){
			score += 4;
		}
		else if (color == Color.GREEN){
			score += 2;
		}
		else {
			score += 1;
		}
	}
	
	/* Method: getLastDigit */
	/** Returns the last digit of an int. */
	private int getLastDigit(int number) {
		int lastDigit = number % 10;
		return lastDigit;
	}
	
	/* Method: addPaddle */
	/** Adds the paddle to the game board. */
	private void addPaddle() {
		paddle = new GRect( 0, PADDLE_Y, PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setFilled(true);
		paddle.setColor(Color.BLACK);
		add(paddle);
	}
	
	/* Method: addBall */
	/** Adds the ball to the game board. */
	private void addBall() {
		int ballStartX = ( WIDTH - BALL_RADIUS ) / 2 ;
		int ballStartY = ( HEIGHT - BALL_RADIUS ) / 2 ;
		ball = new GOval( ballStartX, ballStartY, BALL_DIAMETER, BALL_DIAMETER);
		ball.setFilled(true);
		ball.setFillColor(Color.BLACK);
		add(ball);
		initialiseBallVelocityX();
	}
	
	/* Method: moveBall */
	/** Moves the ball one step. */	
	private void moveBall() {
		ball.move(ballVelocityX,ballVelocityY);
	}
	
	/* Method: checkForBounce */
	/** Checks to see if the ball has collided with an object, and redirects its path of travel if so. */
	private void checkForBounce() {
		double x = ball.getX();
		double y = ball.getY();
		if ( y <= 0 ) {
			ballVelocityY = -ballVelocityY;
			ball.move(0, y * -2);
		}
		else if ( x >= WIDTH - BALL_DIAMETER ) {
			ballVelocityX = -ballVelocityX;
			ball.move( ( x - (WIDTH - BALL_DIAMETER) ) * -2, 0);
		}
		else if ( x <= 0 ) {
			ballVelocityX = -ballVelocityX;
			ball.move( ball.getX() * -2, 0);
		}
		else if (checkPaddleMiddleBounce()) {
			ballVelocityY = -ballVelocityY;
			moveBallClearOfPaddle();
			paddleHits++;
			bounceClip.play();
		}
		else if (checkPaddleLeftEdgeBounce()) {
			ballVelocityY = -ballVelocityY;
			if(ballVelocityX > 0) {
				ballVelocityX = -ballVelocityX;
			}
			moveBallClearOfPaddle();
			paddleHits++;
			bounceClip.play();
		}
		else if (checkPaddleRightEdgeBounce()) {
			ballVelocityY = -ballVelocityY;
			if(ballVelocityX < 0) {
				ballVelocityX = -ballVelocityX;
			}
			moveBallClearOfPaddle();
			paddleHits++;
			bounceClip.play();
		}
		else if (getCollidingBrick() != null && getCollidingBrick() != paddle && getCollidingBrick() != scoreLabel) {
			ballVelocityY = -ballVelocityY;
			increaseScore(getCollidingBrick());
			scoreLabel.setLabel("Score: " + score);
			remove(getCollidingBrick());
			numBricksLeft--;
			bounceClip.play();
		}
	
	}
	
	/* Method: getCollidingBrick */
	/** returns the GObject (i.e. the brick) that the ball has collided with. */
	private GObject getCollidingBrick() {
		double x = ball.getX();
		double y = ball.getY();
		GObject collidingBrick = null;
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (getElementAt(x+(i*(BALL_RADIUS/2)),y+(j*(BALL_RADIUS/2))) != null) {
					collidingBrick = getElementAt(x,y);
				}
			}
		}
		return collidingBrick;
	}
		
	/* Method: checkPaddleMiddleBounce */
	/** Checks if the ball has hit the middle of the paddle */
	private boolean checkPaddleMiddleBounce() {
		boolean checkPaddleMiddleBounce = false;
		for (int i = 1; i < PADDLE_COLLISION_POINTS - 1; i++) {
			double x = paddle.getX() + ( i * BALL_DIAMETER ) ;
			if (getElementAt(x,PADDLE_Y) == ball){
				checkPaddleMiddleBounce = true;
			}
		}
		return checkPaddleMiddleBounce;
	}
	
	/* Method: checkPaddleLeftEdgeBounce */
	/** Checks if the ball has hit the left edge of the paddle */	
	private boolean checkPaddleLeftEdgeBounce() {
		boolean checkPaddleLeftEdgeBounce = false;
		if (getElementAt(paddle.getX(),PADDLE_Y) == ball) {
			checkPaddleLeftEdgeBounce = true;
		}
		return checkPaddleLeftEdgeBounce;
	}
	
	/* Method: checkPaddleRightEdgeBounce */
	/** Checks if the ball has hit the right edge of the paddle */	
	private boolean checkPaddleRightEdgeBounce() {
		boolean checkPaddleRightEdgeBounce = false;
		if (getElementAt(paddle.getX()+PADDLE_WIDTH,PADDLE_Y) == ball) {
			checkPaddleRightEdgeBounce = true;
		}
		return checkPaddleRightEdgeBounce;
	}
	
	/* Method: checkPaddleBounce */
	/** Moves the ball clear of the paddle once it has hit, to avoid it getting "stuck" in the paddle */
	private void moveBallClearOfPaddle(){
		double diff = PADDLE_Y - (ball.getY() + BALL_DIAMETER);
		if (diff < 0) {
			ball.move(0,diff);
		}		
	}
	
	/* Method: initialiseBallVelocityX */
	/** Generates a random value for ballVelocityX between 1 and 3, converted to a negative with probability 0.5 */
	private void initialiseBallVelocityX(){	
		ballVelocityX = rgen.nextDouble(1, 3);
		if (rgen.nextBoolean(0.5)) ballVelocityX = -ballVelocityX;
	}
	
	/* Private instance variable for the paddle */
	private GRect paddle;
	
	/* Private instance variable for the ball */
	private GOval ball;
	
	/* Private instance variables for ball velocity */
	private double ballVelocityX;
	private double ballVelocityY = -3;
	
	/* Private instance variable for the random-number generator */
	private RandomGenerator rgen = RandomGenerator.getInstance();
	
	/* Private instance variable for the number of bricks left */
	private int numBricksLeft = NBRICK_ROWS * NBRICKS_PER_ROW;
	
	/* Private instance variable for the number of turns left */
	private int nTurnsLeft = NTURNS;
	
	/* Private instance variable to show whether the game has been won or not */
	private boolean gameFinished = false;
	
	/* Private instance variable to count the number of times the ball has hit paddle */
	private int paddleHits = 0;
	
	/* Private instance to count the score */
	private int score = 0;
	
	/* Private instance to allow scoreLabel to be updated when bricks hit */
	private GLabel scoreLabel = new GLabel("Score: " + score);
	
	/* Private instance to allow sound to be played when brick hit */
	private AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");
	
}
