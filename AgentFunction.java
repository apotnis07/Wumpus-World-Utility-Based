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

	// string to store the agent's name
	// do not remove this variable
	private String agentName = "Agent Maverick";

	// all of these variables are created and used
	// for illustration purposes; you may delete them
	// when implementing your own intelligent agent
	private static int count = 0;
	List<int []> percept_sequence = new ArrayList<int []>();
	private int[] actionTable;
	private String[][] grid;

	private int[][] visited_square_times;
	private String[][] probableWumpus;
	private String[][] probablePit;

	private Agent agent;

	private char direction;
	private int[] currentLocation;

	private boolean bump;
	private boolean glitter;
	private boolean breeze;
	private boolean stench;
	private boolean scream;
	private Random rand;

	private int prev_loc_x;
	private int prev_loc_y;

	private int a,b;

	private int m,n;

	private int[] agent_loc;

	private boolean found;
	private int[] prev_loc;

	private static int deadend = 0;
	private int wumpus_encountered = 0;

	private int pit_encountered = 0;
	private int x;

	private static int steps = 0;

	private int[] reward;

	private int[] highest_reward;

	private int idx;

	private int[] utility;

	private int[] total_reward;

	private int q_value;

	private int[][] prev_utility;
	private int[] belief_state_location;

	private static int depth = 2;

	private static int action = 4;

	private int no_of_unvisited = 15;

	private static int[][] track_utility;

	private int[] utilities;

	private int[] res;

	private static boolean arrowShot;

	private static boolean arrowShotLastTurn = false;

	private static boolean wumpusFound = false;


	public AgentFunction(Agent agent) {
		// for illustration purposes; you may delete all code
		// inside this constructor when implementing your
		// own intelligent agent

		// this integer array will store the agent actions
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

	public void update_probable_pit(int[] location, boolean breeze){
//		if (breeze){
		for(int[] sqr : getAdjacentValidSquares(location)) {
			if (probablePit[sqr[0]][sqr[1]].equals("maybe_pit") && !breeze) {
				probablePit[sqr[0]][sqr[1]] = "no_pit";
			} else if(breeze && !grid[sqr[0]][sqr[1]].equals("visited")){
				probablePit[sqr[0]][sqr[1]] = "maybe_pit";
			}
		}
//		}
	}
	public void update_wumpus_found(int x, int y){
		probableWumpus[x][y] = "Wumpus";
	}
	public void update_probable_wumpus(int[] location, boolean stench){
//		if (stench){
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
//		}
	}


	public void updateVisited(int[] location){
		grid[location[0]][location[1]] = "visited";
	}

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
//		action = idx;
		highest_reward[1] = idx;
		highest_reward[0] = max_element;
		return highest_reward;
	}

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

	private boolean isValid(int x, int y) {
		if (x >= 0 && x < 4 && y >= 0 && y < 4)
			return true;
		return false;
	}

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

	public int countAdjSquareSafeAndUnvisited(int[] location){
		int x = 0;
		for(int[] sqr : getAdjacentValidSquares(location)){
			if(grid[sqr[0]][sqr[1]].equals("unvisited") && probablePit[sqr[0]][sqr[1]].equals("no_pit") && probableWumpus[sqr[0]][sqr[1]].equals("no_wumpus")){
				x += 1;
			}
		}
		return x;
	}

	public int find_action(int maximum_utility){
		for (int i = 0; i < depth; i++) {
			for (int j = 0; j < 5; j++) {
				if(maximum_utility == track_utility[i][j]){
					return j;
				}
			}
		}
		return 4;
	}

	public int[] calculate_reward_new(int[] location, char dir, int[] next_sq) {
		int[] reward = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
		int s = getAdjacentValidSquares(location).size();

		for (int i = 0; i < 5; i++) {
			int action = i;
			if (action == 0) { // turn left
				if (s == 0) {
					reward[0] = -100; // No adjacent unvisited square, penalize turning
				} else if (s == 1 && !facingVisited(location, dir)) {
					reward[0] = 100; // Only one unvisited adjacent square, reward turning towards it
				} else if (s != 1 && !facingUnvisited(location, get_new_direction(0, dir))) {
					reward[0] = -100; // Not facing an unvisited square, penalize turning
				}
//				} else if(facingUnvisited(location, get_new_direction(0, dir)) && adjSquareSafeAndUnvisited(location)){
//					reward[0] = 1000/getNo_of_unvisited();
//				} else if (facingUnvisited(location, get_new_direction(0, dir)) && adjSquareSafeAndUnvisited(location) && countAdjSquareSafeAndUnvisited(location) == 1) {
//					reward[0] = 200/getNo_of_unvisited();
//				}
				else {
					reward[0] = -1; // Default small penalty for turning
				}
			} else if (action == 1) { // turn right
				if (s == 0) {
					reward[1] = -100;
				} else if (s != 1 && !facingUnvisited(location, get_new_direction(1, dir))) {
					reward[1] = -100;
//				} else if(facingUnvisited(location, get_new_direction(1, dir)) && adjSquareSafeAndUnvisited(location)){
//					reward[1] = 1000/getNo_of_unvisited();
				}
//			else if (facingUnvisited(location, get_new_direction(1, dir)) && adjSquareSafeAndUnvisited(location) && countAdjSquareSafeAndUnvisited(location) == 1) {
//				reward[1] = 200/getNo_of_unvisited();
//			}
//				else if (facingUnvisited(location, get_new_direction(1, dir)) && adjSquareSafeAndUnvisited(location)) {
//					reward[1] = 100;
//				}

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
				reward[3] = 5; // Small reward for shooting a confirmed Wumpus
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
//	public int[] calculate_reward(int[] location, char dir, int[] next_sq){
////		Create rewards for all actions, if 0 then left, if 1 then right
////		if 2 then straight. For action 2 you have to check if by going straight
////		will the agent land in the next_sq (has to be exact square).
////		if yes, check if unvisited, if yes +1. if no 0. Check if maybe_wumpus, if
////		yes assign -1000. For shooting arrow on wumpus(confirmed) 0.
////		If wumpus. Shoot and go forward if scream heard. else update probable wumpus
////		as no wumpus where arrow was shot and mark the other square as wumpus.
////		For pit assign -1000.
//		int[] reward = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
//		int s = getAdjacentValidSquares(location).size();
//		for(int i = 0; i < 5; i++) {
//			int action = i;
//			if (action == 0) {  //what if against wall
//				//(maybe)if facing unvisited square and no stench no breeze. higher reward to go straight
//				// add higher reward to turn towards unvisited if no breeze no stench
//				// if breeze or stench and only one possible way to go, then check which way that is and turn towards it
//				// can be maybe handled in adjacent square result. if adjacent square in probablePit or probableWumpus
//				// do not add to available squares.
//				// add function to turn towards only remaining square(maybe even function also not needed)
//				// just increase the reward of turning towards the it when only one square possible
//				if(s == 0){
//					reward[0] = -100;
//				}
//				if (s==1 && !facingVisited(location, dir)){
//					reward[0] = 100;
//				}
//				if(s!=1 && !facingUnvisited(location, get_new_direction(0, dir))){
//					reward[0] = -100;
//				}
//
//				else if (location[1] == 0 && dir == 'N') {
//					reward[0] = -2;
//				} else if (location[1] == 3 && dir == 'S') {
//					reward[0] = -2;
//				} else {
//					reward[0] = -1;
//				}
//			} else if (action == 1) {
//				if(s == 0){
//					reward[1] = -100;
//				}
//				if(facingUnvisited(location, get_new_direction(1, dir)) && adjSquareSafeAndUnvisited(location)){
//					reward[1] = 1000/getNo_of_unvisited();
//				}
//
//				else if (location[1] == 3 && dir == 'N') {
//					reward[1] = -2;
//				} else {
//					reward[1] = -1;
//				}
//			} else if (action == 2 && checkLandSquare(location, dir, next_sq)) {
//				no_of_unvisited = getNo_of_unvisited();
//				if (grid[next_sq[0]][next_sq[1]].equals("unvisited")) {
//					reward[2] = 1000 / no_of_unvisited;
//				}
//				// maybe unvisited 2, visited 1, noop 0
//				if (grid[next_sq[0]][next_sq[1]].equals("visited")) {
//					reward[2] = 0;
//				}
//				if (scream){
//					reward[2] = 100;
//				}
////				if (grid[next_sq[0]][next_sq[1]].equals("visited") && (stench || breeze) && s == 1) {
////					reward[2] = 1000/getNo_of_visited();
////				}
//				if (probableWumpus[next_sq[0]][next_sq[1]].equals("maybe_wumpus") || probablePit[next_sq[0]][next_sq[1]].equals("maybe_pit")) {
//					reward[2] = -1000;
//				}
//			} else if (action == 3) {
////				if(s == 0 && !arrowShot && stench){
////					reward[3] = 1100;
////				}
//				if(probableWumpus[next_sq[0]][next_sq[1]].equals("Wumpus") && !arrowShot && stench){
//					reward[3] = 5;
//				}
//			} else if (action == 4) {
//				if(adjSquareSafeAndUnvisited(location)){
//					reward[4] = -300;
//				}
//				else{
//					reward[4] = 0;
//				}
//			}
//		}
//		return argmax(reward);
//	}

	public int[] calculate_utility(int[] location, char dir, int depth1) {
		int max_utility = Integer.MIN_VALUE;
		int r = 4;
		if (depth1 == 0) {
			return new int[]{0, 4}; // Return a default value for depth 0
		}
		if (stench && !breeze && !arrowShot){
//		if (getAdjacentValidSquares(location).size() < 3 && stench && !breeze && !arrowShot){
			arrowShotLastTurn = true;
			arrowShot = true;
			return new int[]{100,3};
		}
		if(scream){
			wumpusFound = true;
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					probableWumpus[i][j] = "no_wumpus";
				}
			}
		}

		if(!scream && arrowShotLastTurn){
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
			int[] total_reward = calculate_reward_new(location, dir, sqr);
			char new_dir = get_new_direction(total_reward[0], dir);
			int[] res = calculate_utility(sqr, new_dir, depth1 - 1);
			int q_value = total_reward[0] + res[0];

			if (q_value > max_utility) {
				r = total_reward[1];
				max_utility = q_value;
//				track_utility[depth1 - 1][r] = max_utility;
			}
		}



		return new int[]{max_utility, r};
	}


//	public int[] calculate_utility(int[] location, char dir, int depth1){
//		int max_utility = Integer.MIN_VALUE;
//		int r = 4;
//		if (depth1 == 0) {
//			return new int[]{0,4};
//		}
//
//		for(int[] sqr: getAdjacentValidSquares(location)) {
//
////				use bellman update equation, where the calculate_reward fn calculates
////				the reward, and have to store the previous utilities in a separate
////			array. first utility is zero. then u(s = 1) = calculate_reward + previous_utility(u(s=0))
////			then u(s=2) = calculate_reward + previous_utility(u(s=1)). Like this for say 5 time steps
////				if prev_utility
////			belief_state_location = new int[2];
////			belief_state_location[0] = location[0];
////			belief_state_location[1] = location[1];
//
////			for (int j = 0; j < 3; j++) {
////				total_reward = calculate_reward(belief_state_location, sqr);
////				if (j == 0) {
//			total_reward = calculate_reward(location, dir, sqr);
//			char new_dir = get_new_direction(total_reward[0], dir);
////			int action1 = total_reward[0];
//			res = calculate_utility(sqr, new_dir, depth1 -1);
//			q_value = total_reward[1] + res[0];
//
////			q_value = total_reward[1] + calculate_utility(sqr, new_dir, depth1 - 1)[0];
////					prev_utility[location[0]][location[1]] = q_value;
//			if (q_value > max_utility) {
//				r = total_reward[0];
//				max_utility = q_value;
// 				track_utility[depth1-1][r] = max_utility;
//			}
////				}
////				else {
////					for(int[] square: getAdjacentValidSquares(belief_state_location)) {
////						total_reward = calculate_reward(belief_state_location, square);
////						q_value = total_reward[1] + prev_utility[location[0]][location[1]];
////						prev_utility[location[0]][location[1]] = q_value;
////						if (q_value > max_utility) {
////							r = total_reward[0];
////							max_utility = q_value;
////							belief_state_location[0] = location[0];
////							belief_state_location[1] = location[1];
////						}
////					}
////				}
////				if (utility[1] > max_utility){
////					r = utility[0];
////					max_utility = utility[1];
////				}
////				prev_utility[location[0]][location[1]] = q_value;
////				if (q_value > max_utility) {
////					r = total_reward[0];
////					max_utility = q_value;
////				}
////			}
//
//		}
//
//		return new int[]{max_utility, r};
////		return max_utility;
////		return action;
//	}


	public int process(TransferPercept tp) {
		// To build your own intelligent agent, replace
		// all code below this comment block. You have
		// access to all percepts through the object
		// 'tp' as illustrated here:

		// read in the current percepts
		bump = tp.getBump();
		glitter = tp.getGlitter();
		breeze = tp.getBreeze();
		stench = tp.getStench();
		scream = tp.getScream();

		if (glitter == true){
			return Action.GRAB;
		}

//		steps += 1;
//		if(steps > 40){
//			return Action.NO_OP;
//		}
		agent_loc = agent.getLocation().clone();

		updateVisited(agent_loc);

		visited_square_times[agent_loc[0]][agent_loc[1]] += 1;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (visited_square_times[i][j] > 6){
					return Action.NO_OP;
				}
			}
		}

//		List<int[]> adjSquares = getAdjacentValidSquares(agent_loc);

		update_probable_pit(agent_loc, breeze);

		update_probable_wumpus(agent_loc, stench);

//		int maximum_utility = calculate_utility(agent_loc, agent.getDirection(), depth);
//		int action = find_action(maximum_utility);
		int[] act = calculate_utility(agent_loc, agent.getDirection(), depth);
		return actionTable[act[1]];
	}

	// public method to return the agent's name
	// do not remove this method
	public String getAgentName() {
		return agentName;
	}
}