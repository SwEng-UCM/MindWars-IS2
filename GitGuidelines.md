# MindWars-IS2 — Team Git Workflow 
  
Goal: **keep `main` always working**, and avoid conflicts/overwrites.

---

## 0) Core rules (read this first)

- ✅ **Everyone works on their own branch**: `dev/<name>`
- ❌ **Never commit directly to `main`**
- ✅ **Before coding each day, update your branch from `main`**
- ✅ **Push your work regularly**
- ✅ **Only merge into main if your branch is up-to-date and everything runs.**

---

## 1) One-time setup (first day only) 
Make sure git is setup correctly on your computer if not ask me

```bash
git clone https://github.com/SwEng-UCM/MindWars-IS2.git
cd MindWars-IS2

git checkout main
git pull origin main

git checkout -b dev/<name>
git push -u origin dev/<name>
```

(Replace `<name>` with your name, e.g. `dev/leo`)

---

## 2) Daily routine (every time BEFORE you start coding)

### Step A — update `main`
```bash
git checkout main
git pull origin main
```

### Step B — update your branch with latest `main` 
```bash
git checkout dev/<name>
git merge main
```

If conflicts appear:
1. Fix the files marked by Git
2. Then run:
```bash
git add .
git commit
```

---

## 3) Working and committing

While coding, commit when you reach a small milestone:

```bash
git add .
git commit -m "Short title" -m "What you changed"
```

Note: You can add in the title the #number of the task/issue you have completed/fixed

*Example:*
```bash
git commit -m "Fixed issue #4" -m "<...>" 
```

Then push your commits:

```bash
git push
```

Tip: can do `git status` to see what changes have been staged, what files are tracked.

---

## 4) When your feature/class is done (merge into `main`)
Make sure you committed all changes in your branch and it runs.

```bash
git checkout main
git pull origin main
git merge --ff-only dev/<name>
git push origin main
```

If `--ff-only` fails: update your branch again (Section 2) and push, then retry.

