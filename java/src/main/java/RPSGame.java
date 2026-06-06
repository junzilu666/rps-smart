import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RPSGame {
    private static final String DATA_FILE = "game_data.json";
    private static final Random RANDOM = new Random();
    private static final Gson GSON;

    // 自定义 LocalDateTime 的序列化/反序列化，避免模块系统限制
    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        });
        builder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        });
        GSON = builder.create();
    }

    private GameData gameData;
    private boolean smartMode;
    private Scanner scanner;

    // 清屏
    private void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public RPSGame() {
        gameData = loadData();
        smartMode = false;
        scanner = new Scanner(System.in);
    }

    public void run() {
        while (true) {
            clearConsole();
            System.out.println("═══════════════════════════════════════════");
            System.out.println("  石头剪刀布 · 智能版    ");
            System.out.println("═══════════════════════════════════════════");
            System.out.println(" 段位: " + padRight(gameData.player.getRankTitle(), 8) +
                               " 经验: " + padRight(String.valueOf(gameData.player.experience), 4) +
                               " 金币: " + gameData.player.gold);
            System.out.println(" 智能模式: " + (smartMode ? "🧠 开启" : "🎲 关闭"));
            System.out.println("═══════════════════════════════════════════");
            System.out.println(" 1. 开始新大局");
            System.out.println(" 2. 查看历史记录");
            System.out.println(" 3. 切换智能模式");
            System.out.println(" 4. 退出游戏");
            System.out.println("═══════════════════════════════════════════");
            System.out.print("请输入选项: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": startMatch(); break;
                case "2": showHistory(); break;
                case "3": smartMode = !smartMode;
                          System.out.println("智能模式已" + (smartMode ? "开启" : "关闭") + "，按回车继续...");
                          scanner.nextLine(); break;
                case "4": System.out.println("再见！"); return;
                default:  System.out.println("无效选项，按回车继续..."); scanner.nextLine();
            }
        }
    }

    private void startMatch() {
        clearConsole();
        System.out.print("请输入本大局的小局数（例如 3 表示三局两胜）: ");
        int totalRounds;
        try {
            totalRounds = Integer.parseInt(scanner.nextLine().trim());
            if (totalRounds < 1) {
                System.out.println("局数必须大于0，按回车返回..."); scanner.nextLine(); return;
            }
        } catch (NumberFormatException e) {
            System.out.println("输入错误，按回车返回..."); scanner.nextLine(); return;
        }

        int playerWins = 0, computerWins = 0, draws = 0;
        int winThreshold = totalRounds / 2 + 1;

        while (playerWins < winThreshold && computerWins < winThreshold && (playerWins + computerWins + draws) < totalRounds) {
            clearConsole();
            printMatchHeader(playerWins, computerWins, draws, totalRounds);

            Move playerMove = null;
            while (playerMove == null) {
                System.out.print("请出拳 (1-石头 2-剪刀 3-布, 输入 0 可退出大局): ");
                String input = scanner.nextLine().trim();
                switch (input) {
                    case "0": System.out.println("已退出当前大局，按回车继续..."); scanner.nextLine(); return;
                    case "1": playerMove = Move.ROCK; break;
                    case "2": playerMove = Move.SCISSORS; break;
                    case "3": playerMove = Move.PAPER; break;
                    default: System.out.println("输入错误，请重新选择。");
                }
            }

            Move computerMove;
            String modeHint;
            if (smartMode) { computerMove = smartMove(); modeHint = "🧠 智能分析中..."; }
            else           { computerMove = randomMove(); modeHint = "🎲 随机出拳"; }

            Result result = judge(playerMove, computerMove);
            Round round = new Round(playerMove, computerMove, result);
            gameData.rounds.add(round);

            if (result == Result.WIN) playerWins++;
            else if (result == Result.LOSE) computerWins++;
            else draws++;

            saveData();
            clearConsole();
            printMatchHeader(playerWins, computerWins, draws, totalRounds);
            System.out.println(modeHint);
            System.out.println("你: " + playerMove.emoji + " " + playerMove.chinese +
                               "  vs  电脑: " + computerMove.emoji + " " + computerMove.chinese);
            System.out.println("结果: " + result.description);
            System.out.println("----------------------------------");
            System.out.print("按回车继续下一小局...");
            scanner.nextLine();
        }

        // 大局结算
        clearConsole();
        printMatchHeader(playerWins, computerWins, draws, totalRounds);
        System.out.println("════════ 大局结束 ════════");
        if (playerWins > computerWins) {
            System.out.println("🎉 恭喜你赢得了这个大局！");
            int exp = 30 + RANDOM.nextInt(21);
            int gold = 50 + RANDOM.nextInt(51);
            gameData.player.experience += exp;
            gameData.player.gold += gold;
            System.out.println("获得经验 +" + exp + "，金币 +" + gold);
        } else if (computerWins > playerWins) {
            System.out.println("😞 很遗憾，你输掉了这个大局。");
            int exp = 10 + RANDOM.nextInt(11);
            int gold = 10 + RANDOM.nextInt(21);
            gameData.player.experience += exp;
            gameData.player.gold += gold;
            System.out.println("获得安慰经验 +" + exp + "，金币 +" + gold);
        } else {
            System.out.println("🤝 大局平局！");
            int exp = 20 + RANDOM.nextInt(11);
            int gold = 30 + RANDOM.nextInt(21);
            gameData.player.experience += exp;
            gameData.player.gold += gold;
            System.out.println("获得经验 +" + exp + "，金币 +" + gold);
        }

        gameData.player.checkLevelUp();
        saveData();
        System.out.println("当前段位: " + gameData.player.getRankTitle() + " (Lv." + gameData.player.level + ")");
        System.out.print("按回车返回主菜单...");
        scanner.nextLine();
    }

    private void printMatchHeader(int pw, int cw, int d, int total) {
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println("段位: " + gameData.player.getRankTitle() +
                           " 经验: " + gameData.player.experience +
                           " 金币: " + gameData.player.gold);
        System.out.println("本大局 " + total + " 局制 | 比分: 你 " + pw + " : " + cw + " 电脑 | 平局 " + d);
        System.out.println("══════════════════════════════════════════════════════════");
    }

    private Move smartMove() {
        List<Round> hist = gameData.rounds;
        if (hist.isEmpty()) return randomMove();
        if (hist.size() >= 3) {
            List<Move> last3 = new ArrayList<>();
            for (int i = hist.size() - 3; i < hist.size(); i++) last3.add(hist.get(i).playerMove);
            Map<Move, Integer> counter = new HashMap<>();
            for (Move m : Move.values()) counter.put(m, 0);
            for (int i = 0; i < hist.size() - 3; i++) {
                if (hist.get(i).playerMove == last3.get(0) &&
                    hist.get(i+1).playerMove == last3.get(1) &&
                    hist.get(i+2).playerMove == last3.get(2)) {
                    Move next = hist.get(i+3).playerMove;
                    counter.put(next, counter.get(next) + 1);
                }
            }
            Move pred = null;
            int max = 0;
            for (Map.Entry<Move, Integer> e : counter.entrySet()) {
                if (e.getValue() > max) { max = e.getValue(); pred = e.getKey(); }
            }
            if (pred != null && max > 0) return getWinningMoveAgainst(pred);
        }
        return predictByFrequency();
    }

    private Move predictByFrequency() {
        Map<Move, Integer> freq = new HashMap<>();
        for (Move m : Move.values()) freq.put(m, 0);
        for (Round r : gameData.rounds) freq.put(r.playerMove, freq.get(r.playerMove) + 1);
        Move most = Move.ROCK;
        int max = 0;
        for (Map.Entry<Move, Integer> e : freq.entrySet()) {
            if (e.getValue() > max) { max = e.getValue(); most = e.getKey(); }
        }
        return getWinningMoveAgainst(most);
    }

    private Move getWinningMoveAgainst(Move m) {
        switch (m) {
            case ROCK: return Move.PAPER;
            case SCISSORS: return Move.ROCK;
            case PAPER: return Move.SCISSORS;
            default: return randomMove();
        }
    }

    private Move randomMove() {
        return Move.values()[RANDOM.nextInt(3)];
    }

    private Result judge(Move p, Move c) {
        if (p == c) return Result.DRAW;
        if ((p == Move.ROCK && c == Move.SCISSORS) ||
            (p == Move.SCISSORS && c == Move.PAPER) ||
            (p == Move.PAPER && c == Move.ROCK)) return Result.WIN;
        return Result.LOSE;
    }

    private void showHistory() {
        clearConsole();
        List<Round> rounds = gameData.rounds;
        if (rounds.isEmpty()) {
            System.out.println("暂无对战记录。");
        } else {
            System.out.println("════════ 全部小局记录（共 " + rounds.size() + " 局）════════");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            for (int i = 0; i < rounds.size(); i++) {
                Round r = rounds.get(i);
                System.out.printf("%3d. [%s] 你:%s%s  电脑:%s%s  %s%n",
                        i + 1, r.time.format(fmt),
                        r.playerMove.emoji, r.playerMove.chinese,
                        r.computerMove.emoji, r.computerMove.chinese,
                        r.result.description);
            }
        }
        System.out.print("\n按回车返回主菜单...");
        scanner.nextLine();
    }

    private GameData loadData() {
        try {
            if (Files.exists(Path.of(DATA_FILE))) {
                String content = Files.readString(Path.of(DATA_FILE));
                GameData data = GSON.fromJson(content, GameData.class);
                if (data != null) {
                    if (data.player == null) data.player = new Player();
                    if (data.rounds == null) data.rounds = new ArrayList<>();
                    return data;
                }
            }
        } catch (IOException e) {
            System.out.println("读取数据失败，将创建新存档。");
        }
        GameData fresh = new GameData();
        fresh.player = new Player();
        fresh.rounds = new ArrayList<>();
        return fresh;
    }

    private void saveData() {
        try (Writer writer = Files.newBufferedWriter(Path.of(DATA_FILE))) {
            GSON.toJson(gameData, writer);
        } catch (IOException e) {
            System.err.println("保存数据失败: " + e.getMessage());
        }
    }

    // 数据模型
    enum Move {
        ROCK("石头", "💎"), SCISSORS("剪刀", "✂️"), PAPER("布", "🧻");
        final String chinese, emoji;
        Move(String c, String e) { chinese = c; emoji = e; }
    }

    enum Result {
        WIN("你赢了！"), LOSE("你输了！"), DRAW("平局！");
        final String description;
        Result(String d) { description = d; }
    }

    static class Player {
        int level = 1, experience = 0, gold = 0;

        String getRankTitle() {
            if (level >= 10) return "👑 传说";
            if (level >= 7) return "💎 钻石";
            if (level >= 5) return "🥇 黄金";
            if (level >= 3) return "🥈 白银";
            return "🥉 青铜";
        }

        int expForNextLevel() { return 100 * level; }

        void checkLevelUp() {
            while (experience >= expForNextLevel()) {
                experience -= expForNextLevel();
                level++;
                System.out.println("🌟 升级了！当前段位: " + getRankTitle() + " (Lv." + level + ")");
            }
        }
    }

    static class Round {
        Move playerMove, computerMove;
        Result result;
        LocalDateTime time;

        public Round(Move pm, Move cm, Result r) {
            playerMove = pm; computerMove = cm; result = r;
            time = LocalDateTime.now();
        }
    }

    static class GameData {
        Player player;
        List<Round> rounds;
    }

    private String padRight(String s, int len) {
        if (s.length() >= len) return s;
        return s + " ".repeat(len - s.length());
    }

    public static void main(String[] args) {
        new RPSGame().run();
    }
}