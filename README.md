# material-classification

Java 1.8 / Spring Boot 2.7 port of the PiLog material-description classification
engine, merged from the three Python versions supplied:

| Python file | What it contributed to this port |
|---|---|
| `class_run_batch.py` | Canonical, complete control flow for `total_class_procedure` and its rule helpers (RULE1/RULE2/RULE3/LAST RULE) |
| `optimized_code.py` | Precompiled-regex approach, used for the `Pattern` constants in `ClassificationService` |
| `complete_class.py` | `singularize()` helper (ported to `TextInflectionUtil`, since Java has no `inflect` library equivalent) |

## What changed vs. the Python scripts

- **No more hardcoded Windows paths / global script execution.** The Python files read
  Excel files from a hardcoded `C:\Users\D PRIYA\...` path and ran top-to-bottom over an
  entire input workbook. This project instead:
  - Loads the *reference* workbooks (the lookup tables) into an H2 database once at
    startup via `ExcelDataLoader`.
  - Exposes the classification of a **single** description as a REST endpoint, since
    that's how you asked it to be wired up. If you also want the old "read an input
    Excel file, write an output Excel file" batch mode, it's straightforward to add a
    second entry point that loops `ClassificationService.totalClassProcedure(...)` over
    rows read with Apache POI (already a dependency) â€” ask if you'd like that added.
- **Pandas DataFrames -> JPA entities + repositories.** `class_check`, `check1`,
  `r1_check`, `lst_check`, `obj_check`, `alter_df`, `exmp_df` are now `ThirdPref`,
  `SecondPref`, `FirstPref`, `LastPref`, (a filtered view of `ThirdPref`), `AlterWord`,
  `Exclusion` respectively, all cached in memory in `ClassificationService` after being
  loaded from H2 (mirrors keeping the DataFrames resident in memory).
- **Python's `str.index()` (raises `ValueError`) is mimicked with a small `idx()` helper**
  that throws `NoSuchElementException`, so the many `try/except: return X` fallbacks in
  the original translate directly to `try/catch (NoSuchElementException) { return X; }`.
- **Regex**: Java's `java.util.regex` is close enough to Python's `re` that most patterns
  port almost verbatim; word-boundary lookups (`re.search(r'\b({})\b'.format(term), s)`)
  became the `wb(term)` / `search(...)` helpers in `ClassificationService`.

## Project layout

```
src/main/java/com/pilog/classification/
  ClassificationApplication.java     Spring Boot entry point
  entity/                            JPA entities mirroring the Excel sheets
  repository/                        Spring Data JPA repositories
  loader/ExcelDataLoader.java        Reads the 3 reference workbooks into H2 on startup
  service/ClassificationService.java Ported business logic (altr_word, exclusion_cndtn,
                                      rule1, RULE2/RULE3 helpers, total_class_procedure)
  util/TextInflectionUtil.java       Java replacement for Python's inflect.singular_noun()
  controller/ClassificationController.java  REST API
  controller/GlobalExceptionHandler.java
  dto/                               Request/response payloads
src/main/resources/
  application.properties
  data/                              Drop the 3 reference .xlsx files here
```

## Reference data setup

Copy your three reference workbooks into `src/main/resources/data/` (or point
`classification.reference-data.external-dir` at any folder containing them â€” see
`application.properties`):

```
CLASS_CLEAN_Item_Details_NIIC.xlsx   sheets: Third_Pref, Second_Pref, First_Pref(NEW), Last_Pref
IS_ALTER_WORD_ABB.xlsx               columns: WORD, ALTER_WORD
Exclusions REVISED-08.11.22.xlsx     columns: Object, Exemptions
```

`ExcelDataLoader` routes each file by filename substring (`CLASS_CLEAN`, `ALTER_WORD`,
`EXCLUSION`) so date/version suffixes in filenames are fine. On startup it imports rows
into H2 (file-based at `./data/classdb` by default) only if the corresponding table is
empty; call `POST /api/reference-data/reload` to force a re-import after replacing the
files without restarting the app.

If the workbooks aren't present, the app still starts (so you can verify the build), but
every classification request will fall back through the rule chain to `null`.

## Build & run

Requires JDK 8+ and Maven. (This container's network sandbox couldn't reach Maven
Central to verify the build for you â€” run these locally.)

```bash
mvn clean package
java -jar target/material-classification.jar
```

H2 console (to inspect the imported lookup tables): `http://localhost:8080/h2-console`
JDBC URL: `jdbc:h2:file:./data/classdb`, user `sa`, empty password.

## API

### `POST /api/classify`

```json
{ "description": "TEE PIPE SIZE: 160 MM CONN: SOCKET; MATERIAL:UPVC WITH REDUCER ADAPTOR 110 MM" }
```

Response:

```json
{
  "description": "TEE PIPE SIZE: 160 MM CONN: SOCKET; MATERIAL:UPVC WITH REDUCER ADAPTOR 110 MM",
  "normalizedDescription": "TEE PIPE SIZE, 160 MM CONN, SOCKET, MATERIAL, UPVC WITH REDUCER ADAPTOR 110 MM",
  "predictedClass": "<resolved class from your reference data>"
}
```

`GET /api/classify?description=...` does the same for quick manual testing.

### `POST /api/reference-data/reload`

Re-imports the workbooks from the configured data directory and refreshes the in-memory
caches without restarting the JVM.

### `GET /api/health`

Liveness check.

## Notes / things worth validating against your real data

This is a faithful structural port of genuinely intricate, index-comparison-heavy rule
logic (three-way tie-breaking between RULE1/RULE2/RULE3 matches based on *where* each
matched term sits in the string). I translated every branch of `total_class_procedure`
and its helpers, but this kind of code is best validated by running the same batch of
real descriptions through both the Python and Java versions and diffing the output
classes â€” I'd recommend doing that before relying on this in production. I'm glad to
help set up that comparison harness, or add the batch (Excel-in/Excel-out) endpoint
mentioned above, if useful.
