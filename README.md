# Python DocGen AI

Automatically generate Python docstrings using AI, powered by Groq (Llama 3.3 70B).

![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
![Platform](https://img.shields.io/badge/platform-PyCharm%20%7C%20IntelliJ-orange.svg)
![AI](https://img.shields.io/badge/AI-Groq%20%7C%20Llama%203.3%2070B-purple.svg)

---

## Features

- **Single function** - place the cursor inside any function and generate its docstring instantly
- **Entire file** - generate docstrings for all functions in the current file at once
- **3 formats supported** - Google Style, NumPy, reStructuredText
- **Multilingual** - generate docstrings in English or Italian
- **Smart insertion** - automatically detects and replaces existing docstrings
- **Free AI** - uses Groq API (free tier, no credit card required)

---

## Installation

1. Open PyCharm or IntelliJ IDEA
2. Go to `Settings → Plugins → Install Plugin from Disk`
3. Select the `.zip` build file
4. Restart the IDE

---

## Getting a Free Groq API Key

1. Go to [console.groq.com](https://console.groq.com) and sign up
2. Navigate to **API Keys** → **Create API Key**
3. Copy the key
4. In PyCharm: `Settings → Tools → Python DocGen AI` and paste the key

---

## Configuration

| Option | Values | Default |
|---|---|---|
| Docstring Format | Google Style, NumPy, reStructuredText | Google Style |
| Language | English, Italian | Italian |
| Include Examples | On / Off | Off |
| Include Raises | On / Off | On |

---

## Usage

| Action | How |
|---|---|
| Generate for current function | Right-click → **Generate Docstring** |
| Generate for entire file | Right-click → **Generate All Docstrings** |

---

## Tech Stack

- **Language:** Kotlin
- **Platform:** IntelliJ Platform SDK
- **AI Model:** Llama 3.3 70B via Groq API
- **HTTP Client:** OkHttp
- **Serialization:** Gson

---

## License

Distributed under the **Apache License 2.0**. See `LICENSE` for more information.

---

## Author

**Jaiired-gh** — [github.com/Jaiired-gh](https://github.com/Jaiired-gh)
