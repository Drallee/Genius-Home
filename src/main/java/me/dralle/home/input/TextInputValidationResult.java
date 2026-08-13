package me.dralle.home.input;

public class TextInputValidationResult {
    private final boolean valid;
    private final String value;
    private final String errorMessage;

    private TextInputValidationResult(boolean valid, String value, String errorMessage) {
        this.valid = valid;
        this.value = value;
        this.errorMessage = errorMessage;
    }

    public static TextInputValidationResult valid(String value) {
        return new TextInputValidationResult(true, value, null);
    }

    public static TextInputValidationResult invalid(String errorMessage) {
        return new TextInputValidationResult(false, null, errorMessage);
    }

    public boolean isValid() {
        return valid;
    }

    public String getValue() {
        return value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
