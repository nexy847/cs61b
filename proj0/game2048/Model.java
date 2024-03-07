package game2048;

import java.util.Formatter;
import java.util.Objects;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: YOUR NAME HERE
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.

        if(side==Side.NORTH){
        int [][] num=new int[board.size()][board.size()];
        for (int i = 0; i < num.length; i++) {
            for (int j = 0; j < num[i].length; j++) {
                num[i][j] = 0;
            }
        }
        for(int c=0;c<board.size();c+=1){
            for(int r=0;r< board.size();r+=1){
                Tile t=board.tile(c,r);
                if(t!=null){
                    if(side==Side.NORTH){
                        int count=0;
                        for(int i=t.row()+1;i<4;i++){
                            Tile tN=board.tile(t.col(),i);
                            if(tN!=null&&tN.value()==t.value()){
                                count++;
                            }
                        }
                        if(count==0){
                            //move到运动方向上碰到的第一个节点的后一格
                            for(int i=t.row()+1;i<4;i++){
                                Tile ft=board.tile(t.col(),i);
                                if(ft!=null){
                                    boolean merge=board.move(t.col(),i-1,t);
                                    if(merge){
                                        score+=4;
                                        num[t.col()][i-1]=1;
                                    }
                                    changed=true;
                                    break;
                                }else{
                                    changed=false;
                                }
                            }
                            if(changed==false){
                                    boolean merge = board.move(t.col(), 3, t);
                                    if (merge) {
                                        score += 4;
                                        num[t.col()][3] = 1;
                                    }
                                    changed=true;
                            }
                        }else if(count%2==0&&count!=0){
                            for(int i=t.row()+1;i<4;i++){
                                Tile ft=board.tile(t.col(),i);
                                if(ft!=null) {
                                    if(num[t.col()][t.row()]==0){
                                    if (ft.value() == t.value()) {
                                        int col = ft.col();
                                        int row = ft.row();
                                        for (int j = row + 1; j < 4; j++) {
                                            Tile fT = board.tile(col, j);
                                            if (fT != null) {
                                                if (ft.value() == fT.value()) {
                                                    if(num[ft.col()][ft.row()]==0) {
                                                        boolean merge = board.move(col, fT.row(), ft);
                                                        if (merge) {
                                                            score += 4;
                                                            num[ft.col()][fT.row()] = 1;
                                                        }
                                                        changed = true;
                                                    }
                                                } else {
                                                    if(num[ft.col()][ft.row()]==0) {
                                                        boolean merge = board.move(col, fT.row(), ft);
                                                        changed=true;
                                                        if (merge) {
                                                            score += 4;
                                                            num[ft.col()][fT.row()] = 1;
                                                        }
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        boolean merge = board.move(col, row, t);
                                        if (merge) {
                                            score += 4;
                                            num[col][row] = 1;
                                        }
                                        changed = true;}
                                    }else {
                                        if (num[t.col()][t.row()] == 0) {
                                            boolean merge = board.move(ft.col(), ft.row() - 1, t);
                                            if (merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                            changed = true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }else if(count%2!=0){
                            for(int i=t.row()+1;i<4;i++){
                                Tile ft=board.tile(t.col(),i);
                                if(ft!=null){
                                    if(ft.value()==t.value()){
                                        if(num[t.col()][t.row()]==0) {
                                            boolean merge = board.move(ft.col(), ft.row(), t);
                                            changed=true;
                                            if (merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                        }
                                    }else {
                                        if(num[t.col()][t.row()]==0) {
                                            boolean merge = board.move(ft.col(), ft.row() - 1, t);
                                            changed=true;
                                            if(merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        if(r==3){
                            for(int i= 3;i>=0;i--){
                                Tile tb=board.tile(c,i);
                                if(tb!=null){
                                    for(int j=tb.row()+1;j<4;j++){
                                        Tile tf=board.tile(c,j);
                                        if(tf!=null){
                                            boolean merge=board.move(c,tf.row()-1,tb);
                                            changed=true;
                                            if(merge){
                                                score+=4;
                                                num[c][tf.row()]=1;
                                            }
                                        }else{
                                            board.move(c,j,tb);
                                            changed=true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }}

        if(side==Side.SOUTH){
            int [][] num=new int[board.size()][board.size()];
            for (int i = 0; i < num.length; i++) {
                for (int j = 0; j < num[i].length; j++) {
                    num[i][j] = 0;
                }
            }
            for(int c= 3;c>=0;c--){
                for(int r=3;r>=0;r--){
                    Tile t=board.tile(c,r);
                    if(t!=null){
                        int count=0;
                        for(int i=t.row()-1;i>=0;i--){
                            Tile tN=board.tile(t.col(),i);
                            if(tN!=null&&tN.value()==t.value()){
                                count++;
                            }
                        }

                        if(count==0){
                            for(int i=t.row()-1;i>=0;i--){
                                Tile ft=board.tile(t.col(),i);
                                if(ft!=null){
                                    board.move(t.col(),i+1,t);
                                    changed=true;
                                    break;
                                }else changed=false;
                            }
                            if(changed==false){
                                board.move(t.col(),0,t);
                                changed=true;
                            }
                        }else if(count%2==0&&count!=0){
                            for(int i=t.row()-1;i>=0;i--){
                                Tile ft=board.tile(t.col(),i);
                                if(ft!=null) {
                                    if(num[t.col()][t.row()]==0){
                                        if (ft.value() == t.value()) {
                                            int col = ft.col();
                                            int row = ft.row();
                                            for (int j = row - 1; j >= 0; j--) {
                                                Tile fT = board.tile(col, j);
                                                if (fT != null) {
                                                    if (ft.value() == fT.value()) {
                                                        if(num[ft.col()][ft.row()]==0) {
                                                            boolean merge = board.move(col, fT.row(), ft);
                                                            if (merge) {
                                                                score += 4;
                                                                num[ft.col()][fT.row()] = 1;
                                                            }
                                                            changed = true;
                                                        }
                                                    } else {
                                                        if(num[ft.col()][ft.row()]==0) {
                                                            boolean merge = board.move(col, fT.row(), ft);
                                                            changed=true;
                                                            if (merge) {
                                                                score += 4;
                                                                num[ft.col()][fT.row()] = 1;
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                            boolean merge = board.move(col, row, t);
                                            if (merge) {
                                                score += 4;
                                                num[col][row] = 1;
                                            }
                                            changed = true;}
                                    }else {
                                        if (num[t.col()][t.row()] == 0) {
                                            boolean merge = board.move(ft.col(), ft.row() + 1, t);
                                            if (merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                            changed = true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }else if(count%2!=0){
                            for(int i=t.row()-1;i>=0;i--){
                                Tile ft=board.tile(t.col(),i);
                                if(ft!=null){
                                    if(ft.value()==t.value()){
                                        if(num[t.col()][t.row()]==0) {
                                            boolean merge = board.move(ft.col(), ft.row(), t);
                                            changed=true;
                                            if (merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                        }
                                    }else {
                                        if(num[t.col()][t.row()]==0) {
                                            boolean merge = board.move(ft.col(), ft.row() + 1, t);
                                            changed=true;
                                            if(merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        if(r==0){
                            for(int i= 0;i<4;i++){
                                Tile tb=board.tile(c,i);
                                if(tb!=null){
                                    for(int j=tb.row()-1;j>=0;j--){
                                        Tile tf=board.tile(c,j);
                                        if(tf!=null){
                                            boolean merge=board.move(c,tf.row()+1,tb);
                                            changed=true;
                                            if(merge){
                                                score+=4;
                                                num[c][tf.row()]=1;
                                            }
                                        }else{
                                            board.move(c,j,tb);
                                            changed=true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(side==Side.EAST){
            int [][] num=new int[board.size()][board.size()];
            for (int i = 0; i < num.length; i++) {
                for (int j = 0; j < num[i].length; j++) {
                    num[i][j] = 0;
                }
            }

            for(int r=0;r< board.size();r++){
                for(int c=0;c< board.size();c++){
                    Tile t=board.tile(c,r);
                    if(t!=null){
                        int count=0;
                        for(int i=t.col()+1;i<4;i++){
                            Tile tN=board.tile(i,t.row());
                            if(tN!=null&&tN.value()==t.value()){
                                count++;
                            }
                        }

                        if(count==0){
                            for(int i=t.col()+1;i<4;i++){
                                Tile ft=board.tile(i,t.row());
                                if(ft!=null){
                                    board.move(i-1,t.row(),t);
                                    changed=true;
                                    break;
                                }else changed=false;
                            }
                            if(changed==false){
                                board.move(3,t.row(),t);
                                changed=true;
                            }
                        }else if(count%2==0&&count!=0){
                            for(int i=t.col()+1;i<4;i++){
                                Tile ft=board.tile(i,t.row());
                                if(ft!=null) {
                                    if(num[t.col()][t.row()]==0){
                                        if (ft.value() == t.value()) {
                                            int col = ft.col();
                                            int row = ft.row();
                                            for (int j = col + 1; j < 4; j++) {
                                                Tile fT = board.tile(j, row);
                                                if (fT != null) {
                                                    if (ft.value() == fT.value()) {
                                                        if(num[ft.col()][ft.row()]==0) {
                                                            boolean merge = board.move(fT.col(), row, ft);
                                                            if (merge) {
                                                                score += 4;
                                                                num[fT.col()][ft.row()] = 1;
                                                            }
                                                            changed = true;
                                                        }
                                                    } else {
                                                        if(num[ft.col()][ft.row()]==0) {
                                                            boolean merge = board.move(fT.col(), ft.row(), ft);
                                                            changed=true;
                                                            if (merge) {
                                                                score += 4;
                                                                num[fT.col()][ft.row()] = 1;
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                            boolean merge = board.move(col, row, t);
                                            if (merge) {
                                                score += 4;
                                                num[col][row] = 1;
                                            }
                                            changed = true;}
                                    }else {
                                        if (num[t.col()][t.row()] == 0) {
                                            boolean merge = board.move(ft.col()-1, ft.row(), t);
                                            if (merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                            changed = true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }else if(count%2!=0){
                            for(int i=t.col()+1;i<4;i++){
                                Tile ft=board.tile(i,t.row());
                                if(ft!=null){
                                    if(ft.value()==t.value()){
                                        if(num[t.col()][t.row()]==0) {
                                            boolean merge = board.move(ft.col(), ft.row(), t);
                                            changed=true;
                                            if (merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                        }
                                    }else {
                                        if(num[t.col()][t.row()]==0) {
                                            boolean merge = board.move(ft.col()-1, ft.row(), t);
                                            changed=true;
                                            if(merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        if(c==3){
                            for(int i=3;i>=0;i--){
                                Tile tb=board.tile(i,r);
                                if(tb!=null){
                                    for(int j=tb.col()+1;j<4;j++){
                                        Tile tf=board.tile(j,r);
                                        if(tf!=null){
                                            boolean merge=board.move(tf.col()-1,r,tb);
                                            changed=true;
                                            if(merge){
                                                score+=4;
                                                num[tf.col()][r]=1;
                                            }
                                        }else{
                                            board.move(j,r,tb);
                                            changed=true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(side==Side.WEST){
            int [][] num=new int[board.size()][board.size()];
            for (int i = 0; i < num.length; i++) {
                for (int j = 0; j < num[i].length; j++) {
                    num[i][j] = 0;
                }
            }

            for(int r=3;r>=0;r--){
                for(int c=3;c>=0;c--){
                    Tile t=board.tile(c,r);
                    if(t!=null){
                        int count=0;
                        for(int i=t.col()-1;i>=0;i--){
                            Tile tN=board.tile(i,t.row());
                            if(tN!=null&&tN.value()==t.value()){
                                count++;
                            }
                        }

                        if(count==0){
                            for(int i=t.col()-1;i>=0;i--){
                                Tile ft=board.tile(i,t.row());
                                if(ft!=null){
                                    board.move(i+1,t.row(),t);
                                    changed=true;
                                    break;
                                }else changed=false;
                            }
                            if(changed==false){
                                board.move(0,t.row(),t);
                                changed=true;
                            }
                        }else if(count%2==0&&count!=0){
                            for(int i=t.col()-1;i>=0;i--){
                                Tile ft=board.tile(i,t.row());
                                if(ft!=null) {
                                    if(num[t.col()][t.row()]==0){
                                        if (ft.value() == t.value()) {
                                            int col = ft.col();
                                            int row = ft.row();
                                            for (int j = col - 1; j >= 0; j--) {
                                                Tile fT = board.tile(j, row);
                                                if (fT != null) {
                                                    if (ft.value() == fT.value()) {
                                                        if(num[ft.col()][ft.row()]==0) {
                                                            boolean merge = board.move(fT.col(), row, ft);
                                                            if (merge) {
                                                                score += 4;
                                                                num[fT.col()][ft.row()] = 1;
                                                            }
                                                            changed = true;
                                                        }
                                                    } else {
                                                        if(num[ft.col()][ft.row()]==0) {
                                                            boolean merge = board.move(fT.col(), ft.row(), ft);
                                                            changed=true;
                                                            if (merge) {
                                                                score += 4;
                                                                num[fT.col()][ft.row()] = 1;
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                            boolean merge = board.move(col, row, t);
                                            if (merge) {
                                                score += 4;
                                                num[col][row] = 1;
                                            }
                                            changed = true;}
                                    }else {
                                        if (num[t.col()][t.row()] == 0) {
                                            boolean merge = board.move(ft.col()+1, ft.row(), t);
                                            if (merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                            changed = true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }else if(count%2!=0){
                            for(int i=t.col()-1;i>=0;i--){
                                Tile ft=board.tile(i,t.row());
                                if(ft!=null){
                                    if(ft.value()==t.value()){
                                        if(num[t.col()][t.row()]==0) {
                                            boolean merge = board.move(ft.col(), ft.row(), t);
                                            changed=true;
                                            if (merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                        }
                                    }else {
                                        if(num[t.col()][t.row()]==0) {
                                            boolean merge = board.move(ft.col()+1, ft.row(), t);
                                            changed=true;
                                            if(merge) {
                                                score += 4;
                                                num[ft.col()][ft.row()] = 1;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        if(c==0){
                            for(int i=0;i<4;i++){
                                Tile tb=board.tile(i,r);
                                if(tb!=null){
                                    for(int j=tb.col()-1;j>=0;j--){
                                        Tile tf=board.tile(j,r);
                                        if(tf!=null){
                                            boolean merge=board.move(tf.col()+1,r,tb);
                                            changed=true;
                                            if(merge){
                                                score+=4;
                                                num[tf.col()][r]=1;
                                            }
                                        }else{
                                            board.move(j,r,tb);
                                            changed=true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.
        for(int row = 0;row < b.size();row += 1){
            for(int col = 0;col < b.size();col+=1){
                if(b.tile(col,row)!=null){
                    continue;
                }else{
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        for(int row = 0;row < b.size();row += 1){
            for(int col = 0;col < b.size();col += 1){
                if(!Objects.isNull(b.tile(col,row)) && b.tile(col,row).value()==MAX_PIECE){
                    return true;
                }else {
                    continue;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        for(int col = 0;col < b.size();col += 1){
            for(int row = 0;row < b.size();row += 1){
                if(b.tile(col,row)==null){
                    return true;
                }else if(row==0){
                    if(col==0){
                        if((!Objects.isNull(b.tile(col+1,row))&&(b.tile(col+1,row).value()==b.tile(col,row).value()))
                            ||(!Objects.isNull(b.tile(col,row+1))&&(b.tile(col,row+1).value()==b.tile(col,row).value()))){
                            return true;
                        }
                    }else if(col==3){
                        if((!Objects.isNull(b.tile(col-1,row))&&(b.tile(col-1,row).value()==b.tile(col,row).value()))
                            ||(!Objects.isNull(b.tile(col,row+1))&&(b.tile(col,row+1).value()==b.tile(col,row).value()))){
                            return true;
                        }
                    }else{
                        if((!Objects.isNull(b.tile(col-1,row))&&(b.tile(col-1,row).value()==b.tile(col,row).value()))
                            ||(!Objects.isNull(b.tile(col+1,row))&&(b.tile(col+1,row).value()==b.tile(col,row).value()))
                            ||(!Objects.isNull(b.tile(col,row+1))&&(b.tile(col,row+1).value()==b.tile(col,row).value()))){
                            return true;
                        }
                    }
                }else if(row==3){
                    if(col==0){
                        if((!Objects.isNull(b.tile(col+1,row))&&(b.tile(col+1,row).value()==b.tile(col,row).value()))
                            ||(!Objects.isNull(b.tile(col,row-1))&&(b.tile(col,row-1).value()==b.tile(col,row).value()))){
                            return true;
                        }
                    }else if(col==3){
                        if((!Objects.isNull(b.tile(col-1,row))&&(b.tile(col-1,row).value()==b.tile(col,row).value()))
                            ||(!Objects.isNull(b.tile(col,row-1))&&(b.tile(col,row-1).value()==b.tile(col,row).value()))){
                            return true;
                        }
                    }
                }else if(col==0){
                    if(row==1||row==2){
                        if((!Objects.isNull(b.tile(col,row+1))&&(b.tile(col,row+1).value()==b.tile(col,row).value()))
                                ||(!Objects.isNull(b.tile(col,row-1))&&(b.tile(col,row-1).value()==b.tile(col,row).value()))
                                ||(!Objects.isNull(b.tile(col+1,row))&&(b.tile(col+1,row).value()==b.tile(col,row).value()))){
                            return true;
                        }
                    }
                }else if(col==3){
                    if(row==1||row==2){
                        if(!Objects.isNull(b.tile(col-1,row))&&(b.tile(col-1,row).value()==b.tile(col,row).value())
                            ||(!Objects.isNull(b.tile(col,row-1))&&(b.tile(col,row+1).value()==b.tile(col,row).value()))
                            ||(!Objects.isNull(b.tile(col,row+1))&&(b.tile(col,row+1).value()==b.tile(col,row).value()))){
                            return true;
                        }
                    }
                }else{
                    if((!Objects.isNull(b.tile(col-1,row))&&(b.tile(col-1,row).value()==b.tile(col,row).value()))
                        ||(!Objects.isNull(b.tile(col+1,row))&&(b.tile(col+1,row).value()==b.tile(col,row).value()))
                        ||(!Objects.isNull(b.tile(col,row-1))&&(b.tile(col,row-1).value()==b.tile(col,row).value()))
                        ||(!Objects.isNull(b.tile(col,row+1))&&(b.tile(col,row+1).value()==b.tile(col,row).value()))){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
