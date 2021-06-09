package timber.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kt
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class MokaUsageDetectorTest {

    @Test
    fun usingOnViewJava() {
        lint().files(java("""
                |package foo;
                |import android.app.Activity;
                |import androidx.test.espresso.Espresso.onView;
                |
                |class EditTextTest {
                |  @Test
                |  fun wrongUsage() {
                |     onView(ViewMatchers.withId(R.id.edit)).perform(ViewActions.typeText(STRING_TO_BE_TYPED), ViewActions.closeSoftKeyboard());
                |  }
                |}"""
                        .trimMargin())
                ).issues(Moka2UsageDetector.ISSUE_ESPRESSO_ONVIEW)
                .run()
                .expect("""
                |src/foo/Example.java:6: Warning: Using 'SharedPreferences' instead of 'DBPrefs' [SharedPrefsUsage]
                |    SharedPreferences sharedPreferences = getSharedPreferences("java", 0);
                |                                          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings
                |""".trimMargin())
                .expectFixDiffs("""
                |Fix for src/foo/Example.java line 5: Replace with DBPrefs():
                |@@ -6 +6
                |-     SharedPreferences sharedPreferences = getSharedPreferences("java", 0);
                |+     SharedPreferences sharedPreferences = DBPrefs();
                |""".trimMargin())
    }

    @Test
    fun usingOnViewKotlin() {
        lint().files(kt("""
                |package foo;
                |import androidx.test.espresso.Espresso.onView
                |
                |class EditTextTest {
                |  @Test
                |  fun wrongUsage() {
                |     onView(ViewMatchers.withId(R.id.edit)).perform(ViewActions.typeText(STRING_TO_BE_TYPED), ViewActions.closeSoftKeyboard())
                |  }
                |}""".trimMargin())
                )
                .issues(Moka2UsageDetector.ISSUE_ESPRESSO_ONVIEW)
                .run()
                .expect("""
                |src/foo/Example.kt:6: Warning: Using 'SharedPreferences' instead of 'DBPrefs' [SharedPrefsUsage]
                |    val sharedPreferences = getSharedPreferences("kotlin", Context.MODE_PRIVATE)
                |                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings
                |""".trimMargin())
                .expectFixDiffs("""
                |Fix for src/foo/Example.kt line 5: Replace with DBPrefs():
                |@@ -6 +6
                |-     val sharedPreferences = getSharedPreferences("kotlin", Context.MODE_PRIVATE)
                |+     val sharedPreferences = DBPrefs()
                |""".trimMargin())
    }

}
