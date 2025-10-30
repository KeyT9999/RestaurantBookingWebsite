# 📊 Coverage Report Index

Welcome to the Restaurant Booking System Code Coverage Reports!

This directory contains comprehensive coverage analysis generated on **October 30, 2024** using **JaCoCo 0.8.11**.

---

## 📑 Available Reports

### 1. 📄 Quick Summary
**File:** [COVERAGE_SUMMARY.md](./COVERAGE_SUMMARY.md)

A concise one-page summary with key metrics, top insights, and immediate action items.

**Best for:** Quick overview, daily standup, management reports

**Contains:**
- Overall coverage metrics
- Top 5 best/worst packages
- Failed tests summary
- Quick commands

---

### 2. 📊 Visual Breakdown
**File:** [COVERAGE_VISUAL_BREAKDOWN.md](./COVERAGE_VISUAL_BREAKDOWN.md)

Visual charts, graphs, and heatmaps showing coverage distribution.

**Best for:** Understanding patterns, presentations, identifying trends

**Contains:**
- Coverage distribution charts
- Heatmaps by function
- Risk assessment visualization
- Coverage roadmap
- File-level champions

---

### 3. 📕 Detailed Report
**File:** [COVERAGE_REPORT_2024.md](./COVERAGE_REPORT_2024.md)

Comprehensive analysis with detailed breakdowns, explanations, and recommendations.

**Best for:** In-depth analysis, planning, technical deep-dive

**Contains:**
- Detailed package analysis
- File-by-file breakdown
- Uncovered scenarios
- Test failure analysis
- Improvement roadmap
- Best practices guide

---

### 4. 🌐 Interactive HTML Report
**File:** `target/site/jacoco/index.html`

JaCoCo's interactive HTML report with source code highlighting.

**Best for:** Developer investigation, code review, line-by-line analysis

**How to open:**
```bash
# Windows
start target/site/jacoco/index.html

# macOS
open target/site/jacoco/index.html

# Linux
xdg-open target/site/jacoco/index.html
```

**Features:**
- Click-through navigation
- Source code with coverage highlighting
- Package drill-down
- Sortable tables

---

### 5. 📊 Data Exports

#### CSV Report
**File:** `target/site/jacoco/jacoco.csv`

Machine-readable coverage data for spreadsheet analysis.

**Use for:** Excel analysis, custom reports, data visualization

#### XML Report
**File:** `target/site/jacoco/jacoco.xml`

Standard XML format for tool integration.

**Use for:** CI/CD pipelines, SonarQube, quality gates

---

## 🎯 Quick Stats

```
📊 Line Coverage:        21.53% (4,789 / 22,246)
🌿 Branch Coverage:      15.05% (901 / 5,987)
🔧 Method Coverage:      22.94% (1,222 / 5,328)
📝 Instruction Coverage: 21.59% (19,710 / 91,272)

📦 Classes Analyzed:     423
✅ Tests Passed:         578 / 590
❌ Tests Failed:         12
```

---

## 🚀 How to Use These Reports

### For Developers
1. Start with [COVERAGE_SUMMARY.md](./COVERAGE_SUMMARY.md) for overview
2. Check [COVERAGE_VISUAL_BREAKDOWN.md](./COVERAGE_VISUAL_BREAKDOWN.md) to see where your component stands
3. Review [COVERAGE_REPORT_2024.md](./COVERAGE_REPORT_2024.md) for specific recommendations
4. Use HTML report to see exactly which lines need coverage

### For Team Leads
1. Review [COVERAGE_SUMMARY.md](./COVERAGE_SUMMARY.md) for status update
2. Check [COVERAGE_VISUAL_BREAKDOWN.md](./COVERAGE_VISUAL_BREAKDOWN.md) for risk assessment
3. Use [COVERAGE_REPORT_2024.md](./COVERAGE_REPORT_2024.md) for sprint planning

