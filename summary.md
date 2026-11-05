# Summary

## Task: Separate "Ressources du Module" into its own page

### Changes made:

1. **`src/main/resources/fxml/dashboard.fxml`**
   - Added a new `resourcesBtn` button in the sidebar with a folder icon
   - Tooltip: "Ressources des Modules"

2. **`src/main/java/com/scolaris/controller/DashboardController.java`**
   - Added `resourcesBtn` FXML field
   - Added `handleLoadResources` handler to load `student_resources_view`
   - Added `resourcesBtn` to `updateActiveButton()` array
   - Hidden for TEACHER and ADMIN roles; visible for STUDENT

3. **`src/main/resources/fxml/student_resources_view.fxml`** (new)
   - SplitPane layout with modules table on the left, resources panel on the right
   - Shows only modules (Code, Name, Semester) — no subjects/courses

4. **`src/main/java/com/scolaris/controller/StudentResourcesController.java`** (new)
   - Loads all modules via `ModuleDao.findAll()`
   - On module selection, fetches and displays resources via `ModuleResourceService`
   - Reuses the same resource card rendering as `StudentCoursesController`
   - Supports downloading/opening resource files

### Behavior:
- Students see a new "Ressources des Modules" button in the left navbar
- Clicking it opens a page with the module list (M1–M11)
- Selecting a module shows resources uploaded by the teacher

---

## Task: Fix teacher registration — assign semester + subject

### Problem:
When a new teacher registered via the UI, only a User account was created. No Teacher record was created and no subject was assigned, so the teacher saw an empty page.

### Changes:

1. **`src/main/resources/fxml/register.fxml`**
   - Added `semesterSection` / `semesterComboBox` (hidden by default)
   - Added `subjectSection` / `subjectComboBox` (hidden by default)

2. **`src/main/java/com/scolaris/controller/RegisterController.java`**
   - When role = TEACHER, semester and subject fields appear
   - Semester combo: S1, S2, S3
   - Subject combo: filtered by selected semester, shows only unassigned subjects
   - On registration: creates User + Teacher record + assigns teacher to chosen subject

### Behavior:
- Teacher registers with username (CIN), password, selects semester, then selects subject
- The subject's `teacher_id` is set to the new teacher
- On login, the teacher sees their assigned subject(s) in "Mes Matières"
