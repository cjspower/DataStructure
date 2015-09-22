/**
 * Created by anderson on 9/20/15.
 * Implement Double Array Trie.
 * Support only character a to z, no bound check, just for test.
 * Doesn't allow delete of word
 * Say something I am giving up on you ...
 */
import java.util.*;

public class DoubleArrayTrie {
    private static final int LENGTH = 1024; //initial length of base and check array
    private static final int alphabet[] = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26};
    //alphabet table, replace it if more characters need to be supported
    private static final int EMPTY = -1;
    private static final int LEAF = -3;
    //private static final int BOTH = -2;
    public static HashMap<Integer,List<Integer>> map = new HashMap<Integer,List<Integer>>();
    public static HashSet<Integer> tail = new HashSet<Integer>();
    //this map contains the successors of each state, used while collision occurs.

    int base[] = new int[LENGTH];
    int check[] = new int[LENGTH];

    public DoubleArrayTrie() {
        base[0] = 1;
        Arrays.fill(check, EMPTY);
        check[0] = LEAF;  //-3 indicates the root
    }

    //Double array size if the states exceeds array index.
    private void arrayExtend() {
        int length = check.length;
        base = Arrays.copyOf(base, base.length * 2);
        check = Arrays.copyOf(check, length * 2);
        Arrays.fill(check,length,length*2-1,EMPTY);
    }

    private void reSizeArray(int index) {
        int length = check.length;
        if(index >= length) arrayExtend();
    }

    //relocate a base value
    private void relocate(int state, int newBase) {
        //for each successor state of current state
        for (int p: map.get(state)) {
            int c = alphabet[p];
            if(check[base[state] + c] == state) {
                check[newBase + c] = state;
                base[newBase + c] = base[base[state] + c];
                //copy the content of old successor state to new state num
                if(map.containsKey(base[state]+c)) {
                    for (int i: map.get(base[state]+c)) {
                        if(check[base[base[state]+c]+alphabet[i]] == base[state]+c) {
                            check[base[base[state]+c]+alphabet[i]] = newBase + c;
                        }
                    }
                }
                //little bit tricky here
                /*for (int ch: alphabet) {
                    if(check[base[base[state]+c]+ch] == base[state]+c) {
                        check[base[base[state]+c]+ch] = newBase + c;
                    }

                }*/
                //suppose the state is s, for those check(i) = base(i) + c
                // should be update to check(i) = newBase + c
                check[base[state]+c] = EMPTY;
            }
        }
        base[state] = newBase;
    }

    private void addSuccessor(int currentState, int c) {
        if(map.containsKey(currentState)) {
            map.get(currentState).add(c);
        }else {
            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(c);
            map.put(currentState, list);
        }
    }

    public void addWord(String word) {
        int currentState = 0;
        //current state num
        int i = 0;
        //input string index
        int c;
        //current character
        int nextState;
        while (i < word.length()) {

            c = word.charAt(i) - 'a';
            if(base[currentState] <= 0) base[currentState] = 1;
            nextState = base[currentState] + alphabet[c];
            reSizeArray(nextState);
            //make sure that nextState num doesn't exceed the max array index, if exceeded,
            //Call arrayExtend to the current arrays.

            if(check[nextState] == EMPTY) {
                //good news, that state is not occupied just create a new state
                addSuccessor(currentState, c);
                check[nextState] = currentState;
                if(i == word.length() - 1) {
                    base[nextState] = LEAF;
                    tail.add(nextState);
                } else {
                    base[nextState] = 1;
                }

            }else if(check[nextState] != currentState) {
                //worst case, target state num is occupied, a relocate is needed for current state base.
                //compare the cost of relocation
                int newBase;
                int collisionState = check[nextState];
                addSuccessor(currentState, c);
                /*
                if(map.get(currentState).size() < map.get(check[nextState]).size()) {
                    //relocate base for current state
                    newBase = findNextBase(currentState);
                    relocate(currentState, newBase);
                }else {
                    //relocate base for the collision state
                    newBase = findNextBase(collisionState);
                    relocate(collisionState, newBase);
                }   //can't resolve some bug, removed ...
                */

                newBase = findNextBase(currentState);
                relocate(currentState, newBase);

                //do necessary work to add state after resolve the collision problem
                nextState = base[currentState] + alphabet[c];
                reSizeArray(nextState);
                check[nextState] = currentState;
                if(i == word.length() - 1) {
                    base[nextState] = LEAF;
                    tail.add(nextState);
                } else {
                    base[nextState] = 1;
                }

            }else {
                //this prefix existed in state space, go ahead
                //assert map.containsKey(currentState);
                //assert map.get(currentState).contains(c);//for test
                if(i == word.length() - 1) {
                    tail.add(nextState);
                }
            }
            currentState = nextState;
            i += 1;
        }
        /*
        try {
            assert containsWord(word);
        }catch (AssertionError e) {
            for (int q = 0; q < 50; q++) {
                System.out.print(base[q]+" ");
            }
            System.out.println();
            for (int q = 0; q < 50; q++) {
                System.out.print(check[q]+" ");
            }
            System.out.println();
        }
        */
    }

    //find next base which could avoid collision with all successor state to given state.
    //brute force approach, may cause a slow build, won't affect search speed.
    private int findNextBase(int stateNum) {
        int tempBase = 1;
        while (true) {
            boolean collision = false;
            for (int c: map.get(stateNum)) {
                if(check[tempBase + alphabet[c]] != EMPTY){
                    collision = true;
                    break;
                }
            }
            if(!collision) {
                return tempBase;
            }
            tempBase += 1;
        }
    }

    public void addWord(Collection<String> words) {
        for (String word: words) {
            addWord(word);
        }
    }

    //check if a certain word is in the tire
    public boolean containsWord(String word) {
        int currentState = 0;
        for (int i = 0; i < word.length(); i++) {
            int c = word.charAt(i) - 'a';
            int nextState = base[currentState] + alphabet[c];
            if(base[currentState] <= 0 ||check[nextState] != currentState) {
                return false;
            }
            if(i == word.length() - 1) {
                if(base[nextState] == LEAF || tail.contains(nextState)) {
                    return true;
                }
            }
            currentState = nextState;
        }
        return false;
    }


}
