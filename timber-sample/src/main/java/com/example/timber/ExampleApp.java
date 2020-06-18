package com.example.timber;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public class ExampleApp extends Application {
  @Override public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      Timber.plant(new DebugTree());
    } else {
      Timber.plant(new CrashReportingTree());
    }
  }

  /** A tree which logs important information for crash reporting. */
  private static class CrashReportingTree extends Timber.Tree {
    CrashReportingTree() {
      // attachStackTraceString=false
      // usually crash reporting tools already gets stack traces in a structured way,
      // because they might need to be demangled.
      super(false);
    }

    @Override protected void logMessage(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t, @Nullable Object... args) {
      if (priority == Log.VERBOSE || priority == Log.DEBUG) {
        return;
      }

      FakeCrashLibrary.log(priority, tag, message);

      if (t != null) {
        if (priority == Log.ERROR) {
          FakeCrashLibrary.logError(t);
        } else if (priority == Log.WARN) {
          FakeCrashLibrary.logWarning(t);
        }
      }
    }
  }
}
