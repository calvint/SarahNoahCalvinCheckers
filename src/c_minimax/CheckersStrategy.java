package c_minimax;

import java.lang.Math;
import java.util.Random;
import java.util.TreeMap;

//author: Gary Kalmanovich; rights reserved

public class CheckersStrategy implements InterfaceStrategy {
    Random rand = new Random(); // One can seed with a parameter variable here
    @Override
    public InterfaceSearchResult getBestMove(InterfacePosition position, InterfaceSearchContext context) {
        InterfaceSearchResult searchResult = new CheckersSearchResult(); // Return information

        int player   = position.getPlayer();
        if (player == 0) {
            System.out.println("NO");
        	player = 1;
        }
        int opponent = 3-player; // There are two players, 1 and 2.
        float uncertaintyPenalty = .01f;
        boolean jumpPossible = ((CheckersPosition)position).jumpPossible();
        
        for ( InterfaceIterator iPos = new CheckersIterator(8,8); iPos.isInBounds(); iPos.increment() ) {
            InterfacePosition posNew = new CheckersPosition( position );
            if (((CheckersPosition)posNew).validMove( iPos, jumpPossible)) { // The column is not yet full
                posNew.setColor(iPos, player);
                int isWin = posNew.isWinner( iPos ); // iPos
                float score;
                if        ( isWin ==   player ) { score =  1f;  // Win
                } else if ( isWin ==        0 ) { score =  0f;  // Draw
                } else if ( isWin == opponent ) { score = -1f;  // Loss
                } else { // Game is not over, so check further down the game
                    if ( context.getCurrentDepth()   < context.getMaxDepthSearchForThisPos() &&     // No more than max
                         context.getCurrentDepth()   < context.getMinDepthSearchForThisPos()    ) { // No more than min
                        posNew.setPlayer(opponent);
                        context.setCurrentDepth(context.getCurrentDepth()+1);
                        InterfaceSearchResult opponentResult = getBestMove(posNew,context); // Return information is in opponentContext
                        context.setCurrentDepth(context.getCurrentDepth()-1);
                        score = -opponentResult.getBestScoreSoFar();
                        // Note, for player, opponent's best move has negative worth
                        //   That is because, score = ((probability of win) - (probability of loss))
                        
                        if ( opponentResult.isResultFinal() == false ) { // if the result is not final, reverse penalty
                            searchResult.setIsResultFinal(false);
                            score -= 2*uncertaintyPenalty;
                        }
                    } else { 
                        // We cannot recurse further down the minimax search
                        score = -uncertaintyPenalty;
                        searchResult.setIsResultFinal(false);
                    }
                }

                if (searchResult.getBestScoreSoFar() <  score ) {
                    searchResult.setBestMoveSoFar(iPos, score );
                    if ( score == 1f ) break; // No need to search further if one can definitely win
                }
            }
            long timeNow = System.nanoTime();
            if ( context.getMaxSearchTimeForThisPos() - timeNow <= 0 ) {
                //System.out.println("CheckersStrategyB:getBestMove(): ran out of time: maxTime("
                //        +context.getMaxSearchTimeForThisPos()+") :time("
                //        +timeNow+"): recDepth("+context.getCurrentDepth()+")");
                break; // Need to make any move now
            }
        }
        return searchResult;
    }
    
	@Override
    public void setContext(InterfaceSearchContext strategyContext) {
        // Not used in this strategy
    }

    @Override
    public InterfaceSearchContext getContext() {
        // Not used in this strategy
        return null;
    }
}


