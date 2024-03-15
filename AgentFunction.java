/*
 * Class that defines the agent function.
 *
 * Written by James P. Biagioni (jbiagi1@uic.edu)
 * for CS511 Artificial Intelligence II
 * at The University of Illinois at Chicago
 *
 * Last modified 2/19/07
 *
 * DISCLAIMER:
 * Elements of this application were borrowed from
 * the client-server implementation of the Wumpus
 * World Simulator written by Kruti Mehta at
 * The University of Texas at Arlington.
 *
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class AgentFunction {
	private String agentName = "Agent Maverick";
	private int[] actionTable;
	private String[][] grid;
	private int[][] visited_square_times;
	private String[][] probableWumpus;
	private String[][] probablePit;
	private Agent agent;
	private char direction;
	private boolean bump;
	private boolean glitter;
	private boolean breeze;
	private boolean stench;
	private boolean scream;
	private Random rand;
	private int[] agent_loc;
	private int[] prev_loc;
	private int[] highest_reward;
	private int idx;
	private int[][] prev_utility;
	private static int depth = 2;
	private int no_of_unvisited = 15;
	private static int[][] track_utility;
	private static boolean arrowShot;
	private static boolean arrowShotLastTurn = false;
	private static boolean wumpusFound = false;
	private static int steps;
	public AgentFunction(Agent agent) {

		this.agent = agent;

		actionTable = new int[6];

		grid = new String[4][4];

		visited_square_times = new int[4][4];

		int[] res = {Integer.MIN_VALUE, Integer.MIN_VALUE};

		probableWumpus = new String[4][4];

		probablePit = new String[4][4];

		prev_utility = new int[4][4];

		agent_loc = new int[3];

		track_utility = new int[depth][5];

		direction = 1;

		arrowShot = false;

		prev_loc = new int[3];

		boolean west = true;
		boolean east = true;
		boolean north = true;
		boolean south = true;

		for (int i = 0; i < depth; i++) {
			for (int j = 0; j < 5; j++) {
				track_utility[i][j] = Integer.MIN_VALUE;
			}
		}

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				grid[i][j] = "unvisited";
			}
		}
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				probableWumpus[i][j] = "no_wumpus";
			}
		}
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				probablePit[i][j] = "no_pit";
			}
		}
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				visited_square_times[i][j] = 0;
			}
		}

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				prev_utility[i][j] = Integer.MIN_VALUE;
			}
		}
		// Possible Actions
		actionTable[0] = Action.TURN_LEFT;
		actionTable[1] = Action.TURN_RIGHT;
		actionTable[2] = Action.GO_FORWARD;
		actionTable[3] = Action.SHOOT;
		actionTable[4] = Action.NO_OP;
		actionTable[5] = Action.GRAB;

		// new random number generator, for
		// randomly picking actions to execute
		rand = new Random();
	}
	//Get new direction given the current direction once the action is performed
	public char get_new_direction(int action, char direction){
		if (direction == 'N' && action == 0)
			return 'W';
		if (direction == 'S' && action == 0)
			return 'E';
		if (direction == 'E' && action == 0)
			return 'N';
		if (direction == 'W' && action == 0)
			return 'S';
		if (direction == 'N' && action == 1)
			return 'E';
		if (direction == 'S' && action == 1)
			return 'W';
		if (direction == 'E' && action == 1)
			return 'S';
		if (direction == 'W' && action == 1)
			return 'N';
		else
			return direction;
	}

	public List<int[]> getAdjacentValidSquares(int[] location) {
		List<int[]> squares = new ArrayList<>();
		if (location != null) {
			// Check and add the square to the south
			if (location[0] - 1 >= 0 && probableWumpus[location[0] - 1][location[1]].equals("no_wumpus") && probablePit[location[0] - 1][location[1]].equals("no_pit")) {
				squares.add(new int[]{location[0] - 1, location[1]});
			}
			// Check and add the square to the north
			if (location[0] + 1 < 4 && probableWumpus[location[0] + 1][location[1]].equals("no_wumpus") && probablePit[location[0] + 1][location[1]].equals("no_pit")) {
				squares.add(new int[]{location[0] + 1, location[1]});
			}
			// Check and add the square to the east
			if (location[1] + 1 < 4 && probableWumpus[location[0]][location[1] + 1].equals("no_wumpus") && probablePit[location[0]][location[1] + 1].equals("no_pit")) {
				squares.add(new int[]{location[0], location[1] + 1});
			}
			// Check and add the square to the west
			if (location[1] - 1 >= 0 && probableWumpus[location[0]][location[1] - 1].equals("no_wumpus") && probablePit[location[0]][location[1] - 1].equals("no_pit")) {
				squares.add(new int[]{location[0], location[1] - 1});
			}
		}
		return squares;
	}

	// Update the grid of the probable locations of the pit
	public void update_probable_pit(int[] location, boolean breeze){
		for(int[] sqr : getAdjacentValidSquares(location)) {
			if (probablePit[sqr[0]][sqr[1]].equals("maybe_pit") && !breeze) {  //If has been tagged maybe_pit and no breeze is felt no_pit
				probablePit[sqr[0]][sqr[1]] = "no_pit";
			} else if(breeze && !grid[sqr[0]][sqr[1]].equals("visited")){ // If not visited tag as maybe_pit
				probablePit[sqr[0]][sqr[1]] = "maybe_pit";
			}
		}
	}

	//Update the grid when the wumpus is found
	public void update_wumpus_found(int x, int y){
		probableWumpus[x][y] = "Wumpus";
	}

	// Update the grid of the probable wumpus locations
	public void update_probable_wumpus(int[] location, boolean stench){
		for(int[] sqr: getAdjacentValidSquares(location)){
			if(stench) {
				if (probableWumpus[sqr[0]][sqr[1]].equals("maybe_wumpus")) {
					update_wumpus_found(sqr[0], sqr[1]);
				} else if (!grid[sqr[0]][sqr[1]].equals("visited")) {
					probableWumpus[sqr[0]][sqr[1]] = "maybe_wumpus";
				}
			}
			else if(probableWumpus[sqr[0]][sqr[1]].equals("maybe_wumpus") && !stench){
				probableWumpus[sqr[0]][sqr[1]] = "no_wumpus";
			}
		}
	}


	// Update the grid when the agent visits a square
	public void updateVisited(int[] location){
		grid[location[0]][location[1]] = "visited";
	}

	// Check if the agent will land in the square passed in the argument when it is located in location, facing direction dir and goes forward
	public boolean checkLandSquare(int[] location, char dir, int[] next_sq){
		if(dir == 'N' && location[0] + 1 == next_sq[0]){
			return true;
		} else if (dir == 'S' && location[0] - 1 == next_sq[0]) {
			return true;
		}else if (dir == 'W' && location[1] - 1 == next_sq[1]) {
			return true;
		}else if (dir == 'E' && location[1] + 1 == next_sq[1]) {
			return true;
		}
		return false;
	}


	// Return the maximum utility and action from all the possible utilities explored through depth first search
	public int[] argmax(int[] reward){
		int max_element = Integer.MIN_VALUE;
		idx = 0;
		highest_reward = new int[2];
		for(int i = 0; i < 5; i++){
			if(reward[i] > max_element){
				max_element = reward[i];
				idx = i;
			}
		}
		highest_reward[1] = idx;
		highest_reward[0] = max_element;
		return highest_reward;
	}

	// Get the no of unvisited squares in the environment
	public int getNo_of_unvisited(){
		int count1 = 0;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if(grid[i][j].equals("unvisited")){
					count1 += 1;
				}
			}
		}
		return count1;
	}

	// Get the no of visited squares in the environment
	public int getNo_of_visited(){
		int count1 = 0;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if(grid[i][j].equals("visited")){
					count1 += 1;
				}
			}
		}
		return count1;
	}

	// Check if the square provided with the x, y co-ordinate is a valid location(checks for borders of the grid)
	private boolean isValid(int x, int y) {
		if (x >= 0 && x < 4 && y >= 0 && y < 4)
			return true;
		return false;
	}

	// Checks if the agent is facing a visited square
	public boolean facingVisited(int[] location, char direction){
		boolean check = false;
		if(direction == 'N' && isValid(location[0] + 1, location[1]) && grid[location[0] + 1][location[1]].equals("visited")){
			check = true;
		}
		if(direction == 'S' && isValid(location[0] - 1, location[1]) && grid[location[0] - 1][location[1]].equals("visited")){
			check = true;
		}
		if(direction == 'E' && isValid(location[0], location[1] + 1) && grid[location[0]][location[1] + 1].equals("visited")){
			check = true;
		}
		if(direction == 'W' && isValid(location[0], location[1] - 1) && grid[location[0]][location[1] - 1].equals("visited")) {
			check = true;
		}
		return check;
	}


	// Checks if the agent is facing an unvisited square
	public boolean facingUnvisited(int[] location, char direction){
		boolean check = false;
		if(direction == 'N' && isValid(location[0] + 1, location[1]) && grid[location[0] + 1][location[1]].equals("unvisited")){
			check = true;
		}
		if(direction == 'S' && isValid(location[0] - 1, location[1]) && grid[location[0] - 1][location[1]].equals("unvisited")){
			check = true;
		}
		if(direction == 'E' && isValid(location[0], location[1] + 1) && grid[location[0]][location[1] + 1].equals("unvisited")){
			check = true;
		}
		if(direction == 'W' && isValid(location[0], location[1] - 1) && grid[location[0]][location[1] - 1].equals("unvisited")){
			check = true;
		}
		return check;
	}

	// Checks if there is an adjacent square that is safe and unvisited
	public boolean adjSquareSafeAndUnvisited(int[] location){
		boolean check = false;
		for(int[] sqr : getAdjacentValidSquares(location)){
			if(grid[sqr[0]][sqr[1]].equals("unvisited") && probablePit[sqr[0]][sqr[1]].equals("no_pit") && probableWumpus[sqr[0]][sqr[1]].equals("no_wumpus")){
				check = true;
				return true;
			}
		}
		return check;
	}

	// Count the number of adjacent squares that are safe and unvisited
	public int countAdjSquareSafeAndUnvisited(int[] location){
		int x = 0;
		for(int[] sqr : getAdjacentValidSquares(location)){
			if(grid[sqr[0]][sqr[1]].equals("unvisited") && probablePit[sqr[0]][sqr[1]].equals("no_pit") && probableWumpus[sqr[0]][sqr[1]].equals("no_wumpus")){
				x += 1;
			}
		}
		return x;
	}

	// Calculate reward as part of the bellman equation
	public int[] calculate_reward_new(int[] location, char dir, int[] next_sq) {
		int[] reward = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
		int s = getAdjacentValidSquares(location).size();

		for (int i = 0; i < 5; i++) {
			int action = i;
			if (action == 0) { // Turn Left
				if (s == 0) { // No way to move (no valid and safe adjacent squares)
					reward[0] = -100;
				} else if (s == 1 && !facingVisited(location, dir)) { // Not facing a visited square
					reward[0] = 100;
				} else if (s != 1 && !facingUnvisited(location, get_new_direction(0, dir))) {  //Not facing unvisited square
//					reward[0] = -200;
					reward[0] = -1000/getNo_of_unvisited();
				}
				else {
					reward[0] = -1;
				}
			} else if (action == 1) { // Turn Right
				if (s == 0) { // No way to move (no valid and safe adjacent squares)
					reward[1] = -100;
				} else if (s != 1 && !facingUnvisited(location, get_new_direction(1, dir))) { //Not facing unvisited square
//					reward[1] = -200;
					reward[1] = -1000/getNo_of_unvisited();
				}
				else {
					reward[1] = -1;
				}
			} else if (action == 2 && checkLandSquare(location, dir, next_sq)) { // go forward
				no_of_unvisited = getNo_of_unvisited();
				if (grid[next_sq[0]][next_sq[1]].equals("unvisited")) {
					reward[2] = 1000 / no_of_unvisited; // Higher reward for unvisited squares
				} else if (grid[next_sq[0]][next_sq[1]].equals("visited")) {
					reward[2] = 0; // Lower reward for visited squares
				}
				if (probableWumpus[next_sq[0]][next_sq[1]].equals("maybe_wumpus") || probablePit[next_sq[0]][next_sq[1]].equals("maybe_pit")) {
					reward[2] = -1000; // High penalty for potentially dangerous squares
				}
			} else if (action == 3 && probableWumpus[next_sq[0]][next_sq[1]].equals("Wumpus")) {
				reward[3] = 5;
			} else if (action == 4) { // no-op
				if (adjSquareSafeAndUnvisited(location)) {
					reward[4] = -300; // Penalize no-op when there are safe unvisited squares
				} else {
					reward[4] = 0; // No penalty for no-op when all adjacent squares are visited or dangerous
				}
			}
		}
		return argmax(reward);
	}

	public int[] calculate_utility(int[] location, char dir, int depth1) {
		int max_utility = Integer.MIN_VALUE;
		int r = 4;
		if (depth1 == 0) {
			return new int[]{0, 4}; // Return a default value for depth 0
		}
		if (getAdjacentValidSquares(location).size() < 3 && stench && !breeze && !arrowShot){ //If stench and not breeze and haven't shot an arrow yet
			arrowShotLastTurn = true;
			arrowShot = true;
			return new int[]{100,3}; // Shoot an arrow
		}
		if(scream){
			wumpusFound = true;
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					probableWumpus[i][j] = "no_wumpus"; //mark the grid clear of the wumpus
				}
			}
		}

		// As we haven't turned yet from when we shot the arrow, if we don't hear a scream we can move forward as the square is clear
		if(!scream && arrowShotLastTurn){  // If we missed the wumpus
			arrowShotLastTurn = false;

			if (dir == 'N' && isValid(location[0] + 1 ,location[1])){
				probableWumpus[location[0] + 1][location[1]] = "no_wumpus";
				return new int[] {100,2};
			}
			else if (dir == 'S' && isValid(location[0] - 1 ,location[1])){
				probableWumpus[location[0] - 1][location[1]] = "no_wumpus";
				return new int[] {100,2};
			}
			else if (dir == 'E' && isValid(location[0],location[1] + 1)){
				probableWumpus[location[0]][location[1]+1] = "no_wumpus";
				return new int[] {100,2};

			}else if (dir == 'W' && isValid(location[0] ,location[1] - 1)){
				probableWumpus[location[0]][location[1] - 1] = "no_wumpus";
				return new int[] {100,2};

			}

		}

		arrowShotLastTurn = false;

		for (int[] sqr : getAdjacentValidSquares(location)) {
			int[] total_reward = calculate_reward_new(location, dir, sqr);  //calculate reward for every adjacent square
			char new_dir = get_new_direction(total_reward[0], dir); // get the new direction, for when the max utility action is to turn but the physical direction is different from the belief state
			int[] res = calculate_utility(sqr, new_dir, depth1 - 1); // recursively call the calculate_utility function to perform dfs
			int q_value = total_reward[0] + res[0]; // adding all the utilities in a branch

			if (q_value > max_utility) { // select max q_value
				r = total_reward[1];
				max_utility = q_value;
			}
		}
		return new int[]{max_utility, r}; // return max_utility and action
	}



	public int process(TransferPercept tp) {
		bump = tp.getBump();
		glitter = tp.getGlitter();
		breeze = tp.getBreeze();
		stench = tp.getStench();
		scream = tp.getScream();

		if (glitter == true){
			return Action.GRAB;
		}


		if(steps > 48){
			return Action.NO_OP; // If steps greater than 48 start doing No-Op
		}
		agent_loc = agent.getLocation().clone();

		updateVisited(agent_loc); // update visited state of the grid

		visited_square_times[agent_loc[0]][agent_loc[1]] += 1; //count the number of times the agent has visited a particular square
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (visited_square_times[i][j] > 6){ //  if visited a same square more than 6 times start doing no-op
					return Action.NO_OP;
				}
			}
		}

		update_probable_pit(agent_loc, breeze);

		update_probable_wumpus(agent_loc, stench);

		int[] act = calculate_utility(agent_loc, agent.getDirection(), depth);
		return actionTable[act[1]];
	}
	public String getAgentName() {
		return agentName;
	}
}