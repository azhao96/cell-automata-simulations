package cellsociety_team21;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

public class PredatorPreyRules extends Rules {
	private int mySharkEnergy;
	private int mySharkReproductionTime;
	private int myFishReproductionTime;
	private int myInitSharkReproductionTime;
	private int myInitFishReproductionTime;
	private static final String FISH = "FISH";
	private static final String SHARK = "SHARK";
	private static final String WATER = "WATER";
	private static final Color FISHCOLOR = Color.TEAL;
	private static final Color SHARKCOLOR = Color.GRAY;
	private static final Color WATERCOLOR = Color.BLUE;
	private static final int NUM_NEIGHBORS = 4;
	
	public PredatorPreyRules(int initialSharkEnergy, int sharkReproductionTime, int fishReproductionTime) {
		mySharkEnergy = initialSharkEnergy;
		mySharkReproductionTime = sharkReproductionTime;
		myFishReproductionTime = fishReproductionTime;
		myInitSharkReproductionTime = sharkReproductionTime;
		myInitFishReproductionTime = fishReproductionTime;
	}
	
	/**
	 * Apply the rules of the Predator-Prey simulation to a Cell based on its state.
	 */
	@Override
	public void applyRulesToCell(Cell cell, Grid grid) {
		String curState = cell.getCurState();
		
		if (cell.getCurRow() == grid.getNumRows() - 1 && cell.getCurCol() == grid.getNumCols() - 1) {
			updateReproductionTimes();
		}
		
		if (curState.equals(FISH)) {
			handleFishCell(cell, grid);
		} else if (curState.equals(SHARK)) {
			handleSharkCell(cell, grid);
		}
	}

	/**
	 * Reduce the number of moves left for each Cell type to reproduce; reset reproduction times if they reproduced this round.
	 */
	private void updateReproductionTimes() {
		if (mySharkReproductionTime > 0) {
			mySharkReproductionTime = myInitSharkReproductionTime;
		} else {
			mySharkReproductionTime--;
		}
		
		if (myFishReproductionTime > 0) {
			myFishReproductionTime = myInitFishReproductionTime;
		} else {
			myFishReproductionTime--;
		}
	}

	/**
	 * Try to move fish if possible. If the fish has already been eaten by a shark then do nothing.
	 * @param cell: fish Cell of interest.
	 * @param grid: Simulation grid.
	 */
	private void handleFishCell(Cell cell, Grid grid) {
		if (!fishHasAlreadyBeenEaten(cell)) {
			Cell[][] neighborhood = grid.getNeighborhood(cell.getCurRow(), cell.getCurCol(), NUM_NEIGHBORS);
			Cell nextLocation = cellToMoveTo(neighborhood, WATER);

			if (nextLocation != null) {
				switchCells(cell, nextLocation);
				checkForReproduction(cell);
			}
		}
	}

	private boolean fishHasAlreadyBeenEaten(Cell fish) {
		return (fish.getNextState() == SHARK);
	}
	/**
	 * Try to eat a fish, otherwise try to move the shark.
	 * @param cell: shark Cell of interest.
	 * @param grid: Simulation grid.
	 */
	private void handleSharkCell(Cell cell, Grid grid) {
		Cell[][] neighborhood = grid.getNeighborhood(cell.getCurRow(), cell.getCurCol(), NUM_NEIGHBORS);
		
		Cell fishToEat = cellToMoveTo(neighborhood, FISH);
		if (fishToEat != null) {
			eatFish(fishToEat, cell, grid);
			checkForReproduction(cell);
			mySharkEnergy++;
		} else {
			if (noMoreEnergy(cell)) {
				cell.setNextState(WATER);
				addCellToBeUpdated(cell);
			} else {
				Cell nextLocation = cellToMoveTo(neighborhood, WATER);
				if (nextLocation != null) {
					switchCells(cell, nextLocation);
					checkForReproduction(cell);
				}
			}
			mySharkEnergy--;
		}
		
		System.out.println("handled shark cell:");
		System.out.println("energy left: " + mySharkEnergy);
	}
	
	/**
	 * Check if a shark has any more energy.
	 * @param cell: shark Cell of interest.
	 * @return true if shark has no more energy; false otherwise.
	 */
	private boolean noMoreEnergy(Cell cell) {
		return mySharkEnergy == 0;
	}

	/**
	 * Have shark eat the fish and move to its location.
	 * @param fishToEat: fish Cell to be eaten.
	 * @param curShark: shark Cell eating the fish.
	 * @param grid: Simulation grid.
	 */
	private void eatFish(Cell fishToEat, Cell curShark, Grid grid) {
		if (fishAlreadyMoved(curShark, fishToEat)) {
			undoFishMove(fishToEat, grid);
		}
		
		System.out.println(fishToEat.getCurRow());
		System.out.println(fishToEat.getCurCol())
		;
		fishToEat.setNextState(SHARK);
		curShark.setNextState(WATER);
		addCellToBeUpdated(fishToEat);
		addCellToBeUpdated(curShark);
	}
	
