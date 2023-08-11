package net.stelitop.battledudestcg.commons.pojos;

/**
 * Represents the result from a generic action.
 */
public class ActionResult {

    private boolean isSuccessful;
    private String errorMessage;

    private ActionResult() {

    }

    public static ActionResult success() {
        ActionResult ret = new ActionResult();
        ret.isSuccessful = true;
        ret.errorMessage = null;
        return ret;
    }

    public static ActionResult fail(String errorMessage) {
        ActionResult ret = new ActionResult();
        ret.isSuccessful = false;
        ret.errorMessage = errorMessage;
        return ret;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public boolean hasFailed() {
        return !isSuccessful;
    }

    public String errorMessage() {
        return errorMessage;
    }
}
