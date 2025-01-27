package ink.anh.gitplugin;

import org.bukkit.Bukkit;
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
        if (command.getName().equalsIgnoreCase("gitpull")) {
            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage("Цю команду можна виконувати лише з консолі.");
                return true;
            }

            try {
                File pluginsDirectory = getDataFolder().getParentFile();

                ProcessBuilder processBuilder = new ProcessBuilder("git", "pull");
                processBuilder.directory(pluginsDirectory);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    sender.sendMessage(line);
                }

                process.waitFor();
                sender.sendMessage("Git pull завершено.");
            } catch (Exception e) {
                sender.sendMessage("Сталася помилка при виконанні команди: " + e.getMessage());
                e.printStackTrace();
            }

            return true;
        }

        if (command.getName().equalsIgnoreCase("gitpush")) {
            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage("Цю команду можна виконувати лише з консолі.");
                return true;
            }

            File pluginsDirectory = getDataFolder().getParentFile();
            performGitOperations(sender, pluginsDirectory);
            return true;
        }

        if (command.getName().equalsIgnoreCase("gitlogs")) {
            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage("Цю команду можна виконувати лише з консолі.");
                return true;
            }

            File serverRoot = Bukkit.getWorldContainer();
            File logsDirectory = new File(serverRoot, "logs");

            if (!logsDirectory.exists() || !logsDirectory.isDirectory()) {
                sender.sendMessage("Папка logs не знайдена або не є директорією.");
                return true;
            }

            performGitOperations(sender, logsDirectory);
            return true;
        }

        return false;
    }

    private void performGitOperations(CommandSender sender, File directory) {
        try {
            sender.sendMessage("Виконуємо git add у директорії: " + directory.getAbsolutePath());
            // Додаємо всі зміни
            ProcessBuilder addBuilder = new ProcessBuilder("git", "add", "-A");
            addBuilder.directory(directory);
            Process addProcess = addBuilder.start();
            addProcess.waitFor();

            sender.sendMessage("Виконуємо git commit у директорії: " + directory.getAbsolutePath());
            // Отримуємо поточну дату і час для повідомлення коміта
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            ProcessBuilder commitBuilder = new ProcessBuilder("git", "commit", "-m", "Auto commit: " + timeStamp);
            commitBuilder.directory(directory);
            Process commitProcess = commitBuilder.start();

            // Зчитуємо результат коміта
            BufferedReader commitReader = new BufferedReader(new InputStreamReader(commitProcess.getInputStream()));
            String commitLine;
            while ((commitLine = commitReader.readLine()) != null) {
                sender.sendMessage(commitLine);
            }

            commitProcess.waitFor();

            sender.sendMessage("Виконуємо git push у директорії: " + directory.getAbsolutePath());
            // Пушимо зміни на віддалений репозиторій
            ProcessBuilder pushBuilder = new ProcessBuilder("git", "push");
            pushBuilder.directory(directory);
            Process pushProcess = pushBuilder.start();

            // Зчитуємо результат пушу
            BufferedReader pushReader = new BufferedReader(new InputStreamReader(pushProcess.getInputStream()));
            String pushLine;
            while ((pushLine = pushReader.readLine()) != null) {
                sender.sendMessage(pushLine);
            }

            pushProcess.waitFor();
            sender.sendMessage("Git операції завершено для директорії: " + directory.getAbsolutePath());
        } catch (Exception e) {
            sender.sendMessage("Сталася помилка при виконанні git операцій: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public GlobalManager getManager() {
        return manager;
    }
}
