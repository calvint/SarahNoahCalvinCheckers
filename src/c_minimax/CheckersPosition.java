package c_minimax;

//author: Gary Kalmanovich; rights reserved

public class CheckersPosition implements InterfacePosition {

    // This implementation is designed for at most 8 columns by 8 rows
    // It packs the entire position into a single long
    
    // Rightmost 51=cap(log2(3^32)) bits are used to store a 32 digit trinary (0..2) number
    // Leftmost 1 bit stores the player 1 or 2

    private long position = 0;
    private int nC = 0;
    private int nR = 0;
    static private long[] powerOfThree = new long[32];

    CheckersPosition( int nC, int nR) {
        position = 0;
        this.nC = nC;
        this.nR = nR;
        if (powerOfThree[0] != 1) setPowerOfThree();
    }

    CheckersPosition( InterfacePosition pos ) {
        position = pos.getRawPosition();
        nC       = pos.nC();
        nR       = pos.nR();
        if (powerOfThree[0] != 1) setPowerOfThree();
    }
    
    private void setPowerOfThree() { 
        powerOfThree[0] = 1;
        for( int iPow = 1 ; iPow < 32 ; iPow++ ) {
            powerOfThree[iPow] = 3*powerOfThree[iPow-1];
        }
    }

    @Override public int nC() { return nC; }
    @Override public int nR() { return nR; }

    @Override public long getRawPosition() { return position; }

    @Override
    public int getColor( InterfaceIterator iPos ) { // 0 if transparent, 1 if white, 2 if black
        return getColor( iPos.iC(), iPos.iR() );
    }

    public int getColor( int iC, int iR ) { // 0 if transparent, 1 if white, 2 if black
        int retVal = (int) ( ( position / powerOfThree[(8*iR+iC)/2] ) % 3 );
        return (retVal < 0) ? retVal+3 : retVal;
    }

    @Override
    public void setColor( InterfaceIterator iPos, int color ) { // color is 1 if red, 2 if yellow
        setColor( iPos.dC(), iPos.dR(), color );
        setColor(iPos.iC(), iPos.iR(), 0);
        // if move is a jump
        if (Math.abs(iPos.dC()-iPos.iR()) > 1) {
        	//remove the opponents piece in between
        	int columnBetween = iPos.iC() + ((iPos.dC()-iPos.iC())/2);
    		int rowBetween = iPos.iR() + ((iPos.dR()-iPos.iR())/2);
    		setColor(columnBetween, rowBetween, 0);
        }
        
    }

    private void setColor( int iC, int iR, int color ) { // 0 if transparent, 1 if white, 2 if black
        int oldColor = getColor(iC,iR);
        position += (color-oldColor) * powerOfThree[(8*iR+iC)/2];
    }

    @Override
    public void setPlayer(int iPlayer) { // Only 1 or 2 are valid
        if ( !(0<iPlayer && iPlayer<3) ) {
            System.err.println("Error(Connect4Position::setPlayer): iPlayer ("+iPlayer+") out of bounds!!!");
        } else {
            int  currentPlayer = getPlayer();
            if ( currentPlayer != iPlayer ) {
                position ^= 1L << 63;
            }
        }
    }

    @Override
    public int getPlayer() {
        return ((int)(position>>>63))+1;
    }

