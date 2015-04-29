package c_minimax;

import java.util.Random;
import java.util.TreeMap;

//author: Gary Kalmanovich; rights reserved

public class Connect4Strategy implements InterfaceStrategy {
    TreeMap<Long,Integer> checkedPositions = new TreeMap<Long,Integer>(); // minor slowdown @16.7 million (try mapDB?)
    Random rand = new Random(); // One can seed with a parameter variable here

    Connect4Strategy() {
        int seed = rand.nextInt();
        System.out.println("Connect4Strategy: Seed set to: " + seed);
        rand.setSeed(seed);
    }

    @Override
    public InterfaceSearchResult getBestMove(InterfacePosition position, InterfaceSearchContext context) {
        InterfaceSearchResult searchResult = new Connect4SearchResult(); // Return information

        Integer checkedResult = checkedPositions.get(position.getRawPosition());
        if (checkedResult != null) {
            searchResult.setClassStateFromCompacted(checkedResult);
        } else { // position is not hashed, so let's see if we can process it
    
            int player   = position.getPlayer();
            int opponent = 3-player; // There are two players, 1 and 2.

            int  nRandom = rand.nextInt(position.nC());
            float uncertaintyPenalty = .01f;
            
            for ( int iC_raw = 0; iC_raw < position.nC(); iC_raw++) {
                int iC = (iC_raw+nRandom)% position.nC();
                InterfacePosition posNew = new Connect4Position( position );
                InterfaceIterator iPos   = new Connect4Iterator( position.nC(), position.nR() ); iPos.set(iC, 0);
                int iR = position.nR() - posNew.getChipCount(iPos) - 1;                          iPos.set(iC,iR); 
                if (iR >= 0) { // The column is not yet full
                    // Check if we have any legal move yet, if no, add this so we have a !=null move
                    if (searchResult.getBestMoveSoFar()==null) searchResult.setBestMoveSoFar(iPos, searchResult.getBestScoreSoFar());

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

                    if (searchResult.getBestMoveSoFar()  == null ||
                        searchResult.getBestScoreSoFar() <  score ) {
                        searchResult.setBestMoveSoFar(iPos, score );
                        if ( score == 1f ) break; // No need to search further if one can definitely win
                    }
                }
                long timeNow = System.nanoTime();
                if ( context.getMaxSearchTimeForThisPos() - timeNow <= 0 ) {
                    System.out.println("Connect4Strategy:getBestMove(): ran out of time: maxTime("
                            +context.getMaxSearchTimeForThisPos()+") :time("
                            +timeNow+"): recDepth("+context.getCurrentDepth()+")");
                    if (context.getCurrentDepth()==0) {
                        // Revert back to a lesser search
                        System.out.print("Connect4Strategy: Depth limit of "+context.getMinDepthSearchForThisPos()+" -> ");
                        context.setMinDepthSearchForThisPos(context.getMinDepthSearchForThisPos()-1);
                        System.out.println(context.getMinDepthSearchForThisPos());
                    }
                    if ( ((Connect4SearchContext)context).getOriginalPlayer() == opponent ) { // TODO: add to interface
                        searchResult.setBestMoveSoFar(searchResult.getBestMoveSoFar(), 0.95f); // Set to original opponent almost-win
                    }
                    searchResult.setIsResultFinal(false);
                    break; // Need to make any move now
                }
            }
    
            //if (searchResult.isResultFinal() && position.getChipCount()%3==1) // Hash this result
            //    checkedPositions.put(position.getRawPosition(),searchResult.getClassStateCompacted());

        }
        
        long timeLeftInNanoSeconds = context.getMaxSearchTimeForThisPos() - System.nanoTime();
        if (context.getCurrentDepth()== 0 && !searchResult.isResultFinal() && 
                timeLeftInNanoSeconds > ((Connect4SearchContext)context).getOriginalTimeLimit()*9/10 ) { // TODO: add to interface
            System.out.print("Connect4Strategy: Depth limit of "+context.getMinDepthSearchForThisPos()+" -> ");
            context.setMinDepthSearchForThisPos(context.getMinDepthSearchForThisPos()+1);
            System.out.println(context.getMinDepthSearchForThisPos());
            InterfaceSearchResult anotherResult = getBestMove(position,context);
            if (anotherResult.getBestScoreSoFar() > searchResult.getBestScoreSoFar()) {
                searchResult.setBestMoveSoFar(anotherResult.getBestMoveSoFar(), anotherResult.getBestScoreSoFar());
                searchResult.setIsResultFinal(anotherResult.isResultFinal());
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

class Connect4SearchContext implements InterfaceSearchContext {
    
    long timeLimit; // Original time limit
    long maxTime;   // Cut off all calculations by this time (System.nanoTime())
    int  minSearchDepth;
    int  maxSearchDepth;
    int    currentDepth;
    int  originalPlayer;

    @Override
    public int getCurrentDepth() {
        return currentDepth;
    }

    @Override
    public void setCurrentDepth(int depth) {
        currentDepth = depth;
    }

    @Override
    public int getMinDepthSearchForThisPos() {
        return minSearchDepth;
    }

    @Override
    public void setMinDepthSearchForThisPos(int minDepth) {
        minSearchDepth = minDepth;
    }

    @Override
    public int getMaxDepthSearchForThisPos() {
        return maxSearchDepth;
    }

    @Override
    public void setMaxDepthSearchForThisPos(int maxDepth) {
        maxSearchDepth = maxDepth;
    }

    @Override
    public long getMaxSearchTimeForThisPos() {
        // Cut off all calculations by this time (System.nanoTime())
        return maxTime;
    }

    @Override
    public void setMaxSearchTimeForThisPos(long timeLimit) {
        this.timeLimit =                        timeLimit;
        this.maxTime   = System.nanoTime()    + timeLimit;
    }

    //TODO: PUT THIS IN THE INTERFACE @Override
    public long getOriginalTimeLimit() {
        return timeLimit;
    }

    //TODO: PUT THIS IN THE INTERFACE @Override
    public int getOriginalPlayer() {
        return originalPlayer;
    }

    //TODO: PUT THIS IN THE INTERFACE @Override
    public void setOriginalPlayer(int player) {
        originalPlayer = player;
    }

}

class Connect4SearchResult implements InterfaceSearchResult {

    InterfaceIterator bestMoveSoFar  =     null;
    private short     bestScoreSoFar = -(1<<15); // (1<<14) is +1.f and -(1<<14) is -1.f
    boolean           isFinal        =     true;

    @Override
    public InterfaceIterator getBestMoveSoFar() {
        return bestMoveSoFar;
    }

    @Override
    public float getBestScoreSoFar() {
        return bestScoreSoFar/((float)(1<<14));
    }

    @Override
    public void setBestMoveSoFar(InterfaceIterator newMove, float newScore) {
        bestMoveSoFar  = new Connect4Iterator(newMove);
        bestScoreSoFar = (short)(newScore*(1<<14));
    }

    @Override
    public int getClassStateCompacted() {
        int compacted = 0;
        compacted |= bestScoreSoFar;
        compacted <<=16;
        compacted |= bestMoveSoFar.nC()*bestMoveSoFar.iR()+bestMoveSoFar.iC();
        compacted |= bestMoveSoFar.nC() <<  8;
        compacted |= bestMoveSoFar.nR() << 12;
        return compacted;
    }

    @Override
    public void setClassStateFromCompacted(int compacted) {
        bestScoreSoFar = (short)(compacted>>>16);
        compacted ^=                             bestScoreSoFar << 16;
        int nR = compacted >> 12;
        compacted ^=                                         nR << 12;
        int nC = compacted >>  8;
        compacted ^=                                         nC <<  8;
        int iR = compacted / nC;
        int iC = compacted - nC*iR;
        bestMoveSoFar  = new Connect4Iterator(nC,nR); bestMoveSoFar.set(iC,iR);
    }

    @Override
    public void setIsResultFinal(boolean isFinal) {
        this.isFinal = isFinal;
        
    }

    @Override
    public boolean isResultFinal() {
        return isFinal;
    }

    @Override
    public float getOpponentBestScoreOnPreviousMoveSoFar() {
        // Not used in this strategy
        return 0/0;
    }

    @Override
    public void setOpponentBestScoreOnPreviousMoveSoFar(float scoreToBeat) {
        // Not used in this strategy
    }

}
