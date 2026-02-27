# P4OC - Pocket for OpenCode

## 1. Project Overview

**Project Name:** P4OC (Pocket for OpenCode)  
**Type:** Native Android Application  
**Core Functionality:** Remote client for OpenCode AI coding assistant - connects to OpenCode server running on a remote machine and provides chat interface, diff viewing, file browsing, and session control capabilities.

## 2. Technology Stack & Choices

### Framework & Language
- **Language:** Kotlin 1.9.x
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **UI Framework:** Jetpack Compose with Material 3

### Key Libraries/Dependencies
- **Networking:** Retrofit2 + OkHttp4 for REST API
- **SSE Client:** OkHttp EventSource for Server-Sent Events streaming
- **JSON Parsing:** Kotlinx Serialization
- **DI:** Hilt (Dagger)
- **Async:** Kotlin Coroutines + Flow
- **Navigation:** Compose Navigation
- **Storage:** DataStore Preferences for server configuration

### State Management
- ViewModel + StateFlow pattern
- Unidirectional data flow (UDF)

### Architecture Pattern
- Clean Architecture (3 layers)
  - **Presentation:** Compose UI + ViewModels
  - **Domain:** Use Cases + Repository Interfaces
  - **Data:** Repository Implementations + API Services

## 3. Feature List

### Core Features
1. **Server Connection**
   - Configure server address (IP/hostname + port)
   - Basic Auth support (username/password)
   - Connection status indicator
   - Auto-reconnect on connection loss

2. **Chat Interface**
   - Real-time message streaming via SSE
   - Send text prompts to OpenCode
   - Display AI responses with markdown rendering
   - Message history within session

3. **Diff Viewer**
   - Inline diff display (additions in green, deletions in red)
   - Tap to expand side-by-side view
   - File path and change summary

4. **File Browser**
   - Browse project files
   - Syntax highlighted code preview
   - Line numbers display

5. **Session Control**
   - Tool call approval/rejection
   - Approve/deny user confirmations
   - Session management (continue, interrupt)

### Secondary Features
6. **Settings**
   - Server configuration
   - Theme selection (light/dark/system)
   - Connection timeout settings

7. **Connection Screen**
   - Onboarding for first-time users
   - Saved server profiles

## 4. UI/UX Design Direction

### Overall Visual Style
- Material Design 3 (Material You)
- Clean, developer-focused interface
- Terminal-inspired aesthetic where appropriate

### Color Scheme
- Primary: Deep Purple (#6750A4)
- Secondary: Teal accent for terminal elements
- Dark mode: True black background for OLED
- Light mode: Clean white/gray surface

### Layout Approach
- Bottom navigation with 3 main tabs:
  1. **Chat** - Main conversation interface
  2. **Files** - Project file browser
  3. **Settings** - Configuration
- Connection status in top app bar
- Floating action button for quick actions in chat

### Typography
- Monospace font (JetBrains Mono) for code/diff
- System default for UI text
- Compact density for maximum content visibility