    @Override
    public int isWinner(InterfaceIterator iPos) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int isWinner() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float valuePosition() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        position = 0;
    }

    @Override
    public int getChipCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getChipCount(InterfaceIterator iPos) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public boolean jumpPossible() {
    	// loop over possible moves
    	for ( InterfaceIterator iPos = new CheckersIterator(nC,nR); iPos.isInBounds(); iPos.increment() ) {
    		if (possibleMove(iPos)) {
    			//if move is a jump
    			if (Math.abs(iPos.dC()-iPos.iR()) > 1) {
    				return true;
    			}
    		}		
    	}
		return false;
	}
    
    private boolean possibleMove(InterfaceIterator iPos) {
    	//check if the destination is on the board and return false if it is
    	if (iPos.dC() >= 8 || iPos.dR() >= 8) {
    		return false;
    	}
    	//check if there is a piece at the initial square and that the piece is owned by the current player
    	if (getColor(iPos) == 0 || getColor(iPos) != getPlayer() ) {
    		return false;
    	}
    	//check if the destination is already filled and return false if it is
    	if (getColor(iPos.dC(), iPos.dR()) != 0) {
    		return false;
    	}
    	//check to see if iPos represents a jump
    	if (Math.abs(iPos.dC()-iPos.iR()) > 1) {
        	//if it is a jump
        	//check to see if there is an opponent's piece to jump in between initial and destination squares
    		int columnBetween = iPos.iC() + ((iPos.dC()-iPos.iC())/2);
    		int rowBetween = iPos.iR() + ((iPos.dR()-iPos.iR())/2);
    		int opponent = 3-getPlayer();
    		if (getColor(columnBetween, rowBetween) == opponent) {
    			return true;
    		} else {
    			return false;
    		}
    	} else {
	    	//if the move is not a jump
    		return true;
    	}
    }
    
    public boolean validMove( InterfaceIterator iPos, boolean jumpPossible) {
    	if (possibleMove(iPos)) {
    		//check to see if iPos represents a jump
        	if (Math.abs(iPos.dC()-iPos.iR()) > 1) {
            	//if it is a jump
            	return true;
        	} else {
    	    	//if the move is not a jump
        		if (jumpPossible) {
        			return false;
        		} else {
        			return true;
        		}
        	}
    	} else {
    		return false;
    	}
	}
    
    //test depricated due to changes in set color
//    public static void testMe(String[] args) {// Unit test (incomplete)
//        CheckersPosition position = new CheckersPosition(8,8);
//        
//        for (int iR = 0; iR < 8; iR++) {
//            for (int iC = (iR+1)%2; iC < 8; iC+=2) {
//                System.out.println("(" + iC + ", " + iR + ") = " + position.getColor(iC, iR));
//            }
//        }
//        System.out.println("-------------------------");
//
//        position.setColor(0, 0, 1);
//        position.setColor(1, 0, 0);
//        position.setColor(3, 0, 1);
//        position.setColor(4, 1, 1);
//        for (int iR = 0; iR < 8; iR++) {
//            for (int iC = (iR+1)%2; iC < 8; iC+=2) {
//                System.out.println("(" + iC + ", " + iR + ") = " + position.getColor(iC, iR));
//            }
//        }
//        System.out.println("-------------------------");
//
//        position.setColor(1, 0, 2);
//        position.setColor(3, 0, 2);
//        position.setColor(5, 0, 2);
//        position.setColor(7, 0, 2);
//        position.setColor(0, 1, 2);
//        position.setColor(2, 1, 2);
//        position.setColor(4, 1, 2);
//        position.setColor(6, 1, 2);
//        position.setColor(1, 2, 2);
//        position.setColor(3, 2, 2);
//        position.setColor(5, 2, 2);
//        position.setColor(7, 2, 2);
//        position.setColor(0, 5, 1);
//        position.setColor(2, 5, 1);
//        position.setColor(4, 5, 1);
//        position.setColor(6, 5, 1);
//        position.setColor(1, 6, 1);
//        position.setColor(3, 6, 1);
//        position.setColor(5, 6, 1);
//        position.setColor(7, 6, 1);
//        position.setColor(0, 7, 1);
//        position.setColor(2, 7, 1);
//        position.setColor(4, 7, 1);
//        position.setColor(6, 7, 1);
//        for (int iR = 0; iR < 8; iR++) {
//            for (int iC = (iR+1)%2; iC < 8; iC+=2) {
//                System.out.println("(" + iC + ", " + iR + ") = " + position.getColor(iC, iR));
//            }
//        }
//    }
}
