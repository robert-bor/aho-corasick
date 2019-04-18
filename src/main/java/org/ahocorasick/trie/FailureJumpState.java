package org.ahocorasick.trie;

/**
 * The failure-jump state for failure-jump pattern.
 * <p>
 * When failure-jump config open it will jump a few of continues characters
 * while current not math.
 *
 * @author wangzhao
 */
public class FailureJumpState {
    private State state;

    /**
     * the current failure times
     * <p>
     * <li>-1: INIT state
     * <li> 0: READY to failure-jump
     * <li> N: PLUS the failure-jump times
     * <li>+N: TOTAL the current failure-jump times
     */
    private int failureTimes;
    /**
     * the max failure-jump times
     */
    private final int maxFailureTimes;
    private int total;

    private FailureJumpState(int maxFailureTimes) {
        if (maxFailureTimes > 0) {
            this.maxFailureTimes = maxFailureTimes;
        } else {
            throw new IllegalArgumentException("the maxFailureTimes max large than 0: " + maxFailureTimes);
        }
    }

    public static FailureJumpState createInstance(State state, int maxFailureTimes) {
        FailureJumpState failureJumpState = new FailureJumpState(maxFailureTimes);
        failureJumpState.reset(state);

        return failureJumpState;
    }

    /**
     * reset to INIT state
     *
     * @param state
     */
    public void reset(State state) {
        this.state = state;
        this.failureTimes = -1;
        this.total = 0;
    }

    /**
     * get next failure-jump state
     *
     * <li>INIT(-1): the start state when a failure-jump state created or reset.
     * <li>READY(0): INIT will goto READY while current char is matched. READY
     * state will remains until current not match.
     * <li>PLUS(N): READY goto PLUS when current not match. It will be END when
     * current failure-jump times larger than {@link #maxFailureTimes}
     * <li>TOTAL(+N): when current match, the PLUS has done and sum current failureTimes.
     * Then turn on READY for next failure-jump.
     *
     * @param nextState
     */
    public void nextState(State nextState) {
        if (this.failureTimes == -1 && nextState.getDepth() > 0) {
            // init --> ready
            this.failureTimes = 0;
        } else if (this.failureTimes >= 0 && nextState.getDepth() == 0) {
            // ready --> plus
            if (this.failureTimes < this.maxFailureTimes) {
                // remains the previous state before the failure-jump end or
                // out of the max times.
                this.failureTimes++;
            } else {
                // reset the current state when current failure-jump
                // times large than maxFailureTimes
                this.reset(state);
            }
            return;
        } else if (nextState.getDepth() > 0) {
            // plus --> total
            this.total += this.failureTimes;
            this.failureTimes = 0;
        }
        this.state = nextState;
    }

    public int getTotal() {
        return this.total;
    }

    public State getState() {
        return state;
    }
}
