package net.stelitop.battledudestcg.discord.slashcommands.base.requirements;

public class ConditionResult {

    private boolean isSuccessful;
    private String errorMessage;

    private ConditionResult() {

    }

    public static ConditionResult success() {
        ConditionResult ret = new ConditionResult();
        ret.isSuccessful = true;
        ret.errorMessage = null;
        return ret;
    }

    public static ConditionResult fail(String errorMessage) {
        ConditionResult ret = new ConditionResult();
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
