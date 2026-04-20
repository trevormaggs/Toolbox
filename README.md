## 🛠️ Toolbox (Java 8)

A centralised collection of Java 8 utilities and helper classes designed for seamless integration across various development environments. This repository focuses on reducing boilerplate and facilitating common logic.

### 📋 Overview
The Toolbox provides a set of lightweight, reusable components. Built with *Java 8* compatibility in mind, these utilities ensure broad compatibility while leveraging functional programming patterns to keep codebases clean and maintainable.

### 📁 Project Structure (util)
```text
util/
├── Platform.java         # Platform constants & version metadata
├── PeekingIterator.java  # LL(1) Look-ahead utility
├── RunCommand.java       # Native process execution wrapper
├── SystemInfo.java       # Centralised hardware & OS metadata
├── SmartDateParser.java  # Date capture and parsing for standard Australian times
├── BuildInfo.java        # Runtime build metadata retrieval
├── Separator.java        # Stateful delimiter management for StringBuilders
├── ConsoleBar.java       # Real-time console progress bar renderer
└── FileChecksum.java     # Efficient cryptographic hashing for file integrity
```

### ✨ Key Features

* **File Integrity (`FileChecksum`):** A high-performance utility for generating **MD5, SHA-1, or SHA-256** hashes. It employs a buffered streaming approach (8KB chunks) to ensure that even multi-gigabyte files can be check-summed without impacting the JVM heap or causing memory exhaustion.
* **System Discovery (`SystemInfo`):** A high-performance utility that captures hardware architecture, network identity, and OS identifications during class initialisation. It specifically resolves Windows build numbers to distinguish between **Windows 10, 11, and Server variants**.
* **Native Execution (`RunCommand`):** A streamlined utility for executing external system commands and capturing output. It redirects `stderr` to `stdout` to prevent stream deadlocks and includes a built-in tokeniser to handle complex quoted arguments.
* **Platform Mapping (`Platform`):** A robust Enum-based system that translates raw system properties into strongly-typed metadata. It supports **Windows/Server**, **major Linux distributions (RHEL, CentOS, Debian, Ubuntu, Fedora, SuSE, Alpine)**, and **Unix flavours (AIX, Solaris, FreeBSD, HP-UX)**.
* **Console Progress (`ConsoleBar`):** A lightweight utility for rendering a real-time progress bar in the terminal. It utilises carriage returns to update a single line dynamically and includes a throttle mechanism to prevent redundant console I/O.
* **Smart Date Parser:** A robust tool for converting date strings into `java.util.Date` and `java.time.ZonedDateTime` objects.
    * *Optimised for **Australian** (DD/MM/YYYY) patterns while maintaining support for **US** (MM/DD/YYYY) and **ISO-8601** formats.*
* **Smart String Joining (`Separator`):** A stateful utility designed for `StringBuilder` loops. It elegantly manages delimiters by tracking its own state, ensuring no trailing characters remain without requiring manual "first-element" conditional logic.
* **Look-Ahead Iterator (`PeekingIterator`):** An enhanced iterator interface that supports **LL(1)** parsing patterns. It allows developers to "peek" at the next element without advancing the cursor—essential for complex token or argument processing.
* **Project Build Info:** Automatically identifies the active JAR library or class resource at runtime to retrieve compilation timestamps and build metadata.

### 🚀 Getting Started

#### Prerequisites
* **Java 8 (JDK 1.8) or higher.**

#### Installation & Build
To include these utilities in your local environment:
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/trevormaggs/Toolbox.git
    ```

### 🤝 Collaboration & Feedback

Maintaining a **positive collaboration** is a priority for this project. If you are using these tools and have feedback, please direct your questions to me via email: **[trevmaggs@tpg.com.au](mailto:trevmaggs@tpg.com.au)**.

## ✍️ Credits

Developed and maintained by **Trevor Maggs**.