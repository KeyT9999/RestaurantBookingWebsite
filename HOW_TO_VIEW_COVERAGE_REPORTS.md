# 📖 How to View Coverage Reports

This guide helps you navigate and understand the coverage reports generated for the Restaurant Booking System.

---

## 🌐 Viewing the HTML Report (Recommended)

The HTML report provides an interactive, visual way to explore coverage.

### Step 1: Open the HTML Report

**Windows:**
```cmd
start target\site\jacoco\index.html
```

**macOS:**
```bash
open target/site/jacoco/index.html
```

**Linux:**
```bash
xdg-open target/site/jacoco/index.html
```

Or simply double-click the file: `target/site/jacoco/index.html`

---

### Step 2: Understanding the HTML Report

#### Main Page (index.html)

The main page shows:
- **Overall metrics** at the top (Lines, Branches, Methods, Instructions)
- **Package list** with individual coverage for each package
- **Color coding:**
  - 🟢 **Green bars** = Good coverage
  - 🟡 **Yellow bars** = Moderate coverage
  - 🔴 **Red bars** = Low coverage

#### Navigating Packages

1. Click on any **package name** to drill down
2. See list of **classes** in that package
3. Click on a **class name** to see the source code

#### Source Code View

- **Green highlighting** = Code covered by tests ✅
- **Red highlighting** = Code NOT covered by tests ❌
- **Yellow highlighting** = Partially covered branches ⚠️
- **Line numbers** on the left show coverage status

#### Coverage Metrics Explained

| Metric | What it Means |
|--------|---------------|
| **Missed Instructions** | Number of bytecode instructions not executed |
| **Cov. (Coverage)** | Percentage of instructions/lines/branches covered |
| **Missed Branches** | Number of if/switch paths not tested |
| **Missed Lines** | Number of source code lines not executed |
| **Missed Methods** | Number of methods not called in tests |
| **Missed Classes** | Number of classes with no test coverage |

---

## 📄 Reading Markdown Reports

### Quick Start: COVERAGE_SUMMARY.md

**Purpose:** Get a quick overview in 2 minutes

**What you'll find:**
- Overall coverage percentages
- Top 5 best covered packages
- Top 5 packages needing tests
- Failed tests summary
- Quick action items

**Best for:** Daily standup, quick status check

---

### Visual Analysis: COVERAGE_VISUAL_BREAKDOWN.md

**Purpose:** See visual representation of coverage

**What you'll find:**
- ASCII bar charts showing coverage distribution
- Coverage heatmap for critical functions
- Risk assessment with color coding
- Coverage roadmap
- Top tested files

**Best for:** Presentations, identifying patterns, risk assessment

---

### Detailed Analysis: COVERAGE_REPORT_2024.md

**Purpose:** Comprehensive analysis and recommendations

**What you'll find:**
- Detailed package-by-package breakdown
- File-by-file analysis with explanations
- Uncovered critical scenarios
- Test failure analysis
- Improvement roadmap
- Best practices guide
- Coverage improvement plan

**Best for:** Sprint planning, technical deep-dive, strategy

---

## 📊 Understanding Coverage Percentages

### What do the numbers mean?

#### Line Coverage: 21.53%

```
21.53% = 4,789 lines covered / 22,246 total lines
```

This means:
- ✅ 4,789 lines of code were executed during tests
- ❌ 17,457 lines were NOT executed during tests

#### Branch Coverage: 15.05%

```
15.05% = 901 branches covered / 5,987 total branches
```

A "branch" is a decision point (if/else, switch, etc.)
- ✅ 901 decision paths were tested
- ❌ 5,086 decision paths were NOT tested

Example:
```java
if (user.isActive()) {  // Branch 1
    return true;
} else {               // Branch 2
    return false;
}
```
100% branch coverage means both paths were tested.

#### Method Coverage: 22.94%

```
22.94% = 1,222 methods covered / 5,328 total methods
```

- ✅ 1,222 methods were called during tests
- ❌ 4,106 methods were never called

---

## 🎯 What Coverage Should We Aim For?

### Industry Standards

| Type | Minimum | Good | Excellent |
|------|---------|------|-----------|
| **Line Coverage** | 60% | 70-80% | 85%+ |
| **Branch Coverage** | 50% | 60-70% | 75%+ |
| **Method Coverage** | 60% | 70-80% | 85%+ |

### Our Targets

| Timeframe | Line | Branch | Method |
|-----------|------|--------|--------|
| **Current** | 21.5% | 15.1% | 22.9% |
| **Next Sprint** | 35% | 25% | 35% |
| **3 Months** | 60% | 50% | 65% |
| **6 Months** | 80% | 70% | 85% |

---

## 🔍 How to Find What Needs Testing

### Method 1: HTML Report

1. Open `target/site/jacoco/index.html`
2. Look for packages with **red** coverage bars
3. Click to drill down to specific classes
4. Click on a class to see **red highlighted lines** (untested code)

