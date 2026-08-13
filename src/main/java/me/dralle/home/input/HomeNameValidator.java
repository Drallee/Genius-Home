package me.dralle.home.input;

import me.dralle.home.HomePlugin;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.UUID;

import static me.dralle.home.database.HomeDBManager.checkHomeExistInDB;
import static me.dralle.home.utils.Utils.getConfigMessage;
import static me.dralle.home.utils.Utils.getConfigNumber;
import static me.dralle.home.utils.Utils.rep;

public class HomeNameValidator {
    public enum Mode {
        CREATE,
        RENAME
    }

    public TextInputValidationResult validate(OfflinePlayer target, String input, Mode mode, String currentName) {
        String trimmed = input == null ? "" : input.trim();
        String messagePrefix = mode == Mode.CREATE ? "text-input.home.create" : "text-input.home.rename";

        if (trimmed.isEmpty()) {
            return TextInputValidationResult.invalid(getConfigMessage(messagePrefix + ".empty"));
        }

        int minLength = Math.max(1, getConfigNumber("settings.homes.names.min-length"));
        int maxLength = Math.max(minLength, getConfigNumber("settings.homes.names.max-length"));

        if (trimmed.length() < minLength) {
            return TextInputValidationResult.invalid(rep(getConfigMessage(messagePrefix + ".too-short"), "%min%", minLength));
        }

        if (trimmed.length() > maxLength) {
            return TextInputValidationResult.invalid(rep(getConfigMessage(messagePrefix + ".too-long"), "%max%", maxLength));
        }

        String allowedPattern = HomePlugin.getHomeConfig().getString("settings.homes.names.allowed-pattern", "^[A-Za-z0-9_-]+$");
        if (!trimmed.matches(allowedPattern)) {
            return TextInputValidationResult.invalid(getConfigMessage(messagePrefix + ".invalid-characters"));
        }

        if (mode == Mode.RENAME && currentName != null && trimmed.equalsIgnoreCase(currentName)) {
            return TextInputValidationResult.invalid(getConfigMessage(messagePrefix + ".same-name"));
        }

        UUID targetUuid = target.getUniqueId();
        try {
            if (checkHomeExistInDB(targetUuid, trimmed)) {
                return TextInputValidationResult.invalid(getConfigMessage(messagePrefix + ".already-exists"));
            }
        } catch (SQLException ignored) {
            return TextInputValidationResult.invalid(getConfigMessage(messagePrefix + ".invalid"));
        }

        return TextInputValidationResult.valid(trimmed);
    }
}
