## 🛠️ Toolbox (Java 8)

A centralised collection of Java 8 utilities and helper classes designed for seamless integration across various development environments. This repository focuses on reducing boilerplate and facilitating common logic.

### 📋 Overview
The **Toolbox** provides a set of lightweight, reusable components. Built with **Java 8** compatibility in mind, these utilities ensure broad compatibility while leveraging functional programming patterns to keep codebases clean and maintainable.

### 📁 Project Structure (util)
```text
util/
├── OperatingSystem.java  # Platform constants & version metadata
├── PeekingIterator.java  # LL(1) Look-ahead utility
├── RunCommand.java       # Native process execution wrapper
├── SystemInfo.java       # Centralised hardware & OS metadata
├── SmartDateParser.java  # Date capture and parsing for standard Australian times
├── BuildInfo.java        # Runtime build metadata retrieval
└── Separator.java        # Stateful delimiter management for StringBuilders
```

### ✨ Key Features
* **Run Command:** A streamlined utility for executing external system commands (such as `nslookup`, `ipconfig`, or `tracert`) and capturing output streams for easy processing.
* **Project Build Info:** Automatically identifies the active JAR library or class resource at runtime to retrieve compilation timestamps and build metadata.
* **Smart Date Parser:** A robust tool for converting date strings of varying formats into `java.util.Date` and `java.time.ZonedDateTime` objects.
    * *Optimised for **Australian** (DD/MM/YYYY) patterns while maintaining support for standard **US** (MM/DD/YYYY) and **ISO-8601** formats.*
* **System Discovery (`SystemInfo`):** A high-performance utility that captures hardware architecture, network identity, and OS identifications during class initialisation for ease of retrieval.
* **Platform Mapping (`OperatingSystem`):** A robust Enum-based system that translates raw system properties and environment strings into strongly-typed metadata, supporting such as **Windows and Server variants**, **most Linux variants (RHEL, CentOS, Debian, Ubuntu, Fedora, SuSE, Alpine)** and **Unix flavours (AIX, Solaris, FreeBSD, HP-UX)**.
* **Look-Ahead Iterator (`PeekingIterator`):** An enhanced iterator interface that supports **LL(1)** parsing patterns. It allows developers to "peek" at the next element without advancing the cursor—essential for complex command-line argument processing.
* **Smart String Joining (`Separator`):** A stateful utility designed for StringBuilder loops. It elegantly manages delimiters, ensuring no leading or trailing characters remain without requiring manual "first-element" checks.
* **Integration Boilerplate:** Helper classes to standardise communication and data flow between disparate Java modules.

### 🚀 Getting Started

#### Prerequisites
* **Java 8 (JDK 1.8) is a minimum**

#### Installation & Build
To include these utilities in your local environment:
1. **Clone the repository:**
    ```bash
    git clone https://github.com/trevormaggs/Toolbox.git
    ```

### 🤝 Collaboration & Feedback

Maintaining a **positive collaboration** is a priority for this project. If you are using these tools and have feedback, please direct your questions to me via email: **[trevmaggs@tpg.com.au](mailto:trevmaggs@tpg.com.au)**.

## ✍️ Credits

Developed and maintained by **Trevor Maggs**.

---