### Method 2: COVERAGE_VISUAL_BREAKDOWN.md

1. Open the file in any text editor or GitHub
2. Look at the "Low Coverage" section
3. Check the "High Risk Areas" section
4. Review the "Critical Functions" heatmap

### Method 3: COVERAGE_REPORT_2024.md

1. Skip to "Critical Files with Low Coverage"
2. Read the detailed analysis for each file
3. Review "Uncovered Critical Scenarios"
4. Check "Coverage Improvement Plan"

---

## 📸 HTML Report Screenshots (Description)

Since you requested screenshots, here's what you'll see in the HTML report:

### Screenshot 1: Main Dashboard
**File:** `target/site/jacoco/index.html`

**What you see:**
- Header: "restaurant-booking"
- Overall metrics bar showing 21.5% line coverage
- Table with columns: Element, Missed Instructions %, Cov., Missed Branches %, Cov., etc.
- List of all packages with their individual coverage percentages
- Green/yellow/red bars visualizing coverage

### Screenshot 2: Package View
**Example:** `com.example.booking.service`

**What you see:**
- Package name at top
- Coverage metrics for this package (24.71% line coverage)
- List of all classes in the package
- Each class with its coverage percentage
- Color-coded bars

### Screenshot 3: Class View
**Example:** `BookingService.java`

**What you see:**
- Class name at top: "BookingService"
- Coverage: 55.23% line coverage
- Table showing each method with its coverage
- "+" icons to expand method details

### Screenshot 4: Source Code View
**Example:** `BookingService.java` source

**What you see:**
- Source code with syntax highlighting
- Line numbers on the left
- Coverage indicators:
  - **Green background** = Line was tested ✅
  - **Red background** = Line was NOT tested ❌
  - **Yellow background** = Partial branch coverage ⚠️
- Diamond icons showing branch coverage

---

## 🖼️ Taking Screenshots Yourself

### To Capture HTML Report Screenshots:

1. **Open the HTML report:**
   ```bash
   start target\site\jacoco\index.html
   ```

2. **For Main Page Screenshot:**
   - Open `index.html`
   - Press `Windows Key + Shift + S` (Windows) or `Cmd + Shift + 4` (Mac)
   - Capture the entire page

3. **For Package View:**
   - Click on any package name
   - Capture screenshot

4. **For Source Code:**
   - Click on a class name
   - Capture screenshot showing red/green highlighting

---

## 📊 Exporting Data

### CSV Export (For Excel)

1. Open `target/site/jacoco/jacoco.csv`
2. Import into Excel or Google Sheets
3. Create custom charts and analysis

**Data includes:**
- Package name
- Class name
- Missed/Covered instructions
- Missed/Covered branches
- Missed/Covered lines
- Missed/Covered methods

### XML Export (For CI/CD)

The XML file (`target/site/jacoco/jacoco.xml`) can be:
- Imported into SonarQube
- Used in Jenkins/GitHub Actions
- Processed by custom tools

---

## 🔧 Troubleshooting

### "Cannot find jacoco report"

**Solution:**
```bash
# Regenerate the report
mvn clean test jacoco:report
```

### "HTML report shows 0% coverage"

**Solution:**
```bash
# Delete old data and regenerate
rm -rf target/
mvn clean test jacoco:report
```

### "Some files not showing in report"

**Possible reasons:**
- Files not compiled
- Files in `test/` directory (not counted)
- Files excluded in configuration

---

## 💡 Tips for Using Reports

### For Daily Development

1. Before writing code:
   - Check if the area has tests (`COVERAGE_SUMMARY.md`)
   
2. After writing code:
   - Run tests: `mvn test`
   - Check coverage: Open HTML report
   - Ensure your new code has tests

3. Before submitting PR:
   - Run full coverage: `mvn clean test jacoco:report`
   - Check that coverage didn't decrease
   - Add tests for any red lines in your changes

### For Code Review

1. Ask: "Did coverage increase or stay the same?"
2. Check HTML report for new files
3. Verify all new code paths are tested
4. Look for red lines in modified files

---

## 📞 Need Help?

- **Can't open HTML report?** Make sure tests have run: `mvn test`
- **Don't understand a metric?** See "Understanding Coverage Percentages" above
- **Need to improve coverage?** Check `COVERAGE_REPORT_2024.md` for recommendations

---

## ✅ Quick Checklist

Before your next commit:

- [ ] Run tests: `mvn test`
- [ ] Generate coverage: `mvn jacoco:report`
- [ ] Open HTML report to check your changes
- [ ] Ensure coverage didn't decrease
- [ ] Add tests for any red-highlighted code you wrote
- [ ] Check that new features have >70% coverage

---

**Last Updated:** October 30, 2024  
**Next Steps:** See [COVERAGE_INDEX.md](./COVERAGE_INDEX.md) for full navigation