### For QA Engineers
1. Review [COVERAGE_REPORT_2024.md](./COVERAGE_REPORT_2024.md) for uncovered scenarios
2. Check failed tests section for issues to investigate
3. Use HTML report to identify untested code paths

### For Management
1. [COVERAGE_SUMMARY.md](./COVERAGE_SUMMARY.md) has executive summary
2. [COVERAGE_VISUAL_BREAKDOWN.md](./COVERAGE_VISUAL_BREAKDOWN.md) shows risk heatmap
3. Review improvement roadmap in [COVERAGE_REPORT_2024.md](./COVERAGE_REPORT_2024.md)

---

## 🔧 Regenerating Reports

### Run Tests and Generate Coverage

```bash
# Clean and run all tests
mvn clean test

# Generate JaCoCo report
mvn jacoco:report

# View HTML report
start target/site/jacoco/index.html  # Windows
open target/site/jacoco/index.html   # macOS
```

### Run Specific Test Suites

```bash
# Run only service tests
mvn test -Dtest="*Service*Test"

# Run only controller tests
mvn test -Dtest="*Controller*Test"

# Run specific test class
mvn test -Dtest=BookingServiceTest
```

### Coverage with Quality Check

```bash
# Run tests and enforce coverage thresholds
mvn clean verify

# This will fail if coverage is below:
# - Line Coverage: 60%
# - Branch Coverage: 50%
```

---

## 📈 Current Issues

### Failed Tests (12)

All failures are in `BookingConflictServiceTest` due to restaurant operating hours validation.

**Error:** `Nhà hàng đóng cửa: Nhà hàng chỉ hoạt động từ 10:00 đến 22:00`

**Action Required:** Update test data to use booking times within 10:00-22:00

---

## 🎯 Top Priorities

### Critical (This Week) 🔴
1. Fix 12 failing tests
2. Add payment flow tests (currently 0.3% coverage)
3. Add restaurant owner operation tests (currently 3.6% coverage)

### High (Next Sprint) 🟠
1. Increase overall coverage to 35%
2. Add integration tests for critical flows
3. Add WebSocket chat tests

### Medium (Next Month) 🟡
1. Achieve 50% overall coverage
2. Add comprehensive controller tests
3. Add email notification tests

---

## 📞 Support

### Questions?
- Development Lead: Check the detailed report
- QA Team: Review uncovered scenarios section
- DevOps: See CI/CD integration section

### Need Help?
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [Project Testing Guidelines](./docs/TESTING_GUIDE.md)

---

## 📅 Review Schedule

- **Generated:** October 30, 2024
- **Next Review:** November 13, 2024
- **Frequency:** Bi-weekly
- **Owner:** Development Team

---

## 📊 Coverage Trend (Future)

Once we implement coverage tracking in CI/CD, we'll track:
- Coverage per commit
- Coverage per PR
- Coverage trends over time
- Coverage by developer

---

## 🏆 Coverage Goals

| Timeframe | Current | Target |
|-----------|---------|--------|
| **Current** | 21.5% | - |
| **1 Month** | 21.5% | 35% |
| **3 Months** | 21.5% | 60% |
| **6 Months** | 21.5% | 80% |

---

## 📝 Notes

- DTOs have low coverage by design (mostly data containers)
- Some configuration classes are excluded from coverage
- Test utilities are not counted in coverage metrics
- Coverage is measured at line, branch, method, and instruction levels

---

**Last Updated:** October 30, 2024  
**JaCoCo Version:** 0.8.11  
**Maven Version:** 3.x  
**Java Version:** 17

---

## 🔗 Quick Links

- 📄 [Quick Summary](./COVERAGE_SUMMARY.md)
- 📊 [Visual Breakdown](./COVERAGE_VISUAL_BREAKDOWN.md)
- 📕 [Detailed Report](./COVERAGE_REPORT_2024.md)
- 🌐 [HTML Report](./target/site/jacoco/index.html)
- 📋 [CSV Data](./target/site/jacoco/jacoco.csv)
- 📄 [XML Data](./target/site/jacoco/jacoco.xml)