	/**
	 * Undos a fish move if it's already moved but a shark who moves later wants to eat it.
	 * @param fishToEat: fish Cell that's to be eaten.
	 * @param grid: Simulation grid.
	 */
	private void undoFishMove(Cell fishToEat, Grid grid) {
		Cell fishNextLocation = grid.getCell(fishToEat.getNextRow(), fishToEat.getNextCol());
		fishNextLocation.setNextState(null);
		removeCellToBeUpdated(fishNextLocation);
	}

	/**
	 * Checks if a fish Cell has moved before a shark Cell.
	 * @param shark: current shark Cell.
	 * @param fish: fish Cell to be eaten.
	 * @return
	 */
	private boolean fishAlreadyMoved(Cell shark, Cell fish) {
		int fishRow = fish.getCurRow();
		int fishCol = fish.getCurCol();
		int sharkRow = shark.getCurRow();
		int sharkCol = shark.getCurCol();
		
		return ((fishRow < sharkRow) || (fishRow == sharkRow && fishCol < sharkCol)) && (fish.getNextRow() != -1 && fish.getNextCol() != -1);
	}
	
	/**
	 * Check if Cell can reproduce based on state.
	 * @param cell: Cell to check for reproduction.
	 */
	private void checkForReproduction(Cell cell) {
		switch (cell.getCurState()) {
			case FISH:
				if (fishCanReproduce()) {
					cell.setNextState(FISH);
				}
				break;
			case SHARK:
				if (sharkCanReproduce()) {
					cell.setNextState(SHARK);
				}
		}
	}
	
	/**
	 * Checks if the fish has lived for enough rounds to reproduce.
	 * @return true if the fish can reproduce; false otherwise.
	 */
	private boolean fishCanReproduce() {
		return (myFishReproductionTime == 0);
	}
	
	/**
	 * Checks if the shark has lived for enough rounds to reproduce.
	 * @return true if the shark can reproduce; false otherwise.
	 */
	private boolean sharkCanReproduce() {
		return (mySharkReproductionTime == 0);
	}

	/**
	 * Generates a random integer between 0 and (max-1) for indexing into a list.
	 * @param max: size of list you're indexing into.
	 * @return an integer for the random index.
	 */
	private int generateRandom(int max) {
		return (int) Math.round(Math.random() * (max-1));
	}
	
	/**
	 * Gets the Cell that the current Cell will move to.
	 * @param neighborhood: 3x3 array of Cells with the Cell of interest in the center and its neighbors surrounding it.
	 * @param stateToMoveTo: state of the Cells that can be taken over.
	 * @return Cell that current Cell wants to move to.
	 */
	private Cell cellToMoveTo(Cell[][] neighborhood, String stateToMoveTo) {
		List<Cell> optionList = new ArrayList<Cell>();

		checkIfCanMoveTo(neighborhood[0][1], stateToMoveTo, optionList);
		checkIfCanMoveTo(neighborhood[1][0], stateToMoveTo, optionList);
		checkIfCanMoveTo(neighborhood[1][2], stateToMoveTo, optionList);
		checkIfCanMoveTo(neighborhood[2][1], stateToMoveTo, optionList);
		
		if (optionList.size() > 0) {
			return optionList.get(generateRandom(optionList.size()));
		} else {
			return null;
		}
	}
	
	public Color getFill(String s){
		switch(s){
		case FISH:
			return FISHCOLOR;
		case SHARK:
			return SHARKCOLOR;
		case WATER:
			return WATERCOLOR;
		default:
			return ERRORCOLOR;
		}
	}
	
	public String toString(){
		return "Predator Prey";
	}

	/**
	 * Adds a Cell to a list of options for the current Cell to move to if it is in fact a candidate for its next location.
	 * @param cellToCheck: Cell that you want to move to.
	 * @param stateToMoveTo: state that can be moved to.
	 * @param optionList: list of options for Cells that you can move to.
	 */
	private void checkIfCanMoveTo(Cell cellToCheck, String stateToMoveTo, List<Cell> optionList) {
		if (canMoveTo(cellToCheck, stateToMoveTo)) {
			optionList.add(cellToCheck);
		}
	}
	
	/**
	 * Checks if the current Cell can move to a specific Cell's location.
	 * @param cellToCheck: Cell to potentially be taken over.
	 * @param stateToMoveTo: state that can be moved to.
	 * @return true if that Cell can be taken over; false otherwise.
	 */
	private boolean canMoveTo(Cell cellToCheck, String stateToMoveTo) {
		if (cellToCheck == null || cellToCheck.getNextState() == FISH) {
			return false;
		} else {
			return cellToCheck.getCurState().equals(stateToMoveTo);
		}
	}
}
