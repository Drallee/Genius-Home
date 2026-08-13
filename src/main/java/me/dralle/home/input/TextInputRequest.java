package me.dralle.home.input;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class TextInputRequest {
    private final Player player;
    private final String title;
    private final String initialText;
    private final String itemMaterial;
    private final BiFunction<Player, String, CompletableFuture<TextInputValidationResult>> validator;
    private final Consumer<String> onConfirm;
    private final Runnable onCancel;

    private TextInputRequest(Builder builder) {
        this.player = Objects.requireNonNull(builder.player, "player");
        this.title = Objects.requireNonNullElse(builder.title, "Enter text");
        this.initialText = Objects.requireNonNullElse(builder.initialText, "");
        this.itemMaterial = Objects.requireNonNullElse(builder.itemMaterial, "NAME_TAG");
        this.validator = Objects.requireNonNull(builder.validator, "validator");
        this.onConfirm = Objects.requireNonNull(builder.onConfirm, "onConfirm");
        this.onCancel = builder.onCancel;
    }

    public Player getPlayer() {
        return player;
    }

    public String getTitle() {
        return title;
    }

    public String getInitialText() {
        return initialText;
    }

    public String getItemMaterial() {
        return itemMaterial;
    }

    public BiFunction<Player, String, CompletableFuture<TextInputValidationResult>> getValidator() {
        return validator;
    }

    public Consumer<String> getOnConfirm() {
        return onConfirm;
    }

    public Runnable getOnCancel() {
        return onCancel;
    }

    public static Builder builder(Player player) {
        return new Builder(player);
    }

    public static class Builder {
        private final Player player;
        private String title;
        private String initialText;
        private String itemMaterial;
        private BiFunction<Player, String, CompletableFuture<TextInputValidationResult>> validator;
        private Consumer<String> onConfirm;
        private Runnable onCancel;

        private Builder(Player player) {
            this.player = player;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder initialText(String initialText) {
            this.initialText = initialText;
            return this;
        }

        public Builder itemMaterial(String itemMaterial) {
            this.itemMaterial = itemMaterial;
            return this;
        }

        public Builder validator(BiFunction<Player, String, CompletableFuture<TextInputValidationResult>> validator) {
            this.validator = validator;
            return this;
        }

        public Builder onConfirm(Consumer<String> onConfirm) {
            this.onConfirm = onConfirm;
            return this;
        }

        public Builder onCancel(Runnable onCancel) {
            this.onCancel = onCancel;
            return this;
        }

        public TextInputRequest build() {
            return new TextInputRequest(this);
        }
    }
}
