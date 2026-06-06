# rps-smart
一个包含Java命令行版和Web版的智能石头剪刀布游戏（段位/经验/金币/AI预测）An intelligent Rock-Paper-Scissors game featuring both Java CLI and web versions (Rank/Experience/Gold/AI Prediction)
# 🪨✂️🧻 Rock Paper Scissors · Smart Edition

> A rock-paper-scissors game with rank system, experience, gold, and AI prediction.  
> 一个带段位、经验和智能预测的石头剪刀布游戏。

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

---

## ✨ Features | 功能特色

- 🧠 **Smart Mode / 智能模式** — analyzes your full history to predict and counter your next move  
  分析你的全部历史记录，预测并克制你的下一手
- 🏅 **Rank System / 段位系统** — Bronze → Silver → Gold → Diamond → Legend  
  青铜 → 白银 → 黄金 → 钻石 → 传说
- 📊 **Experience & Gold / 经验与金币** — earn rewards after each match, level up over time  
  每大局结束后结算，经验满值即可升级
- 📋 **Custom Round Count / 自定义局数** — BO3 / BO5 / BO7 …  
  三局两胜、五局三胜，由你决定
- 💾 **JSON Save / JSON 存档** — data stored in `game_data.json`, fully compatible between both versions  
  游戏数据统一保存为 `game_data.json`，两个版本格式完全互通
- 🎮 **Two Versions / 两种游玩方式** — Java CLI for terminal lovers & Web single‑page for browser  
  Java 命令行版（极客风） + 单文件网页版（简约白蓝 UI）

---

## 📂 Project Structure | 项目结构

| Folder | Description | How to Run |
|--------|-------------|-------------|
| [`java/`](java/) | Java CLI version | Compile & run in terminal |
| [`web/`](web/) | Single‑file HTML version | Open in browser or deploy |

---

## 🚀 Quick Start | 快速开始

### 1. Java Version | Java 版
```bash
cd java
# download gson-2.10.1.jar first
javac -encoding UTF-8 -cp gson-2.10.1.jar RPSGame.java
java -cp ".;gson-2.10.1.jar" RPSGame
