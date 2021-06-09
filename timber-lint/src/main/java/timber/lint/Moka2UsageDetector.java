package timber.lint;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.checks.StringFormatDetector;
import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;

import org.jetbrains.uast.UBinaryExpression;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UIfExpression;
import org.jetbrains.uast.ULiteralExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UQualifiedReferenceExpression;
import org.jetbrains.uast.USimpleNameReferenceExpression;
import org.jetbrains.uast.UastBinaryOperator;
import org.jetbrains.uast.util.UastExpressionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_BOOLEAN;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_BYTE;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_CHAR;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_DOUBLE;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_FLOAT;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_INT;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_LONG;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_NULL;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_OBJECT;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_SHORT;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_STRING;
import static com.android.tools.lint.detector.api.ConstantEvaluator.evaluateString;
import static org.jetbrains.uast.UastBinaryOperator.PLUS;
import static org.jetbrains.uast.UastBinaryOperator.PLUS_ASSIGN;
import static org.jetbrains.uast.UastLiteralUtils.isStringLiteral;
import static org.jetbrains.uast.UastUtils.evaluateString;

public final class Moka2UsageDetector extends Detector implements Detector.UastScanner {

  @Override public List<String> getApplicableMethodNames() {
    return Arrays.asList("onView", "format", "v", "d", "onView");
  }

  @Override public void visitMethod(JavaContext context, UCallExpression call, PsiMethod method) {
    String methodName = call.getMethodName();
    JavaEvaluator evaluator = context.getEvaluator();

    if (evaluator.isMemberInClass(method, "androidx.test.espresso.Espresso.onView")) {
      System.err.println("Espresso.onView found $method");
      LintFix fix = quickFixIssueLog(call);
      context.report(ISSUE_ESPRESSO_ONVIEW, call, context.getLocation(call), "Using 'Espresso.onView()' instead of 'com.moka.EspressoMoka.onView()'", fix);
    } else
      System.err.println("Espresso.onView not found "+ method);
  }

  private LintFix quickFixIssueLog(UCallExpression logCall) {
    List<UExpression> arguments = logCall.getValueArguments();
    String methodName = logCall.getMethodName();
    UExpression tag = arguments.get(0);

    // 1st suggestion respects author's tag preference.
    // 2nd suggestion drops it (Timber defaults to calling class name).
    String fixSource1 = "Timber.tag(" + tag.asSourceString() + ").";
    String fixSource2 = "Timber.";

    int numArguments = arguments.size();
    if (numArguments == 2) {
      UExpression msgOrThrowable = arguments.get(1);
      fixSource1 += methodName + "(" + msgOrThrowable.asSourceString() + ")";
      fixSource2 += methodName + "(" + msgOrThrowable.asSourceString() + ")";
    } else if (numArguments == 3) {
      UExpression msg = arguments.get(1);
      UExpression throwable = arguments.get(2);
      fixSource1 +=
          methodName + "(" + throwable.asSourceString() + ", " + msg.asSourceString() + ")";
      fixSource2 +=
          methodName + "(" + throwable.asSourceString() + ", " + msg.asSourceString() + ")";
    } else {
      throw new IllegalStateException("android.util.Log overloads should have 2 or 3 arguments");
    }

    String logCallSource = logCall.asSourceString();
    LintFix.GroupBuilder fixGrouper = fix().group();
    fixGrouper.add(
        fix().replace().text(logCallSource).shortenNames().reformat(true).with(fixSource1).build());
    fixGrouper.add(
        fix().replace().text(logCallSource).shortenNames().reformat(true).with(fixSource2).build());
    return fixGrouper.build();
  }

  static Issue[] getIssues() {
    return new Issue[] {
            ISSUE_ESPRESSO_ONVIEW
    };
  }

  public static final Issue  ISSUE_ESPRESSO_ONVIEW = Issue.create("Espresso.onView", "Espresso.onView() is used, use com.moka.EspressoMoka.onView() instead",
          "Since Espresso.onView() are used in the project, it can happen that some flakiness can happen",
          Category.USABILITY, 5, Severity.ERROR,
          new Implementation(Moka2UsageDetector.class, Scope.JAVA_FILE_SCOPE));

}
