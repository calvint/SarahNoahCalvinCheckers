package c_minimax;

class CheckersSearchResult implements InterfaceSearchResult {

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