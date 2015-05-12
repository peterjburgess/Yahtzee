/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		setCatCheckArray();
		for (int i = 0; i < N_SCORING_CATEGORIES; i++){
			for(int j = 1; j <= nPlayers; j++){
				playerTurn(j);
			}
		}
		endgame(nPlayers);
	}
	
	private void endgame(int nPlayers){
		for (int i = 1; i <= nPlayers; i++){
			calculateScore(i);
		}
	}
	
	private void calculateScore(int player){
		int upperScore = 0;
		for(int i = 1; i < UPPER_SCORE; i++){
			upperScore += scorecard[player - 1][i - 1];
		}
		scorecard[player - 1][UPPER_SCORE - 1] = upperScore;
		display.updateScorecard(UPPER_SCORE, player, upperScore);
		
		if(upperScore >= 63){
			scorecard[player - 1][UPPER_BONUS - 1] = 35;
		}
		else{
			scorecard[player - 1][UPPER_BONUS - 1] = 0;
		}
		
		display.updateScorecard(UPPER_BONUS, player, scorecard[player - 1][UPPER_BONUS - 1]);
		
		int lowerScore = 0;
		for(int i = THREE_OF_A_KIND; i < LOWER_SCORE; i++){
			lowerScore += scorecard[player - 1][i - 1];
		}
		scorecard[player - 1][LOWER_SCORE - 1] = lowerScore;
		display.updateScorecard(LOWER_SCORE, player, lowerScore);
		
		int total = scorecard[player - 1][UPPER_SCORE - 1] + scorecard[player-1][UPPER_BONUS - 1]
		                                                + scorecard[player - 1][LOWER_SCORE - 1];
		scorecard[player - 1][TOTAL - 1] = total;
		display.updateScorecard(TOTAL, player, scorecard[player - 1][TOTAL - 1]);
	}
	
	/**
	 * Initialises array for checking whether a legal category is selected. Will be used later.
	 * When array address has a true value, category will not be able to be selected. 
	 * The rest of the array is set to false
	 */
	private void setCatCheckArray(){
		for (int i = 0; i < N_SCORING_CATEGORIES; i++){
			for (int j = 0; j < MAX_PLAYERS; j++){
					catCheck[j][i] = false;
			}
		}
	}
	
	/**
	 * Completes a single turn for a player, including a rolling phase and a scoring phase
	 * @param player: determines which player's turn it is
	 */
	private void playerTurn(int player){
		int [] dice = rollPhase (player);
		scorePhase(player, dice);
		
	}
	
	/**
	 * After the roll phase of the player's turn. Allows player to select category for scoring
	 * checks whether category has been previously selected and adds the score onto the scorecard
	 * in the appropriate category
	 * @param player
	 * @param dice
	 */
	private void scorePhase(int player, int[] dice){
		display.printMessage("Select Category");
		int category = display.waitForPlayerToSelectCategory();
		boolean newCat = false;
		while(!newCat){
			newCat =  isNewCat(player, category);
			if(newCat){
				scorecard[player - 1][category - 1] = checkScore(category, dice);
				scorecard[player -1][TOTAL -1] += scorecard[player -1][category - 1];
				display.updateScorecard(category, player, scorecard[player - 1][category - 1]);
				display.updateScorecard(TOTAL, player, scorecard[player - 1][TOTAL - 1]);
				catCheck[player - 1][category - 1] = true;
			}
			else{
				display.printMessage("Invalid category selection. Select again");
				category = display.waitForPlayerToSelectCategory();
			}
		}
	}
	
	/**
	 * Checks whether the category selected is a valid selection. A selection is invalid
	 * if it has already been selected or isn't a direct scoring category. Returns true
	 * if category is valid.
	 * @param player
	 * @param category
	 * @return
	 */
	private boolean isNewCat(int player, int category){
		if (catCheck[player - 1][category - 1] == false)
			return true;
		else
			return false;
	}
	
	/**
	 * Completes the Roll phase of a player's turn. Player rolls the dice 3 times, selecting
	 * which dice to reroll after the first two rolls
	 * @param player specifies which player's turn this is
	 * @return returns the final dice configuration
	 */
	private int [] rollPhase (int player){
		display.waitForPlayerToClickRoll(player);
		int[] dice = new int[N_DICE];
		dice = rollDice(dice);
		display.displayDice(dice);
		for(int i = 0; i < 2; i++){
			display.printMessage("Select dice to reroll");
			display.waitForPlayerToSelectDice();
			dice = reRollDice(dice);
			display.displayDice(dice);
		}
		return dice;
	}
	
	/**
	 * Checks the score for the selected category. Returns the score
	 * @param category
	 * @param dice
	 * @return
	 */
	private int checkScore(int category, int[] dice){
		int score = 0;
		if(category <= SIXES){
			for(int i = 0; i < N_DICE; i++){
				if(dice[i] == category){
					score += category;
				}
			}
		}
		else 
			score = checkCategory(category, dice);
		
		return score;
	}
	
	/**
	 * Checks whether the selected category is a point scoring one. If it is, the appropriate 
	 * score is returned.
	 * @param category
	 * @param dice
	 * @return
	 */
	private int checkCategory(int category, int[] dice){
		int score = 0;
		int [] xOfAKind = checkXOfAKind(dice);
		if (category == THREE_OF_A_KIND){
			if (xOfAKind[0] >= 3){
				score = xOfAKind[1] * 3;
			}
		}
		else if (category == FOUR_OF_A_KIND){
			if (xOfAKind[0] >= 4){
				score = xOfAKind[1] * 4;
			}
		}
		else if (category == FULL_HOUSE){
			if (xOfAKind[0] == 3 && xOfAKind[2] == 2){
				score = 25;
			}
		}
		
		else if (category == YAHTZEE){
			if (xOfAKind[0] == 5){
				score = 50;
			}
		}
		
		else if (category == CHANCE){
			for(int i = 0; i < dice.length; i++){
				score += dice[i];
			}
		}
		
		else if(category == SMALL_STRAIGHT){
			if (checkForStraight(dice) >= 4){
				score = 30;
			}
		}
		
		else if(category == LARGE_STRAIGHT){
			if (checkForStraight(dice) >= 5){
				score = 40;
			}
		}
		return score;
	}
	
	/**
	 * Checks for both a small straight and a large straight. If a small straight is found
	 * 4 is returned. If a large straight is found, 5 is returned.
	 * @param dice
	 * @return
	 */
	
	private int checkForStraight(int[] dice){
		int straightCounter = 1;
		int lowestDiceValue = 6;
		int nextLowestDiceValue = 6;
		//find the lowest dice value
		for(int i = 0; i < dice.length; i++){
			if(dice[i] < lowestDiceValue){
				lowestDiceValue = dice[i];
			
			}
		}
		for(int i = 0; i < dice.length; i++){

			if(dice[i] < nextLowestDiceValue && dice[i] > lowestDiceValue){
				nextLowestDiceValue = dice[i];
			}
		}
		
		//for any sort of straight, lowest dice value must be 3 or lower
		if(lowestDiceValue == 1 && nextLowestDiceValue == 3){
			int currentDiceValue = nextLowestDiceValue; //use currentDiceValue to find next dice value
			for(int i = 0; i < dice.length; i++){
				if (nextDiceValueExists(currentDiceValue, dice)){
					straightCounter++;
					currentDiceValue++;
				}
				else
					break;
			}
		}
		else if(lowestDiceValue <=3){
			int currentDiceValue = lowestDiceValue; //use currentDiceValue to find next dice value
			for(int i = 0; i < dice.length; i++){
				if (nextDiceValueExists(currentDiceValue, dice)){
					straightCounter++;
					currentDiceValue++;
				}
				else
					break;
			}
		}
		

		return straightCounter;
	}
	
	/**
	 * When checking for a straight, this method takes in a value and looks for the next
	 * value in the straight. If it finds it, it returns true. If not, it returns false
	 * @param currentDiceValue: The current highest value in the straight
	 * @param dice: Array of dice with values from 1 to 6
	 * @return
	 */
	private boolean nextDiceValueExists(int currentDiceValue, int[] dice){
		for (int i = 0; i < dice.length; i++){
			if (dice[i] == currentDiceValue + 1)
				return true;
		}
		return false;
	}
	
	/**
	 * Checks for the number of dice with the same number on for the 3 of a kind, 4 of a kind,
	 * Full House and Yahtzee categories. Returns an array with 4 elements. The first element
	 * contains whether there is a 3, 4 or 5 of a kind. The 2nd element contains the dice value
	 * for this x of a kind. The 3rd element contains whether there is a 2 of a kind that is separate
	 * from any 3, 4 or 5 of a kind. The 4th element contains the dice value for this 2 of a kind
	 * @param dice
	 * @return
	 */
	private int[] checkXOfAKind(int[] dice){
		int[] xOfAKind = new int[4];
		for(int i = 0; i < dice.length - 1; i++){
			int counter = 1;
			for(int j = i+1 ; j < dice.length; j++){
				if (dice[j] == dice[i]){
					counter ++;
				}
			}
			if (counter >= 3){
				xOfAKind[0] = counter;
				xOfAKind[1] = dice[i];
			}
			if (counter >= 4){
				break;
			}
			if (counter == 2 && dice[i] != xOfAKind[1]){
				xOfAKind[2] = counter;
				xOfAKind[3] = dice[i];
			}
		}
		return xOfAKind;
	}
	
	/**
	 * Assigns a random number between 1 and 6 to each dice. Only used for the first roll
	 * @param dice is an array of integers passed when called. Each element of the array
	 * is assigned a random integer value between 1 and 6
	 * @return
	 */
	private int[] rollDice(int[] dice){
		for(int i = 0; i < N_DICE; i++){
			if(!display.isDieSelected(i)){
				dice[i] =  rgen.nextInt(1,6);
	
			}

		}
		return dice;
	}

	/**
	 * Assigns a random number between 1 and 6 to each selected dice. Used for the 2 rerolls
	 * @param dice is an array of integers passed when called. Each element of the array
	 * is assigned a random integer value between 1 and 6
	 * @return
	 */
	private int[] reRollDice(int[] dice){
		for(int i = 0; i < N_DICE; i++){
			if(display.isDieSelected(i)){
				dice[i] =  rgen.nextInt(1,6);
			}

		}
		return dice;
	}
/* Java main method to ensure that this program starts correctly */
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}

/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = RandomGenerator.getInstance();
	private int [][] scorecard = new int[MAX_PLAYERS][TOTAL];
	//keeps track of categories that can be selected by the player
	private boolean [][] catCheck = new boolean[MAX_PLAYERS][TOTAL]; 
}
