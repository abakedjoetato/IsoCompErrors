# AI REPLIT SESSION PROMPT — PHASE 0: EXPANDED PROJECT INIT & COMPILATION FIX

## OBJECTIVE

This session is a structured, diagnostic-focused Phase 0 designed to **methodically resolve all compilation errors** and ensure the bot builds cleanly before any runtime or functional testing begins.

You are not fixing logic, output, or runtime bugs — your only responsibility in this phase is to make the project **compile successfully** with **no warnings or errors** using the latest compatible environment.

---

## PHASE 0 — EXPANDED PROJECT INIT & COMPILATION FIX

### 1. UNPACK AND PREPARE

- [ ] Unzip the `.zip` file from attached assets  
- [ ] Move all files and folders to the project root  
- [ ] Remove any nested project directories (e.g., `DeadsideBot/`, `main/`, `project/`)  
- [ ] Delete empty folders and symbolic links

### 2. STRUCTURE VALIDATION

- [ ] Confirm existence and placement of:
  - `src/main/java/`  
  - `src/main/resources/`  
  - `pom.xml` or `build.gradle`  
  - `.env` or `config.properties` (if required at runtime)

### 3. DEPENDENCY VERIFICATION

- [ ] Open `pom.xml` and validate:
  - Use of **JDA 5.x** or latest compatible version  
  - Required libraries are listed (MongoDB, SFTP, logging, JSON, etc.)  
  - No outdated or duplicate dependency declarations  
- [ ] Trigger a full Maven refresh or reimport to ensure dependencies are pulled correctly

### 4. COMPILATION PASS — PRIMARY

- [ ] Attempt full compile  
- [ ] Log all error types and affected files, categorized as:
  - Missing imports  
  - Invalid method calls  
  - Package mismatches  
  - Unresolved symbols  
  - Deprecated or removed APIs

### 5. SYSTEMATIC ERROR FIXING

- [ ] For each error category:
  - Resolve **package-level conflicts** (wrong folder/package structure)  
  - Resolve **API mismatches** (e.g., changed JDA method signatures)  
  - Refactor **missing types** or class references  
  - Comment and flag **stub logic** for replacement in future phases  
- [ ] Ensure no fix introduces new imports from unapproved or unstable libraries

### 6. REBUILD & RETEST

- [ ] Rebuild the project after all targeted fixes  
- [ ] Confirm that **no compile-time errors** remain  
- [ ] Confirm all warnings are either suppressed or logged for next-phase cleanup

---

## COMPLETION CRITERIA

This phase is only complete when:

- [✓] The project compiles 100% clean  
- [✓] No compiler errors or breaking warnings remain  
- [✓] JDA and core dependencies are loaded and resolvable  
- [✓] Main entrypoint is recognized  
- [✓] No files are stranded, mispackaged, or undetected by the build system

---

## EXECUTION POLICY — STRICT

- No trial-and-error; each fix must be accurate and scoped  
- Do not proceed to runtime validation or logic repair  
- No commits, reports, or output until **project builds cleanly**  
- Halt and log only when zero compiler issues remain  
