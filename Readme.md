# 💰 The Ledger — Smart Expense Tracker

**The Ledger** is a personal Android app project developed by **Kavy Chaturvedi**, designed to make expense tracking simple, secure, and intelligent.  
It combines **Firebase Authentication**, **Cloud Firestore**, and **Firebase Remote Config** to deliver a dynamic and safe financial tracking experience — all wrapped in a modern **Material Design** interface.

---

## 🧭 Overview

The Ledger helps users record and manage daily expenses effortlessly.  
It ensures **data security** with Firebase Auth and **real-time synchronization** using Firestore, while **Remote Config** enables adaptive features and experiments — paving the way for AI-powered financial insights.

---

## 🚀 Core Features

### 🔐 Secure Authentication
- Firebase **Email/Password login**
- **Biometric unlock** (Fingerprint / Face ID)
- Smart error handling and intuitive animations

### 💸 Expense Management
- Add, edit, and delete expense entries  
- Categorize transactions (Food, Bills, Travel, etc.)  
- Real-time data updates powered by **Cloud Firestore**

### 🧠 Intelligent AI Features *(Powered by Firebase Remote Config)*
- Remote feature control and dynamic app behavior  
- AI-based personalization and configuration fetched securely from Firebase  
- Allows controlled feature rollouts (e.g., beta features, recommendations)

### 📊 Expense Analysis *(Coming Soon 🚧)*
- Visual analytics and monthly spending summaries  
- Smart budget insights and data-driven suggestions  

### 🌙 User Interface
- Clean **Material UI** using AppCompat, CardView & ConstraintLayout  
- Smooth animations and intuitive navigation  
- Light/Dark theme ready  

---

## 🧩 Tech Stack

| Category | Technology |
|-----------|-------------|
| Language | Java / Kotlin |
| Backend | Firebase (Auth, Cloud Firestore, Remote Config) |
| UI | XML layouts + Material Components |
| Libraries | Glide, BiometricPrompt, AndroidX Components |
| IDE | Android Studio (Arctic Fox or above) |

---

---

## 📸 Screenshots

Here’s a quick look at **The Ledger** app interface 👇  

<div align="center">

### 🧭 App Overview
<table>
<tr>
<td align="center"><b>Welcome / Main Screen</b></td>
<td align="center"><b>Choose Login / Signup</b></td>
<td align="center"><b>Login Screen</b></td>
</tr>
<tr>
<td><img src="app/src/main/assets/Screenshots/main.jpg" alt="Main Screen" width="250"/></td>
<td><img src="app/src/main/assets/Screenshots/Choose.jpg" alt="Choose Screen" width="250"/></td>
<td><img src="app/src/main/assets/Screenshots/Login.jpg" alt="Login Screen" width="250"/></td>
</tr>
</table>

### 🔐 Authentication
<table>
<tr>
<td align="center"><b>Signup Screen</b></td>
<td align="center"><b>Profile Screen</b></td>
<td align="center"><b>Privacy Policy</b></td>
</tr>
<tr>
<td><img src="app/src/main/assets/Screenshots/Signup.jpg" alt="Signup Screen" width="250"/></td>
<td><img src="app/src/main/assets/Screenshots/PRofile.png" alt="Profile Screen" width="250"/></td>
<td><img src="app/src/main/assets/Screenshots/Privacy.png" alt="Privacy Policy" width="250"/></td>
</tr>
</table>

### 💸 Expense Tracking
<table>
<tr>
<td align="center"><b>Dashboard</b></td>
<td align="center"><b>Transactions</b></td>
<td align="center"><b>About Screen</b></td>
</tr>
<tr>
<td><img src="app/src/main/assets/Screenshots/Dash.png" alt="Dashboard" width="250"/></td>
<td><img src="app/src/main/assets/Screenshots/Txn.jpg" alt="Transactions" width="250"/></td>
<td><img src="app/src/main/assets/Screenshots/about.png" alt="About Screen" width="250"/></td>
</tr>
</table>

</div>
---


## 🛠️ Project Setup & Installation

### 1️⃣ Clone the repository
```bash
git clone https://github.com/<your-username>/TheLedger.git
cd TheLedger
