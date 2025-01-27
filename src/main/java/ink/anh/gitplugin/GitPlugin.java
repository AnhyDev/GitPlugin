package ink.anh.gitplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GitPlugin extends JavaPlugin {

    private static GitPlugin instance;
    private GlobalManager manager;

    public static GitPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        manager = GlobalManager.getManager(instance);

        this.getCommand("gitpull").setExecutor(this);
        this.getCommand("gitpush").setExecutor(this);
        this.getCommand("gitlogs").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("Цю команду можна виконувати лише з консолі.");
            return true;
        }

        try {
            if (command.getName().equalsIgnoreCase("gitpull")) {
                File pluginsDirectory = getDataFolder().getParentFile();
                executeGitCommand(sender, pluginsDirectory, "pull");
                sender.sendMessage("Git pull завершено.");
                return true;
            }

            if (command.getName().equalsIgnoreCase("gitpush")) {
                File pluginsDirectory = getDataFolder().getParentFile();
                executeGitPush(sender, pluginsDirectory);
                return true;
            }

            if (command.getName().equalsIgnoreCase("gitlogs")) {
                File logsDirectory = getDataFolder().getParentFile().getParentFile().toPath().resolve("logs").toFile();

                if (!logsDirectory.exists() || !logsDirectory.isDirectory()) {
                    sender.sendMessage("Папка logs не знайдена або не є директорією.");
                    return true;
                }

                executeGitCommand(sender, logsDirectory, "pull");
                sender.sendMessage("Git pull для папки logs завершено.");
                return true;
            }

        } catch (Exception e) {
            sender.sendMessage("Сталася помилка при виконанні команди: " + e.getMessage());
            e.printStackTrace();
            return true;
        }

        return false;
    }

    private void executeGitCommand(CommandSender sender, File directory, String... commands) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(directory);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sender.sendMessage(line);
            }
        }

        process.waitFor();
    }

    private void executeGitPush(CommandSender sender, File directory) throws Exception {
        // Додаємо всі зміни
        executeGitCommand(sender, directory, "git", "add", ".");

        // Отримуємо поточну дату і час для коміту
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        executeGitCommand(sender, directory, "git", "commit", "-m", "Auto commit: " + timeStamp);

        // Пушимо зміни на віддалений репозиторій
        executeGitCommand(sender, directory, "git", "push");
        sender.sendMessage("Git push завершено.");
    }

    public GlobalManager getManager() {
        return manager;
    }
}
